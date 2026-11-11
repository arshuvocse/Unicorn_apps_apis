package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

/**
 * The public entry point of the tracking module. Typical use:
 * <pre>
 *   // after login
 *   TrackingManager.get(ctx).setSession(employeeId, accessToken);
 *   TrackingSetupActivity.launch(ctx);      // consent + permissions, then starts tracking
 *   // after logout
 *   TrackingManager.get(ctx).clearSession();
 * </pre>
 * The manager owns the persisted "tracking wanted" intent, so tracking resumes after a reboot, an app
 * update or a process kill for as long as it has not been stopped on purpose.
 */
public final class TrackingManager {

    public enum StartResult {
        /** Tracking is running (or was already). */
        STARTED,
        /** A blocking problem exists; run {@link TrackingPermissionFlow} / {@link TrackingSetupActivity}. */
        NEEDS_SETUP,
        /** {@link #setSession} has not been called. */
        NOT_SIGNED_IN,
        /** Android refused to start the service right now. */
        START_REFUSED
    }

    private static final String TAG = "TrackingManager";
    private static volatile TrackingManager instance;

    private final Context app;
    private final TrackingPrefs prefs;
    private volatile TokenProvider tokens;

    private TrackingManager(Context context, @Nullable TokenProvider provider) {
        this.app = context.getApplicationContext();
        TrackLog.init(app);
        this.prefs = TrackingPrefs.get(app);
        this.tokens = provider != null ? provider : new SecureTokenStore(app);
        TrackingPolicy.apply(app);
        TrackingPolicy.registerForChanges(app, this::onManagedConfigChanged);
    }

    public static TrackingManager get(Context context) {
        if (instance == null) {
            synchronized (TrackingManager.class) {
                if (instance == null) {
                    instance = new TrackingManager(context, null);
                }
            }
        }
        return instance;
    }

    /**
     * Optional: call from Application.onCreate to plug in the app's own token source (for example one
     * that can refresh an expired token). Without it, tokens given to {@link #setSession} are kept in
     * encrypted storage.
     */
    public static void init(Context context, TokenProvider provider) {
        synchronized (TrackingManager.class) {
            if (instance == null) {
                instance = new TrackingManager(context, provider);
            } else {
                instance.tokens = provider;
            }
        }
    }

    public TokenProvider getTokenProvider() {
        return tokens;
    }

    public TrackingPrefs getPrefs() {
        return prefs;
    }

    // ------------------------------------------------------------------ session / config

    /**
     * Call after a successful sign-in. The employee id is stamped on every location captured from now on.
     * If the default token store is in use the token is saved encrypted; queued locations start uploading.
     */
    public void setSession(String employeeId, @Nullable String accessToken) {
        prefs.setUserId(employeeId);
        if (accessToken != null && tokens instanceof SecureTokenStore) {
            ((SecureTokenStore) tokens).saveAccessToken(accessToken);
        }
        SyncScheduler.enqueue(app, false);
    }

    /** Call on logout: stops tracking and forgets the credentials. Locations still queued stay queued. */
    public void clearSession() {
        stopInternal();
        prefs.setUserId(null);
        if (tokens instanceof SecureTokenStore) {
            ((SecureTokenStore) tokens).clear();
        }
    }

    public void setConsent(boolean granted) {
        prefs.setConsent(granted);
    }

    public void applyConfig(TrackingConfig config) {
        prefs.setConfig(config);
        LocationTrackingService.notifyConfigChanged();
        SyncScheduler.enqueue(app, false);
    }

    private void onManagedConfigChanged() {
        LocationTrackingService.notifyConfigChanged();
        SyncScheduler.enqueue(app, false);
        TrackingDevicePolicy.apply(app);
    }

    // ------------------------------------------------------------------ state

    /** Everything currently stopping or degrading tracking. */
    public List<TrackingProblem> getProblems() {
        return LocationHelper.detectProblems(app, prefs);
    }

    public List<TrackingProblem> getBlockingProblems() {
        List<TrackingProblem> blocking = new ArrayList<>();
        for (TrackingProblem problem : getProblems()) {
            if (problem.isBlocking()) {
                blocking.add(problem);
            }
        }
        return blocking;
    }

    public boolean isTrackingWanted() {
        return prefs.isTrackingWanted();
    }

    public boolean isServiceRunning() {
        return LocationTrackingService.isRunning();
    }

    // ------------------------------------------------------------------ start / stop

    /** Starts tracking if nothing blocks it. Call from the foreground (user action). */
    public StartResult startTracking() {
        String user = prefs.getUserId();
        if (user == null || user.isEmpty()) {
            return StartResult.NOT_SIGNED_IN;
        }
        TrackingDevicePolicy.apply(app); // may pre-grant permissions on a device-owner device
        if (!getBlockingProblems().isEmpty()) {
            return StartResult.NEEDS_SETUP;
        }
        prefs.setTrackingWanted(true);
        SyncScheduler.schedulePeriodicHealthCheck(app);
        SyncScheduler.enqueue(app, false);
        return launchService() ? StartResult.STARTED : StartResult.START_REFUSED;
    }

    /**
     * Stops tracking on the user's request. Refused (returns false) when policy says the employee may
     * not stop tracking. Locations already queued are still uploaded.
     */
    public boolean stopByUser() {
        if (!prefs.getConfig().userCanStop) {
            TrackLog.i(TAG, "Stop refused: disabled by policy");
            return false;
        }
        stopInternal();
        TrackingDevicePolicy.release(app);
        SyncScheduler.enqueue(app, false); // flush what is queued
        return true;
    }

    private void stopInternal() {
        prefs.setTrackingWanted(false);
        SyncScheduler.cancelPeriodicHealthCheck(app);
        app.stopService(new Intent(app, LocationTrackingService.class));
        TrackingNotifications.cancelAlert(app);
    }

    /**
     * Brings tracking back if it is wanted but not running. Called after boot / app update, and by the
     * periodic health worker. Safe to call at any time.
     */
    public void resume() {
        if (!prefs.isTrackingWanted()) {
            return;
        }
        SyncScheduler.schedulePeriodicHealthCheck(app);
        if (!LocationTrackingService.isRunning() && !launchService()) {
            TrackingNotifications.showResumeNeeded(app);
        }
    }

    /**
     * Starts the foreground service. Refuses up front when location permission is missing (the service
     * could not become a foreground service anyway, and Android would raise a crash), and copes with
     * Android's background-start restrictions.
     */
    private boolean launchService() {
        if (!LocationHelper.hasLocationPermission(app)) {
            TrackingNotifications.showProblems(app, getBlockingProblems());
            return false;
        }
        Intent intent = new Intent(app, LocationTrackingService.class).setAction(LocationTrackingService.ACTION_START);
        try {
            ContextCompat.startForegroundService(app, intent);
            return true;
        } catch (RuntimeException e) {
            // Android 12+: ForegroundServiceStartNotAllowedException when started from the background.
            TrackLog.w(TAG, "Android refused to start the tracking service", e);
            return false;
        }
    }
}
