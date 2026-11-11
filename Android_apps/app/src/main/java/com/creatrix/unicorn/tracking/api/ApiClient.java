package com.creatrix.unicorn.tracking.api;

import androidx.annotation.Nullable;

import com.creatrix.unicorn.tracking.TokenProvider;
import com.creatrix.unicorn.tracking.TrackLog;
import com.creatrix.unicorn.tracking.TrackingConfig;

import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import okhttp3.Authenticator;
import okhttp3.CertificatePinner;
import okhttp3.ConnectionSpec;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Builds the HTTPS client for the tracking backend: TLS 1.2+ only (cleartext only if the config
 * explicitly allows it, which only debuggable builds honour), optional certificate pinning, bearer
 * authentication with a single token refresh on 401. Deliberately has no logging interceptor: the
 * payload is personal location data and the header carries a credential.
 */
public final class ApiClient {

    private static final Object LOCK = new Object();
    private static TrackingApiService cached;
    private static String cachedKey;

    private ApiClient() {
    }

    public static TrackingApiService get(TrackingConfig config, TokenProvider tokens) {
        String key = config.baseUrl + "|" + Arrays.toString(config.certificatePins) + "|" + config.allowCleartext
                + "|" + System.identityHashCode(tokens);
        synchronized (LOCK) {
            if (cached == null || !key.equals(cachedKey)) {
                cached = build(config, tokens);
                cachedKey = key;
            }
            return cached;
        }
    }

    private static TrackingApiService build(TrackingConfig config, TokenProvider tokens) {
        OkHttpClient.Builder http = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .callTimeout(60, TimeUnit.SECONDS)
                .retryOnConnectionFailure(true)
                // Cleartext only in debuggable builds, whatever the config says.
                .connectionSpecs(config.allowCleartext && TrackLog.isDebuggable()
                        ? Arrays.asList(ConnectionSpec.MODERN_TLS, ConnectionSpec.CLEARTEXT)
                        : Collections.singletonList(ConnectionSpec.MODERN_TLS))
                .addInterceptor(chain -> {
                    Request.Builder request = chain.request().newBuilder().header("Accept", "application/json");
                    String token = tokens.getAccessToken();
                    if (token != null && !token.isEmpty()) {
                        request.header("Authorization", "Bearer " + token);
                    }
                    return chain.proceed(request.build());
                })
                .authenticator(new TokenAuthenticator(tokens));

        HttpUrl base = HttpUrl.parse(config.baseUrl);
        if (base != null && config.certificatePins.length > 0) {
            http.certificatePinner(new CertificatePinner.Builder()
                    .add(base.host(), config.certificatePins)
                    .build());
        }

        return new Retrofit.Builder()
                .baseUrl(config.baseUrl)
                .client(http.build())
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(TrackingApiService.class);
    }

    /** On 401, refresh the token once and replay the request; a second 401 is given up on. */
    private static final class TokenAuthenticator implements Authenticator {
        private final TokenProvider tokens;

        TokenAuthenticator(TokenProvider tokens) {
            this.tokens = tokens;
        }

        @Nullable
        @Override
        public Request authenticate(@Nullable okhttp3.Route route, Response response) {
            if (priorResponseCount(response) >= 1) {
                return null;
            }
            String sent = response.request().header("Authorization");
            String fresh;
            synchronized (tokens) {
                String current = tokens.getAccessToken();
                // Another call may already have refreshed the token while this one was in flight.
                if (current != null && sent != null && !sent.equals("Bearer " + current)) {
                    fresh = current;
                } else if (tokens.refreshAccessToken()) {
                    fresh = tokens.getAccessToken();
                } else {
                    fresh = null;
                }
            }
            if (fresh == null || fresh.isEmpty()) {
                return null;
            }
            return response.request().newBuilder().header("Authorization", "Bearer " + fresh).build();
        }

        private static int priorResponseCount(Response response) {
            int count = 0;
            for (Response prior = response.priorResponse(); prior != null; prior = prior.priorResponse()) {
                count++;
            }
            return count;
        }
    }
}
