package com.creatrix.unicorn.tracking.api;

import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

/** Wire format of POST api/Tracking/ResolveLocation (cell towers / WiFi -> lat/long on the server). */
public final class GeoResolveModels {

    private GeoResolveModels() {
    }

    public static final class CellTower {
        /** gsm | wcdma | lte | nr */
        @SerializedName("radioType")
        public String radioType;
        @SerializedName("mobileCountryCode")
        public int mobileCountryCode;
        @SerializedName("mobileNetworkCode")
        public int mobileNetworkCode;
        @SerializedName("locationAreaCode")
        public int locationAreaCode;
        @SerializedName("cellId")
        public long cellId;
        @SerializedName("signalStrength")
        public Integer signalStrength;
    }

    public static final class WifiAccessPoint {
        @SerializedName("macAddress")
        public String macAddress;
        @SerializedName("signalStrength")
        public Integer signalStrength;
    }

    public static final class ResolveRequest {
        @SerializedName("employeeId")
        public String employeeId;
        @SerializedName("cellTowers")
        public List<CellTower> cellTowers = new ArrayList<>();
        @SerializedName("wifiAccessPoints")
        public List<WifiAccessPoint> wifiAccessPoints = new ArrayList<>();
    }

    /** Wire format of POST api/Tracking/LearnCell: one tower paired with a GPS fix we already have. */
    public static final class LearnRequest {
        @SerializedName("employeeId")
        public String employeeId;
        @SerializedName("radioType")
        public String radioType;
        @SerializedName("mobileCountryCode")
        public int mobileCountryCode;
        @SerializedName("mobileNetworkCode")
        public int mobileNetworkCode;
        @SerializedName("locationAreaCode")
        public int locationAreaCode;
        @SerializedName("cellId")
        public long cellId;
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
        /** Metres, from the GPS fix. */
        @SerializedName("accuracy")
        public double accuracy;
    }

    public static final class ResolveResponse {
        @SerializedName("success")
        public boolean success;
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
        @SerializedName("accuracy")
        public double accuracy;
        /** wifi | cell */
        @SerializedName("source")
        public String source;
        @SerializedName("message")
        public String message;
    }
}
