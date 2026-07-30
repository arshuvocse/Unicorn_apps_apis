package com.creatrix.unicorn.Presenter;

import android.app.ProgressDialog;
import android.content.Context;

import com.creatrix.unicorn.Interface.IAttendanceReport;
import com.creatrix.unicorn.Model.Customer;
import com.creatrix.unicorn.Model.Report_AttendanceViewModel;
import com.creatrix.unicorn.Network.GetDataService;
import com.creatrix.unicorn.Network.RetrofitClientInstance;

import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Rpt_AttendancePresenter implements IAttendanceReport.Presenter {
    ProgressDialog progressDoalog;
    IAttendanceReport.View view;

    public Rpt_AttendancePresenter(IAttendanceReport.View view) {
        this.view = view;
    }

    @Override
    public void GetAttendanceData(int empId, String startDate, String endDate) {

        progressDoalog = new ProgressDialog((Context) view);
        progressDoalog.setMessage("Attendance is Loading....Please wait");
        progressDoalog.show();
        progressDoalog.setCanceledOnTouchOutside(false);
        try{
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<List<Report_AttendanceViewModel>> call = service.GetAttendanceData(empId,startDate,endDate);
            HttpUrl ds = call.request().url();
            call.enqueue(new Callback<List<Report_AttendanceViewModel>>() {
                @Override
                public void onResponse(Call<List<Report_AttendanceViewModel>> call, Response<List<Report_AttendanceViewModel>> response) {
                    progressDoalog.dismiss();
                    view.OnSuccess(response.body());
                }
                @Override
                public void onFailure(Call<List<Report_AttendanceViewModel>> call, Throwable t) {
                    progressDoalog.dismiss();
                    if(t instanceof SocketTimeoutException){
                        view.OnError("Some Error");
                    }else{
                        view.OnError("Error");
                    }

                }
            });

        }catch (Exception ex){
            progressDoalog.dismiss();
            view.OnError("Error");
        }

    }
}
