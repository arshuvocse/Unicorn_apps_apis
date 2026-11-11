package com.creatrix.unicorn.Network;

import com.creatrix.unicorn.Setting.AppSetting;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClientInstanceAttendance {
   private static final String BASE_URL = AppSetting.newBASE_URL;
   private static Retrofit retrofit = null;
    public static Retrofit getRetrofitInstance() {
        OkHttpClient clientz =new OkHttpClient.Builder()
                .connectTimeout(10,TimeUnit.MINUTES)
                .writeTimeout(10,TimeUnit.MINUTES)
                .readTimeout(10,TimeUnit.MINUTES)
                .build();
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(clientz)
                    .build();
        }
        return retrofit;
    }
}