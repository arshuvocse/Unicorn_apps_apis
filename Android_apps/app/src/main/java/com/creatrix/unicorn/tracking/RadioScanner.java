package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.SystemClock;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellIdentityWcdma;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellInfoWcdma;
import android.telephony.TelephonyManager;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.creatrix.unicorn.tracking.api.GeoResolveModels;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Collects the serving/neighbour cell towers and the cached WiFi scan, for server-side geolocation when
 * there is no GPS/fused fix. Note: on Android 10+ both getAllCellInfo() and getScanResults() return
 * nothing when the system location toggle is off, so this only helps when location is on but no fix
 * arrives (indoors, weak sky view).
 */
public final class RadioScanner {
    private static final String TAG = "RadioScanner";
    private static final int MAX_CELLS = 10;
    private static final int MAX_WIFI = 15;
    private static final long MAX_WIFI_AGE_MS = 5 * 60_000L;
    // CellInfo.UNAVAILABLE / UNAVAILABLE_LONG are API 29 constants; their values are these.
    private static final int UNAVAILABLE = Integer.MAX_VALUE;
    private static final long UNAVAILABLE_LONG = Long.MAX_VALUE;

    private RadioScanner() {
    }

    /** @return towers + access points (employeeId left null), or null if neither could be read. */
    @Nullable
    @WorkerThread
    public static GeoResolveModels.ResolveRequest scan(Context context) {
        Context app = context.getApplicationContext();
        GeoResolveModels.ResolveRequest request = new GeoResolveModels.ResolveRequest();
        request.cellTowers.addAll(scanCells(app, false));
        request.wifiAccessPoints.addAll(scanWifi(app));
        if (request.cellTowers.isEmpty() && request.wifiAccessPoints.isEmpty()) {
            TrackLog.d(TAG, "no cells or wifi available");
            return null;
        }
        TrackLog.d(TAG, "cells=" + request.cellTowers.size() + " wifi=" + request.wifiAccessPoints.size());
        return request;
    }

    /** @return the serving cell, or null when none could be read. Only this one marks where we are. */
    @Nullable
    @WorkerThread
    public static GeoResolveModels.CellTower servingCell(Context context) {
        List<GeoResolveModels.CellTower> cells = scanCells(context.getApplicationContext(), true);
        return cells.isEmpty() ? null : cells.get(0);
    }

    private static List<GeoResolveModels.CellTower> scanCells(Context context, boolean registeredOnly) {
        List<GeoResolveModels.CellTower> registered = new ArrayList<>();
        List<GeoResolveModels.CellTower> neighbours = new ArrayList<>();
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (tm == null) {
            return registered;
        }
        List<CellInfo> cells;
        try {
            cells = tm.getAllCellInfo();
        } catch (SecurityException e) {
            TrackLog.w(TAG, "cell info not permitted", e);
            return registered;
        }
        if (cells == null) {
            return registered;
        }
        for (CellInfo info : cells) {
            GeoResolveModels.CellTower tower = toTower(info);
            if (tower == null) {
                continue;
            }
            if (info.isRegistered()) {
                registered.add(tower);
            } else if (!registeredOnly) {
                neighbours.add(tower);
            }
        }
        registered.addAll(neighbours);
        return registered.size() > MAX_CELLS ? new ArrayList<>(registered.subList(0, MAX_CELLS)) : registered;
    }

    /** @return the tower, or null when any ID part is unavailable (typical for neighbour cells). */
    @Nullable
    @SuppressWarnings("deprecation")
    private static GeoResolveModels.CellTower toTower(CellInfo info) {
        String radio;
        String mccStr = null;
        String mncStr = null;
        int mcc = UNAVAILABLE;
        int mnc = UNAVAILABLE;
        int area;
        long cellId;
        int dbm;
        if (info instanceof CellInfoGsm) {
            CellIdentityGsm id = ((CellInfoGsm) info).getCellIdentity();
            radio = "gsm";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mccStr = id.getMccString();
                mncStr = id.getMncString();
            } else {
                mcc = id.getMcc();
                mnc = id.getMnc();
            }
            area = id.getLac();
            cellId = id.getCid();
            dbm = ((CellInfoGsm) info).getCellSignalStrength().getDbm();
        } else if (info instanceof CellInfoWcdma) {
            CellIdentityWcdma id = ((CellInfoWcdma) info).getCellIdentity();
            radio = "wcdma";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mccStr = id.getMccString();
                mncStr = id.getMncString();
            } else {
                mcc = id.getMcc();
                mnc = id.getMnc();
            }
            area = id.getLac();
            cellId = id.getCid();
            dbm = ((CellInfoWcdma) info).getCellSignalStrength().getDbm();
        } else if (info instanceof CellInfoLte) {
            CellIdentityLte id = ((CellInfoLte) info).getCellIdentity();
            radio = "lte";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                mccStr = id.getMccString();
                mncStr = id.getMncString();
            } else {
                mcc = id.getMcc();
                mnc = id.getMnc();
            }
            area = id.getTac();
            cellId = id.getCi();
            dbm = ((CellInfoLte) info).getCellSignalStrength().getDbm();
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q && info instanceof CellInfoNr) {
            CellIdentityNr id = (CellIdentityNr) ((CellInfoNr) info).getCellIdentity();
            radio = "nr";
            mccStr = id.getMccString();
            mncStr = id.getMncString();
            area = id.getTac();
            cellId = id.getNci();
            dbm = ((CellInfoNr) info).getCellSignalStrength().getDbm();
        } else {
            return null; // CDMA / TD-SCDMA: not used in Bangladesh, Google needs a different ID shape
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            mcc = parse(mccStr);
            mnc = parse(mncStr);
        }
        if (mcc == UNAVAILABLE || mnc == UNAVAILABLE || area == UNAVAILABLE
                || cellId == UNAVAILABLE || cellId == UNAVAILABLE_LONG || cellId < 0) {
            return null;
        }
        GeoResolveModels.CellTower tower = new GeoResolveModels.CellTower();
        tower.radioType = radio;
        tower.mobileCountryCode = mcc;
        tower.mobileNetworkCode = mnc;
        tower.locationAreaCode = area;
        tower.cellId = cellId;
        tower.signalStrength = dbm < 0 && dbm > -200 ? dbm : null;
        return tower;
    }

    private static int parse(@Nullable String value) {
        if (value == null || value.isEmpty()) {
            return UNAVAILABLE;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return UNAVAILABLE;
        }
    }

    private static List<GeoResolveModels.WifiAccessPoint> scanWifi(Context context) {
        List<GeoResolveModels.WifiAccessPoint> points = new ArrayList<>();
        WifiManager wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        if (wifi == null) {
            return points;
        }
        List<ScanResult> results;
        try {
            // Cached results only: startScan() is throttled to 4 calls / 2 min on Android 9+.
            results = wifi.getScanResults();
        } catch (SecurityException e) {
            TrackLog.w(TAG, "wifi scan results not permitted", e);
            return points;
        }
        if (results == null || results.isEmpty()) {
            return points;
        }
        List<ScanResult> sorted = new ArrayList<>(results);
        Collections.sort(sorted, (a, b) -> Integer.compare(b.level, a.level));
        for (ScanResult result : sorted) {
            if (points.size() >= MAX_WIFI) {
                break;
            }
            // "_nomap" is the owner's opt-out from location databases.
            if (result.SSID != null && result.SSID.endsWith("_nomap")) {
                continue;
            }
            // Cached results can be from another place; a stale list gives a confident wrong fix.
            if (SystemClock.elapsedRealtime() - result.timestamp / 1000 > MAX_WIFI_AGE_MS) {
                continue;
            }
            String mac = result.BSSID == null ? "" : result.BSSID.toLowerCase(Locale.US);
            // Same pattern the server validates; one bad MAC would reject the whole request.
            if (!mac.matches("([0-9a-f]{2}:){5}[0-9a-f]{2}") || "00:00:00:00:00:00".equals(mac) || "02:00:00:00:00:00".equals(mac)) {
                continue;
            }
            GeoResolveModels.WifiAccessPoint point = new GeoResolveModels.WifiAccessPoint();
            point.macAddress = mac;
            point.signalStrength = result.level < 0 ? result.level : null;
            points.add(point);
        }
        return points;
    }
}
