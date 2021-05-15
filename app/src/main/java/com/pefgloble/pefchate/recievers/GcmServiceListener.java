package com.pefgloble.pefchate.recievers;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;

import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ForegroundRuning;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;


import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

/**
 * Created by Abderrahim El imame on 2/1/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class GcmServiceListener extends FirebaseMessagingService {
    private static final String CHANNEL_ID="Fcm Test";
    private static final String CHANNEL_NAME="Fcm Test";
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        AppHelper.LogCat("fcm Message Recieve Called");

        SocketConnectionManager.getInstance().checkSocketConnection();

    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);

    }
}