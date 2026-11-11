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

import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.SyncDb_Helper;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.Doctor.DoctorListViewModel;
import com.creatrix.unicorn.Network.ApiCustomerCall;
import com.creatrix.unicorn.Network.ApiDoctorCall;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;

import java.util.HashMap;
import java.util.List;

import retrofit2.Response;

// Runs a cheap count-only check against the server for Customer/Doctor lists and only
// re-downloads+rewrites the local table when the counts actually disagree, instead of the
// SyncFromServerAc screen re-pulling every master list on every login. Every step is wrapped
// so a network hiccup or bad response here can never surface as a crash to the user - worst
// case this check silently does nothing and the next trigger tries again.
public class SyncCheckWorker extends Worker {
    private static final String TAG = "SyncCheckWorker";
    private static final String UNIQUE_WORK_NAME = "sync_count_check";

    public SyncCheckWorker(@NonNull Context context, @NonNull WorkerParameters params) {
        super(context, params);
    }

    public static void schedule(Context context) {
        try {
            Constraints constraints = new Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build();
            OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(SyncCheckWorker.class)
                    .setConstraints(constraints)
                    .build();
            WorkManager.getInstance(context.getApplicationContext())
                    .enqueueUniqueWork(UNIQUE_WORK_NAME, ExistingWorkPolicy.KEEP, request);
        } catch (Exception exception) {
            Log.e(TAG, "Could not schedule sync check", exception);
        }
    }

    @NonNull
    @Override
    public Result doWork() {
        try {
            Context context = getApplicationContext();
            SessionManagement session = new SessionManagement(context);
            HashMap<String, String> user = session.getUserDetails();
            String empIdStr = user != null ? user.get(SessionManagement.KEY_EmpId) : null;
            if (empIdStr == null) {
                return Result.success();
            }

            int empId;
            try {
                empId = Integer.parseInt(empIdStr);
            } catch (NumberFormatException nfe) {
                return Result.success();
            }

            checkAndSyncCustomers(context, empId);
            checkAndSyncDoctors(context, empId);
        } catch (Exception exception) {
            // Never let a background check crash or fail the caller - just log and give up quietly.
            Log.e(TAG, "Sync count check failed", exception);
        }
        return Result.success();
    }

    private void checkAndSyncCustomers(Context context, int empId) {
        try {
            DBCrudHelper crudHelper = new DBCrudHelper(context);
            int localCount = crudHelper.GetRowCount("tblCustomerInfo");
            if (localCount < 0) {
                return; // local count unreadable - don't risk an unnecessary sync
            }

            ApiCustomerCall service = RetrofitClientInstance.getRetrofitInstance().create(ApiCustomerCall.class);
            Response<Integer> countResponse = service.GetCustomerCountByUser(empId).execute();
            if (!countResponse.isSuccessful() || countResponse.body() == null) {
                return;
            }

            int remoteCount = countResponse.body();
            if (remoteCount <= 0 || remoteCount == localCount) {
                return; // nothing changed, or server returned an empty/invalid count - skip
            }

            Response<List<Customer>> listResponse = service.GetCustomerByUser(empId).execute();
            if (!listResponse.isSuccessful()) {
                return;
            }
            List<Customer> customers = listResponse.body();
            if (customers != null && !customers.isEmpty()) {
                new SyncDb_Helper(context).InsertCustomerList(customers);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Customer count check failed", exception);
        }
    }

    private void checkAndSyncDoctors(Context context, int empId) {
        try {
            DBCrudHelper crudHelper = new DBCrudHelper(context);
            int localCount = crudHelper.GetRowCount("tblDoctorInfo");
            if (localCount < 0) {
                return;
            }

            ApiDoctorCall service = RetrofitClientInstance.getRetrofitInstance().create(ApiDoctorCall.class);
            Response<Integer> countResponse = service.GetDoctorCountByUser(empId).execute();
            if (!countResponse.isSuccessful() || countResponse.body() == null) {
                return;
            }

            int remoteCount = countResponse.body();
            if (remoteCount <= 0 || remoteCount == localCount) {
                return;
            }

            Response<List<DoctorListViewModel>> listResponse = service.GetDoctorList(empId).execute();
            if (!listResponse.isSuccessful()) {
                return;
            }
            List<DoctorListViewModel> doctors = listResponse.body();
            if (doctors != null && !doctors.isEmpty()) {
                new SyncDb_Helper(context).InsertDoctorList(doctors);
            }
        } catch (Exception exception) {
            Log.e(TAG, "Doctor count check failed", exception);
        }
    }
}
