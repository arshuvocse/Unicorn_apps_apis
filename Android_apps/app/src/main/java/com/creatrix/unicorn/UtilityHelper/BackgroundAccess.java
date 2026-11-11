package com.creatrix.unicorn.UtilityHelper;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

/**
 * Things the phone must allow for tracking to keep running in the background:
 * "Allow all the time" location, and (on Xiaomi/Oppo/Vivo/Huawei...) the vendor's Autostart switch.
 */
public final class BackgroundAccess {
    private static final String PREFS = "background_access";
    private static final String KEY_AUTOSTART_ASKED = "autostart_asked";

    private static AlertDialog backgroundDialog;
    private static AlertDialog batteryDialog;

    private BackgroundAccess() {
    }

    /**
     * Non-cancelable dialog that keeps returning until location is set to "Allow all the time".
     * Only meaningful once the normal (foreground) location permission has been granted.
     */
    public static void enforceAllTheTimeLocation(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        if (!LocationAccess.hasForegroundPermission(activity) || LocationAccess.hasBackgroundPermission(activity)) {
            if (backgroundDialog != null && backgroundDialog.isShowing() && LocationAccess.hasBackgroundPermission(activity)) {
                backgroundDialog.dismiss();
            }
            return;
        }
        if (backgroundDialog != null && backgroundDialog.isShowing()) return;

        backgroundDialog = new AlertDialog.Builder(activity)
                .setTitle("Allow location all the time")
                .setMessage("Unicorn must track your location in the background during work. "
                        + "Open Permissions > Location and choose \"Allow all the time\" (and keep Precise on).")
                .setCancelable(false)
                .setPositiveButton("Open settings", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + activity.getPackageName()));
                    activity.startActivity(intent);
                })
                .create();
        backgroundDialog.show();
    }

    /**
     * Non-cancelable dialog until the app is exempt from battery optimisation. Without the exemption
     * Android's Doze mode delays the 5-minute alarm to roughly one per 9 minutes while the phone sits idle.
     */
    public static void enforceBatteryExemption(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        android.os.PowerManager powerManager = (android.os.PowerManager) activity.getSystemService(Context.POWER_SERVICE);
        boolean exempt = powerManager == null || powerManager.isIgnoringBatteryOptimizations(activity.getPackageName());
        if (exempt) {
            if (batteryDialog != null && batteryDialog.isShowing()) batteryDialog.dismiss();
            return;
        }
        if (batteryDialog != null && batteryDialog.isShowing()) return;

        batteryDialog = new AlertDialog.Builder(activity)
                .setTitle("Allow background activity")
                .setMessage("To send your location every 5 minutes, Unicorn must be allowed to run without battery restrictions. "
                        + "Tap Allow on the next screen.")
                .setCancelable(false)
                .setPositiveButton("Continue", (dialog, which) -> {
                    try {
                        activity.startActivity(new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                                Uri.parse("package:" + activity.getPackageName())));
                    } catch (Exception e) {
                        activity.startActivity(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                    }
                })
                .create();
        batteryDialog.show();
    }

    /** Once per install, on phones that kill background apps unless Autostart is on, open that screen. */
    public static void promptAutostartOnce(Activity activity) {
        if (activity.isFinishing() || activity.isDestroyed()) return;
        SharedPreferences prefs = activity.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        if (prefs.getBoolean(KEY_AUTOSTART_ASKED, false)) return;

        Intent target = findAutostartIntent(activity);
        if (target == null) return; // stock-like phone: nothing vendor specific to ask for
        prefs.edit().putBoolean(KEY_AUTOSTART_ASKED, true).apply();

        new AlertDialog.Builder(activity)
                .setTitle("Allow Unicorn to auto-start")
                .setMessage("Your phone can stop Unicorn in the background. Please turn ON Autostart / "
                        + "Run in background for Unicorn on the next screen so tracking never stops.")
                .setCancelable(false)
                .setPositiveButton("Open", (dialog, which) -> {
                    try {
                        activity.startActivity(target);
                    } catch (Exception ignored) {
                    }
                })
                .show();
    }

    private static Intent findAutostartIntent(Context context) {
        String maker = Build.MANUFACTURER == null ? "" : Build.MANUFACTURER.toLowerCase();
        String[][] candidates;
        if (maker.contains("xiaomi") || maker.contains("redmi") || maker.contains("poco")) {
            candidates = new String[][]{{"com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity"}};
        } else if (maker.contains("oppo") || maker.contains("realme") || maker.contains("oneplus")) {
            candidates = new String[][]{
                    {"com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity"},
                    {"com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity"},
                    {"com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity"}};
        } else if (maker.contains("vivo") || maker.contains("iqoo")) {
            candidates = new String[][]{
                    {"com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity"},
                    {"com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity"}};
        } else if (maker.contains("huawei") || maker.contains("honor")) {
            candidates = new String[][]{
                    {"com.huawei.systemmanager", "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"},
                    {"com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity"}};
        } else if (maker.contains("asus")) {
            candidates = new String[][]{{"com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivity"}};
        } else {
            return null;
        }
        for (String[] candidate : candidates) {
            Intent intent = new Intent().setComponent(new ComponentName(candidate[0], candidate[1]));
            if (context.getPackageManager().resolveActivity(intent, 0) != null) {
                return intent;
            }
        }
        return null;
    }
}
