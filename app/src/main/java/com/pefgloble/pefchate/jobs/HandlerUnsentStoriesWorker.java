package com.pefgloble.pefchate.jobs;

import android.content.Context;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.stories.StoryModel;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class HandlerUnsentStoriesWorker extends Worker {

    public static final String TAG = HandlerUnsentStoriesWorker.class.getSimpleName();

    public HandlerUnsentStoriesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
            try {

                List<StoryModel> storyModels = realm.where(StoryModel.class)
                        .equalTo("status", AppConstants.IS_WAITING)
                        .equalTo("userId", PreferenceManager.getInstance().getID(getApplicationContext()))
                        .sort("date", Sort.ASCENDING).findAll();

                AppHelper.LogCat("Job unSentStories unsetn" + storyModels.size());
                if (storyModels.size() > 0) {
                    if (!AppHelper.isServiceRunning(SendSingleStoryToServerWorker.class)) {
                        SendSingleStoryToServerWorker.start(getApplicationContext());
                        try {
                            Thread.sleep(200);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return Result.success();
                    } else {
                        return Result.success();
                    }


                } else {
                    return Result.failure();
                }

            } finally {
                if (!realm.isClosed())
                    realm.close();
            }
        } else {
            return Result.failure();
        }
    }
}