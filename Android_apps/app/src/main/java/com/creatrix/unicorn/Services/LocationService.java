package com.creatrix.unicorn.Services;

import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ServiceInfo;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

import static android.app.AlarmManager.ELAPSED_REALTIME;

/**
 * Foreground service that keeps the process alive and the GPS warm. The 5-minute tracking points
 * themselves are recorded by {@link TrackingAlarmReceiver}, so a missing location or a dead service
 * never leaves a gap. This service is not meant to be stoppable: it restarts itself whenever it dies.
 */
public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final long RESTART_DELAY_MS = 5_000L;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() != null) {
                // Only cache: the alarm tick records the point, so there is exactly one per 5 minutes.
                TrackingScheduler.saveFix(getApplicationContext(), locationResult.getLastLocation());
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    /** Best effort start; never gated on permission/GPS, because the alarm tick keeps recording regardless. */
    public static void start(Context context) {
        TrackingScheduler.ensure(context);
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception exception) {
            // e.g. background start not allowed right now; the next alarm tick / watchdog retries.
            Log.e(TAG, "Unable to start location service", exception);
        }
    }

    private void startLocationService() {
        String channelId = "location_notification_channel";
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        Intent resultIntent = new Intent();
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 0, resultIntent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(
                getApplicationContext(), channelId
        );
        builder.setSmallIcon(R.mipmap.ic_logomain);
        builder.setContentTitle("Unicorn Service");
        builder.setDefaults(NotificationCompat.DEFAULT_ALL);
        builder.setContentText("Running");
        builder.setContentIntent(pendingIntent);
        builder.setAutoCancel(false);
        builder.setOngoing(true);
        builder.setPriority(NotificationCompat.PRIORITY_MAX);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (notificationManager != null && notificationManager.getNotificationChannel(channelId) == null) {
                NotificationChannel notificationChannel = new NotificationChannel(
                        channelId, "Location Services", NotificationManager.IMPORTANCE_HIGH
                );
                notificationChannel.setDescription("This channel is used by location services");
                notificationManager.createNotificationChannel(notificationChannel);
            }
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(Constants.LOCATION_SERVICE_ID, builder.build(), ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(Constants.LOCATION_SERVICE_ID, builder.build());
            }
        } catch (Exception e) {
            // Typically: location permission not granted yet. The alarm tick still records "no location"
            // points, and the service is retried on the next tick.
            Log.e(TAG, "Failed to start foreground location service", e);
            stopSelf();
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, TrackingScheduler.INTERVAL_MS)
                .setMinUpdateIntervalMillis(2 * 60 * 1000L)
                .setMinUpdateDistanceMeters(0f) // no displacement filter: standing still still updates
                .build();

        try {
            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Missing location permissions for location updates", e);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        TrackingScheduler.ensure(this);
        // Every start (including a null intent after a kill) brings the service fully up; there is no stop action.
        startLocationService();
        return START_STICKY;
    }

    private void scheduleRestart() {
        if (!new SessionManagement(getApplicationContext()).isLoggedIn()) {
            return;
        }
        Intent restartIntent = new Intent(getApplicationContext(), LocationServiceReceiver.class);
        restartIntent.setAction("com.creatrix.unicorn.RESTART_LOCATION_SERVICE");
        PendingIntent restartPendingIntent = PendingIntent.getBroadcast(
                getApplicationContext(),
                1,
                restartIntent,
                PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarmService = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        if (alarmService != null) {
            alarmService.setAndAllowWhileIdle(
                    ELAPSED_REALTIME,
                    SystemClock.elapsedRealtime() + RESTART_DELAY_MS,
                    restartPendingIntent);
        }
    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        scheduleRestart();
        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        try {
            LocationServices.getFusedLocationProviderClient(this)
                    .removeLocationUpdates(locationCallback);
        } catch (Exception e) {
            Log.e(TAG, "Error removing location updates", e);
        }
        scheduleRestart();
        super.onDestroy();
    }
}
