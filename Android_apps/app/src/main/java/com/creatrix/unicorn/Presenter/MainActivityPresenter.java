package com.creatrix.unicorn.Presenter;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.creatrix.unicorn.Activity.DashboardActivity;
import com.creatrix.unicorn.Activity.MainActivity;
import com.creatrix.unicorn.Interface.LoginInterface;
import com.creatrix.unicorn.Model.AppVersionModel;
import com.creatrix.unicorn.Model.User;
import com.creatrix.unicorn.Network.ApiMasterSync;
import com.creatrix.unicorn.Network.GetDataService;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;
import com.creatrix.unicorn.UtilityHelper.SnackBarManagement;

import java.net.SocketTimeoutException;
import java.net.UnknownServiceException;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivityPresenter implements LoginInterface.Presenter {

    LoginInterface.View loginView;
    SessionManagement session;
    ProgressDialog progressDoalog;
    public MainActivityPresenter(LoginInterface.View loginView) {
        this.loginView = loginView;
    }
    @Override
    public void doLogin(String UserName, String password, Context context,String Imei,String Token,String Device,String AppVersion,String OS, String OS_Version) {
        progressDoalog = new ProgressDialog(context);
        progressDoalog.setMessage("Attempting to Login....");
        progressDoalog.show();
        progressDoalog.setCanceledOnTouchOutside(false);
        try{
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<User> call = service.LoginRequest(UserName,password,Imei,Token,Device,AppVersion,OS,OS_Version);
            call.enqueue(new Callback<User>() {
                @Override
                public void onResponse(@NonNull Call<User> call, @NonNull Response<User> response) {
                    User user = response.body();
                    if (!response.isSuccessful() || user == null) {
                        progressDoalog.dismiss();
                        loginView.OnError("User Not Found...Please try again or contact with Admin Department!");
                    } else if (user.getUserId() == 0) {
                        progressDoalog.dismiss();

                        String twoDeviceMessage = user.getTwoDeviceMsg();
                        if (twoDeviceMessage != null && !twoDeviceMessage.trim().isEmpty()) {
                            loginView.OnError("Maximum Two Device Allowed!!");
                        } else {
                            loginView.OnError("You Login Name and Password do not match");
                        }
                    } else {
                        int usid = user.getUserId();
                        int EmpInfoId = user.getEmpInfoId();
                        String userName = user.getUserName();
                        String empMasterCo=user.getEmpMasterCode();
                        String loginName = user.getLoginName();
                        String passwrd = user.getPassword();
                        String userCo = user.getUserCo();
                        String versionName = user.getVersionName();
                        String designation=user.getDesigName();
                        int IsImeiMatch = user.getIsImeiMatched();
                        String emprole = user.getEmpRole();

                        //Convert bool to int
                        int isAccepted = user.getisApprove() ? 1 : 0;
                        int isForward = user.getisForward() ? 1 : 0;
                        //Toast.makeText(context, emprole, Toast.LENGTH_SHORT).show();
                        System.out.println(emprole);
                        progressDoalog.dismiss();
                        loginView.OnSuccess(usid,userName,empMasterCo,loginName,passwrd,EmpInfoId,userCo,IsImeiMatch,versionName,emprole,user.getRoleTypeId(), user.getRoleType(),isAccepted,isForward,designation);
                    }

                }
                @Override
                public void onFailure(@NonNull Call<User> call, @NonNull Throwable t) {
                    progressDoalog.dismiss();
                    Log.e("LoginRequest", "Login API failed", t);
                    if(t instanceof SocketTimeoutException){
                        loginView.OnError("Slow Connection Detected. Please try again");
                    }else if (t instanceof UnknownServiceException) {
                        loginView.OnError("Server HTTP connection is blocked. Please update network settings.");
                    }else{
                        loginView.OnError("Slow Connection Detected With Network. Please try again");
                    }
                }
            });

        }catch (Exception ex){
            progressDoalog.dismiss();
            String str = ex.toString();
            loginView.OnError(ex.toString());
        }
    }

  /*  @Override
    public void GetActiveVersion() {

            try{

                GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
                Call<AppVersionModel> call = service.GetAppVersionInfo();
                call.enqueue(new Callback<AppVersionModel>() {
                    @Override
                    public void onResponse(Call<AppVersionModel> call, Response<AppVersionModel> response) {
                        AppVersionModel aInfo = response.body();
                        loginView.onVersionGet(aInfo.getVersionName());
                    }

                    @Override
                    public void onFailure(Call<AppVersionModel> call, Throwable t) {

                    }
                });
            }catch (Exception ex){

            }
    }*/


}
