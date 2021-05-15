package com.pefgloble.pefchate.recievers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.jobs.GcmTopicSubscribe;
import com.pefgloble.pefchate.jobs.SendSingleMessageToServerWorker;
import com.pefgloble.pefchate.jobs.SendSingleStoryToServerWorker;
import com.pefgloble.pefchate.util.Util;

import androidx.annotation.RequiresApi;

/**
 * Created by Abderrahim El imame on 2019-05-18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class BootCompleteReceiver extends BroadcastReceiver {
    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onReceive(Context mContext, Intent intent) {

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SendSingleMessageToServerWorker.start(mContext);
            SendSingleStoryToServerWorker.start(mContext);
        }
    }
}
