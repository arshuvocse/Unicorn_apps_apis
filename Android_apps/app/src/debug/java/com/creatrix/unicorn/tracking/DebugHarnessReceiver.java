package com.creatrix.unicorn.tracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.creatrix.unicorn.tracking.data.TrackedLocation;

/** TEMPORARY debug-only test harness driven by `adb shell am broadcast`. Not part of the deliverable. */
public class DebugHarnessReceiver extends BroadcastReceiver {
    private static final String TAG = "TrackHarness";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        final PendingResult pending = goAsync();
        new Thread(() -> {
            try {
                handle(context.getApplicationContext(), intent);
            } catch (Exception e) {
                Log.e(TAG, "harness failed", e);
            } finally {
                pending.finish();
            }
        }).start();
    }

    private void handle(Context ctx, Intent intent) {
        TrackingManager m = TrackingManager.get(ctx);
        String cmd = intent.getStringExtra("cmd");
        Log.i(TAG, "cmd=" + cmd);
        if ("config".equals(cmd)) {
            TrackingConfig.Builder b = m.getPrefs().getConfig().toBuilder();
            String url = intent.getStringExtra("url");
            if (url != null) b.baseUrl(url).allowCleartext(true);
            int sec = intent.getIntExtra("interval", 0);
            if (sec > 0) b.intervalMs(sec * 1000L).fastestIntervalMs(sec * 500L);
            b.requireBackgroundLocation(intent.getBooleanExtra("bg", true));
            m.applyConfig(b.build());
            Log.i(TAG, "config applied: " + m.getPrefs().getConfig().toJson());
        } else if ("session".equals(cmd)) {
            m.setSession(intent.getStringExtra("emp"), intent.getStringExtra("token"));
            Log.i(TAG, "session set");
        } else if ("consent".equals(cmd)) {
            m.setConsent(true);
        } else if ("problems".equals(cmd)) {
            Log.i(TAG, "problems=" + m.getProblems());
        } else if ("start".equals(cmd)) {
            Log.i(TAG, "start result=" + m.startTracking() + " problems=" + m.getProblems());
        } else if ("stop".equals(cmd)) {
            Log.i(TAG, "stop result=" + m.stopByUser());
        } else if ("resume".equals(cmd)) {
            m.resume();
        } else if ("status".equals(cmd)) {
            LocationRepository r = LocationRepository.get(ctx);
            Log.i(TAG, "wanted=" + m.isTrackingWanted() + " running=" + m.isServiceRunning()
                    + " unsynced=" + r.getUnsyncedCount());
            for (TrackedLocation t : r.getRecent(intent.getIntExtra("n", 5))) {
                Log.i(TAG, "row#" + t.id + " status=" + t.status + " attempts=" + t.attemptCount
                        + " prov=" + t.provider + " acc=" + t.accuracy + " emp=" + t.employeeId
                        + " dev=" + t.deviceId + " serverAt=" + t.serverSyncedAt + " addr=" + t.address
                        + " err=" + t.lastError);
            }
        }
    }
}
