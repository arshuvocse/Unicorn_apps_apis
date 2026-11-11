package com.creatrix.unicorn.tracking;

import android.Manifest;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.UserManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * What this app can enforce when the organisation has made it the Device Owner of a company-owned,
 * fully managed device. Every call is a no-op on a device where the app is not the device owner, and
 * every individual policy is attempted separately, so a policy a given Android version refuses does not
 * prevent the others.
 *
 * <p>This uses only public DevicePolicyManager APIs that Android provides for device owners. It does
 * not bypass any restriction: on Android 12+ Android may itself refuse to let an admin auto-grant
 * sensor permissions such as location; in that case the employee is still asked through the normal
 * permission flow.
 */
public final class TrackingDevicePolicy {
    private static final String TAG = "TrackingDevicePolicy";

    private TrackingDevicePolicy() {
    }

    public static boolean isDeviceOwner(Context context) {
        DevicePolicyManager dpm = dpm(context);
        return dpm != null && dpm.isDeviceOwnerApp(context.getPackageName());
    }

    /** Applies the policy if this app is the device owner and the config asks for enforcement. */
    public static void apply(Context context) {
        TrackingConfig config = TrackingPrefs.get(context).getConfig();
        if (!config.enforceDevicePolicy) {
            return;
        }
        DevicePolicyManager dpm = dpm(context);
        if (dpm == null || !dpm.isDeviceOwnerApp(context.getPackageName())) {
            return;
        }
        ComponentName admin = admin(context);
        String pkg = context.getPackageName();

        // 1. Pre-grant the runtime permissions tracking needs (Android may decline sensor permissions).
        for (String permission : permissionsToGrant()) {
            try {
                boolean ok = dpm.setPermissionGrantState(admin, pkg, permission,
                        DevicePolicyManager.PERMISSION_GRANT_STATE_GRANTED);
                if (!ok) {
                    TrackLog.i(TAG, "Android did not allow granting " + permission + "; employee will be asked");
                }
            } catch (RuntimeException e) {
                TrackLog.w(TAG, "Grant failed for " + permission, e);
            }
        }

        // 2. Keep Location Services on, and stop the user turning them off (Android 9+/11+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            attempt("setLocationEnabled", () -> dpm.setLocationEnabled(admin, true));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attempt("DISALLOW_CONFIG_LOCATION",
                    () -> dpm.addUserRestriction(admin, UserManager.DISALLOW_CONFIG_LOCATION));
        }

        // 3. Stop the user force-stopping or clearing the data of the tracking app (Android 11+).
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            attempt("setUserControlDisabledPackages",
                    () -> dpm.setUserControlDisabledPackages(admin, Collections.singletonList(pkg)));
        }
        TrackLog.i(TAG, "Device-owner policy applied");
    }

    /** Lifts what {@link #apply} imposed. Called when tracking is stopped by an allowed user/policy stop. */
    public static void release(Context context) {
        DevicePolicyManager dpm = dpm(context);
        if (dpm == null || !dpm.isDeviceOwnerApp(context.getPackageName())) {
            return;
        }
        final ComponentName admin = admin(context);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            attempt("clear DISALLOW_CONFIG_LOCATION",
                    () -> dpm.clearUserRestriction(admin, UserManager.DISALLOW_CONFIG_LOCATION));
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            attempt("clear setUserControlDisabledPackages",
                    () -> dpm.setUserControlDisabledPackages(admin, Collections.<String>emptyList()));
        }
    }

    private static List<String> permissionsToGrant() {
        List<String> permissions = new ArrayList<>();
        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            permissions.add(Manifest.permission.ACCESS_BACKGROUND_LOCATION);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS);
        }
        return permissions;
    }

    private static void attempt(String what, Runnable action) {
        try {
            action.run();
        } catch (RuntimeException e) {
            TrackLog.w(TAG, what + " refused", e);
        }
    }

    private static DevicePolicyManager dpm(Context context) {
        return (DevicePolicyManager) context.getSystemService(Context.DEVICE_POLICY_SERVICE);
    }

    private static ComponentName admin(Context context) {
        return new ComponentName(context, TrackingDeviceAdminReceiver.class);
    }
}
