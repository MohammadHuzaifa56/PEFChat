package com.pefgloble.pefchate.jobs;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.files.UsersPrivacyModel;
import com.pefgloble.pefchate.presenter.StoriesController;
import com.pefgloble.pefchate.stories.CreateStoryModel;
import com.pefgloble.pefchate.stories.StoryModel;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.JobIntentService;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SendSingleStoryToServerWorker extends JobIntentService {


    private CompositeDisposable compositeDisposable;

    private int mPendingStories = 0;


    public static void start(Context context) {
        Intent starter = new Intent(context, SendSingleStoryToServerWorker.class);
        SendSingleStoryToServerWorker.enqueueWork(context, starter);
    }


    /**
     * Unique job ID for this service.
     */
    public static final int TAG = 1122122234;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    private static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, SendSingleStoryToServerWorker.class, TAG, intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        AppHelper.LogCat("  has Created");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            compositeDisposable = new CompositeDisposable();
            unSentStories();
        } else {
            stopSelf();
        }
    }

    private void unSentStories() {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
        try {
            List<StoryModel> storyModels = realm.where(StoryModel.class)
                    .equalTo("status", AppConstants.IS_WAITING)
                    .equalTo("userId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                    .sort("date", Sort.ASCENDING).findAll();


            mPendingStories = storyModels.size();
            AppHelper.LogCat("Job unSentStories: " + mPendingStories);
            if (storyModels.size() != 0) {

                for (StoryModel storyModel : storyModels) {

                    String lastTime = AppHelper.getCurrentTime();
                    CreateStoryModel createStoryModel = new CreateStoryModel();
                    createStoryModel.setStoryId(storyModel.get_id());
                    createStoryModel.setBody(storyModel.getBody());
                    createStoryModel.setCreated(lastTime);
                    createStoryModel.setDuration(storyModel.getDuration());
                    createStoryModel.setFile(storyModel.getFile());
                    createStoryModel.setType(storyModel.getType());
                    List<UsersPrivacyModel> usersPrivacyModels = realm.where(UsersPrivacyModel.class).findAll();
                    int arraySize = usersPrivacyModels.size();
                    List<String> ids = new ArrayList<>();
                    if (arraySize == 0) {

                        List<UsersModel> usersModels = realm.where(UsersModel.class)
                                .notEqualTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                                .equalTo("exist", true)
                                .equalTo("linked", true)
                                .equalTo("activate", true).findAll();
                        arraySize = usersModels.size();
                        if (arraySize == 0) return;

                        for (int x = 0; x <= arraySize - 1; x++) {
                            if (!usersModels.get(x).get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                                ids.add(usersModels.get(x).get_id());
                        }
                        AppHelper.LogCat("ids "+ids.size());
                    } else {
                        List<UsersModel> usersModels = realm.where(UsersModel.class)
                                .notEqualTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                                .equalTo("exist", true)
                                .equalTo("linked", true)
                                .equalTo("activate", true).findAll();
                        arraySize = usersModels.size();
                        for (int x = 0; x <= arraySize - 1; x++) {
                            if (!usersModels.get(x).get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                                ids.add(usersModels.get(x).get_id());
                        }
                        AppHelper.LogCat("ids "+ids.size());
                    }
                    createStoryModel.setIds(ids);

                    AppHelper.LogCat("Here is the problem5h " + ids);
                    if (storyModel.isUploaded()) {
                        compositeDisposable.add(APIHelper.initialApiUsersContacts().createStory(createStoryModel).subscribe(response -> {
                            AppHelper.LogCat("Here is the problem5h " + response.getMessage());
                            if (response.isSuccess()) {
                                StoriesController.getInstance().makeStoryAsSent(createStoryModel.getStoryId(), response.getStoryId(), PreferenceManager.getInstance().getID(this), lastTime);
                                mPendingStories--;
                                checkCompletion();
                            } else {
                                checkCompletion();
                            }
                        }, throwable -> {
                            AppHelper.LogCat("Here is the problem5 throwable" + throwable.getMessage());
                            checkCompletion();
                        }));
                    } else {
                        AppHelper.LogCat("Here is the problem6 " + arraySize);
                    }


                }
            } else {
                checkCompletion();
            }
        } catch (Exception e) {
            AppHelper.LogCat("hhh " + e.getMessage());
        } finally {

            if (!realm.isClosed())
                realm.close();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPendingStories = 0;
        if (compositeDisposable != null) compositeDisposable.dispose();

    }

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

        AppHelper.LogCat("onStartJob: " + "jobStarted");
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        return START_STICKY;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    // returns whether an attempt was made to send every stories at least once
    private boolean needsReschedule() {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        RealmResults<StoryModel> storyModels;
        int size;
        try {
            storyModels = realm.where(StoryModel.class)
                    .equalTo("status", AppConstants.IS_WAITING)
                    .equalTo("userId", PreferenceManager.getInstance().getID(AGApplication.getInstance()))
                    .sort("date", Sort.ASCENDING).findAll();
            size = storyModels.size();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }

        return size == 0;
    }


    @Override
    public boolean onStopCurrentWork() {
        //return super.onStopCurrentWork();
        return !needsReschedule();
    }

    /**
     * Decides whether the job can be stopped, and whether it needs to be rescheduled in case of
     * pending stories to send.
     */
    private void checkCompletion() {
        if (needsReschedule()) {
            return;
        }

        //  if any sending is not successful, reschedule job for remaining files
        boolean needsReschedule = (mPendingStories > 0);
        AppHelper.LogCat("checkCompletion files: " + mPendingStories);
        //jobFinished(parameters, needsReschedule);
        if (!needsReschedule)
            stopSelf();
    }
}