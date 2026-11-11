package com.creatrix.unicorn.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Restores tracking after a reboot or an app update, if tracking was on before. Only
 * BOOT_COMPLETED (after the user unlocks the device), because the tracking database and preferences
 * live in credential-encrypted storage that does not exist yet at LOCKED_BOOT_COMPLETED.
 *
 * <p>Android decides whether the foreground service may start from here: it is allowed when the app
 * holds "Allow all the time" location access (or is the device owner). If Android refuses, the
 * manager shows a notification asking the user to resume, rather than failing silently.
 */
public class TrackingBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent == null ? null : intent.getAction();
        if (Intent.ACTION_BOOT_COMPLETED.equals(action) || Intent.ACTION_MY_PACKAGE_REPLACED.equals(action)) {
            TrackingManager.get(context).resume();
        }
    }
}
