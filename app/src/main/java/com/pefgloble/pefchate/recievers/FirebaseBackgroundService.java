package com.pefgloble.pefchate.recievers;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.google.android.gms.stats.GCoreWakefulBroadcastReceiver;
import com.google.firebase.messaging.RemoteMessage;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ForegroundRuning;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;


import java.util.HashMap;
import java.util.Map;

/**
 * Created by Abderrahim El imame on 2/1/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class FirebaseBackgroundService extends GCoreWakefulBroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {

        AppHelper.LogCat("fcm On Recieve Called");
        try {
            SocketConnectionManager.getInstance().checkSocketConnection();
        } catch (Exception e) {
            AppHelper.LogCat("onMessageReceived Exception " + e.getMessage());
        }
    }
}