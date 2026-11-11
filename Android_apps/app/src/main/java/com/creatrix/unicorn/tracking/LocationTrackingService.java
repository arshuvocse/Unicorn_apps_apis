package com.creatrix.unicorn.tracking;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.SystemClock;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.creatrix.unicorn.R;
import com.creatrix.unicorn.tracking.api.GeoResolveClient;
import com.creatrix.unicorn.tracking.api.GeoResolveModels;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The long-running foreground service (type "location") that keeps collecting and uploading locations
 * while the app is in the background, minimised, or the screen is locked.
 *
 * <p>Threading: location callbacks, the health tick and provider broadcasts run on one
 * HandlerThread; database writes + geocoding run on {@link #ioExecutor}; uploads run on
 * {@link #syncExecutor}. The main thread is never blocked.
 *
 * <p>The service stays up on its own accord: {@code START_STICKY} restarts it after a kill, the boot
 * receiver and health worker restart it after a reboot or an OEM kill, and it only stops on an
 * explicit user/policy stop ({@link TrackingManager#stopByUser}).
 */
public class LocationTrackingService extends Service {

    public static final String ACTION_START = "com.creatrix.unicorn.tracking.START";
    public static final String ACTION_STOP = "com.creatrix.unicorn.tracking.STOP";

    private static final String TAG = "TrackingService";
    private static final long HEALTH_TICK_MS = 60_000L;
    /** Only a fix this accurate may teach a tower position; a coarse one would move the tower. */
    private static final float LEARN_MAX_ACCURACY_M = 100f;
    /** A tower does not move, so one sample per period is plenty. */
    private static final long LEARN_INTERVAL_MS = 10 * 60_000L;
    private static final long NOTIFICATION_MIN_GAP_MS = 15_000L;

    private static volatile LocationTrackingService instance;

    private HandlerThread locationThread;
    private Handler handler;
    private final ExecutorService ioExecutor = Executors.newSingleThreadExecutor();
    private final ExecutorService syncExecutor = Executors.newSingleThreadExecutor();

    private TrackingPrefs prefs;
    private LocationRepository repository;
    private LocationHelper helper;
    private AddressResolver addressResolver;
    private NetworkMonitor networkMonitor;

    private boolean foregroundStarted;
    private boolean receiversRegistered;
    private boolean locationAvailable = true;
    private volatile long lastFixElapsedMs = SystemClock.elapsedRealtime();
    private long lastRadioFallbackMs;
    private long lastLearnMs;
    private long lastNotificationMs;
    private String lastNotificationText = "";
    private List<TrackingProblem> lastAlerted = new ArrayList<>();

    /** True while the service is alive; used by the health worker to decide whether to restart it. */
    public static boolean isRunning() {
        return instance != null;
    }

    /** Applies a config change (interval, accuracy...) to the running service. */
    static void notifyConfigChanged() {
        LocationTrackingService service = instance;
        if (service != null && service.handler != null) {
            service.handler.post(service::reloadConfig);
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    // ------------------------------------------------------------------ lifecycle

    @Override
    public void onCreate() {
        super.onCreate();
        TrackLog.init(this);
        prefs = TrackingPrefs.get(this);
        repository = LocationRepository.get(this);
        addressResolver = new AddressResolver(this);
        locationThread = new HandlerThread("tracking-location");
        locationThread.start();
        handler = new Handler(locationThread.getLooper());
        helper = new LocationHelper(this, prefs.getConfig(), locationListener, locationThread.getLooper());
        networkMonitor = new NetworkMonitor(this, this::onOnlineChanged);
        instance = this;
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();

        if (ACTION_STOP.equals(action)) {
            if (!TrackingManager.get(this).stopByUser()) {
                // Stopping is not allowed by policy: stay up.
                enterForeground();
            }
            return START_NOT_STICKY;
        }

        // A null intent means Android restarted us after killing the process (START_STICKY).
        // Honour the persisted intent: if tracking is no longer wanted, do not come back to life.
        if (!prefs.isTrackingWanted()) {
            enterForeground(); // startForegroundService() obliges us to; then leave immediately
            stopForegroundAndSelf();
            return START_NOT_STICKY;
        }

        if (!enterForeground()) {
            return START_NOT_STICKY; // could not become a foreground service; alert already posted
        }
        handler.post(this::startTracking);
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        instance = null;
        if (handler != null) {
            handler.removeCallbacksAndMessages(null);
        }
        if (helper != null) {
            helper.stop();
        }
        if (networkMonitor != null) {
            networkMonitor.stop();
        }
        unregisterReceivers();
        if (addressResolver != null) {
            addressResolver.shutdown();
        }
        ioExecutor.shutdown();
        syncExecutor.shutdown();
        if (locationThread != null) {
            locationThread.quitSafely();
        }
        super.onDestroy();
    }

    /** Swiping the app away must not stop tracking, so nothing to do; START_STICKY covers a kill. */
    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
    }

    // ------------------------------------------------------------------ foreground

    /**
     * Promotes the service to foreground with the persistent notification. This must happen within a few
     * seconds of startForegroundService(), so it is the first thing done.
     *
     * @return false if Android refused (typically: location permission missing on Android 14+)
     */
    private boolean enterForeground() {
        boolean allowStop = prefs.getConfig().userCanStop;
        Notification notification = TrackingNotifications.buildStatus(
                this, getString(R.string.tracking_notif_text_waiting), allowStop);
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(TrackingNotifications.ID_STATUS, notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(TrackingNotifications.ID_STATUS, notification);
            }
            foregroundStarted = true;
            return true;
        } catch (RuntimeException e) {
            // SecurityException: location permission missing/revoked. On Android 12+ also
            // ForegroundServiceStartNotAllowedException. Do not crash; tell the user what to fix.
            TrackLog.w(TAG, "Cannot run as foreground service", e);
            TrackingNotifications.showProblems(this, blockingProblems());
            stopSelf();
            return false;
        }
    }

    private void stopForegroundAndSelf() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        stopSelf();
    }

    // ------------------------------------------------------------------ tracking (handler thread)

    private void startTracking() {
        registerReceivers();
        networkMonitor.start();
        lastFixElapsedMs = SystemClock.elapsedRealtime();
        checkProblems();
        if (!helper.start()) {
            TrackLog.w(TAG, "Location updates not started; waiting for the problem to be fixed");
        }
        handler.removeCallbacks(healthTick);
        handler.postDelayed(healthTick, HEALTH_TICK_MS);
        syncSoon(); // upload whatever an earlier run left in the queue
    }

    private void reloadConfig() {
        TrackingConfig config = prefs.getConfig();
        helper.updateConfig(config);
        refreshNotification(true);
    }

    private final LocationHelper.Listener locationListener = new LocationHelper.Listener() {
        @Override
        public void onLocation(final Location location) {
            lastFixElapsedMs = SystemClock.elapsedRealtime();
            locationAvailable = true;
            ioExecutor.execute(() -> storeLocation(location));
        }

        @Override
        public void onAvailabilityChanged(boolean available) {
            locationAvailable = available;
            if (!available) {
                TrackLog.i(TAG, "No location source available; still monitoring");
            }
            refreshNotification(false);
        }
    };

    /** IO thread: address lookup (best effort) then the durable insert, then trigger an upload. */
    private void storeLocation(Location location) {
        String address = addressResolver.resolve(location.getLatitude(), location.getLongitude());
        long id = repository.save(location, address);
        if (id > 0) {
            TrackLog.d(TAG, "Stored location #" + id + " provider=" + location.getProvider());
            syncSoon();
        }
        refreshNotification(false);
        learnServingCell(location);
    }

    /**
     * IO thread: while this device still has GPS, tell the server where its serving tower is, so the
     * same tower can place a device that has none (indoors, weak sky view). Only real, accurate,
     * non-mock GPS fixes teach the map: everything else would move a tower rather than confirm it.
     */
    private void learnServingCell(Location location) {
        long now = SystemClock.elapsedRealtime();
        if (lastLearnMs != 0 && now - lastLearnMs < LEARN_INTERVAL_MS) {
            return;
        }
        if (!location.hasAccuracy() || location.getAccuracy() > LEARN_MAX_ACCURACY_M
                || LocationHelper.isMock(location)) {
            return;
        }
        String provider = location.getProvider();
        // "cell" / "wifi" are our own radio fallback and "network" is tower-derived already: a
        // position from the map must never be fed back into the map.
        if (provider == null || provider.equals("cell") || provider.equals("wifi")
                || provider.equals("network")) {
            return;
        }
        String employeeId = prefs.getUserId();
        if (employeeId == null || employeeId.isEmpty() || !NetworkMonitor.isOnline(this)) {
            return;
        }
        GeoResolveModels.CellTower tower = RadioScanner.servingCell(this);
        if (tower == null) {
            return;
        }
        lastLearnMs = now;
        GeoResolveModels.LearnRequest request = new GeoResolveModels.LearnRequest();
        request.employeeId = employeeId;
        request.radioType = tower.radioType;
        request.mobileCountryCode = tower.mobileCountryCode;
        request.mobileNetworkCode = tower.mobileNetworkCode;
        request.locationAreaCode = tower.locationAreaCode;
        request.cellId = tower.cellId;
        request.latitude = location.getLatitude();
        request.longitude = location.getLongitude();
        request.accuracy = location.getAccuracy();
        GeoResolveClient.learn(this, request);
    }

    private void syncSoon() {
        if (syncExecutor.isShutdown()) {
            return;
        }
        syncExecutor.execute(() -> {
            if (!NetworkMonitor.isOnline(this)) {
                SyncScheduler.enqueue(this, false); // WorkManager uploads when connectivity returns
                return;
            }
            LocationRepository.SyncResult result = repository.syncPending();
            if (result == LocationRepository.SyncResult.RETRY) {
                SyncScheduler.enqueue(this, false); // exponential backoff
            }
            refreshNotification(false);
        });
    }

    private void onOnlineChanged(boolean online) {
        if (online) {
            syncExecutor.execute(() -> {
                repository.onNetworkRestored();
                SyncScheduler.enqueue(this, true); // replaces a work request that is deep in backoff
                LocationRepository.SyncResult result = repository.syncPending();
                if (result == LocationRepository.SyncResult.RETRY) {
                    SyncScheduler.enqueue(this, false);
                }
                refreshNotification(false);
            });
        } else {
            refreshNotification(false);
        }
    }

    // ------------------------------------------------------------------ health tick

    private final Runnable healthTick = new Runnable() {
        @Override
        public void run() {
            checkProblems();
            long silentFor = SystemClock.elapsedRealtime() - lastFixElapsedMs;
            long staleAfter = 2 * prefs.getConfig().intervalMs + 30_000L;
            if (silentFor > staleAfter) {
                // Nothing for too long: ask every source once and (re)arm the fallback providers.
                locationAvailable = false;
                helper.requestImmediateFix();
                if (!helper.start()) {
                    TrackLog.d(TAG, "Health tick: location updates still cannot start");
                }
                refreshNotification(false);
                long now = SystemClock.elapsedRealtime();
                if (now - lastRadioFallbackMs >= prefs.getConfig().intervalMs
                        && LocationHelper.hasLocationPermission(LocationTrackingService.this)) {
                    lastRadioFallbackMs = now;
                    final long fixMarker = lastFixElapsedMs;
                    if (!ioExecutor.isShutdown()) {
                        ioExecutor.execute(() -> radioFallback(fixMarker));
                    }
                }
            }
            handler.postDelayed(this, HEALTH_TICK_MS);
        }
    };

    /** IO thread: cell/WiFi location via the server, stored like a real fix unless one arrived meanwhile. */
    private void radioFallback(long fixMarker) {
        Location location = FallbackLocator.locate(this);
        // The lookup can take seconds; the service may have been destroyed meanwhile.
        if (location == null || ioExecutor.isShutdown()) {
            return;
        }
        // Same accuracy gate LocationHelper.deliver applies to platform fallback fixes.
        if (location.getAccuracy() > Math.max(prefs.getConfig().maxAccuracyMeters, LocationHelper.FALLBACK_MAX_ACCURACY_M)) {
            TrackLog.d(TAG, "Radio fallback too inaccurate (" + location.getAccuracy() + " m); dropped");
            return;
        }
        if (lastFixElapsedMs != fixMarker) {
            TrackLog.d(TAG, "Real fix arrived during radio fallback; discarding " + location.getProvider());
            return;
        }
        TrackLog.i(TAG, "Radio fallback location from " + location.getProvider());
        storeLocation(location);
    }

    /**
     * Compares what Android currently allows with what tracking needs. Posts an "action required"
     * notification for anything blocking (permission revoked, Location Services switched off...) and
     * withdraws it once fixed. Never bypasses anything: the notification opens the setup screen, which
     * opens the real Android permission / settings screens.
     */
    private void checkProblems() {
        List<TrackingProblem> blocking = blockingProblems();
        if (blocking.isEmpty()) {
            if (!lastAlerted.isEmpty()) {
                TrackingNotifications.cancelAlert(this);
                lastAlerted = new ArrayList<>();
                helper.start(); // e.g. permission was just granted: begin receiving updates
            }
            return;
        }
        if (!blocking.equals(lastAlerted)) {
            lastAlerted = blocking;
            TrackingNotifications.showProblems(this, blocking);
        }
    }

    private List<TrackingProblem> blockingProblems() {
        List<TrackingProblem> blocking = new ArrayList<>();
        for (TrackingProblem problem : LocationHelper.detectProblems(this, prefs)) {
            if (problem.isBlocking()) {
                blocking.add(problem);
            }
        }
        return blocking;
    }

    private final BroadcastReceiver providerReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            // GPS / network provider switched on or off: re-evaluate now instead of at the next tick.
            handler.post(() -> {
                checkProblems();
                if (LocationHelper.isLocationServicesEnabled(LocationTrackingService.this)) {
                    helper.start();
                }
            });
        }
    };

    private void registerReceivers() {
        if (receiversRegistered) {
            return;
        }
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.location.PROVIDERS_CHANGED");
        filter.addAction("android.location.MODE_CHANGED");
        ContextCompat.registerReceiver(this, providerReceiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED);
        receiversRegistered = true;
    }

    private void unregisterReceivers() {
        if (!receiversRegistered) {
            return;
        }
        try {
            unregisterReceiver(providerReceiver);
        } catch (IllegalArgumentException ignored) {
            // Already unregistered.
        }
        receiversRegistered = false;
    }

    // ------------------------------------------------------------------ notification

    private void refreshNotification(boolean force) {
        if (!foregroundStarted) {
            return;
        }
        ioExecutor.execute(() -> {
            String text = buildStatusText();
            long now = SystemClock.elapsedRealtime();
            if (!force && (text.equals(lastNotificationText) || now - lastNotificationMs < NOTIFICATION_MIN_GAP_MS)) {
                return;
            }
            lastNotificationText = text;
            lastNotificationMs = now;
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (nm != null) {
                try {
                    nm.notify(TrackingNotifications.ID_STATUS,
                            TrackingNotifications.buildStatus(this, text, prefs.getConfig().userCanStop));
                } catch (RuntimeException e) {
                    TrackLog.d(TAG, "Notification update failed: " + e.getClass().getSimpleName());
                }
            }
        });
    }

    private String buildStatusText() {
        int pending;
        try {
            pending = repository.getUnsyncedCount();
        } catch (RuntimeException e) {
            pending = 0;
        }
        boolean online = networkMonitor.isOnline();
        if (!locationAvailable) {
            return getString(R.string.tracking_notif_text_waiting);
        }
        if (!online && pending > 0) {
            return getString(R.string.tracking_notif_text_offline, pending);
        }
        if (pending > 0) {
            return getString(R.string.tracking_notif_text_pending, pending);
        }
        return getString(R.string.tracking_notif_text_ok);
    }
}
