package com.creatrix.unicorn.Services;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.creatrix.unicorn.DBInfo.TrackingQueueDbHelper;
import com.creatrix.unicorn.Model.UserTracking;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SmartAddressResolver;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Builds a tracking record and queues it for upload. Blocking, so call it off the main thread.
 * A record with null coordinates is valid: it means "the app was alive but had no location".
 */
public final class TrackingRecorder {
    private static final String TAG = "TrackingRecorder";
    private static final long GEOCODE_TIMEOUT_MS = 7_000L; // same budget as tracking.AddressResolver
    private static final ExecutorService GEOCODE_EXECUTOR = Executors.newSingleThreadExecutor();

    private TrackingRecorder() {
    }

    /**
     * @param lat  null when no location could be obtained
     * @param lon  null when no location could be obtained
     * @param note stored in the address column when there is no location (why it is missing)
     */
    public static void record(Context context, @Nullable Double lat, @Nullable Double lon, @Nullable String note) {
        Context appContext = context.getApplicationContext();
        int empId = getEmpId(appContext);
        if (empId <= 0) {
            Log.w(TAG, "No logged-in employee, tracking point skipped");
            return;
        }

        UserTracking userTracking = new UserTracking();
        userTracking.setEmpInfoId(empId);
        if (lat != null && lon != null) {
            userTracking.setLatValue(String.valueOf(lat));
            userTracking.setLongValue(String.valueOf(lon));
            String address = getAddress(appContext, lat, lon);
            // A note with real coordinates flags something suspicious, e.g. "[MOCK LOCATION]".
            userTracking.setAddressName(note == null || note.isEmpty() ? address : note + " " + address);
        } else {
            userTracking.setLatValue(null);
            userTracking.setLongValue(null);
            userTracking.setAddressName(note == null ? "" : note);
        }
        Date now = new Date();
        userTracking.setTimeStr(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now));
        userTracking.setTrackDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now));

        try (TrackingQueueDbHelper database = new TrackingQueueDbHelper(appContext)) {
            database.enqueue(userTracking);
            TrackingSyncWorker.schedule(appContext);
        } catch (Exception exception) {
            Log.e(TAG, "Unable to queue tracking data", exception);
        }
    }

    private static int getEmpId(Context context) {
        try {
            HashMap<String, String> user = new SessionManagement(context).getUserDetails();
            String value = user.get(SessionManagement.KEY_EmpId);
            return value == null ? 0 : Integer.parseInt(value);
        } catch (Exception exception) {
            Log.e(TAG, "Unable to read employee id from session", exception);
            return 0;
        }
    }

    // Uses SmartAddressResolver for high-accuracy place names, stripping Plus Codes
    // and combining Upazila/District components with online reverse-geocoding fallback.
    private static String getAddress(Context context, double lat, double lon) {
        Future<String> future = GEOCODE_EXECUTOR.submit(() -> SmartAddressResolver.resolveAddress(context, lat, lon));
        try {
            return future.get(GEOCODE_TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (Exception exception) {
            future.cancel(true);
            Log.w(TAG, "Unable to resolve address in time", exception);
            return "";
        }
    }
}
