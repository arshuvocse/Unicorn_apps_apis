package com.creatrix.unicorn.Presenter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.creatrix.unicorn.Activity.Approval.Leave.LeaveApprovalData;
import com.creatrix.unicorn.Activity.Approval.Leave.LeaveApprovalRQ;
import com.creatrix.unicorn.Interface.ILeaveApproval;
import com.creatrix.unicorn.Model.ResultInfo;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Network.UserProcessAPI;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LeaveApprovalPresenter implements ILeaveApproval.Presenter {
    ILeaveApproval.View view;
    Context context;
    public LeaveApprovalPresenter(ILeaveApproval.View view, Context context) {
        this.view = view;
        this.context = context;
    }
    @Override
    public void getLeaveApprovalList(String pram, Map<String, String> mapparam) {
        UserProcessAPI service = RetrofitClientInstance.getRetrofitInstance().create(UserProcessAPI.class);
        Call<List<LeaveApprovalData>> calls = service.GetLeaveListApproval(pram,mapparam);
        calls.enqueue(new Callback<List<LeaveApprovalData>>() {
            @Override
            public void onResponse(@NonNull Call<List<LeaveApprovalData>> call, @NonNull Response<List<LeaveApprovalData>> response) {
                if(response!=null)
                {
                    List<LeaveApprovalData> data= response.body();
                    view.onGetLeaveApprovalList(data);
                }
                else {
                    view.onError("Leave Data Not Found");
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<LeaveApprovalData>> call, @NonNull Throwable t) {

            }
        });
    }
    @Override
    public void SaveLeaveApproval(LeaveApprovalRQ aInfo) {
        try {
            UserProcessAPI service = RetrofitClientInstance.getRetrofitInstance().create(UserProcessAPI.class);
            Call<ResultInfo> call = service.SaveLeaveApproval(aInfo);
            call.enqueue(new Callback<ResultInfo>() {
                @Override
                public void onResponse(@NonNull Call<ResultInfo> call, @NonNull Response<ResultInfo> response) {
                    if(response.body().isSuccess)
                    {
                        view.onSaveSuccess(response.body().getMsd(),"Success");
                    }
                    else {
                        view.onSaveSuccess(response.body().getMsd(),"Info");
                    }
                }

                @Override
                public void onFailure(@NonNull Call<ResultInfo> call, @NonNull Throwable t) {
                    view.onError(t.getMessage().toString());
                    if (t instanceof SocketTimeoutException) {
                    } else {
                    }
                }
            });
        } catch (Exception exception) {
            //exception.printStackTrace();
        }
    }
}

