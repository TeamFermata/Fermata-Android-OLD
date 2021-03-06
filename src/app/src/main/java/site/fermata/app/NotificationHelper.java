/*
    Copyright © 2007 Free Software Foundation, Inc. <https://fsf.org/>
    Everyone is permitted to copy and distribute verbatim copies of this license document, but changing it is not allowed.
    Github Repository : https://github.com/TracetogetherKorea/Fermata-Android

    - Notification 클래스 -
 */

package site.fermata.app;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Color;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper class to manage notification channels, and create notifications.
 */
public class NotificationHelper extends ContextWrapper {
    private NotificationManager manager;
    public static final String WAVE_CHANNEL = "default";


    public NotificationHelper(Context mContext) {
        super(mContext);
        NotificationChannel mChannel = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            mChannel = new NotificationChannel(WAVE_CHANNEL,
                    "Default Channel", NotificationManager.IMPORTANCE_DEFAULT);

            mChannel.setLightColor(Color.GREEN);
            mChannel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            getManager().createNotificationChannel(mChannel);
        }
    }

    public NotificationCompat.Builder getNotification(String title, String body, int progress) {
        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), WAVE_CHANNEL);
        mBuilder.setSmallIcon(getSmallIcon());
        mBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        mBuilder.setContentTitle(title)
                .setContentText(body)
                .setOngoing(true)
                //.setContentIntent(resultPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT);
        mBuilder.setVibrate(new long[]{0L});
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
        mBuilder.setProgress(100, progress, false);
        if (progress == 100) {
            mBuilder.setProgress(0, 0, false);
            mBuilder.setContentText(body);
        }
        return mBuilder;
    }


    public NotificationCompat.Builder getNotification(String title, String body, PendingIntent resultPendingIntent) {
        NotificationCompat.Builder mBuilder;
        mBuilder = new NotificationCompat.Builder(getApplicationContext(), WAVE_CHANNEL);
        mBuilder.setSmallIcon(getSmallIcon());
        mBuilder.setColor(ContextCompat.getColor(getApplicationContext(), R.color.colorAccent));
        mBuilder.setContentTitle(title)
                .setContentText(body)
                .setContentIntent(resultPendingIntent)
                .setDefaults(NotificationCompat.DEFAULT_ALL)
                .setPriority(NotificationCompat.PRIORITY_HIGH);
        mBuilder.setVibrate(new long[]{0L});
        mBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC);

        return mBuilder;
    }

    /**
     * Send a notification.
     *
     * @param id           The ID of the notification
     * @param notification The notification object
     */
    public void notify(int id, NotificationCompat.Builder notification) {
        getManager().notify(id, notification.build());
    }

    /**
     * Get the small icon for this app
     *
     * @return The small icon resource id
     */
    private int getSmallIcon() {
        return R.drawable.ic_stat_name;
    }

    /**
     * Get the notification manager.
     * <p>
     * Utility method as this helper works with it a lot.
     *
     * @return The system service NotificationManager
     */
    private NotificationManager getManager() {
        if (manager == null) {
            manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        }
        return manager;
    }

    public void cancelNotification(int notificationId) {
        getManager().cancel(notificationId);
    }
}
