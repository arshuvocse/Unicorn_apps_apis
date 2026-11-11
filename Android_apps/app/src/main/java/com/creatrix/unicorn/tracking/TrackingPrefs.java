package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;

/**
 * Non-secret tracking state: configuration, the signed-in user id, the employee's consent and whether
 * tracking is currently wanted (survives process death and reboot). Tokens live in {@link SecureTokenStore}.
 */
public final class TrackingPrefs {
    private static final String FILE = "tracking_prefs";
    private static final String K_CONFIG = "config";
    private static final String K_USER = "user_id";
    private static final String K_CONSENT_AT = "consent_at";
    private static final String K_WANTED = "wanted";
    private static final String K_MANAGED = "managed_by_policy";

    private static volatile TrackingPrefs instance;
    private final SharedPreferences sp;

    private TrackingPrefs(Context context) {
        sp = context.getApplicationContext().getSharedPreferences(FILE, Context.MODE_PRIVATE);
    }

    public static TrackingPrefs get(Context context) {
        if (instance == null) {
            synchronized (TrackingPrefs.class) {
                if (instance == null) {
                    instance = new TrackingPrefs(context);
                }
            }
        }
        return instance;
    }

    public TrackingConfig getConfig() {
        TrackingConfig stored = TrackingConfig.fromJson(sp.getString(K_CONFIG, null));
        return stored != null ? stored : TrackingConfig.defaults();
    }

    public void setConfig(TrackingConfig config) {
        sp.edit().putString(K_CONFIG, config.toJson()).commit();
    }

    @Nullable
    public String getUserId() {
        return sp.getString(K_USER, null);
    }

    public void setUserId(@Nullable String userId) {
        sp.edit().putString(K_USER, userId).commit();
    }

    public boolean hasConsent() {
        return sp.getLong(K_CONSENT_AT, 0L) > 0L;
    }

    public void setConsent(boolean granted) {
        sp.edit().putLong(K_CONSENT_AT, granted ? System.currentTimeMillis() : 0L).commit();
    }

    /** The user's (or policy's) intent. Only an explicit stop clears it, never a crash or a kill. */
    public boolean isTrackingWanted() {
        return sp.getBoolean(K_WANTED, false);
    }

    public void setTrackingWanted(boolean wanted) {
        sp.edit().putBoolean(K_WANTED, wanted).commit();
    }

    public boolean isManagedByPolicy() {
        return sp.getBoolean(K_MANAGED, false);
    }

    public void setManagedByPolicy(boolean managed) {
        sp.edit().putBoolean(K_MANAGED, managed).commit();
    }
}
