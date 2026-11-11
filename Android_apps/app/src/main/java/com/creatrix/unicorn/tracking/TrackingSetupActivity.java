package com.creatrix.unicorn.tracking;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.creatrix.unicorn.R;

import java.util.List;

/**
 * Transparent host for {@link TrackingPermissionFlow}: shows the consent / permission / settings steps
 * as dialogs over whatever screen launched it, starts tracking when everything is in order, and
 * finishes. Also the target of the "tracking needs attention" notification.
 */
public class TrackingSetupActivity extends AppCompatActivity {

    private TrackingPermissionFlow flow;

    public static void launch(Context context) {
        Intent intent = new Intent(context, TrackingSetupActivity.class);
        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        context.startActivity(intent);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final TrackingManager manager = TrackingManager.get(this);

        String user = manager.getPrefs().getUserId();
        if (user == null || user.isEmpty()) {
            finish(); // nobody is signed in: nothing to attribute locations to
            return;
        }

        flow = new TrackingPermissionFlow(this, new TrackingPermissionFlow.Callback() {
            @Override
            public void onReady() {
                TrackingManager.StartResult result = manager.startTracking();
                if (result != TrackingManager.StartResult.STARTED) {
                    Toast.makeText(TrackingSetupActivity.this, R.string.tracking_setup_failed, Toast.LENGTH_LONG).show();
                }
                finish();
            }

            @Override
            public void onBlocked(List<TrackingProblem> remaining) {
                Toast.makeText(TrackingSetupActivity.this, R.string.tracking_setup_failed, Toast.LENGTH_LONG).show();
                finish();
            }
        });
        // On recreation (rotation, process death) the launchers redeliver pending results, which
        // continue the flow by themselves; starting again would stack a second dialog.
        if (savedInstanceState == null) {
            flow.start();
        }
    }
}
