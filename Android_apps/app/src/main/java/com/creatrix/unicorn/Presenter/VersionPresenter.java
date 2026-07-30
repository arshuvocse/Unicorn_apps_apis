package com.creatrix.unicorn.Presenter;
import androidx.annotation.NonNull;

import com.creatrix.unicorn.Interface.IVersionUpdate;
import com.creatrix.unicorn.Model.AppVersionModel;
import com.creatrix.unicorn.Network.GetDataService;
import com.creatrix.unicorn.Network.RetrofitClientInstance;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class VersionPresenter implements IVersionUpdate.Presenter {
    IVersionUpdate.View versionview;
    public VersionPresenter(IVersionUpdate.View versionview) {
        this.versionview=versionview;
    }
    @Override
    public void GetActiveVersion() {
        try{
            GetDataService service = RetrofitClientInstance.getRetrofitInstance().create(GetDataService.class);
            Call<AppVersionModel> call = service.GetAppVersionInfo();
            call.enqueue(new Callback<AppVersionModel>() {
                @Override
                public void onResponse(@NonNull Call<AppVersionModel> call, @NonNull Response<AppVersionModel> response) {
                    if (!response.isSuccessful() || response.body() == null || versionview == null) {
                        return;
                    }
                    AppVersionModel aInfo = response.body();
                    if (aInfo.getVersionName() != null) {
                        versionview.onVersionGet(aInfo.getVersionName());
                    }
                }
                @Override
                public void onFailure(@NonNull Call<AppVersionModel> call, @NonNull Throwable t) {
                }
            });
        }catch (Exception ex){
        }
    }
}
