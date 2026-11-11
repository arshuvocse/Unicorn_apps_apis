package com.creatrix.unicorn.Services;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.creatrix.unicorn.UtilityHelper.SessionManagement;

import java.util.concurrent.TimeUnit;

/**
 * Second safety net, every 15 minutes: if the service or the 5-minute alarm chain was killed
 * (battery savers, OEM task killers, force stop followed by any app launch), bring both back.
 * It does not record points itself; {@link TrackingAlarmReceiver} does, so nothing is duplicated.
 */
public class TrackingWatchdogWorker extends Worker {
    private static final String UNIQUE_WORK_NAME = "tracking_watchdog";

    public TrackingWatchdogWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void schedule(Context context) {
        PeriodicWorkRequest request = new PeriodicWorkRequest.Builder(
                TrackingWatchdogWorker.class, 15, TimeUnit.MINUTES).build();
        WorkManager.getInstance(context.getApplicationContext())
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        if (!new SessionManagement(context).isLoggedIn()) {
            return Result.success();
        }
        LocationService.start(context);      // also re-arms the alarm chain if it is missing
        TrackingSyncWorker.schedule(context); // flush anything still queued
        return Result.success();
    }
}
