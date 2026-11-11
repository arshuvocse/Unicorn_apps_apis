package com.creatrix.unicorn.tracking;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.IntentSenderRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.StringRes;
import androidx.appcompat.app.AlertDialog;

import com.creatrix.unicorn.R;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Walks the employee through everything tracking needs, one Android screen at a time, in a
 * privacy-respecting order: consent first, then permissions, then system settings. It only ever uses
 * the standard Android permission dialogs and settings screens; when a permission was denied "for
 * good" the only path is the app's settings page, where the employee changes it themselves.
 *
 * <p>Create it in Activity.onCreate (the result launchers must be registered before the activity is
 * STARTED), then call {@link #start()}.
 */
public final class TrackingPermissionFlow {

    public interface Callback {
        /** No blocking problem is left; it is safe to call {@link TrackingManager#startTracking()}. */
        void onReady();

        /** The employee declined, or a problem could not be resolved. */
        void onBlocked(List<TrackingProblem> remaining);
    }

    private final ComponentActivity activity;
    private final Callback callback;
    private final TrackingManager manager;

    private final ActivityResultLauncher<String[]> foregroundLocationLauncher;
    private final ActivityResultLauncher<String> backgroundLocationLauncher;
    private final ActivityResultLauncher<String> notificationLauncher;
    private final ActivityResultLauncher<Intent> settingsLauncher;
    private final ActivityResultLauncher<IntentSenderRequest> resolutionLauncher;

    private final Set<TrackingProblem> requested = EnumSet.noneOf(TrackingProblem.class);
    private final Set<TrackingProblem> sentToSettings = EnumSet.noneOf(TrackingProblem.class);
    private boolean batteryOffered;

    public TrackingPermissionFlow(ComponentActivity activity, Callback callback) {
        this.activity = activity;
        this.callback = callback;
        this.manager = TrackingManager.get(activity);

        foregroundLocationLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestMultiplePermissions(), (Map<String, Boolean> result) -> next());
        backgroundLocationLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> next());
        notificationLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.RequestPermission(), granted -> next());
        settingsLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(), result -> next());
        resolutionLauncher = activity.registerForActivityResult(
                new ActivityResultContracts.StartIntentSenderForResult(), result -> next());
    }

    public void start() {
        requested.clear();
        sentToSettings.clear();
        batteryOffered = false;
        next();
    }

    // ------------------------------------------------------------------ state machine

    private void next() {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;
        }
        List<TrackingProblem> problems = manager.getProblems();
        TrackingProblem problem = firstToResolve(problems);
        if (problem == null) {
            callback.onReady();
            return;
        }
        if (sentToSettings.contains(problem)) {
            // The employee was already sent to the right settings screen and it is still unresolved.
            callback.onBlocked(problems);
            return;
        }

        switch (problem) {
            case CONSENT_MISSING:
                askConsent();
                break;
            case LOCATION_PERMISSION_MISSING:
            case PRECISE_LOCATION_MISSING:
                if (requested.add(problem)) {
                    explain(R.string.tracking_setup_location_title, R.string.tracking_setup_location_message,
                            () -> foregroundLocationLauncher.launch(new String[]{
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.ACCESS_COARSE_LOCATION}));
                } else {
                    openAppSettings(problem, appDetailsIntent());
                }
                break;
            case NOTIFICATION_PERMISSION_MISSING:
                if (requested.add(problem) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    notificationLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
                } else {
                    openAppSettings(problem, notificationSettingsIntent());
                }
                break;
            case BACKGROUND_LOCATION_MISSING:
                if (requested.add(problem) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    explain(R.string.tracking_setup_background_title, R.string.tracking_setup_background_message,
                            () -> backgroundLocationLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION));
                } else {
                    openAppSettings(problem, appDetailsIntent());
                }
                break;
            case LOCATION_SERVICES_OFF:
                if (requested.add(problem)) {
                    requestLocationServices();
                } else {
                    openAppSettings(problem, new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                }
                break;
            case BATTERY_OPTIMIZED:
                batteryOffered = true;
                askBatteryExemption();
                break;
            default:
                callback.onBlocked(problems);
                break;
        }
    }

    /** The first blocking problem, or, once none is left, the advisory battery one (offered once). */
    private TrackingProblem firstToResolve(List<TrackingProblem> problems) {
        for (TrackingProblem problem : problems) {
            if (problem.isBlocking()) {
                return problem;
            }
        }
        if (!batteryOffered && problems.contains(TrackingProblem.BATTERY_OPTIMIZED)) {
            return TrackingProblem.BATTERY_OPTIMIZED;
        }
        return null;
    }

    // ------------------------------------------------------------------ individual steps

    private void askConsent() {
        int minutes = Math.max(1, (int) (manager.getPrefs().getConfig().intervalMs / 60_000L));
        new AlertDialog.Builder(activity)
                .setTitle(R.string.tracking_setup_consent_title)
                .setMessage(activity.getString(R.string.tracking_setup_consent_message, minutes))
                .setCancelable(false)
                .setPositiveButton(R.string.tracking_setup_accept, (d, w) -> {
                    manager.setConsent(true);
                    next();
                })
                .setNegativeButton(R.string.tracking_setup_decline, (d, w) -> callback.onBlocked(manager.getProblems()))
                .show();
    }

    private void explain(@StringRes int title, @StringRes int message, Runnable onContinue) {
        new AlertDialog.Builder(activity)
                .setTitle(title)
                .setMessage(message)
                .setCancelable(false)
                .setPositiveButton(R.string.tracking_setup_continue, (d, w) -> onContinue.run())
                .setNegativeButton(R.string.tracking_setup_decline, (d, w) -> callback.onBlocked(manager.getProblems()))
                .show();
    }

    /** Shows the standard "turn on location" system dialog, or the Location settings screen if that fails. */
    private void requestLocationServices() {
        LocationSettingsRequest request = new LocationSettingsRequest.Builder()
                .addLocationRequest(LocationHelper.buildRequest(manager.getPrefs().getConfig()))
                .setAlwaysShow(true)
                .build();
        LocationServices.getSettingsClient(activity).checkLocationSettings(request)
                .addOnSuccessListener(response -> next())
                .addOnFailureListener(error -> {
                    if (error instanceof ResolvableApiException) {
                        try {
                            resolutionLauncher.launch(new IntentSenderRequest.Builder(
                                    ((ResolvableApiException) error).getResolution()).build());
                            return;
                        } catch (RuntimeException ignored) {
                            // Fall through to the settings screen.
                        }
                    }
                    openAppSettings(TrackingProblem.LOCATION_SERVICES_OFF,
                            new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                });
    }

    private void askBatteryExemption() {
        new AlertDialog.Builder(activity)
                .setTitle(R.string.tracking_setup_battery_title)
                .setMessage(R.string.tracking_setup_battery_message)
                .setCancelable(false)
                .setPositiveButton(R.string.tracking_setup_continue, (d, w) -> {
                    Intent request = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                            Uri.parse("package:" + activity.getPackageName()));
                    try {
                        settingsLauncher.launch(request);
                    } catch (ActivityNotFoundException e) {
                        try {
                            settingsLauncher.launch(new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS));
                        } catch (ActivityNotFoundException ignored) {
                            next();
                        }
                    }
                })
                // Advisory only: tracking still works, just less reliably while the device is idle.
                .setNegativeButton(R.string.tracking_setup_skip, (d, w) -> next())
                .show();
    }

    /** Explains, then opens the relevant Android settings screen. The employee must change it there. */
    private void openAppSettings(TrackingProblem problem, Intent settings) {
        sentToSettings.add(problem);
        new AlertDialog.Builder(activity)
                .setTitle(R.string.tracking_setup_settings_title)
                .setMessage(activity.getString(R.string.tracking_setup_settings_message) + "\n\n"
                        + activity.getString(problem.getMessageRes()))
                .setCancelable(false)
                .setPositiveButton(R.string.tracking_setup_open_settings, (d, w) -> {
                    try {
                        settingsLauncher.launch(settings);
                    } catch (ActivityNotFoundException e) {
                        callback.onBlocked(manager.getProblems());
                    }
                })
                .setNegativeButton(R.string.tracking_setup_decline, (d, w) -> callback.onBlocked(manager.getProblems()))
                .show();
    }

    private Intent appDetailsIntent() {
        return new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                Uri.fromParts("package", activity.getPackageName(), null));
    }

    private Intent notificationSettingsIntent() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS)
                    .putExtra(Settings.EXTRA_APP_PACKAGE, activity.getPackageName());
        }
        return appDetailsIntent();
    }
}
