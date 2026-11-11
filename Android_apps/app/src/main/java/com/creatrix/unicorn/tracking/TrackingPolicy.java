package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.BroadcastReceiver;
import android.os.Bundle;
import android.os.UserManager;

/**
 * Reads the managed configuration ("app restrictions") an EMM/MDM pushes to this app and merges it
 * into {@link TrackingConfig}. Keys are declared in res/xml/tracking_app_restrictions.xml. Values the
 * EMM does not set keep their current value; invalid values (for example a non-HTTPS URL) are ignored.
 */
public final class TrackingPolicy {
    private static final String TAG = "TrackingPolicy";
    public static final String KEY_BASE_URL = "tracking_base_url";
    public static final String KEY_INTERVAL_SECONDS = "tracking_interval_seconds";
    public static final String KEY_USER_CAN_STOP = "tracking_user_can_stop";
    public static final String KEY_ENFORCE = "tracking_enforce_device_policy";

    private TrackingPolicy() {
    }

    /** @return true if a managed configuration was present and applied */
    public static boolean apply(Context context) {
        UserManager um = (UserManager) context.getSystemService(Context.USER_SERVICE);
        Bundle restrictions = um == null ? null : um.getApplicationRestrictions(context.getPackageName());
        if (restrictions == null || restrictions.isEmpty()) {
            return false;
        }
        TrackingPrefs prefs = TrackingPrefs.get(context);
        TrackingConfig.Builder builder = prefs.getConfig().toBuilder();
        if (restrictions.containsKey(KEY_BASE_URL)) {
            String url = restrictions.getString(KEY_BASE_URL, "");
            if (url != null && !url.trim().isEmpty()) {
                builder.baseUrl(url.trim());
            }
        }
        if (restrictions.containsKey(KEY_INTERVAL_SECONDS)) {
            int seconds = restrictions.getInt(KEY_INTERVAL_SECONDS, 0);
            if (seconds > 0) {
                builder.intervalMs(seconds * 1000L).fastestIntervalMs(seconds * 500L);
            }
        }
        if (restrictions.containsKey(KEY_USER_CAN_STOP)) {
            builder.userCanStop(restrictions.getBoolean(KEY_USER_CAN_STOP, true));
        }
        if (restrictions.containsKey(KEY_ENFORCE)) {
            builder.enforceDevicePolicy(restrictions.getBoolean(KEY_ENFORCE, false));
        }
        try {
            TrackingConfig merged = builder.build();
            if (!merged.equals(prefs.getConfig())) {
                prefs.setConfig(merged);
            }
            prefs.setManagedByPolicy(true);
            return true;
        } catch (IllegalArgumentException e) {
            TrackLog.w(TAG, "Managed configuration rejected: " + e.getMessage());
            return false;
        }
    }

    /** Re-applies the configuration whenever the EMM changes it. Register once per process. */
    public static void registerForChanges(Context context, final Runnable onChanged) {
        final Context app = context.getApplicationContext();
        app.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context c, Intent intent) {
                if (apply(app)) {
                    onChanged.run();
                }
            }
        }, new IntentFilter(Intent.ACTION_APPLICATION_RESTRICTIONS_CHANGED));
    }
}
