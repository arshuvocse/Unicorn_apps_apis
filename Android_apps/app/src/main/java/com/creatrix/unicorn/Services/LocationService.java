package com.creatrix.unicorn.Services;

import android.Manifest;
import android.app.AlarmManager;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Address;
import android.location.Geocoder;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.creatrix.unicorn.DBInfo.TrackingQueueDbHelper;
import com.creatrix.unicorn.Model.UserTracking;
import com.creatrix.unicorn.R;
import com.creatrix.unicorn.UtilityHelper.LocationAccess;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.AlarmManager.ELAPSED_REALTIME;

public class LocationService extends Service {
    private static final String TAG = "LocationService";
    private static final long RESTART_DELAY_MS = 5_000L;
    private final LocationCallback locationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult locationResult) {
            super.onLocationResult(locationResult);
            if (locationResult.getLastLocation() != null) {
                double latitude = locationResult.getLastLocation().getLatitude();
                double longitude = locationResult.getLastLocation().getLongitude();
                UpdateLocationData(latitude, longitude);
            }
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    public static void start(Context context) {
        if (!LocationAccess.hasRequiredPermissions(context) || !LocationAccess.isLocationEnabled(context)) {
            Log.w(TAG, "Cannot start LocationService: missing permissions or location disabled");
            return;
        }
        Intent intent = new Intent(context, LocationService.class);
        intent.setAction(Constants.ACTION_START_LOCATION_SERVICE);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ContextCompat.startForegroundService(context, intent);
            } else {
                context.startService(intent);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Unable to start location service", exception);
        }
    }

    private void startLocationService() {
        if (!LocationAccess.hasRequiredPermissions(this) || !LocationAccess.isLocationEnabled(this)) {
            Log.w(TAG, "Location permissions not granted or location disabled. Stopping service.");
            stopSelf();
            return;
        }

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
            Log.e(TAG, "Failed to start foreground location service", e);
            stopSelf();
            return;
        }

        LocationRequest locationRequest = new LocationRequest();
        locationRequest.setInterval(300000);
        locationRequest.setFastestInterval(180000);
        locationRequest.setSmallestDisplacement(10.2f);
        locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        try {
            LocationServices.getFusedLocationProviderClient(this)
                    .requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
        } catch (SecurityException e) {
            Log.e(TAG, "Missing location permissions for location updates", e);
            stopSelf();
        }
    }

    private void stopLocationService() {
        try {
            LocationServices.getFusedLocationProviderClient(this)
                    .removeLocationUpdates(locationCallback);
        } catch (Exception e) {
            Log.e(TAG, "Error removing location updates", e);
        }
        stopForeground(true);
        stopSelf();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!LocationAccess.hasRequiredPermissions(this) || !LocationAccess.isLocationEnabled(this)) {
            Log.w(TAG, "Location permissions not granted or location disabled in onStartCommand. Stopping service.");
            stopSelf();
            return START_NOT_STICKY;
        }
        if(intent !=null){
            String action = intent.getAction();
            if(action !=null){
                if(action.equals(Constants.ACTION_START_LOCATION_SERVICE)){
                    startLocationService();
                }else if(action.equals(Constants.ACTION_STOP_LOCATION_SERVICE)){
                    stopLocationService();
                }
            }
        } else {
            startLocationService();
        }
        return START_STICKY;
    }

    private void scheduleRestart() {
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
        LocationServices.getFusedLocationProviderClient(this)
                .removeLocationUpdates(locationCallback);
        scheduleRestart();
        super.onDestroy();
    }

    public void UpdateLocationData(Double lat,Double lon){
        try {
            String addName = getCompleteAddressString(lat,lon);
            UserTracking userTracking = new UserTracking();
            int sessionId = SessionGet();
            userTracking.setEmpInfoId(sessionId);
            userTracking.setLatValue(String.valueOf(lat));
            userTracking.setLongValue(String.valueOf(lon));
            userTracking.setAddressName(String.valueOf(addName));
            Date now = new Date();
            userTracking.setTimeStr(new SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(now));
            userTracking.setTrackDate(new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(now));

            SaveTracking(userTracking);

        }catch (Exception ex){
            Log.d(TAG, "UpdateLocationData: "+ex);
        }

    }
    private String getCompleteAddressString(double LATITUDE, double LONGITUDE) {
        String strAdd = "";
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(LATITUDE, LONGITUDE, 1);
            if (addresses != null) {
                Address returnedAddress = addresses.get(0);
                StringBuilder strReturnedAddress = new StringBuilder("");

                for (int i = 0; i <= returnedAddress.getMaxAddressLineIndex(); i++) {
                    strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                }
                strAdd = strReturnedAddress.toString();
                Log.w("address", strReturnedAddress.toString());
            } else {
                Log.w("address", "No Address returned!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            Log.w("address", "Can't get Address!");
        }
        return strAdd;
    }
    public void SaveTracking(UserTracking aInfo) {
        try (TrackingQueueDbHelper database = new TrackingQueueDbHelper(getApplicationContext())) {
            database.enqueue(aInfo);
            TrackingSyncWorker.schedule(getApplicationContext());
        } catch (Exception exception) {
            Log.e(TAG, "Unable to queue tracking data", exception);
        }
    }
    public int SessionGet(){
        SessionManagement  session = new SessionManagement(getApplicationContext());
        HashMap<String, String> user = session.getUserDetails();
        return Integer.parseInt(Objects.requireNonNull(user.get(SessionManagement.KEY_EmpId)));
    }

}
