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
        TrackingQueueDbHelper database = new TrackingQueueDbHelper(getApplicationContext());
        GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);

        try {
            while (!isStopped()) {
                List<TrackingQueueDbHelper.PendingTracking> pending = database.getPending(BATCH_SIZE);
                if (pending.isEmpty()) {
                    return Result.success();
                }

                for (TrackingQueueDbHelper.PendingTracking item : pending) {
                    Response<ResultInfo> response = service.SaveTracking(item.getTracking()).execute();
                    ResultInfo body = response.body();
                    if (!response.isSuccessful() || body == null || !Boolean.TRUE.equals(body.getSuccess())) {
                        return Result.retry();
                    }
                    database.delete(item.getId());
                }
            }
            return Result.retry();
        } catch (Exception exception) {
            Log.e("TrackingSyncWorker", "Tracking sync failed", exception);
            return Result.retry();
        } finally {
            database.close();
        }
    }
}
