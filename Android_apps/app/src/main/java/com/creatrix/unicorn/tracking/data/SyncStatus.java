package com.creatrix.unicorn.tracking.data;

/**
 * Upload state of one {@link TrackedLocation}. Stored as an int, so never renumber; the literal
 * values are also used inside the {@link LocationDao} queries.
 */
public final class SyncStatus {
    /** Captured, not uploaded yet. */
    public static final int PENDING = 0;
    /** Part of an upload that is in flight (reset to PENDING if the process dies mid-upload). */
    public static final int UPLOADING = 1;
    /** The server acknowledged the record. */
    public static final int SYNCED = 2;
    /** An upload failed with a retryable error; will be retried after {@code nextRetryAt}. */
    public static final int FAILED = 3;
    /** The server permanently refused this record (validation error). Kept for audit, never retried. */
    public static final int REJECTED = 4;

    private SyncStatus() {
    }
}
