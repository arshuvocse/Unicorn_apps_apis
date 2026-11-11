package com.creatrix.unicorn.tracking;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

/**
 * Background upload of the offline queue. Runs only with a network (see {@link SyncScheduler}), also
 * when the app process is not running. Returning retry() makes WorkManager back off exponentially.
 */
public class LocationSyncWorker extends Worker {

    public LocationSyncWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    @NonNull
    @Override
    public Result doWork() {
        LocationRepository repository = LocationRepository.get(getApplicationContext());
        repository.onNetworkRestored(); // WorkManager only runs us with a network: skip record backoff
        LocationRepository.SyncResult result = repository.syncPending();
        // NOT_CONFIGURED / NOT_AUTHENTICATED cannot be fixed by retrying: setSession()/applyConfig()
        // enqueue a fresh sync when they change.
        return result == LocationRepository.SyncResult.RETRY ? Result.retry() : Result.success();
    }
}
