package com.creatrix.unicorn.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.util.Log;

import com.creatrix.unicorn.UtilityHelper.LocationAccess;
import com.creatrix.unicorn.UtilityHelper.LocationGuard;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.tracking.FallbackLocator;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;
import com.google.android.gms.tasks.Tasks;

import java.util.concurrent.TimeUnit;

/**
 * Fires every 5 minutes. Always records exactly one tracking point: a real fix when possible,
 * otherwise a point with null coordinates and the reason. Also revives {@link LocationService}.
 */
public class TrackingAlarmReceiver extends BroadcastReceiver {
    private static final String TAG = "TrackingAlarm";
    private static final long FRESH_FIX_TIMEOUT_SECONDS = 6;
    private static final long CACHED_FIX_MAX_AGE_MS = 6 * 60 * 1000L;
    private static final long UPLOAD_WAIT_MS = 8_000L;

    @Override
    public void onReceive(Context context, Intent intent) {
        final Context app = context.getApplicationContext();
        if (!new SessionManagement(app).isLoggedIn()) {
            return; // logged out: stop the chain until the next login re-arms it
        }

        // Re-arm first so a crash below can never break the 5-minute chain.
        TrackingScheduler.scheduleNext(app, TrackingScheduler.INTERVAL_MS);
        LocationService.start(app);
        TrackingWatchdogWorker.schedule(app);

        final PendingResult pendingResult = goAsync();
        new Thread(() -> {
            try {
                recordTick(app);
            } catch (Exception exception) {
                Log.e(TAG, "Tracking tick failed", exception);
            } finally {
                pendingResult.finish();
            }
        }, "tracking-tick").start();
    }

    private void recordTick(Context context) {
        boolean permission = LocationAccess.hasRequiredPermissions(context);
        boolean enabled = LocationAccess.isLocationEnabled(context);

        Double lat = null;
        Double lon = null;
        String note = null;

        if (!permission) {
            note = "Location unavailable: permission denied";
        } else if (!enabled) {
            note = "Location unavailable: GPS/location turned off";
        } else {
            Location fresh = requestFreshFix(context);
            if (fresh != null) {
                lat = fresh.getLatitude();
                lon = fresh.getLongitude();
                if (LocationGuard.isMock(fresh)) {
                    note = "[MOCK LOCATION]";
                }
            } else {
                double[] cached = TrackingScheduler.getCachedFix(context, CACHED_FIX_MAX_AGE_MS);
                Location radio = cached == null ? FallbackLocator.locate(context) : null;
                if (cached != null) {
                    lat = cached[0];
                    lon = cached[1];
                } else if (radio != null) {
                    // indoors / narrow lane: position from nearby cell towers or WiFi, marked as approximate
                    lat = radio.getLatitude();
                    lon = radio.getLongitude();
                    note = "[approx. " + radio.getProvider() + " ±" + Math.round(radio.getAccuracy()) + "m]";
                } else {
                    note = "Location unavailable: no GPS fix";
                }
            }
        }

        TrackingRecorder.record(context, lat, lon, note); // also queues a WorkManager upload as backup

        // Upload right now, inside the tick, so the point reaches the server every 5 minutes.
        // Bounded wait: the broadcast must finish quickly; if the network is slow the upload keeps
        // going on its own thread (the foreground service keeps the process alive).
        Thread upload = new Thread(() -> TrackingSyncWorker.flushQueue(context, () -> false), "tracking-upload");
        upload.start();
        try {
            upload.join(UPLOAD_WAIT_MS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
        }
    }

    private Location requestFreshFix(Context context) {
        try {
            CancellationTokenSource token = new CancellationTokenSource();
            return Tasks.await(
                    LocationServices.getFusedLocationProviderClient(context)
                            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, token.getToken()),
                    FRESH_FIX_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (Exception exception) {
            Log.w(TAG, "No fresh fix", exception);
            return null;
        }
    }
}
