package com.creatrix.unicorn.tracking.api;

import androidx.annotation.Nullable;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Wire format of the batch upload. Timestamps are epoch milliseconds (UTC).
 *
 * <p>Contract the backend must implement:
 * <ul>
 *   <li>{@code recordId} is an idempotency key. If it was stored before, do not insert again; still
 *       list it in {@code acknowledged} so the device can stop retrying it.</li>
 *   <li>Every accepted (or already-stored) record must appear in {@code acknowledged} with the
 *       server's own receive time in {@code syncedAt}.</li>
 *   <li>A record that is invalid and will never be accepted goes in {@code rejected}.</li>
 *   <li>Anything in neither list is treated as "not stored" and retried.</li>
 * </ul>
 */
public final class UploadModels {

    private UploadModels() {
    }

    public static final class UploadRequest {
        /** Random id for this HTTP request, for server-side logging only. */
        @SerializedName("batchId")
        public String batchId;
        @SerializedName("deviceId")
        public String deviceId;
        @SerializedName("locations")
        public List<LocationPayload> locations;
    }

    public static final class LocationPayload {
        @SerializedName("recordId")
        public String recordId;
        @SerializedName("employeeId")
        public String employeeId;
        @SerializedName("deviceId")
        public String deviceId;
        @SerializedName("latitude")
        public double latitude;
        @SerializedName("longitude")
        public double longitude;
        /** Metres; null when the device did not report an accuracy. */
        @SerializedName("accuracy")
        @Nullable
        public Float accuracy;
        /** When the fix was taken. */
        @SerializedName("timestamp")
        public long timestamp;
        @SerializedName("provider")
        public String provider;
        /** Reverse-geocoded place name; null if unavailable. */
        @SerializedName("address")
        @Nullable
        public String address;
        /** Device clock at upload time. The server should store its own receive time as the authoritative value. */
        @SerializedName("syncTimestamp")
        public long syncTimestamp;
        @SerializedName("mockLocation")
        public boolean mockLocation;
    }

    public static final class UploadResponse {
        @SerializedName("acknowledged")
        @Nullable
        public List<Ack> acknowledged;
        @SerializedName("rejected")
        @Nullable
        public List<Rejection> rejected;
    }

    public static final class Ack {
        @SerializedName("recordId")
        public String recordId;
        /** Server receive time, epoch millis. Optional; the device falls back to its own clock. */
        @SerializedName("syncedAt")
        @Nullable
        public Long syncedAt;
    }

    public static final class Rejection {
        @SerializedName("recordId")
        public String recordId;
        @SerializedName("reason")
        @Nullable
        public String reason;
    }
}
