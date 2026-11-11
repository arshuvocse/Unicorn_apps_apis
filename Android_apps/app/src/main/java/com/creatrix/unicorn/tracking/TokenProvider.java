package com.creatrix.unicorn.tracking;

import androidx.annotation.Nullable;
import androidx.annotation.WorkerThread;

/**
 * How the tracking module obtains the backend authentication token. Implement it on top of your
 * existing login/session code and pass it to {@link TrackingManager#init}.
 */
public interface TokenProvider {

    /** @return the current bearer token, or null when the user is signed out. Must be cheap and thread-safe. */
    @Nullable
    String getAccessToken();

    /**
     * Called at most once per upload attempt after the server answered 401. Runs on a background
     * thread, so a blocking network refresh is fine.
     *
     * @return true if {@link #getAccessToken()} now returns a fresh token
     */
    @WorkerThread
    boolean refreshAccessToken();
}
