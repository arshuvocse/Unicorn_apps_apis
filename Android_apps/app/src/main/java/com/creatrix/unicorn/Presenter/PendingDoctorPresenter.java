package com.creatrix.unicorn.Presenter;

import android.app.ProgressDialog;
import android.content.Context;

import androidx.annotation.NonNull;

import com.creatrix.unicorn.Activity.Customer.Pending.CustomerARModel;
import com.creatrix.unicorn.Activity.Doctor.Pending.DoctorARModel;
import com.creatrix.unicorn.DBAdapter.DBCrudHelper;
import com.creatrix.unicorn.DBAdapter.DBDoctor.DBDoctorHelper;
import com.creatrix.unicorn.DBAdapter.SyncDb_Helper;
import com.creatrix.unicorn.DBInfo.DBHelperMain;
import com.creatrix.unicorn.Interface.ICustomer;
import com.creatrix.unicorn.Interface.IPendingDoctor;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Network.ApiCustomerCall;
import com.creatrix.unicorn.Network.ApiDoctorCall;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;

import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PendingDoctorPresenter implements IPendingDoctor.Presenter {
    IPendingDoctor.View view;
    DBHelperMain dbHelper;
    DBCrudHelper crudHelper;
    SyncDb_Helper syncDb_helper;
    DBDoctorHelper dbDoctorHelper;
    public PendingDoctorPresenter(IPendingDoctor.View view, Context context) {
        this.view = view;
        this.dbHelper = new DBHelperMain(context);
        this.crudHelper = new DBCrudHelper(context);
        this.syncDb_helper = new SyncDb_Helper(context);
        this.dbDoctorHelper = new DBDoctorHelper(context);
    }

    @Override
    public void GetDoctorReport(int empId) {
        try {
            ApiDoctorCall service = RetrofitClientInstance.getRetrofitInstance().create(ApiDoctorCall.class);
            Call<List<DoctorARModel>> call = service.GetDoctorApproveRejLists(empId);
            call.enqueue(new Callback<List<DoctorARModel>>() {
                @Override
                public void onResponse(@NonNull Call<List<DoctorARModel>> call, @NonNull Response<List<DoctorARModel>> response) {
                    if(response.body()!=null){
                        crudHelper._deleteAllRecordsFromaTable("tblDoctorReport");
                        crudHelper.InsertCDoctorReport_SQLite(response.body());
                        view.OnSuccessDoctorReport(response.body());
                    }else {
                        view.OnSuccessDoctorReport(null);
                    }

                }
                @Override
                public void onFailure(@NonNull Call<List<DoctorARModel>> call, @NonNull Throwable t) {
                    if(t instanceof SocketTimeoutException){
                        view.OnErrorReport("Slow Connection Detected");
                    }else{
                        view.OnErrorReport("Loading Failed!!Try Again");
                    }

                }
            });

        } catch (Exception ex) {
        }
    }
}
