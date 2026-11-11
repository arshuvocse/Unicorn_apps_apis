package com.creatrix.unicorn.tracking;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.creatrix.unicorn.R;

import java.util.List;

/** Notification channels and the two notifications the tracking module shows. */
public final class TrackingNotifications {

    public static final String CHANNEL_STATUS = "tracking_status";
    public static final String CHANNEL_ALERT = "tracking_alert";
    public static final int ID_STATUS = 7101;
    public static final int ID_ALERT = 7102;

    private TrackingNotifications() {
    }

    /** Idempotent. Must run before the first notification is posted (Android 8+). */
    public static void ensureChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (nm == null) {
            return;
        }
        // LOW: the persistent notification is visible but never makes a sound or pops up.
        NotificationChannel status = new NotificationChannel(CHANNEL_STATUS,
                context.getString(R.string.tracking_channel_status_name), NotificationManager.IMPORTANCE_LOW);
        status.setDescription(context.getString(R.string.tracking_channel_status_desc));
        status.setShowBadge(false);
        status.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
        nm.createNotificationChannel(status);

        NotificationChannel alert = new NotificationChannel(CHANNEL_ALERT,
                context.getString(R.string.tracking_channel_alert_name), NotificationManager.IMPORTANCE_HIGH);
        alert.setDescription(context.getString(R.string.tracking_channel_alert_desc));
        nm.createNotificationChannel(alert);
    }

    /** The persistent foreground-service notification. */
    public static Notification buildStatus(Context context, String text, boolean withStopAction) {
        ensureChannels(context);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_STATUS)
                .setSmallIcon(R.drawable.ic_tracking_notification)
                .setContentTitle(context.getString(R.string.tracking_notif_title))
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setShowWhen(false)
                .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
                .setContentIntent(openAppIntent(context));
        if (withStopAction) {
            Intent stop = new Intent(context, LocationTrackingService.class)
                    .setAction(LocationTrackingService.ACTION_STOP);
            PendingIntent stopIntent = PendingIntent.getService(context, 1, stop,
                    PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
            builder.addAction(0, context.getString(R.string.tracking_notif_action_stop), stopIntent);
        }
        return builder.build();
    }

    /** "Action required": opens the setup screen, which walks through the permission/settings screens. */
    public static void showProblems(Context context, List<TrackingProblem> problems) {
        StringBuilder text = new StringBuilder();
        for (TrackingProblem problem : problems) {
            if (text.length() > 0) {
                text.append('\n');
            }
            text.append(context.getString(problem.getMessageRes()));
        }
        postAlert(context, context.getString(R.string.tracking_alert_title), text.toString());
    }

    /** Shown when Android refused to restart the service in the background. */
    public static void showResumeNeeded(Context context) {
        postAlert(context, context.getString(R.string.tracking_alert_resume_title),
                context.getString(R.string.tracking_alert_resume_text));
    }

    public static void cancelAlert(Context context) {
        NotificationManagerCompat.from(context).cancel(ID_ALERT);
    }

    private static void postAlert(Context context, String title, String text) {
        ensureChannels(context);
        Intent setup = new Intent(context, TrackingSetupActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent fix = PendingIntent.getActivity(context, 2, setup,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        Notification notification = new NotificationCompat.Builder(context, CHANNEL_ALERT)
                .setSmallIcon(R.drawable.ic_tracking_notification)
                .setContentTitle(title)
                .setContentText(text)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(text))
                .setCategory(NotificationCompat.CATEGORY_ERROR)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true)
                .setContentIntent(fix)
                .addAction(0, context.getString(R.string.tracking_notif_action_fix), fix)
                .build();
        try {
            NotificationManagerCompat.from(context).notify(ID_ALERT, notification);
        } catch (SecurityException e) {
            // POST_NOTIFICATIONS denied on Android 13+; the setup screen still reports it.
            TrackLog.w("Notifications", "Cannot post alert without notification permission", e);
        }
    }

    @Nullable
    private static PendingIntent openAppIntent(Context context) {
        Intent launch = context.getPackageManager().getLaunchIntentForPackage(context.getPackageName());
        if (launch == null) {
            return null;
        }
        return PendingIntent.getActivity(context, 0, launch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
    }
}
