package com.creatrix.unicorn.tracking;

import androidx.annotation.StringRes;

import com.creatrix.unicorn.R;

/**
 * Something that stops or degrades tracking. Declared in the order the UI should resolve them.
 * Blocking problems must be fixed before tracking can run; advisory ones only reduce reliability.
 */
public enum TrackingProblem {
    CONSENT_MISSING(true, R.string.tracking_problem_consent),
    LOCATION_PERMISSION_MISSING(true, R.string.tracking_problem_location_permission),
    PRECISE_LOCATION_MISSING(true, R.string.tracking_problem_precise_location),
    NOTIFICATION_PERMISSION_MISSING(true, R.string.tracking_problem_notifications),
    BACKGROUND_LOCATION_MISSING(true, R.string.tracking_problem_background_location),
    LOCATION_SERVICES_OFF(true, R.string.tracking_problem_location_services),
    BATTERY_OPTIMIZED(false, R.string.tracking_problem_battery);

    private final boolean blocking;
    @StringRes
    private final int messageRes;

    TrackingProblem(boolean blocking, @StringRes int messageRes) {
        this.blocking = blocking;
        this.messageRes = messageRes;
    }

    public boolean isBlocking() {
        return blocking;
    }

    @StringRes
    public int getMessageRes() {
        return messageRes;
    }
}
