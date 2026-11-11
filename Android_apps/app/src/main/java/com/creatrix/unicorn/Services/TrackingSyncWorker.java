package com.creatrix.unicorn.Services;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.creatrix.unicorn.DBInfo.TrackingQueueDbHelper;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Network.GetDataService;
import com.creatrix.unicorn.Network.RetrofitClientInstance;

import java.util.List;

import retrofit2.Response;

public class TrackingSyncWorker extends Worker {
    private static final String UNIQUE_WORK_NAME = "tracking_queue_sync";
    private static final int BATCH_SIZE = 100;
    // A point the server keeps rejecting must not block the ones behind it forever, nor pile up.
    private static final long DROP_REJECTED_AFTER_MS = 24L * 60 * 60 * 1000;

    public TrackingSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void schedule(Context context) {
        Constraints constraints = new Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build();
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(TrackingSyncWorker.class)
                .setConstraints(constraints)
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        return flushQueue(getApplicationContext(), this::isStopped) ? Result.success() : Result.retry();
    }

    /**
     * Uploads every queued point now. Also called straight from the 5-minute tick so the point
     * reaches the server immediately instead of waiting for WorkManager.
     *
     * @return true when the queue is empty, false when something is left for a later retry
     */
    public static synchronized boolean flushQueue(Context context, java.util.function.BooleanSupplier isStopped) {
        TrackingQueueDbHelper database = new TrackingQueueDbHelper(context);
        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);

        try {
            boolean anyFailed = false;
            long lastSeenId = 0;
            while (!isStopped.getAsBoolean()) {
                List<TrackingQueueDbHelper.PendingTracking> pending = database.getPending(lastSeenId, BATCH_SIZE);
                if (pending.isEmpty()) {
                    return !anyFailed;
                }

                for (TrackingQueueDbHelper.PendingTracking item : pending) {
                    lastSeenId = item.getId();
                    boolean uploaded;
                    try {
                        Response<ResultInfo> response = service.SaveTracking(item.getTracking()).execute();
                        ResultInfo body = response.body();
                        uploaded = response.isSuccessful() && body != null && Boolean.TRUE.equals(body.getSuccess());
                    } catch (java.io.IOException networkError) {
                        // No connection/timeouts: stop and retry everything later, keep the order.
                        return false;
                    }

                    if (uploaded) {
                        database.delete(item.getId());
                    } else {
                        anyFailed = true;
                        if (System.currentTimeMillis() - item.getCreatedAt() > DROP_REJECTED_AFTER_MS) {
                            database.delete(item.getId());
                        }
                    }
                }
            }
            return false;
        } catch (Exception exception) {
            Log.e("TrackingSyncWorker", "Tracking sync failed", exception);
            return false;
        } finally {
            database.close();
        }
    }
}
