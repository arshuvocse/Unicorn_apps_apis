package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.location.Location;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.creatrix.unicorn.tracking.api.GeoResolveClient;
import com.creatrix.unicorn.tracking.api.GeoResolveModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Last-resort location when GPS / fused deliver nothing: nearby cell towers + WiFi access points are
 * sent to salesrollapi, which resolves them through Google Geolocation (the key never leaves the server).
 * The result is coarse (WiFi tens of metres, cell hundreds of metres or more) and carries its real
 * accuracy and provider "wifi" / "cell".
 */
public final class FallbackLocator {

    private static final String TAG = "FallbackLocator";
    /** Only the strongest access points identify the place; weak ones come and go between scans. */
    private static final int KEY_TOP_WIFI = 3;

    // Cost guard: the same radio environment resolves to the same place, so do not ask the server again.
    private static String lastKey;
    private static GeoResolveModels.ResolveResponse lastResponse;

    private FallbackLocator() {
    }

    @WorkerThread
    @Nullable
    public static synchronized Location locate(Context context) {
        GeoResolveModels.ResolveRequest request = RadioScanner.scan(context);
        if (request == null) {
            TrackLog.d(TAG, "No cell towers or WiFi access points visible");
            return null;
        }
        request.employeeId = TrackingPrefs.get(context).getUserId();

        String key = keyOf(request);
        GeoResolveModels.ResolveResponse response;
        if (key.equals(lastKey) && lastResponse != null) {
            TrackLog.d(TAG, "Radio environment unchanged; reusing previous result");
            response = lastResponse;
        } else {
            response = GeoResolveClient.resolve(context, request);
            if (response == null) {
                TrackLog.d(TAG, "Server could not resolve cells=" + request.cellTowers.size()
                        + " wifi=" + request.wifiAccessPoints.size());
                return null;
            }
            lastKey = key;
            lastResponse = response;
        }

        Location location = new Location(response.source != null ? response.source : "cell");
        location.setLatitude(response.latitude);
        location.setLongitude(response.longitude);
        location.setAccuracy((float) response.accuracy);
        location.setTime(System.currentTimeMillis());
        location.setElapsedRealtimeNanos(SystemClock.elapsedRealtimeNanos());
        return location;
    }

    private static String keyOf(GeoResolveModels.ResolveRequest request) {
        List<String> cells = new ArrayList<>();
        for (GeoResolveModels.CellTower c : request.cellTowers) {
            cells.add(c.radioType + "/" + c.mobileCountryCode + "/" + c.mobileNetworkCode + "/"
                    + c.locationAreaCode + "/" + c.cellId);
        }
        Collections.sort(cells);

        List<GeoResolveModels.WifiAccessPoint> byStrength = new ArrayList<>(request.wifiAccessPoints);
        Collections.sort(byStrength, (a, b) -> Integer.compare(
                b.signalStrength != null ? b.signalStrength : Integer.MIN_VALUE,
                a.signalStrength != null ? a.signalStrength : Integer.MIN_VALUE));
        List<String> wifi = new ArrayList<>();
        for (int i = 0; i < byStrength.size() && i < KEY_TOP_WIFI; i++) {
            wifi.add(String.valueOf(byStrength.get(i).macAddress));
        }
        Collections.sort(wifi);
        return cells + "|" + wifi;
    }
}
