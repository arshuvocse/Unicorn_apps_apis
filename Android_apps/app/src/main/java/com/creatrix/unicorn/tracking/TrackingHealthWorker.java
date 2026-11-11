package com.creatrix.unicorn.tracking;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Periodic safety net. If tracking is wanted but the service is gone (OEM task killer, crash, a boot
 * start that Android refused), try to bring it back; and make sure the upload queue is draining.
 * Android 12+ may refuse a foreground-service start from the background; in that case the manager
 * shows a "tap to resume" notification instead of failing silently.
 */
public class TrackingHealthWorker extends Worker {

    public TrackingHealthWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        Context context = getApplicationContext();
        TrackingManager manager = TrackingManager.get(context);
        if (manager.isTrackingWanted()) {
            manager.resume();
            SyncScheduler.enqueue(context, false);
        }
        return Result.success();
    }
}
