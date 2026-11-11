package com.creatrix.unicorn.UtilityHelper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

/**
 * Single place for the "strict location" rules: precise permission, GPS on, a fresh and accurate
 * fix, and no mock (fake GPS) provider. Screens that save attendance/DCR/coordinates go through it.
 */
public final class LocationGuard {
    /** A fix must be at least this accurate (metres) to be accepted. */
    public static final float MAX_ACCURACY_M = 500f;
    /** DCR submit: inside a hospital/chamber only cell/WiFi fixes exist; IP guesses (5000 m) are still rejected. */
    public static final float DCR_MAX_ACCURACY_M = 3_000f;
    /** A fix older than this (ms) is rejected. */
    public static final long MAX_AGE_MS = 300_000L;
    private static final long FIX_TIMEOUT_MS = 5_000L;
    private static final float IP_ACCURACY_M = 5_000f;

    public interface Callback {
        /** Exactly one of the arguments is non-null. Always delivered on the main thread. */
        void onResult(@Nullable Location location, @Nullable String error);
    }

    private LocationGuard() {
    }

    /** @return null when permission and GPS are fine, otherwise a message for the user. */
    @Nullable
    public static String checkEnvironment(@NonNull Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
                && ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return "Location permission is required. Please allow location for this app.";
        }
        LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        if (manager == null) {
            return "Location service not available.";
        }
        boolean isGpsEnabled = manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        boolean isNetworkEnabled = manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!isGpsEnabled && !isNetworkEnabled) {
            return "Please turn on GPS (location) to continue.";
        }
        return null;
    }

    /** @return null when the fix is acceptable, otherwise a message for the user. */
    @Nullable
    public static String checkFix(@Nullable Location location) {
        return checkFix(location, MAX_ACCURACY_M);
    }

    /** Same as {@link #checkFix(Location)} with a caller-chosen accuracy limit (metres). */
    @Nullable
    public static String checkFix(@Nullable Location location, float maxAccuracyM) {
        if (location == null) {
            return "Your location is not available yet. Please wait for GPS and try again.";
        }
        if (com.creatrix.unicorn.BuildConfig.DEBUG) {
            return null; // In DEBUG build, allow testing from any location or with fallback fix
        }
        if (isMock(location)) {
            return "Fake/mock location detected. Turn off any mock location app to continue.";
        }
        long ageMs = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1_000_000L;
        if (ageMs > MAX_AGE_MS) {
            return "Your location is outdated. Please wait for a fresh GPS fix and try again.";
        }
        if (!location.hasAccuracy() || location.getAccuracy() > maxAccuracyM) {
            return "GPS accuracy is too low. Move to an open area and try again.";
        }
        return null;
    }

    /** @return true when the fix is recent enough to pass {@link #checkFix}'s age rule. */
    public static boolean isFresh(@Nullable Location location) {
        if (location == null) {
            return false;
        }
        long ageMs = (SystemClock.elapsedRealtimeNanos() - location.getElapsedRealtimeNanos()) / 1_000_000L;
        return ageMs <= MAX_AGE_MS;
    }

    public static boolean isMock(@Nullable Location location) {
        if (location == null) {
            return false;
        }
        if (com.creatrix.unicorn.BuildConfig.DEBUG) {
            return false;
        }
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                ? location.isMock()
                : location.isFromMockProvider();
    }

    /** Environment check, then active live updates (Fused + Network) with instant cached check and IP fallback. */
    public static void requestFix(@NonNull Context context, @NonNull Callback callback) {
        String environmentError = checkEnvironment(context);
        if (environmentError != null) {
            callback.onResult(null, environmentError);
            return;
        }
        try {
            final boolean[] delivered = {false};
            final FusedLocationProviderClient fused = LocationServices.getFusedLocationProviderClient(context);
            final LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);

            // 1. Instant check: If fused or location manager already has a recent location, return it immediately
            fused.getLastLocation().addOnSuccessListener(lastLoc -> {
                if (!delivered[0] && lastLoc != null && !isMock(lastLoc) && isFresh(lastLoc)) {
                    android.util.Log.d("LocationGuard", "Delivered from fused.getLastLocation: " + lastLoc);
                    delivered[0] = true;
                    callback.onResult(lastLoc, null);
                }
            });

            Location bestImmediate = getBestLocationFromManager(context);
            if (!delivered[0] && bestImmediate != null && !isMock(bestImmediate) && isFresh(bestImmediate)) {
                android.util.Log.d("LocationGuard", "Delivered from bestImmediate: " + bestImmediate);
                delivered[0] = true;
                callback.onResult(bestImmediate, null);
                return;
            }

            // 2. Active Fused Location Updates (High Accuracy + Network/Wi-Fi)
            final LocationRequest liveReq = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setMinUpdateIntervalMillis(500L)
                    .setMaxUpdateDelayMillis(1500L)
                    .setDurationMillis(FIX_TIMEOUT_MS)
                    .build();

            final LocationCallback fusedCallback = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult locationResult) {
                    if (delivered[0]) return;
                    Location loc = locationResult.getLastLocation();
                    if (loc != null && !isMock(loc)) {
                        android.util.Log.d("LocationGuard", "Delivered from fusedCallback: " + loc);
                        delivered[0] = true;
                        try {
                            fused.removeLocationUpdates(this);
                        } catch (Exception ignored) {}
                        callback.onResult(loc, null);
                    }
                }
            };

            fused.requestLocationUpdates(liveReq, fusedCallback, Looper.getMainLooper());

            // 3. Single-shot requests with Fused Provider (both High Accuracy and Balanced Power/Network)
            fused.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnSuccessListener(loc -> {
                if (!delivered[0] && loc != null && !isMock(loc)) {
                    android.util.Log.d("LocationGuard", "Delivered from getCurrentLocation(HIGH_ACCURACY): " + loc);
                    delivered[0] = true;
                    try {
                        fused.removeLocationUpdates(fusedCallback);
                    } catch (Exception ignored) {}
                    callback.onResult(loc, null);
                }
            });

            fused.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, null).addOnSuccessListener(loc -> {
                if (!delivered[0] && loc != null && !isMock(loc)) {
                    android.util.Log.d("LocationGuard", "Delivered from getCurrentLocation(BALANCED): " + loc);
                    delivered[0] = true;
                    try {
                        fused.removeLocationUpdates(fusedCallback);
                    } catch (Exception ignored) {}
                    callback.onResult(loc, null);
                }
            });

            // 4. Concurrently request from Android's LocationManager (Network & GPS)
            final LocationListener managerListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    if (delivered[0]) return;
                    if (!isMock(location)) {
                        android.util.Log.d("LocationGuard", "Delivered from managerListener: " + location);
                        delivered[0] = true;
                        try {
                            if (locationManager != null) {
                                locationManager.removeUpdates(this);
                            }
                            fused.removeLocationUpdates(fusedCallback);
                        } catch (Exception ignored) {}
                        callback.onResult(location, null);
                    }
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(@NonNull String provider) {}
                @Override
                public void onProviderDisabled(@NonNull String provider) {}
            };

            if (locationManager != null) {
                if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000L, 0f, managerListener, Looper.getMainLooper());
                }
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000L, 0f, managerListener, Looper.getMainLooper());
                }
            }

            // 5. Timeout handler: if GPS/Network cannot lock indoors, fallback to IP Geolocation
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (delivered[0]) return;
                try {
                    fused.removeLocationUpdates(fusedCallback);
                    if (locationManager != null) {
                        locationManager.removeUpdates(managerListener);
                    }
                } catch (Exception ignored) {}

                // Check best location from manager or fused one more time; a stale cache would only fail checkFix's age rule
                Location best = getBestLocationFromManager(context);
                if (best != null && !isMock(best) && isFresh(best)) {
                    delivered[0] = true;
                    android.util.Log.d("LocationGuard", "Delivered on timeout from getBestLocationFromManager: " + best);
                    callback.onResult(best, null);
                    return;
                }
                fused.getLastLocation().addOnSuccessListener(loc -> {
                    if (!delivered[0] && loc != null && !isMock(loc) && isFresh(loc)) {
                        delivered[0] = true;
                        android.util.Log.d("LocationGuard", "Delivered on timeout from fused.getLastLocation: " + loc);
                        callback.onResult(loc, null);
                    } else if (!delivered[0]) {
                        delivered[0] = true;
                        android.util.Log.d("LocationGuard", "GPS/Network unavailable indoors. Trying cell/WiFi fallback...");
                        fetchFallbackLocation(context, callback);
                    }
                }).addOnFailureListener(e -> {
                    if (!delivered[0]) {
                        delivered[0] = true;
                        android.util.Log.d("LocationGuard", "Fused failed on timeout. Trying cell/WiFi fallback: " + e.getMessage());
                        fetchFallbackLocation(context, callback);
                    }
                });
            }, FIX_TIMEOUT_MS);

        } catch (SecurityException e) {
            callback.onResult(null, "Location permission is required.");
        }
    }

    /**
     * Keeps high-accuracy updates (GPS + WiFi + cell) running for up to waitMs and delivers the first fix
     * within maxAccuracyM, else the most accurate one seen; with none at all it falls back to {@link #requestFix}.
     */
    public static void requestAccurateFix(@NonNull Context context, float maxAccuracyM, long waitMs, @NonNull Callback callback) {
        String environmentError = checkEnvironment(context);
        if (environmentError != null) {
            callback.onResult(null, environmentError);
            return;
        }
        try {
            final FusedLocationProviderClient fused = LocationServices.getFusedLocationProviderClient(context);
            final Handler handler = new Handler(Looper.getMainLooper());
            final Location[] best = {null};
            final boolean[] done = {false};
            final LocationCallback updates = new LocationCallback() {
                @Override
                public void onLocationResult(@NonNull LocationResult result) {
                    if (done[0]) return;
                    for (Location loc : result.getLocations()) {
                        if (!isMock(loc) && loc.hasAccuracy()
                                && (best[0] == null || loc.getAccuracy() < best[0].getAccuracy())) {
                            best[0] = loc;
                        }
                    }
                    if (best[0] != null && best[0].getAccuracy() <= maxAccuracyM) {
                        done[0] = true;
                        fused.removeLocationUpdates(this);
                        handler.removeCallbacksAndMessages(null);
                        callback.onResult(best[0], null);
                    }
                }
            };
            fused.requestLocationUpdates(new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
                    .setMinUpdateIntervalMillis(500L)
                    .setDurationMillis(waitMs)
                    .build(), updates, Looper.getMainLooper());
            handler.postDelayed(() -> {
                if (done[0]) return;
                done[0] = true;
                fused.removeLocationUpdates(updates);
                if (best[0] != null) {
                    callback.onResult(best[0], null);
                } else {
                    requestFix(context, callback);
                }
            }, waitMs);
        } catch (SecurityException e) {
            callback.onResult(null, "Location permission is required.");
        }
    }

    /**
     * Indoors / narrow lanes, where no GPS or fused fix arrives: nearby cell towers + WiFi resolved by
     * salesrollapi (same path the background tracking uses), then IP geolocation as the very last resort.
     */
    private static void fetchFallbackLocation(@NonNull Context context, @NonNull Callback callback) {
        final Context app = context.getApplicationContext();
        new Thread(() -> {
            Location resolved = com.creatrix.unicorn.tracking.FallbackLocator.locate(app);
            if (resolved == null) {
                resolved = tryQueryIpApi();
            }
            if (resolved == null) {
                resolved = tryQueryIpInfo();
            }
            final Location finalLoc = resolved;
            new Handler(Looper.getMainLooper()).post(() -> {
                if (finalLoc != null) {
                    android.util.Log.d("LocationGuard", "Delivered from cell/WiFi/IP fallback: " + finalLoc);
                    callback.onResult(finalLoc, null);
                } else {
                    android.util.Log.w("LocationGuard", "All location sources failed including IP");
                    callback.onResult(null, "Could not get GPS fix indoors. Please check internet/GPS or enter coordinates manually.");
                }
            });
        }).start();
    }

    @Nullable
    private static Location tryQueryIpApi() {
        try {
            java.net.URL url = new java.net.URL("http://ip-api.com/json");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                org.json.JSONObject obj = new org.json.JSONObject(sb.toString());
                if ("success".equalsIgnoreCase(obj.optString("status"))) {
                    double lat = obj.getDouble("lat");
                    double lon = obj.getDouble("lon");
                    Location loc = new Location(LocationManager.NETWORK_PROVIDER);
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    // IP gives the ISP's city, not the user: honest accuracy so checkFix never treats it as precise
                    loc.setAccuracy(IP_ACCURACY_M);
                    loc.setTime(System.currentTimeMillis());
                    loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    return loc;
                }
            }
        } catch (Exception e) {
            android.util.Log.w("LocationGuard", "ip-api failed: " + e.getMessage());
        }
        return null;
    }

    @Nullable
    private static Location tryQueryIpInfo() {
        try {
            java.net.URL url = new java.net.URL("https://ipinfo.io/json");
            java.net.HttpURLConnection conn = (java.net.HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(4000);
            conn.setReadTimeout(4000);
            conn.setRequestMethod("GET");
            if (conn.getResponseCode() == 200) {
                java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(conn.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();
                org.json.JSONObject obj = new org.json.JSONObject(sb.toString());
                String locStr = obj.optString("loc");
                if (locStr != null && locStr.contains(",")) {
                    String[] parts = locStr.split(",");
                    double lat = Double.parseDouble(parts[0].trim());
                    double lon = Double.parseDouble(parts[1].trim());
                    Location loc = new Location(LocationManager.NETWORK_PROVIDER);
                    loc.setLatitude(lat);
                    loc.setLongitude(lon);
                    loc.setAccuracy(IP_ACCURACY_M);
                    loc.setTime(System.currentTimeMillis());
                    loc.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
                    return loc;
                }
            }
        } catch (Exception e) {
            android.util.Log.w("LocationGuard", "ipinfo failed: " + e.getMessage());
        }
        return null;
    }

    @Nullable
    public static Location getBestLocationFromManager(@NonNull Context context) {
        try {
            LocationManager manager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
            if (manager == null) return null;
            java.util.List<String> providers = manager.getProviders(true);
            Location best = null;
            for (String provider : providers) {
                Location l = manager.getLastKnownLocation(provider);
                if (l == null) continue;
                if (best == null || l.getAccuracy() < best.getAccuracy()) {
                    best = l;
                }
            }
            return best;
        } catch (SecurityException e) {
            return null;
        }
    }

    /** Blocking reverse-geocode; returns "" when unavailable. */
    @NonNull
    public static String addressOf(@NonNull Context context, @NonNull Location location) {
        return SmartAddressResolver.resolveAddress(context, location.getLatitude(), location.getLongitude());
    }

    public static float distanceMeters(double lat1, double lon1, double lat2, double lon2) {
        float[] result = new float[1];
        Location.distanceBetween(lat1, lon1, lat2, lon2, result);
        return result[0];
    }

    /** @return the parsed coordinate, or null if empty/invalid/zero (i.e. not a real saved location). */
    @Nullable
    public static Double parseCoordinate(@Nullable String value) {
        if (value == null) {
            return null;
        }
        try {
            double parsed = Double.parseDouble(value.trim());
            return parsed == 0d ? null : parsed;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
