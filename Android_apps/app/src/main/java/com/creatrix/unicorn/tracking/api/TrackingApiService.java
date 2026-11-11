package com.creatrix.unicorn.tracking.api;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Url;

public interface TrackingApiService {

    /**
     * @param path relative to the configured base URL (see TrackingConfig#uploadPath); the
     *             Authorization header is added by {@link ApiClient}.
     */
    @POST
    Call<UploadModels.UploadResponse> uploadLocations(@Url String path, @Body UploadModels.UploadRequest body);
}
