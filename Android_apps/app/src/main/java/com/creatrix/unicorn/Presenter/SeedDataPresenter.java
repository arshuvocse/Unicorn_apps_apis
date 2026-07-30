package com.creatrix.unicorn.Presenter;

import com.creatrix.unicorn.Interface.IDoctor;
import com.creatrix.unicorn.Interface.ISeedData;
import com.creatrix.unicorn.Model.MarketViewModel;
import com.creatrix.unicorn.Model.SubMarket;
import com.creatrix.unicorn.Model.TerritoryViewModel;
import com.creatrix.unicorn.Network.RetrofitClientInstance;
import com.creatrix.unicorn.Network.TourApiCall;

import java.net.SocketTimeoutException;
import java.util.List;

import okhttp3.HttpUrl;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeedDataPresenter implements ISeedData.Presenter {
    ISeedData.View view;

    public SeedDataPresenter(ISeedData.View view) {
        this.view = view;
    }

    @Override
    public void GetTerritory(int empId) {
        try{
            TourApiCall service = RetrofitClientInstance.getRetrofitInstance().create(TourApiCall.class);
            Call<List<TerritoryViewModel>> call = service.GetTerritoryByEmpId(empId);
            HttpUrl ds = call.request().url();
            call.enqueue(new Callback<List<TerritoryViewModel>>() {
                @Override
                public void onResponse(Call<List<TerritoryViewModel>> call, Response<List<TerritoryViewModel>> response) {
                    view.onTerritoryReceived(response.body());
                }
                @Override
                public void onFailure(Call<List<TerritoryViewModel>> call, Throwable t) {
                    if(t instanceof SocketTimeoutException){
                    }else{
                    }
                }
            });

        }catch (Exception ex){
        }

    }

    @Override
    public void GetMarket(int territoryId) {
        try{
            TourApiCall service = RetrofitClientInstance.getRetrofitInstance().create(TourApiCall.class);
            Call<List<MarketViewModel>> call = service.GetMarketByTerritoryId(territoryId);
            call.enqueue(new Callback<List<MarketViewModel>>() {
                @Override
                public void onResponse(Call<List<MarketViewModel>> call, Response<List<MarketViewModel>> response) {
                    view.onMarketReceived(response.body());
                }
                @Override
                public void onFailure(Call<List<MarketViewModel>> call, Throwable t) {
                    if(t instanceof SocketTimeoutException){
                    }else{
                    }
                }
            });

        }catch (Exception ex){
        }

    }

    @Override
    public void GetSubmarket(int marketId) {
        try{
            TourApiCall service = RetrofitClientInstance.getRetrofitInstance().create(TourApiCall.class);
            Call<List<SubMarket>> call = service.GetSubMarketByMarket(marketId);
            call.enqueue(new Callback<List<SubMarket>>() {
                @Override
                public void onResponse(Call<List<SubMarket>> call, Response<List<SubMarket>> response) {
                    view.onSubMarketReceived(response.body());
                }
                @Override
                public void onFailure(Call<List<SubMarket>> call, Throwable t) {
                    if(t instanceof SocketTimeoutException){
                    }else{
                    }
                }
            });

        }catch (Exception ex){
        }

    }
}
