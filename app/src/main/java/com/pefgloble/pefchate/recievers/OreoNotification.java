package com.pefgloble.pefchate.recievers;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Build;


import com.pefgloble.pefchate.R;

import androidx.core.app.NotificationCompat;

public class OreoNotification extends ContextWrapper {
    private static final String CHANNEL_ID="Fcm Test";
    private static final String CHANNEL_NAME="Fcm Test";
    private NotificationManager notificationManager;
    public OreoNotification(Context base) {
        super(base);

        if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
            createChannel();
        }
    }
     @TargetApi(Build.VERSION_CODES.O)
    private void createChannel() {
         NotificationChannel channel = new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
         channel.setDescription("Fcm Test Channel.");
         channel.enableLights(true);
         channel.enableVibration(true);
         channel.setShowBadge(false);
         channel.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
         getManager().createNotificationChannel(channel);
    }

    public NotificationManager getManager() {
    if (notificationManager == null){
        notificationManager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
    return notificationManager;
    }
    @TargetApi(Build.VERSION_CODES.O)
    public Notification.Builder getOreoNotification(String title, String body,String icon,PendingIntent intent){
        return new Notification.Builder(getApplicationContext(),CHANNEL_ID)
                .setAutoCancel(true)
                .setBadgeIconType(NotificationCompat.BADGE_ICON_SMALL)
                .setDefaults(Notification.DEFAULT_ALL)
                .setWhen(System.currentTimeMillis())
                .setSmallIcon(R.drawable.logo)
                .setTicker("Fcm Test")
                .setNumber(10)
                .setContentIntent(intent)
                .setContentTitle(title)
                .setContentText(body)
                .setContentInfo("info");
    }
}
