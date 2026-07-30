package com.creatrix.unicorn.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.creatrix.unicorn.UtilityHelper.LocationAccess;
import com.creatrix.unicorn.UtilityHelper.SessionManagement;

public class LocationServiceReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        SessionManagement session = new SessionManagement(context);
        if (session.isLoggedIn() && LocationAccess.hasRequiredPermissions(context)) {
            LocationService.start(context);
            TrackingSyncWorker.schedule(context);
        }
    }
}
