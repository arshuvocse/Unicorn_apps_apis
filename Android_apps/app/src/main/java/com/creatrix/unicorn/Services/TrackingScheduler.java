package com.creatrix.unicorn.Services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.SystemClock;
import android.util.Log;

/**
 * Drives the 5-minute tracking heartbeat with AlarmManager, independent of whether
 * {@link LocationService} is alive. Every tick records a point (with null coordinates when no
 * location is available) and makes sure the service is running again.
 */
public final class TrackingScheduler {
    private static final String TAG = "TrackingScheduler";
    public static final long INTERVAL_MS = 5 * 60 * 1000L;
    static final String ACTION_TICK = "com.creatrix.unicorn.TRACKING_TICK";
    private static final int REQUEST_CODE = 5150;

    private static final String PREFS = "tracking_fix";
    private static final String KEY_LAT = "lat";
    private static final String KEY_LON = "lon";
    private static final String KEY_ELAPSED_MS = "elapsed_ms";

    private TrackingScheduler() {
    }

    /** Schedules the first tick only if none is pending (so frequent callers don't keep pushing it back). */
    public static void ensure(Context context) {
        Context app = context.getApplicationContext();
        if (buildPendingIntent(app, PendingIntent.FLAG_NO_CREATE) == null) {
            scheduleNext(app, INTERVAL_MS);
        }
    }

    static void scheduleNext(Context context, long delayMs) {
        Context app = context.getApplicationContext();
        AlarmManager alarmManager = (AlarmManager) app.getSystemService(Context.ALARM_SERVICE);
        PendingIntent pendingIntent = buildPendingIntent(app, PendingIntent.FLAG_UPDATE_CURRENT);
        if (alarmManager == null || pendingIntent == null) {
            return;
        }
        long triggerAt = SystemClock.elapsedRealtime() + delayMs;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && !alarmManager.canScheduleExactAlarms()) {
                alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
            } else {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
            }
        } catch (SecurityException exception) {
            Log.w(TAG, "Exact alarm refused, using inexact", exception);
            alarmManager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, triggerAt, pendingIntent);
        }
    }

    private static PendingIntent buildPendingIntent(Context context, int flag) {
        Intent intent = new Intent(context, TrackingAlarmReceiver.class).setAction(ACTION_TICK);
        return PendingIntent.getBroadcast(context, REQUEST_CODE, intent, flag | PendingIntent.FLAG_IMMUTABLE);
    }

    // ---- last fix cache: LocationService keeps it warm, a tick falls back to it ----

    static void saveFix(Context context, Location location) {
        context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
                .putString(KEY_LAT, String.valueOf(location.getLatitude()))
                .putString(KEY_LON, String.valueOf(location.getLongitude()))
                .putLong(KEY_ELAPSED_MS, location.getElapsedRealtimeNanos() / 1_000_000L)
                .apply();
    }

    /** @return {lat, lon} of the cached fix if it is younger than maxAgeMs, otherwise null. */
    static double[] getCachedFix(Context context, long maxAgeMs) {
        SharedPreferences prefs = context.getApplicationContext().getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        String lat = prefs.getString(KEY_LAT, null);
        String lon = prefs.getString(KEY_LON, null);
        long age = SystemClock.elapsedRealtime() - prefs.getLong(KEY_ELAPSED_MS, 0L);
        if (lat == null || lon == null || age < 0 || age > maxAgeMs) {
            return null; // age < 0: device rebooted since the fix was stored
        }
        try {
            return new double[]{Double.parseDouble(lat), Double.parseDouble(lon)};
        } catch (NumberFormatException exception) {
            return null;
        }
    }
}
