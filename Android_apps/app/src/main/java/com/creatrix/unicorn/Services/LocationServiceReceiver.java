package com.creatrix.unicorn.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.creatrix.unicorn.UtilityHelper.SessionManagement;

/**
 * Boot / app update / GPS toggled / restart request: bring the whole tracking chain back up.
 * Not gated on permission or GPS state, because "no location" points are still recorded.
 */
public class LocationServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SessionManagement session = new SessionManagement(context);
        if (session.isLoggedIn()) {
            LocationService.start(context);
            TrackingScheduler.ensure(context);
            TrackingSyncWorker.schedule(context);
            TrackingWatchdogWorker.schedule(context);
        }
    }
}
