package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import java.io.File;

/**
 * Default {@link TokenProvider}: keeps the access token in EncryptedSharedPreferences (Android Keystore
 * backed). If the keystore is unusable the token is treated as absent (forcing a re-login); it is never
 * written in plain text. Subclass and override {@link #refreshAccessToken()} to support token refresh.
 */
public class SecureTokenStore implements TokenProvider {
    private static final String TAG = "SecureTokenStore";
    private static final String FILE = "tracking_secure";
    private static final String KEY_TOKEN = "access_token";

    private final Context app;
    private SharedPreferences prefs;

    public SecureTokenStore(Context context) {
        this.app = context.getApplicationContext();
    }

    public synchronized void saveAccessToken(@Nullable String token) {
        SharedPreferences p = prefs();
        if (p != null) {
            p.edit().putString(KEY_TOKEN, token).commit();
        }
    }

    public synchronized void clear() {
        SharedPreferences p = prefs();
        if (p != null) {
            p.edit().clear().commit();
        }
    }

    @Nullable
    @Override
    public synchronized String getAccessToken() {
        SharedPreferences p = prefs();
        return p == null ? null : p.getString(KEY_TOKEN, null);
    }

    @Override
    public boolean refreshAccessToken() {
        return false;
    }

    // Context.deleteSharedPreferences needs API 24; minSdk is 23.
    private void deletePrefsFile() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            app.deleteSharedPreferences(FILE);
        } else {
            new File(app.getApplicationInfo().dataDir, "shared_prefs/" + FILE + ".xml").delete();
        }
    }

    @Nullable
    private SharedPreferences prefs() {
        if (prefs != null) {
            return prefs;
        }
        for (int attempt = 0; attempt < 2; attempt++) {
            try {
                MasterKey key = new MasterKey.Builder(app)
                        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                        .build();
                prefs = EncryptedSharedPreferences.create(
                        app, FILE, key,
                        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM);
                return prefs;
            } catch (Exception e) {
                TrackLog.e(TAG, "Encrypted storage unavailable (attempt " + (attempt + 1) + ")", e);
                // A corrupted keystore entry is the usual cause: drop the file and try once more.
                deletePrefsFile();
            }
        }
        return null;
    }
}
