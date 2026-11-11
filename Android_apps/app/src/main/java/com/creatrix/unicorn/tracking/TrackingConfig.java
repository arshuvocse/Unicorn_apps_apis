package com.creatrix.unicorn.tracking;

import androidx.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Locale;

/**
 * Immutable tracking settings. Build with {@link Builder}; out-of-range numbers are clamped (a bad
 * value from a policy/MDM must never crash tracking) while a non-HTTPS upload URL is rejected.
 */
public final class TrackingConfig {

    public enum Accuracy {
        /** GPS-grade fixes. Best for field staff; uses more battery. */
        HIGH,
        /** Wi-Fi/cell-grade fixes. Cheaper, coarser. */
        BALANCED
    }

    public static final long MIN_INTERVAL_MS = 5_000L;
    public static final long MAX_INTERVAL_MS = 24L * 60 * 60 * 1000;
    public static final long DEFAULT_INTERVAL_MS = 5 * 60_000L;
    public static final String DEFAULT_UPLOAD_PATH = "api/v1/tracking/locations/batch";

    private static final Gson GSON = new Gson();

    public final long intervalMs;
    public final long fastestIntervalMs;
    public final float minDisplacementMeters;
    /** Fixes less accurate than this are discarded. 0 disables the filter. */
    public final float maxAccuracyMeters;
    public final Accuracy accuracy;
    /** Absolute HTTPS base URL ending in "/". Empty = not configured (points are queued locally). */
    public final String baseUrl;
    public final String uploadPath;
    public final int batchSize;
    public final boolean geocodeEnabled;
    public final boolean requireBackgroundLocation;
    /** When false the notification has no Stop action and {@link TrackingManager#stopByUser} is refused. */
    public final boolean userCanStop;
    /** When this app is the device owner: keep location on and block force-stop / clear-data. */
    public final boolean enforceDevicePolicy;
    /** Honoured only in debuggable builds. */
    public final boolean allowCleartext;
    /** Optional OkHttp pins for the upload host, e.g. "sha256/AAAA...=". */
    public final String[] certificatePins;

    private TrackingConfig(Builder b) {
        this.intervalMs = b.intervalMs;
        this.fastestIntervalMs = b.fastestIntervalMs;
        this.minDisplacementMeters = b.minDisplacementMeters;
        this.maxAccuracyMeters = b.maxAccuracyMeters;
        this.accuracy = b.accuracy;
        this.baseUrl = b.baseUrl;
        this.uploadPath = b.uploadPath;
        this.batchSize = b.batchSize;
        this.geocodeEnabled = b.geocodeEnabled;
        this.requireBackgroundLocation = b.requireBackgroundLocation;
        this.userCanStop = b.userCanStop;
        this.enforceDevicePolicy = b.enforceDevicePolicy;
        this.allowCleartext = b.allowCleartext;
        this.certificatePins = b.certificatePins;
    }

    public static TrackingConfig defaults() {
        return new Builder().build();
    }

    public Builder toBuilder() {
        Builder b = new Builder();
        b.intervalMs = intervalMs;
        b.fastestIntervalMs = fastestIntervalMs;
        b.minDisplacementMeters = minDisplacementMeters;
        b.maxAccuracyMeters = maxAccuracyMeters;
        b.accuracy = accuracy;
        b.baseUrl = baseUrl;
        b.uploadPath = uploadPath;
        b.batchSize = batchSize;
        b.geocodeEnabled = geocodeEnabled;
        b.requireBackgroundLocation = requireBackgroundLocation;
        b.userCanStop = userCanStop;
        b.enforceDevicePolicy = enforceDevicePolicy;
        b.allowCleartext = allowCleartext;
        b.certificatePins = certificatePins;
        return b;
    }

    public boolean isUploadConfigured() {
        return !baseUrl.isEmpty();
    }

    public String toJson() {
        return GSON.toJson(this);
    }

    /** @return the parsed config, or null if the JSON is corrupt or no longer valid. */
    @Nullable
    public static TrackingConfig fromJson(@Nullable String json) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        try {
            TrackingConfig parsed = GSON.fromJson(json, TrackingConfig.class);
            // Re-run validation/defaulting: Gson bypasses the constructor and may leave fields unset.
            return parsed == null ? null : parsed.toBuilder().build();
        } catch (RuntimeException e) {
            return null;
        }
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof TrackingConfig && toJson().equals(((TrackingConfig) other).toJson());
    }

    @Override
    public int hashCode() {
        return toJson().hashCode();
    }

    public static final class Builder {
        private long intervalMs = DEFAULT_INTERVAL_MS;
        private long fastestIntervalMs = DEFAULT_INTERVAL_MS / 2;
        private float minDisplacementMeters = 0f;
        private float maxAccuracyMeters = 200f;
        private Accuracy accuracy = Accuracy.HIGH;
        private String baseUrl = "";
        private String uploadPath = DEFAULT_UPLOAD_PATH;
        private int batchSize = 50;
        private boolean geocodeEnabled = true;
        private boolean requireBackgroundLocation = true;
        private boolean userCanStop = true;
        private boolean enforceDevicePolicy = false;
        private boolean allowCleartext = false;
        private String[] certificatePins = new String[0];

        public Builder intervalMs(long value) {
            intervalMs = value;
            return this;
        }

        public Builder fastestIntervalMs(long value) {
            fastestIntervalMs = value;
            return this;
        }

        public Builder minDisplacementMeters(float value) {
            minDisplacementMeters = value;
            return this;
        }

        public Builder maxAccuracyMeters(float value) {
            maxAccuracyMeters = value;
            return this;
        }

        public Builder accuracy(Accuracy value) {
            accuracy = value;
            return this;
        }

        public Builder baseUrl(String value) {
            baseUrl = value;
            return this;
        }

        public Builder uploadPath(String value) {
            uploadPath = value;
            return this;
        }

        public Builder batchSize(int value) {
            batchSize = value;
            return this;
        }

        public Builder geocodeEnabled(boolean value) {
            geocodeEnabled = value;
            return this;
        }

        public Builder requireBackgroundLocation(boolean value) {
            requireBackgroundLocation = value;
            return this;
        }

        public Builder userCanStop(boolean value) {
            userCanStop = value;
            return this;
        }

        public Builder enforceDevicePolicy(boolean value) {
            enforceDevicePolicy = value;
            return this;
        }

        public Builder allowCleartext(boolean value) {
            allowCleartext = value;
            return this;
        }

        public Builder certificatePins(String... value) {
            certificatePins = value == null ? new String[0] : value;
            return this;
        }

        /** @throws IllegalArgumentException if baseUrl is set but is not HTTPS (or HTTP with allowCleartext). */
        public TrackingConfig build() {
            intervalMs = clamp(intervalMs, MIN_INTERVAL_MS, MAX_INTERVAL_MS);
            fastestIntervalMs = clamp(fastestIntervalMs, 1_000L, intervalMs);
            minDisplacementMeters = clamp(minDisplacementMeters, 0f, 10_000f);
            maxAccuracyMeters = clamp(maxAccuracyMeters, 0f, 10_000f);
            batchSize = (int) clamp(batchSize, 1, 500);
            if (accuracy == null) {
                accuracy = Accuracy.HIGH;
            }
            if (uploadPath == null || uploadPath.trim().isEmpty()) {
                uploadPath = DEFAULT_UPLOAD_PATH;
            }
            if (certificatePins == null) {
                certificatePins = new String[0];
            }
            baseUrl = normalizeBaseUrl(baseUrl, allowCleartext);
            return new TrackingConfig(this);
        }

        private static String normalizeBaseUrl(@Nullable String url, boolean allowCleartext) {
            if (url == null || url.trim().isEmpty()) {
                return "";
            }
            String value = url.trim();
            String lower = value.toLowerCase(Locale.ROOT);
            boolean https = lower.startsWith("https://");
            boolean http = lower.startsWith("http://");
            if (!https && !(http && allowCleartext)) {
                throw new IllegalArgumentException("Tracking upload URL must use HTTPS");
            }
            return value.endsWith("/") ? value : value + "/";
        }

        private static long clamp(long value, long min, long max) {
            return Math.max(min, Math.min(max, value));
        }

        private static float clamp(float value, float min, float max) {
            return Math.max(min, Math.min(max, value));
        }
    }
}
