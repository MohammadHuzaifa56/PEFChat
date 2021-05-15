package com.pefgloble.pefchate.jobs;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.job.JobParameters;
import android.app.job.JobService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.JobIntentService;
import androidx.core.app.NotificationCompat;

import static androidx.core.app.NotificationCompat.PRIORITY_MIN;

/**
 * Created by Abderrahim El imame on 2/1/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class GcmTopicSubscribe extends JobIntentService {

    public static void start(Context context) {
        Intent starter = new Intent(context, SendSingleStoryToServerWorker.class);
        GcmTopicSubscribe.enqueueWork(context, starter);
    }


    /**
     * Unique job ID for this service.
     */
    public static final int TAG = 1122122323;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    private static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, GcmTopicSubscribe.class, TAG, intent);
    }


    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppHelper.LogCat("onCreate: " + "onCreate");
        try {
            if (PreferenceManager.getInstance().getToken(this) == null)
                return;
            String topic = PreferenceManager.getInstance().getID(this);
            if (topic != null) {
                try {

                    FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                        @Override
                        public void onComplete(@NonNull Task<InstanceIdResult> task) {
                            if (task.isSuccessful()){
                             String token=task.getResult().getToken();
                             AppHelper.LogCat("FCM Token "+token);
                            }
                        }
                    });

                    FirebaseMessaging.getInstance().subscribeToTopic(topic)
                            .addOnCompleteListener(task -> AppHelper.LogCat("Subscribed to topic " + topic))
                            .addOnFailureListener(e -> AppHelper.LogCat("Failed to subscribe  to topic " + topic));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}