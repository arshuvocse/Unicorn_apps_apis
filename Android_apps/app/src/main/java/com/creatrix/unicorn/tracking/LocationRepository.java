package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.location.Location;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.creatrix.unicorn.tracking.api.ApiClient;
import com.creatrix.unicorn.tracking.api.TrackingApiService;
import com.creatrix.unicorn.tracking.api.UploadModels;
import com.creatrix.unicorn.tracking.data.LocationDao;
import com.creatrix.unicorn.tracking.data.SyncStatus;
import com.creatrix.unicorn.tracking.data.TrackedLocation;
import com.creatrix.unicorn.tracking.data.TrackingDatabase;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.locks.ReentrantLock;

import retrofit2.Response;

/**
 * Single owner of the offline queue and of server synchronisation.
 *
 * <p>Capture path: {@link #save} inserts the location into Room first. Nothing about the network is
 * involved, so a location is never lost because the device is offline.
 *
 * <p>Upload path: {@link #syncPending} sends the oldest uploadable records in batches, marks what
 * the server acknowledged, and backs off exponentially per record for retryable failures. It is safe
 * to call from anywhere (service, WorkManager, network callback): only one sync runs at a time and
 * the server de-duplicates by recordId, so an overlap can at worst repeat a request.
 */
public final class LocationRepository {

    /** Outcome of one {@link #syncPending()} run. */
    public enum SyncResult {
        /** Queue drained (or nothing to do). */
        DONE,
        /** Something is still queued and should be retried with backoff (offline, 5xx, timeouts...). */
        RETRY,
        /** No upload URL configured; records stay queued until a config arrives. */
        NOT_CONFIGURED,
        /** No valid credentials; records stay queued until the next sign-in. */
        NOT_AUTHENTICATED
    }

    private static final String TAG = "LocationRepository";
    private static final long BACKOFF_BASE_MS = 30_000L;
    private static final long BACKOFF_MAX_MS = 30 * 60_000L;
    /** An UPLOADING record older than this belongs to an upload that died. */
    private static final long STUCK_UPLOAD_MS = 5 * 60_000L;
    /** Acknowledged / rejected rows are kept this long for troubleshooting, then purged. */
    private static final long FINISHED_RETENTION_MS = 7L * 24 * 60 * 60 * 1000;
    private static final int ERROR_MAX_CHARS = 200;

    private static volatile LocationRepository instance;

    private final Context app;
    private final LocationDao dao;
    private final TrackingDatabase database;
    private final ReentrantLock syncLock = new ReentrantLock();
    private final Random random = new Random();

    private LocationRepository(Context context) {
        this.app = context.getApplicationContext();
        this.database = TrackingDatabase.get(app);
        this.dao = database.locationDao();
    }

    public static LocationRepository get(Context context) {
        if (instance == null) {
            synchronized (LocationRepository.class) {
                if (instance == null) {
                    instance = new LocationRepository(context);
                }
            }
        }
        return instance;
    }

    // ------------------------------------------------------------------ capture

    /**
     * Stores a location in the offline queue. Blocking disk I/O: call from a background thread.
     *
     * @return the row id, or -1 if it was not stored (no signed-in employee, or duplicate)
     */
    @WorkerThread
    public long save(@NonNull Location location, @Nullable String address) {
        String employeeId = TrackingPrefs.get(app).getUserId();
        if (employeeId == null || employeeId.isEmpty()) {
            TrackLog.w(TAG, "No signed-in employee; location not stored");
            return -1;
        }
        TrackedLocation row = new TrackedLocation();
        row.recordId = UUID.randomUUID().toString();
        row.employeeId = employeeId;
        row.deviceId = DeviceIdentity.get(app);
        row.latitude = location.getLatitude();
        row.longitude = location.getLongitude();
        row.accuracy = location.hasAccuracy() ? location.getAccuracy() : -1f;
        row.timestamp = location.getTime() > 0 ? location.getTime() : System.currentTimeMillis();
        row.provider = location.getProvider() == null ? "unknown" : location.getProvider();
        row.address = address;
        row.mock = LocationHelper.isMock(location);
        row.createdAt = System.currentTimeMillis();
        try {
            return dao.insert(row);
        } catch (RuntimeException e) {
            // Disk full / DB corruption. Surface it loudly: this is the one place data can really be lost.
            TrackLog.e(TAG, "Unable to store location", e);
            return -1;
        }
    }

    // ------------------------------------------------------------------ status

    @WorkerThread
    public int getUnsyncedCount() {
        return dao.countUnsynced();
    }

    @WorkerThread
    @Nullable
    public TrackedLocation getLatest() {
        return dao.getLatest();
    }

    @WorkerThread
    public List<TrackedLocation> getRecent(int limit) {
        return dao.getRecent(limit);
    }

    /** Failed records need not wait out their backoff once the network is back. */
    @WorkerThread
    public void onNetworkRestored() {
        dao.makeFailedRetryableNow();
    }

    // ------------------------------------------------------------------ synchronisation

    /**
     * Uploads everything that is due. Blocking; never call on the main thread.
     */
    @WorkerThread
    public SyncResult syncPending() {
        if (!syncLock.tryLock()) {
            return SyncResult.DONE; // the running sync loops until the queue is empty
        }
        try {
            TrackingPrefs prefs = TrackingPrefs.get(app);
            TrackingConfig config = prefs.getConfig();
            if (!config.isUploadConfigured()) {
                return SyncResult.NOT_CONFIGURED;
            }
            TokenProvider tokens = TrackingManager.get(app).getTokenProvider();
            String token = tokens.getAccessToken();
            if (token == null || token.isEmpty()) {
                return SyncResult.NOT_AUTHENTICATED;
            }

            dao.resetStuckUploading(System.currentTimeMillis() - STUCK_UPLOAD_MS);
            TrackingApiService api = ApiClient.get(config, tokens);

            while (true) {
                List<TrackedLocation> batch = dao.getUploadable(System.currentTimeMillis(), config.batchSize);
                if (batch.isEmpty()) {
                    break;
                }
                Step step = upload(api, config, batch);
                if (step == Step.STOP_RETRY) {
                    return SyncResult.RETRY;
                }
                if (step == Step.STOP_AUTH) {
                    return SyncResult.NOT_AUTHENTICATED;
                }
            }

            dao.purgeFinishedBefore(System.currentTimeMillis() - FINISHED_RETENTION_MS);
            // Records that failed with a retryable error are waiting out their backoff.
            return dao.countFailed() > 0 ? SyncResult.RETRY : SyncResult.DONE;
        } catch (RuntimeException e) {
            TrackLog.e(TAG, "Sync failed unexpectedly", e);
            return SyncResult.RETRY;
        } finally {
            syncLock.unlock();
        }
    }

    private enum Step { CONTINUE, STOP_RETRY, STOP_AUTH }

    private Step upload(TrackingApiService api, TrackingConfig config, List<TrackedLocation> batch) {
        List<Long> ids = new ArrayList<>(batch.size());
        for (TrackedLocation row : batch) {
            ids.add(row.id);
        }
        long now = System.currentTimeMillis();
        dao.markUploading(ids, now);

        Response<UploadModels.UploadResponse> response;
        try {
            response = api.uploadLocations(config.uploadPath, toRequest(batch, now)).execute();
        } catch (IOException e) {
            // Offline / DNS / TLS / timeout: not the records' fault, so do not count an attempt.
            dao.releaseUploading(ids);
            TrackLog.d(TAG, "Upload not possible: " + e.getClass().getSimpleName());
            return Step.STOP_RETRY;
        }

        int code = response.code();
        if (response.isSuccessful() && response.body() != null) {
            applyAcknowledgement(batch, response.body(), now);
            return Step.CONTINUE;
        }
        String reason = describeFailure(response);

        if (code == 401 || code == 403) {
            dao.releaseUploading(ids);
            TrackLog.w(TAG, "Upload refused (" + code + "); waiting for a new sign-in");
            return Step.STOP_AUTH;
        }
        if (code == 408 || code == 429 || code >= 500 || response.isSuccessful()) {
            // Transient server-side trouble (or a 2xx without a usable body): back off and retry.
            markFailed(batch, reason);
            return Step.STOP_RETRY;
        }

        // Any other 4xx means the server rejected this request. Split the batch to find the record
        // that is responsible, so one bad record cannot block the good ones behind it.
        if (batch.size() > 1) {
            dao.releaseUploading(ids);
            int mid = batch.size() / 2;
            Step first = upload(api, config, new ArrayList<>(batch.subList(0, mid)));
            if (first != Step.CONTINUE) {
                return first;
            }
            return upload(api, config, new ArrayList<>(batch.subList(mid, batch.size())));
        }
        dao.markRejected(batch.get(0).recordId, reason);
        TrackLog.w(TAG, "Record permanently rejected: " + reason);
        return Step.CONTINUE;
    }

    private UploadModels.UploadRequest toRequest(List<TrackedLocation> batch, long now) {
        UploadModels.UploadRequest request = new UploadModels.UploadRequest();
        request.batchId = UUID.randomUUID().toString();
        request.deviceId = DeviceIdentity.get(app);
        request.locations = new ArrayList<>(batch.size());
        for (TrackedLocation row : batch) {
            UploadModels.LocationPayload p = new UploadModels.LocationPayload();
            p.recordId = row.recordId;
            p.employeeId = row.employeeId;
            p.deviceId = row.deviceId;
            p.latitude = row.latitude;
            p.longitude = row.longitude;
            p.accuracy = row.accuracy < 0 ? null : row.accuracy;
            p.timestamp = row.timestamp;
            p.provider = row.provider;
            p.address = row.address;
            p.syncTimestamp = now;
            p.mockLocation = row.mock;
            request.locations.add(p);
        }
        return request;
    }

    private void applyAcknowledgement(final List<TrackedLocation> batch, final UploadModels.UploadResponse body,
                                      final long now) {
        database.runInTransaction(() -> {
            Set<String> handled = new HashSet<>();
            if (body.acknowledged != null) {
                for (UploadModels.Ack ack : body.acknowledged) {
                    if (ack == null || ack.recordId == null) {
                        continue;
                    }
                    dao.markSynced(ack.recordId, ack.syncedAt != null ? ack.syncedAt : now);
                    handled.add(ack.recordId);
                }
            }
            if (body.rejected != null) {
                for (UploadModels.Rejection rejection : body.rejected) {
                    if (rejection == null || rejection.recordId == null) {
                        continue;
                    }
                    dao.markRejected(rejection.recordId, truncate(rejection.reason == null ? "rejected" : rejection.reason));
                    handled.add(rejection.recordId);
                }
            }
            // Sent, but the server said nothing about it: it is not stored, so retry it.
            List<TrackedLocation> unacknowledged = new ArrayList<>();
            for (TrackedLocation row : batch) {
                if (!handled.contains(row.recordId)) {
                    unacknowledged.add(row);
                }
            }
            markFailed(unacknowledged, "not acknowledged by server");
        });
    }

    private void markFailed(final List<TrackedLocation> rows, final String reason) {
        final long now = System.currentTimeMillis();
        final String error = truncate(reason);
        database.runInTransaction(() -> {
            for (TrackedLocation row : rows) {
                int attempts = row.attemptCount + 1;
                dao.markFailed(row.id, attempts, now + backoffMs(attempts), error);
            }
        });
    }

    /** 30 s, 60 s, 2 min, ... capped at 30 min, with +/-20% jitter so a fleet does not retry in lockstep. */
    long backoffMs(int attempts) {
        long delay = BACKOFF_BASE_MS;
        for (int i = 1; i < attempts && delay < BACKOFF_MAX_MS; i++) {
            delay *= 2;
        }
        delay = Math.min(delay, BACKOFF_MAX_MS);
        double jitter = 0.8 + random.nextDouble() * 0.4;
        return (long) (delay * jitter);
    }

    private static String describeFailure(Response<?> response) {
        String body = "";
        try {
            if (response.errorBody() != null) {
                body = response.errorBody().string();
            }
        } catch (IOException | RuntimeException ignored) {
            // Body is only used for a diagnostic message.
        }
        return truncate("HTTP " + response.code() + (body.isEmpty() ? "" : ": " + body));
    }

    private static String truncate(String text) {
        return text.length() <= ERROR_MAX_CHARS ? text : text.substring(0, ERROR_MAX_CHARS);
    }
}
