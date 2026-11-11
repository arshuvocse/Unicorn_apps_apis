package com.creatrix.unicorn;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import com.creatrix.unicorn.Activity.MainActivity;
import com.creatrix.unicorn.HelperActivity.SplashScreenActivity;
import com.creatrix.unicorn.Services.LocationService;
import com.creatrix.unicorn.Services.TrackingWatchdogWorker;
import com.creatrix.unicorn.UtilityHelper.BackgroundAccess;
import com.creatrix.unicorn.UtilityHelper.LocationAccess;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;

import java.lang.ref.WeakReference;
import java.util.WeakHashMap;

public class UnicornApplication extends Application implements Application.ActivityLifecycleCallbacks {

    private WeakHashMap<Activity, AlertDialog> gpsDialogs = new WeakHashMap<>();
    private WeakReference<Activity> currentActivityRef;

    @Override
    public void onCreate() {
        super.onCreate();
        registerActivityLifecycleCallbacks(this);
        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (LocationManager.PROVIDERS_CHANGED_ACTION.equals(intent.getAction())) {
                    if (currentActivityRef != null && currentActivityRef.get() != null) {
                        checkAndShowGpsDialog(currentActivityRef.get());
                    }
                }
            }
        }, new IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION));
    }

    private void checkAndShowGpsDialog(Activity activity) {
        if (activity == null || activity.isFinishing() || activity.isDestroyed()) return;
        if (activity instanceof MainActivity || activity instanceof SplashScreenActivity) return;

        boolean isEnabled = isLocationEnabled(activity);
        AlertDialog dialog = gpsDialogs.get(activity);

        if (!isEnabled) {
            if (dialog == null || !dialog.isShowing()) {
                dialog = new AlertDialog.Builder(activity)
                        .setMessage("GPS is disabled in your device. Would you like to enable it?")
                        .setCancelable(false)
                        .setPositiveButton("GOTO SETTINGS PAGE TO ENABLE...",
                                (d, id) -> {
                                    Intent callGPSSettingIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                    activity.startActivity(callGPSSettingIntent);
                                })
                        .create();
                dialog.show();
                gpsDialogs.put(activity, dialog);
            }
        } else {
            if (dialog != null && dialog.isShowing()) {
                dialog.dismiss();
                gpsDialogs.remove(activity);
            }
        }
    }

    private boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null) return false;
        try {
            return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                    || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        currentActivityRef = new WeakReference<>(activity);

        if (activity instanceof MainActivity || activity instanceof SplashScreenActivity) {
            return;
        }

        if (new SessionManagement(activity).isLoggedIn()) {
            LocationService.start(activity);
            TrackingWatchdogWorker.schedule(activity);
            BackgroundAccess.enforceBatteryExemption(activity);
            BackgroundAccess.enforceAllTheTimeLocation(activity);
            BackgroundAccess.promptAutostartOnce(activity);
        }

        checkAndShowGpsDialog(activity);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        if (currentActivityRef != null && currentActivityRef.get() == activity) {
            currentActivityRef.clear();
        }
    }

    @Override public void onActivityCreated(Activity activity, Bundle savedInstanceState) { }
    @Override public void onActivityStarted(Activity activity) { }
    @Override public void onActivityStopped(Activity activity) { }
    @Override public void onActivitySaveInstanceState(Activity activity, Bundle outState) { }

    @Override
    public void onActivityDestroyed(Activity activity) {
        AlertDialog dialog = gpsDialogs.get(activity);
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
            gpsDialogs.remove(activity);
        }
    }
}
