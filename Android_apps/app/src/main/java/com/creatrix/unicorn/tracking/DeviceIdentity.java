package com.creatrix.unicorn.tracking;

import android.annotation.SuppressLint;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.provider.Settings;

import java.util.UUID;

/**
 * Stable per-device id sent with every location. Normal apps cannot read IMEI or serial number, so
 * this uses, in order: the enrollment-specific id (when this app is the device owner, Android 12+),
 * ANDROID_ID, and finally a random UUID kept in app storage. The value is computed once and cached.
 */
public final class DeviceIdentity {
    private static final String TAG = "DeviceIdentity";
    private static final String PREFS = "tracking_device";
    private static final String KEY_ID = "device_id";
    // The well-known bogus ANDROID_ID reported by some old/broken builds.
    private static final String BROKEN_ANDROID_ID = "9774d56d682e549c";

    private static volatile String cached;

    private DeviceIdentity() {
    }

    public static String get(Context context) {
        String id = cached;
        if (id != null) {
            return id;
        }
        synchronized (DeviceIdentity.class) {
            if (cached == null) {
                cached = resolve(context.getApplicationContext());
            }
            return cached;
        }
    }

    private static String resolve(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String stored = prefs.getString(KEY_ID, null);
        if (stored != null && !stored.isEmpty()) {
            return stored;
        }

        String id = enrollmentSpecificId(context);
        if (isBlank(id)) {
            id = androidId(context);
        }
        if (isBlank(id)) {
            id = UUID.randomUUID().toString();
        }
        prefs.edit().putString(KEY_ID, id).apply();
        return id;
    }

    @SuppressLint("NewApi")
    private static String enrollmentSpecificId(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
            return null;
        }
        try {
            DevicePolicyManager dpm = (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
            if (dpm != null && dpm.isDeviceOwnerApp(context.getPackageName())) {
                return dpm.getEnrollmentSpecificId();
            }
        } catch (RuntimeException e) {
            TrackLog.w(TAG, "Enrollment-specific id unavailable", e);
        }
        return null;
    }

    private static String androidId(Context context) {
        try {
            String value = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
            return BROKEN_ANDROID_ID.equals(value) ? null : value;
        } catch (RuntimeException e) {
            return null;
        }
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
