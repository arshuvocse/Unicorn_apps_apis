package com.creatrix.unicorn.tracking;

import android.content.Context;

import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

/** All WorkManager scheduling for the tracking module. Work is persisted, so it survives reboots. */
public final class SyncScheduler {

    private static final String UNIQUE_SYNC = "tracking_sync";
    private static final String UNIQUE_HEALTH = "tracking_health";

    private SyncScheduler() {
    }

    /**
     * Uploads the queue as soon as a validated network exists. WorkManager retries with exponential
     * backoff (30 s, 60 s, ...) whenever the worker returns retry.
     *
     * @param replace true to cancel a request that is waiting out a long backoff and start fresh, which
     *                is right when connectivity has just returned; false to leave a pending one alone
     */
    public static void enqueue(Context context, boolean replace) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(LocationSyncWorker.class)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context.getApplicationContext()).enqueueUniqueWork(
                UNIQUE_SYNC, replace ? ExistingWorkPolicy.REPLACE : ExistingWorkPolicy.KEEP, request);
    }

    /** Every 15 minutes (WorkManager's minimum): make sure the service is alive and the queue is draining. */
    public static void schedulePeriodicHealthCheck(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(TrackingHealthWorker.class, 15, TimeUnit.MINUTES)
                .build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(UNIQUE_HEALTH, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    public static void cancelPeriodicHealthCheck(Context context) {
        WorkManager.getInstance(context.getApplicationContext()).cancelUniqueWork(UNIQUE_HEALTH);
    }
}
