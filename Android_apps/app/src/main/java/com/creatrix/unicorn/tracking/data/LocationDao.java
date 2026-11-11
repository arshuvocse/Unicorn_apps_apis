package com.creatrix.unicorn.tracking.data;

import androidx.annotation.Nullable;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

/**
 * Status literals used below: 0 PENDING, 1 UPLOADING, 2 SYNCED, 3 FAILED, 4 REJECTED
 * (see {@link SyncStatus}).
 */
@Dao
public interface LocationDao {

    /** @return the new row id, or -1 if a record with the same recordId already exists. */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    long insert(TrackedLocation location);

    /** Oldest first, so the server sees the track in the order it happened. */
    @Query("SELECT * FROM tracked_locations WHERE status IN (0, 3) AND nextRetryAt <= :now "
            + "ORDER BY timestamp ASC, id ASC LIMIT :limit")
    List<TrackedLocation> getUploadable(long now, int limit);

    @Query("UPDATE tracked_locations SET status = 1, clientSyncAt = :now WHERE id IN (:ids)")
    void markUploading(List<Long> ids, long now);

    @Query("UPDATE tracked_locations SET status = 2, serverSyncedAt = :serverSyncedAt, lastError = NULL "
            + "WHERE recordId = :recordId")
    void markSynced(String recordId, long serverSyncedAt);

    @Query("UPDATE tracked_locations SET status = 4, lastError = :reason WHERE recordId = :recordId")
    void markRejected(String recordId, String reason);

    @Query("UPDATE tracked_locations SET status = 3, attemptCount = :attempts, nextRetryAt = :nextRetryAt, "
            + "lastError = :error WHERE id = :id")
    void markFailed(long id, int attempts, long nextRetryAt, String error);

    /** Puts records back in the queue without counting an attempt (used when the network was unreachable). */
    @Query("UPDATE tracked_locations SET status = 0 WHERE id IN (:ids) AND status = 1")
    void releaseUploading(List<Long> ids);

    /** Recovers records left in UPLOADING by a process that died mid-upload. */
    @Query("UPDATE tracked_locations SET status = 0 WHERE status = 1 AND clientSyncAt < :before")
    int resetStuckUploading(long before);

    /** Called when connectivity returns: failed records need not wait out their backoff any more. */
    @Query("UPDATE tracked_locations SET nextRetryAt = 0 WHERE status = 3")
    int makeFailedRetryableNow();

    @Query("DELETE FROM tracked_locations WHERE status IN (2, 4) AND createdAt < :cutoff")
    int purgeFinishedBefore(long cutoff);

    @Query("SELECT COUNT(*) FROM tracked_locations WHERE status IN (0, 1, 3)")
    int countUnsynced();

    @Query("SELECT COUNT(*) FROM tracked_locations WHERE status = :status")
    int countByStatus(int status);

    @Query("SELECT COUNT(*) FROM tracked_locations WHERE status = 3")
    int countFailed();

    @Nullable
    @Query("SELECT * FROM tracked_locations ORDER BY timestamp DESC, id DESC LIMIT 1")
    TrackedLocation getLatest();

    @Query("SELECT * FROM tracked_locations ORDER BY timestamp DESC, id DESC LIMIT :limit")
    List<TrackedLocation> getRecent(int limit);
}
