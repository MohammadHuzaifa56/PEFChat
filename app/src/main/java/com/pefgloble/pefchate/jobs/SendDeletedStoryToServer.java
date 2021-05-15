package com.pefgloble.pefchate.jobs;

import android.content.Context;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.stories.StoryModel;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;
import io.socket.client.Socket;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_DELETE_STORIES_ITEM;


/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SendDeletedStoryToServer extends Worker {

    public static final String TAG =SendDeletedStoryToServer.class.getSimpleName();


    private CompositeDisposable compositeDisposable;
    private int mPendingMessages = 0;
    private CountDownLatch latch;


    public SendDeletedStoryToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            compositeDisposable = new CompositeDisposable();
            deleteStories();
            try {
                if (latch != null)
                    latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Result.success();

        } else {
            return Result.failure();
        }
    }


    private void deleteStories() {

        Realm realmSize = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            DateTime expire_date = new DateTime();
            // minus a day
            expire_date = expire_date.minusDays(1);

            List<StoryModel> storyModels = realmSize.where(StoryModel.class)
                    .equalTo("deleted", false)
                    .sort("date", Sort.ASCENDING).findAll();

            latch = new CountDownLatch(storyModels.size());
            mPendingMessages = storyModels.size();
            AppHelper.LogCat("Job deleteStories: " + mPendingMessages);


            if (storyModels.size() != 0) {

                for (StoryModel storyModel : storyModels) {

                    if (expire_date.isAfter(UtilsTime.getCorrectDate(storyModel.getDate()))) {

                        String storyId = storyModel.get_id();
                        String ownerId = storyModel.getUserId();
                        if (ownerId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {

                            compositeDisposable.add(APIHelper.initialApiUsersContacts().deleteStory(storyId).subscribe(statusResponse -> {
                                if (statusResponse.isSuccess()) {

                                    AppHelper.runOnUIThread(() -> {
                                        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                                        try {

                                            //set story as deleted
                                            realm.executeTransaction(realm1 -> {
                                                StoryModel storyModel2 = realm1.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                                                storyModel2.setDeleted(true);
                                                realm1.copyToRealmOrUpdate(storyModel2);

                                            });
                                            RealmResults<StoryModel> storyModels1 = realm.where(StoryModel.class)
                                                    .equalTo("userId", ownerId)
                                                    .equalTo("deleted", false)
                                                    .findAll();
                                            if (storyModels1.size() == 0) {
                                                AppHelper.LogCat("stories deleted successfully  ");
                                                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));
                                            } else {
                                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
                                            }


                                            Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                                            JSONObject updateMessage = new JSONObject();
                                            try {
                                                updateMessage.put("storyId", storyId);
                                                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                                            } catch (JSONException e) {
                                                // e.printStackTrace();
                                            }
                                            if (mSocket != null) {
                                                mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                                            }

                                            mPendingMessages--;
                                            checkCompletion();
                                        } finally {

                                            if (!realm.isClosed())
                                                realm.close();
                                        }
                                    });

                          /*      AppHelper.runOnUIThread(() -> {
                                    Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();
                                    try {
                                        realm.executeTransactionAsync(realm1 -> {
                                            StoryModel storyModel1 = realm1.where(StoryModel.class).equalTo("_id", storyId).equalTo("userId", ownerId).findFirst();
                                            storyModel1.deleteFromRealm();
                                        }, () -> {
                                            AppHelper.LogCat("story deleted successfully  ");

                                            RealmResults<StoryModel> storyModels1 = realm.where(StoryModel.class).equalTo("userId", ownerId).findAll();
                                            if (storyModels1.size() == 0) {
                                                realm.executeTransactionAsync(realm1 -> {
                                                    StoriesHeaderModel storiesHeaderModel = realm1.where(StoriesHeaderModel.class).equalTo("_id", ownerId).findFirst();
                                                    storiesHeaderModel.deleteFromRealm();
                                                }, () -> {
                                                    AppHelper.LogCat("stories deleted successfully  ");
                                                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));

                                                }, error -> {
                                                    AppHelper.LogCat("delete stories failed  " + error.getMessage());
                                                    checkCompletion();
                                                });
                                            } else {
                                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
                                            }


                                            Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                                            JSONObject updateMessage = new JSONObject();
                                            try {
                                                updateMessage.put("storyId", storyId);
                                                updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                                            } catch (JSONException e) {
                                                // e.printStackTrace();
                                            }
                                            if (mSocket != null) {
                                                mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                                            }

                                            mPendingMessages--;
                                            checkCompletion();
                                        }, error -> {
                                            checkCompletion();
                                            AppHelper.LogCat("delete story failed  " + error.getMessage());

                                        });

                                    } finally {
                                        if (!realm.isClosed())
                                            realm.close();
                                    }
                                });*/
                                } else {
                                    checkCompletion();
                                    AppHelper.LogCat("delete story failed  " + statusResponse.getMessage());

                                }


                            }, throwable -> {
                                checkCompletion();
                                AppHelper.LogCat("delete story failed  " + throwable.getMessage());
                            }));
                        } else {


                            AppHelper.runOnUIThread(() -> {
                                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                                try {
                                    RealmResults<StoryModel> storyModels1 = realm.where(StoryModel.class)
                                            .equalTo("userId", ownerId)
                                            .equalTo("deleted", false)
                                            .findAll();
                                    if (storyModels1.size() == 0) {
                                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));
                                    } else {
                                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
                                    }
                                    AppHelper.LogCat("stories deleted successfully  ");

                                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                                    JSONObject updateMessage = new JSONObject();
                                    try {
                                        updateMessage.put("storyId", storyId);
                                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                                    } catch (JSONException e) {
                                        // e.printStackTrace();
                                    }
                                    if (mSocket != null) {
                                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                                    }

                                    mPendingMessages--;
                                    checkCompletion();
                                } finally {

                                    if (!realm.isClosed())
                                        realm.close();
                                }
                            });

                /*        AppHelper.runOnUIThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();
                            try {
                                realm.executeTransactionAsync(realm1 -> {
                                    StoryModel storyModel1 = realm1.where(StoryModel.class).equalTo("_id", storyId).equalTo("userId", ownerId).findFirst();
                                    storyModel1.deleteFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("story deleted successfully  ");

                                    RealmResults<StoryModel> storyModels1 = realm.where(StoryModel.class).equalTo("userId", ownerId).findAll();
                                    if (storyModels1.size() == 0) {
                                        realm.executeTransactionAsync(realm1 -> {
                                            StoriesModel storiesModel = realm1.where(StoriesModel.class).equalTo("_id", ownerId).findFirst();
                                            storiesModel.deleteFromRealm();
                                        }, () -> {
                                            AppHelper.LogCat("stories deleted successfully  ");
                                            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, ownerId));

                                        }, error -> {
                                            AppHelper.LogCat("delete stories failed  " + error.getMessage());
                                        });
                                    } else {
                                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, ownerId));
                                    }


                                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                                    JSONObject updateMessage = new JSONObject();
                                    try {
                                        updateMessage.put("storyId", storyId);
                                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                                    } catch (JSONException e) {
                                        // e.printStackTrace();
                                    }
                                    if (mSocket != null) {
                                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                                    }

                                    mPendingMessages--;
                                    checkCompletion();
                                }, error -> {
                                    AppHelper.LogCat("delete story failed  " + error.getMessage());
                                    checkCompletion();
                                });
                            } finally {
                                if (!realm.isClosed())
                                    realm.close();
                            }
                        });*/


                        }


                    }
                }
            } else {
                checkCompletion();
            }
        } finally {
            if (!realmSize.isClosed())
                realmSize.close();
            if (latch != null)
                latch.countDown();
        }
    }

    @Override
    public void onStopped() {
        super.onStopped();

        mPendingMessages = 0;
        if (compositeDisposable != null) compositeDisposable.dispose();
    }

    // returns whether an attempt was made to send every message at least once
    private boolean isComplete() {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        RealmResults<StoryModel> storyModels;
        int size;
        try {
            storyModels = realm.where(StoryModel.class)
                    .equalTo("deleted", true)
                    .sort("date", Sort.ASCENDING).findAll();
            size = storyModels.size();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }

        return size == 0;
    }

    /**
     * Decides whether the job can be stopped, and whether it needs to be rescheduled in case of
     * pending messages to send.
     */
    private void checkCompletion() {
        if (!isComplete()) {
            return;
        }
        //  if any sending is not successful, reschedule job for remaining files
        boolean needsReschedule = (mPendingMessages > 0);
        AppHelper.LogCat("checkCompletion files: " + mPendingMessages);
        if (!needsReschedule)
            WorkManager.getInstance().cancelAllWorkByTag(TAG);
    }
}
