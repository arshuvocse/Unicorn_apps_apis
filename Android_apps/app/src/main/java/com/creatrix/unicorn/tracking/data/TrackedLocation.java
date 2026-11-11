package com.creatrix.unicorn.tracking.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * One captured location plus its upload bookkeeping. This row is the offline queue: a location is
 * inserted before any network call is made and is only removed after it has been acknowledged and
 * has aged out.
 */
@Entity(
        tableName = "tracked_locations",
        indices = {
                // Client-generated UUID, also sent to the server as the idempotency key.
                @Index(value = "recordId", unique = true),
                @Index(value = {"status", "nextRetryAt"}),
                @Index(value = "createdAt")
        })
public class TrackedLocation {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String recordId = "";
    @NonNull
    public String employeeId = "";
    @NonNull
    public String deviceId = "";

    public double latitude;
    public double longitude;
    /** Horizontal accuracy radius in metres, or -1 if the provider did not report one. */
    public float accuracy = -1f;
    /** When the fix was taken, epoch millis (UTC). */
    public long timestamp;
    /** "fused", "gps", "network", ... as reported by Android. */
    @NonNull
    public String provider = "";
    /** Reverse-geocoded, human readable place; null when it could not be resolved. */
    @Nullable
    public String address;
    /** True if Android flagged the fix as coming from a mock location provider. */
    public boolean mock;

    /** One of {@link SyncStatus}. */
    public int status = SyncStatus.PENDING;
    /** Number of failed (retryable) upload attempts so far; drives the exponential backoff. */
    public int attemptCount;
    /** Earliest time (epoch millis) this record may be uploaded again. 0 = immediately. */
    public long nextRetryAt;
    /** Time of the last upload attempt on the device, epoch millis. Sent to the server as syncTimestamp. */
    public long clientSyncAt;
    /** Time the server says it stored the record, epoch millis. Null until acknowledged. */
    @Nullable
    public Long serverSyncedAt;
    @Nullable
    public String lastError;
    /** When the row was inserted, epoch millis. */
    public long createdAt;
}
