package com.creatrix.unicorn.tracking.api;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

import com.creatrix.unicorn.Setting.AppSetting;
import com.creatrix.unicorn.tracking.TrackLog;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;

/**
 * Asks salesrollapi to turn nearby cell towers / WiFi BSSIDs into a position (the server holds the
 * Google Geolocation key). Same base URL and (absent) auth as the other salesrollapi calls; no
 * logging interceptor because the body is location data.
 */
public final class GeoResolveClient {

    private static final String TAG = "GeoResolveClient";
    private static Service service;

    private GeoResolveClient() {
    }

    interface Service {
        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @POST("api/Tracking/ResolveLocation")
        Call<GeoResolveModels.ResolveResponse> resolve(@Body GeoResolveModels.ResolveRequest body);

        @Headers({"Accept: application/json", "Content-Type: application/json"})
        @POST("api/Tracking/LearnCell")
        Call<GeoResolveModels.ResolveResponse> learn(@Body GeoResolveModels.LearnRequest body);
    }

    /** Returns null on any network/HTTP failure or when the server could not resolve a position. */
    @WorkerThread
    @Nullable
    public static GeoResolveModels.ResolveResponse resolve(Context context, GeoResolveModels.ResolveRequest request) {
        try {
            Response<GeoResolveModels.ResolveResponse> response = service().resolve(request).execute();
            GeoResolveModels.ResolveResponse body = response.body();
            if (!response.isSuccessful() || body == null || !body.success) {
                TrackLog.d(TAG, "Resolve failed: HTTP " + response.code()
                        + (body != null && body.message != null ? " " + body.message : ""));
                return null;
            }
            return body;
        } catch (Exception e) {
            TrackLog.d(TAG, "Resolve failed: " + e.getClass().getSimpleName());
            return null;
        }
    }

    /**
     * Tells the server where a cell tower is, using a GPS fix the device already has, so the same
     * tower can place a device that has no GPS later. Best effort: a failure changes nothing.
     */
    @WorkerThread
    public static void learn(Context context, GeoResolveModels.LearnRequest request) {
        try {
            Response<GeoResolveModels.ResolveResponse> response = service().learn(request).execute();
            GeoResolveModels.ResolveResponse body = response.body();
            if (!response.isSuccessful() || body == null || !body.success) {
                TrackLog.d(TAG, "Learn refused: HTTP " + response.code()
                        + (body != null && body.message != null ? " " + body.message : ""));
            }
        } catch (Exception e) {
            TrackLog.d(TAG, "Learn failed: " + e.getClass().getSimpleName());
        }
    }

    private static synchronized Service service() {
        if (service == null) {
            OkHttpClient client = new OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .writeTimeout(10, TimeUnit.SECONDS)
                    .callTimeout(10, TimeUnit.SECONDS)
                    .build();
            service = new Retrofit.Builder()
                    .baseUrl(AppSetting.newBASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .client(client)
                    .build()
                    .create(Service.class);
        }
        return service;
    }
}
