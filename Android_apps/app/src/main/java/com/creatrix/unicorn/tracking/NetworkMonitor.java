package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

import androidx.annotation.NonNull;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Reports transitions between "has a working internet connection" and "does not". "Working" means
 * Android validated the network (NET_CAPABILITY_VALIDATED), so a Wi-Fi with a captive portal counts
 * as offline. Callbacks arrive on a ConnectivityManager thread.
 */
public final class NetworkMonitor {

    public interface Listener {
        void onOnlineChanged(boolean online);
    }

    private static final String TAG = "NetworkMonitor";

    private final ConnectivityManager connectivity;
    private final Listener listener;
    private final Set<Network> validated = Collections.synchronizedSet(new HashSet<Network>());
    private volatile boolean online;
    private boolean registered;

    private final ConnectivityManager.NetworkCallback callback = new ConnectivityManager.NetworkCallback() {
        @Override
        public void onCapabilitiesChanged(@NonNull Network network, @NonNull NetworkCapabilities capabilities) {
            if (capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)) {
                validated.add(network);
            } else {
                validated.remove(network);
            }
            publish();
        }

        @Override
        public void onLost(@NonNull Network network) {
            validated.remove(network);
            publish();
        }
    };

    public NetworkMonitor(Context context, Listener listener) {
        this.connectivity = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        this.listener = listener;
    }

    public synchronized void start() {
        if (registered || connectivity == null) {
            return;
        }
        try {
            online = isOnline(connectivity);
            connectivity.registerNetworkCallback(
                    new NetworkRequest.Builder().addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET).build(),
                    callback);
            registered = true;
        } catch (RuntimeException e) {
            // e.g. TooManyRequestsException when the app leaks callbacks; tracking must not die over it.
            TrackLog.w(TAG, "Cannot register network callback", e);
        }
    }

    public synchronized void stop() {
        if (!registered || connectivity == null) {
            return;
        }
        try {
            connectivity.unregisterNetworkCallback(callback);
        } catch (RuntimeException ignored) {
            // Already unregistered.
        }
        registered = false;
        validated.clear();
    }

    public boolean isOnline() {
        return online;
    }

    private void publish() {
        boolean now = !validated.isEmpty();
        if (now != online) {
            online = now;
            listener.onOnlineChanged(now);
        }
    }

    /** One-off check, for callers that do not keep a monitor running. */
    public static boolean isOnline(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getApplicationContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        return cm != null && isOnline(cm);
    }

    private static boolean isOnline(ConnectivityManager cm) {
        try {
            Network active = cm.getActiveNetwork();
            NetworkCapabilities caps = active == null ? null : cm.getNetworkCapabilities(active);
            return caps != null
                    && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    && caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED);
        } catch (RuntimeException e) {
            return false;
        }
    }
}
