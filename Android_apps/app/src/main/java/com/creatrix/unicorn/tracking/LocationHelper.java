package com.creatrix.unicorn.tracking;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.location.LocationManagerCompat;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.Granularity;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.tasks.CancellationTokenSource;

import java.util.ArrayList;
import java.util.List;

/**
 * Everything about *getting* a location, plus the static checks for what could stop us from getting one.
 *
 * <p>Primary source: FusedLocationProviderClient with a LocationRequest built from the config. If
 * Google Play services is missing, or the fused provider reports that no location is available (GPS
 * has no sky, Location Services in "device only" mode...), the platform LocationManager providers
 * (GPS + NETWORK) are switched on as a fallback and switched off again as soon as the fused provider
 * delivers again. Fixes are filtered (accuracy, staleness, duplicates) before they reach the listener.
 * The listener is called on the looper passed to the constructor.
 */
public final class LocationHelper {

    public interface Listener {
        /** A fix that passed the filters. */
        void onLocation(@NonNull Location location);

        /** True when a usable location source exists, false while none does. */
        void onAvailabilityChanged(boolean available);
    }

    private static final String TAG = "LocationHelper";
    /** Network-provider fixes are coarse; when they are all we have, be far more tolerant. */
    static final float FALLBACK_MAX_ACCURACY_M = 3_000f;

    private final Context context;
    private final Listener listener;
    private final Looper looper;
    private final FusedLocationProviderClient fused;
    private final LocationManager platform;
    private final boolean playServicesAvailable;

    private TrackingConfig config;
    private boolean started;
    private boolean fallbackActive;
    private long lastDeliveredTime = Long.MIN_VALUE;

    public LocationHelper(Context context, TrackingConfig config, Listener listener, Looper looper) {
        this.context = context.getApplicationContext();
        this.config = config;
        this.listener = listener;
        this.looper = looper;
        this.fused = LocationServices.getFusedLocationProviderClient(this.context);
        this.platform = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
        this.playServicesAvailable = GoogleApiAvailability.getInstance()
                .isGooglePlayServicesAvailable(this.context) == ConnectionResult.SUCCESS;
    }

    // ------------------------------------------------------------------ updates

    private final LocationCallback fusedCallback = new LocationCallback() {
        @Override
        public void onLocationResult(@NonNull LocationResult result) {
            // Batched results can carry several fixes; deliver them oldest first.
            for (Location location : result.getLocations()) {
                deliver(location, false);
            }
        }

        @Override
        public void onLocationAvailability(@NonNull LocationAvailability availability) {
            boolean available = availability.isLocationAvailable();
            if (available) {
                stopFallback();
            } else {
                startFallback();
            }
            listener.onAvailabilityChanged(available || fallbackActive);
        }
    };

    private final LocationListener platformListener = new LocationListener() {
        @Override
        public void onLocationChanged(@NonNull Location location) {
            deliver(location, true);
        }

        // Abstract on API < 30, so they must exist even though they do nothing.
        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {
        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {
        }
    };

    /**
     * Starts requesting updates. Safe to call again (re-registers with the current config).
     *
     * @return false if location permission is missing or the request could not be registered
     */
    @SuppressLint("MissingPermission")
    public synchronized boolean start() {
        if (!hasLocationPermission(context)) {
            return false;
        }
        stopInternal();
        started = true;
        if (!playServicesAvailable) {
            TrackLog.w(TAG, "Google Play services unavailable; using platform location providers");
            startFallback();
            return fallbackActive;
        }
        try {
            fused.requestLocationUpdates(buildRequest(config), fusedCallback, looper);
            return true;
        } catch (SecurityException e) {
            TrackLog.w(TAG, "Location permission revoked while starting", e);
            started = false;
            return false;
        }
    }

    public synchronized void stop() {
        stopInternal();
        started = false;
    }

    public synchronized void updateConfig(TrackingConfig newConfig) {
        this.config = newConfig;
        if (started) {
            start();
        }
    }

    private void stopInternal() {
        try {
            fused.removeLocationUpdates(fusedCallback);
        } catch (RuntimeException ignored) {
            // Not registered.
        }
        stopFallback();
    }

    /**
     * Asks for one fix right now from every source. Called when nothing has arrived for a while, so
     * the location comes back as soon as any source can produce one instead of on the next interval.
     */
    @SuppressLint("MissingPermission")
    public void requestImmediateFix() {
        if (!hasLocationPermission(context)) {
            return;
        }
        if (playServicesAvailable) {
            try {
                CancellationTokenSource cancel = new CancellationTokenSource();
                fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cancel.getToken())
                        .addOnSuccessListener(location -> {
                            if (location != null) {
                                deliver(location, true);
                            }
                        });
                fused.getLastLocation().addOnSuccessListener(location -> {
                    if (location != null && isFreshEnough(location)) {
                        deliver(location, true);
                    }
                });
            } catch (SecurityException e) {
                TrackLog.w(TAG, "Location permission revoked", e);
            }
        }
        startFallback();
    }

    // ------------------------------------------------------------------ platform fallback

    @SuppressLint("MissingPermission")
    private synchronized void startFallback() {
        if (fallbackActive || !started || platform == null || !hasLocationPermission(context)) {
            return;
        }
        long interval = config.intervalMs;
        boolean any = false;
        for (String provider : new String[]{LocationManager.NETWORK_PROVIDER, LocationManager.GPS_PROVIDER}) {
            try {
                if (platform.isProviderEnabled(provider)) {
                    platform.requestLocationUpdates(provider, interval, config.minDisplacementMeters,
                            platformListener, looper);
                    any = true;
                }
            } catch (SecurityException | IllegalArgumentException e) {
                TrackLog.d(TAG, "Provider " + provider + " unusable: " + e.getClass().getSimpleName());
            }
        }
        fallbackActive = any;
        if (any) {
            TrackLog.i(TAG, "Fallback location providers active");
        }
    }

    private synchronized void stopFallback() {
        if (!fallbackActive || platform == null) {
            fallbackActive = false;
            return;
        }
        try {
            platform.removeUpdates(platformListener);
        } catch (RuntimeException ignored) {
            // Nothing registered.
        }
        fallbackActive = false;
        TrackLog.i(TAG, "Fallback location providers stopped");
    }

    // ------------------------------------------------------------------ filtering

    private synchronized void deliver(Location location, boolean coarseSourceAllowed) {
        float maxAccuracy = config.maxAccuracyMeters;
        if (fallbackActive || coarseSourceAllowed) {
            maxAccuracy = Math.max(maxAccuracy, FALLBACK_MAX_ACCURACY_M);
        }
        if (maxAccuracy > 0 && location.hasAccuracy() && location.getAccuracy() > maxAccuracy) {
            TrackLog.d(TAG, "Dropped fix, accuracy " + location.getAccuracy() + " m");
            return;
        }
        // Out of order or repeated (the same fix can arrive from the fused and platform paths).
        if (location.getTime() <= lastDeliveredTime) {
            return;
        }
        lastDeliveredTime = location.getTime();
        listener.onLocation(location);
    }

    private boolean isFreshEnough(Location location) {
        long ageMs = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1_000_000L;
        return ageMs >= 0 && ageMs <= 2 * config.intervalMs;
    }

    // ------------------------------------------------------------------ request construction

    public static LocationRequest buildRequest(TrackingConfig config) {
        int priority = config.accuracy == TrackingConfig.Accuracy.HIGH
                ? Priority.PRIORITY_HIGH_ACCURACY
                : Priority.PRIORITY_BALANCED_POWER_ACCURACY;
        return new LocationRequest.Builder(priority, config.intervalMs)
                .setMinUpdateIntervalMillis(config.fastestIntervalMs)
                .setMinUpdateDistanceMeters(config.minDisplacementMeters)
                // Never hold a report back waiting for a better fix; the accuracy is stored with the record.
                .setWaitForAccurateLocation(false)
                .setGranularity(Granularity.GRANULARITY_PERMISSION_LEVEL)
                .build();
    }

    // ------------------------------------------------------------------ state checks

    public static boolean hasFineLocationPermission(Context context) {
        return granted(context, Manifest.permission.ACCESS_FINE_LOCATION);
    }

    public static boolean hasCoarseLocationPermission(Context context) {
        return granted(context, Manifest.permission.ACCESS_COARSE_LOCATION);
    }

    /** Fine or coarse: enough to start the foreground service and receive updates while it is visible. */
    public static boolean hasLocationPermission(Context context) {
        return hasFineLocationPermission(context) || hasCoarseLocationPermission(context);
    }

    /** Needed to keep receiving location when the service was started from the background (boot, watchdog). */
    public static boolean hasBackgroundLocationPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q
                || granted(context, Manifest.permission.ACCESS_BACKGROUND_LOCATION);
    }

    public static boolean hasNotificationPermission(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU
                || granted(context, Manifest.permission.POST_NOTIFICATIONS);
    }

    public static boolean isLocationServicesEnabled(Context context) {
        LocationManager lm = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return lm != null && LocationManagerCompat.isLocationEnabled(lm);
    }

    public static boolean isIgnoringBatteryOptimizations(Context context) {
        PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        return pm == null || pm.isIgnoringBatteryOptimizations(context.getPackageName());
    }

    /**
     * Everything currently stopping or degrading tracking, in the order the UI should resolve it
     * (the order of {@link TrackingProblem}).
     */
    public static List<TrackingProblem> detectProblems(Context context, TrackingPrefs prefs) {
        Context app = context.getApplicationContext();
        TrackingConfig config = prefs.getConfig();
        List<TrackingProblem> problems = new ArrayList<>();
        if (!prefs.hasConsent()) {
            problems.add(TrackingProblem.CONSENT_MISSING);
        }
        if (!hasLocationPermission(app)) {
            problems.add(TrackingProblem.LOCATION_PERMISSION_MISSING);
        } else if (!hasFineLocationPermission(app)) {
            problems.add(TrackingProblem.PRECISE_LOCATION_MISSING);
        }
        if (!hasNotificationPermission(app)) {
            problems.add(TrackingProblem.NOTIFICATION_PERMISSION_MISSING);
        }
        if (config.requireBackgroundLocation && !hasBackgroundLocationPermission(app)) {
            problems.add(TrackingProblem.BACKGROUND_LOCATION_MISSING);
        }
        if (!isLocationServicesEnabled(app)) {
            problems.add(TrackingProblem.LOCATION_SERVICES_OFF);
        }
        if (!isIgnoringBatteryOptimizations(app)) {
            problems.add(TrackingProblem.BATTERY_OPTIMIZED);
        }
        return problems;
    }

    /** Android flags fixes from "mock location" apps; the record carries the flag instead of hiding it. */
    public static boolean isMock(@Nullable Location location) {
        if (location == null) {
            return false;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? location.isMock()
                : location.isFromMockProvider();
    }

    private static boolean granted(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
