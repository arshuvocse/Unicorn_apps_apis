package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Best-effort reverse geocoding. Geocoder has no timeout of its own and can hang when the network is
 * bad, so each lookup runs on a helper thread and is abandoned after {@link #TIMEOUT_MS}; a missing
 * address never delays or loses a location. The previous result is reused while the device has moved
 * less than {@link #REUSE_RADIUS_M}, which also saves most of the lookups.
 */
public final class AddressResolver {
    private static final String TAG = "AddressResolver";
    // Geocoder (~1-2 s) plus one OSM road-level lookup (<= 5 s) when Geocoder only knows the city
    private static final long TIMEOUT_MS = 7_000L;
    private static final float REUSE_RADIUS_M = 75f;

    private final Context context;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private double lastLat = Double.NaN;
    private double lastLon = Double.NaN;
    @Nullable
    private String lastAddress;

    public AddressResolver(Context context) {
        this.context = context.getApplicationContext();
    }

    /** @return the place name, or null if it could not be resolved (offline, no Geocoder, timeout). */
    @Nullable
    @WorkerThread
    public synchronized String resolve(double lat, double lon) {
        if (lastAddress != null && !Double.isNaN(lastLat)) {
            float[] distance = new float[1];
            Location.distanceBetween(lastLat, lastLon, lat, lon, distance);
            if (distance[0] < REUSE_RADIUS_M) {
                return lastAddress;
            }
        }
        if (!Geocoder.isPresent() || !NetworkMonitor.isOnline(context)) {
            return null;
        }

        Future<String> future = executor.submit(() -> lookup(lat, lon));
        try {
            String address = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
            if (address != null) {
                lastLat = lat;
                lastLon = lon;
                lastAddress = address;
            }
            return address;
        } catch (InterruptedException e) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return null;
        } catch (Exception e) {
            future.cancel(true);
            TrackLog.d(TAG, "Address lookup failed: " + e.getClass().getSimpleName());
            return null;
        }
    }

    public void shutdown() {
        executor.shutdownNow();
    }

    @Nullable
    private String lookup(double lat, double lon) {
        String resolved = com.creatrix.unicorn.UtilityHelper.SmartAddressResolver.resolveAddress(context, lat, lon);
        return (resolved != null && !resolved.trim().isEmpty()) ? resolved.trim() : null;
    }
}
