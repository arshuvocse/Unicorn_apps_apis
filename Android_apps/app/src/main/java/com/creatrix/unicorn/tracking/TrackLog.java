package com.creatrix.unicorn.tracking;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

/**
 * Logging facade for the tracking module. Debug logs are emitted only in debuggable builds, tokens are
 * never logged, and coordinates are only logged at debug level (they are personal data).
 */
public final class TrackLog {

    /** Optional hook, e.g. forward warnings/errors to Crashlytics or a rolling file. */
    public interface Sink {
        void onLog(int priority, String tag, String message, Throwable error);
    }

    private static final String PREFIX = "Track/";
    private static volatile boolean debuggable;
    private static volatile Sink sink;

    private TrackLog() {
    }

    public static void init(Context context) {
        debuggable = (context.getApplicationInfo().flags & ApplicationInfo.FLAG_DEBUGGABLE) != 0;
    }

    public static void setSink(Sink newSink) {
        sink = newSink;
    }

    public static boolean isDebuggable() {
        return debuggable;
    }

    public static void d(String tag, String message) {
        if (debuggable) {
            emit(Log.DEBUG, tag, message, null);
        }
    }

    public static void i(String tag, String message) {
        emit(Log.INFO, tag, message, null);
    }

    public static void w(String tag, String message) {
        emit(Log.WARN, tag, message, null);
    }

    public static void w(String tag, String message, Throwable error) {
        emit(Log.WARN, tag, message, error);
    }

    public static void e(String tag, String message, Throwable error) {
        emit(Log.ERROR, tag, message, error);
    }

    private static void emit(int priority, String tag, String message, Throwable error) {
        String fullTag = PREFIX + tag;
        switch (priority) {
            case Log.DEBUG:
                Log.d(fullTag, message);
                break;
            case Log.INFO:
                Log.i(fullTag, message);
                break;
            case Log.WARN:
                Log.w(fullTag, message, error);
                break;
            default:
                Log.e(fullTag, message, error);
                break;
        }
        Sink target = sink;
        if (target != null) {
            try {
                target.onLog(priority, fullTag, message, error);
            } catch (RuntimeException ignored) {
                // A broken sink must never break tracking.
            }
        }
    }
}
