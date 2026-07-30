package com.creatrix.unicorn.Services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.creatrix.unicorn.Activity.Fragment.HomeFragment;
import com.creatrix.unicorn.Activity.MainDashboardActivity;

public class DashboardRefresh extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            // If it is visible then trigger the task else do nothing
           // if (isVisible == true) {}
                ConnectivityManager connectivityManager = (ConnectivityManager) context
                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connectivityManager
                        .getActiveNetworkInfo();

                // Check internet connection and accrding to state change the
                // text of activity by calling method
                if (networkInfo != null && networkInfo.isConnected()) {
                   TrackingSyncWorker.schedule(context);
                   // new HomeFragment().changeTextStatus(true);
                } else {
                  //  new HomeFragment().changeTextStatus(false);
                }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
