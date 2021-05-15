package com.pefgloble.pefchate.presenter;

import android.annotation.SuppressLint;
import android.util.Log;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersBlockModel;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.jobs.WorkJobsManager;
import com.pefgloble.pefchate.stories.StoriesHeaderModel;
import com.pefgloble.pefchate.stories.StoriesModel;
import com.pefgloble.pefchate.stories.StoryModel;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.socket.client.Ack;
import io.socket.client.Socket;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;


/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class StoriesController {

    private static volatile StoriesController Instance = null;


    public StoriesController() {
    }

    public static StoriesController getInstance() {

        StoriesController localInstance = Instance;
        if (localInstance == null) {
            synchronized (StoriesController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new StoriesController();
                }
            }
        }
        return localInstance;

    }


    private boolean checkIfStoryExist(String storyId, Realm realm) {
        RealmQuery<StoriesModel> query = realm.where(StoriesModel.class).equalTo("_id", storyId);
        return query.count() != 0;

    }


    public void updateStoryStatus(String storyId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            realm.executeTransaction(realm1 -> {


                StoryModel storyModel = realm1.where(StoryModel.class)
                        .equalTo("_id", storyId)
                        .findFirst();
                if (storyModel != null) {
                    storyModel.setStatus(AppConstants.IS_SEEN);
                    storyModel.setDownloaded(true);
                    if (!checkIfStoryDownloadExist()) {
                        StoriesModel storiesModel = realm1.where(StoriesModel.class)
                                .equalTo("_id", storyModel.getUserId())
                                .findFirst();
                        storiesModel.setDownloaded(true);
                        realm1.copyToRealmOrUpdate(storiesModel);
                    }

                    realm1.copyToRealmOrUpdate(storyModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, storyId));
                }


            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no story unRead MessagesPresenter ");
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    private boolean checkIfStoryDownloadExist() {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            RealmQuery<StoryModel> query = realm.where(StoryModel.class)
                    .equalTo("userId", com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                    .equalTo("downloaded", false);
            return query.count() != 0;
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    public static boolean checkIfUserBlockedExist(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<UsersBlockModel> query = realm.where(UsersBlockModel.class).equalTo("usersModel._id", userId);
            count = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        return count != 0;
    }

    /**
     * method to make message as sent
     */
    public void makeStoryAsSent(String storyId, String newStoryId, String oldUserId, String lastTime) {
        updateStatusAsSentBySender(storyId, newStoryId, oldUserId, lastTime);
    }


    /**
     * method to update status for the send message by sender  (as sent message ) in realm  database
     *
     * @param storyId    this is the first parameter for updateStatusAsSentBySender method
     * @param newStoryId
     * @param oldUserId
     * @param lastTime
     */
    private void updateStatusAsSentBySender(String storyId, String newStoryId, String oldUserId, String lastTime) {


        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            realm.executeTransaction(realm1 -> {

                StoryModel storyModel = realm1.where(StoryModel.class)
                        .equalTo("_id", storyId)
                        .equalTo("status", AppConstants.IS_WAITING)
                        .findFirst();

                storyModel.setStatus(AppConstants.IS_SENT);
                storyModel.set_id(newStoryId);
                storyModel.setUserId(oldUserId);
                storyModel.setDate(lastTime);
                StoryModel storyModel1 = realm1.copyFromRealm(storyModel);
                realm1.copyToRealmOrUpdate(storyModel);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, oldUserId));
                JSONObject updateMessage = new JSONObject();


                try {


                    updateMessage.put("message", storyModel1);
                    updateMessage.put("ownerId", oldUserId);


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //emit by socket to other user
                AppHelper.runOnUIThread(() -> {
                    Log.d("SocTag","Send Story Socket");
                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                    if (mSocket != null) {
                        Log.d("SocTag","Story Emit not null");
                        mSocket.emit(AppConstants.SocketConstants.SOCKET_NEW_USER_STORY_TO_SERVER, updateMessage);
                    }

                });
            });

        } catch (Exception e) {
            AppHelper.LogCat(" Is sent messages Realm Error" + e.getMessage());
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }

    /**
     * method to update status as seen by sender (if recipient have been seen the story)  in realm database
     */
    public void updateSeenStatus(String storyId, JSONArray users) {
        //   ArrayList<String> usersList = new ArrayList<String>();

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {


            if (checkIfSingleStoryExist(storyId)) {
                AppHelper.LogCat("Seen storyId " + storyId);
                realm.executeTransaction(realm1 -> {
                    for (int i = 0; i < users.length(); i++) {
                        String recipientId;
                        try {
                            recipientId = users.getString(i);


                            UsersModel usersModel = realm1.where(UsersModel.class).equalTo("_id", recipientId).findFirst();

                            StoryModel storyModel = realm1.where(StoryModel.class).equalTo("_id", storyId)
                                    //.equalTo("status", AppConstants.IS_SENT)
                                    .findFirst();
                            if (storyModel != null) {
                                storyModel.setStatus(AppConstants.IS_SEEN);
                                if (!checkIfSeenUserStoryExist(recipientId) && !recipientId.equals(com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {
                                    AppHelper.LogCat("Seen checkIfSeenUserStoryExist not");
                                    RealmList<UsersModel> usersModels = storyModel.getSeenList();
                                    usersModels.add(usersModel);
                                    storyModel.setSeenList(usersModels);
                                }
                                realm1.copyToRealmOrUpdate(storyModel);
                                AppHelper.LogCat("Seen successfully");


                                JSONObject updateMessage = new JSONObject();
                                try {
                                    updateMessage.put("storyId", storyId);
                                    updateMessage.put("recipientId", recipientId);
                                } catch (JSONException e) {
                                    // e.printStackTrace();
                                }
                                Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                                if (mSocket != null) {
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                                }

                            } else {
                                AppHelper.LogCat("Seen failed ");
                            }
                        } catch (JSONException e) {
                            AppHelper.LogCat("Seen failed " + e.getMessage());
                        }
                    }
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, storyId));

                });
            } else {
                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("storyId", storyId);
                } catch (JSONException e) {
                    // e.printStackTrace();
                }


                Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                if (mSocket != null) {
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_EXIST_AS_FINISHED, updateMessage);
                }
            }
        } finally {

            if (!realm.isClosed())
                realm.close();
        }
    }

    /*for update message status*/


    /**
     * method to save the new stories and mark him as waiting
     */

    public void saveNewUserStory(JSONObject data) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        try {

            //sender object
            String senderId = data.getJSONObject("owner").getString("_id");
            String sender_phone = data.getJSONObject("owner").getString("phone");
            String sender_image = data.getJSONObject("owner").getString("image");
            //story object
            String storyId = data.getString("_id");
            String storyBody = data.getString("body");
            String created = UtilsTime.getCorrectDate(data.getString("created")).toString();
            String storyOwnerId = data.getString("storyOwnerId");
            String file = data.getString("file");
            String file_type = data.getString("file_type");
            String duration = data.getString("duration_file");


            if (senderId.equals(com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (UtilsPhone.checkIfContactExist(WhatsCloneApplication.getInstance(), sender_phone)) {
                if (!StoriesController.getInstance().checkIfStoryExist(storyOwnerId, realm)) {

                    if (!checkIfSingleStoryExist(storyId)) {//avoid duplicate stories
                        realm.executeTransaction(realm1 -> {


                            StoryModel storyModel = realm1.createObject(StoryModel.class, UniqueId.generateUniqueId());
                            storyModel.set_id(storyId);
                            storyModel.setUserId(storyOwnerId);
                            storyModel.setDate(created);
                            storyModel.setDownloaded(false);
                            storyModel.setUploaded(true);
                            storyModel.setDeleted(false);
                            storyModel.setStatus(AppConstants.IS_WAITING);
                            storyModel.setBody(storyBody);
                            storyModel.setFile(file);
                            storyModel.setType(file_type);
                            storyModel.setDuration(duration);


                            RealmList<StoryModel> stories = new RealmList<>();
                            stories.add(storyModel);
                            StoriesModel storiesModel = realm1.createObject(StoriesModel.class, UniqueId.generateUniqueId());
                            storiesModel.set_id(storyOwnerId);


                            String name = UtilsPhone.getContactName(sender_phone);
                            if (name != null) {
                                storiesModel.setUsername(name);
                            } else {
                                storiesModel.setUsername(sender_phone);
                            }
                            if (sender_image != null)
                                storiesModel.setUserImage(sender_image);
                            storiesModel.setDownloaded(false);
                            storiesModel.setPreview(file);
                            storiesModel.setStories(stories);
                            realm1.copyToRealmOrUpdate(storiesModel);
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_NEW_ROW, storyId));
                        });

                    } else {

                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("storyId", storyId);
                            updateMessage.put("ownerId", senderId);
                            updateMessage.put("recipientId", com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                        } catch (JSONException e) {
                            // e.printStackTrace();
                        }


                        Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                        if (mSocket != null) {

                            mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_SEEN, updateMessage, (Ack) args -> {

                                JSONObject data1 = (JSONObject) args[0];
                                try {
                                    if (data1.getBoolean("success")) {
                                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                                        AppHelper.LogCat("--> duplicate story Recipient mark story as  seen <--");
                                    } else {
                                        AppHelper.LogCat(" duplicate story   seen ");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            });

                        }
                    }
                } else {

                    if (!checkIfSingleStoryExist(storyId)) {//avoid duplicate stories
                        realm.executeTransaction(realm1 -> {
                            try {


                                //  UsersModel storyOwner = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(context)).findFirst();


                                StoryModel storyModel = realm1.createObject(StoryModel.class, UniqueId.generateUniqueId());
                                storyModel.set_id(storyId);
                                storyModel.setUserId(storyOwnerId);
                                storyModel.setDate(created);
                                storyModel.setDownloaded(false);
                                storyModel.setUploaded(true);
                                storyModel.setDeleted(false);
                                storyModel.setStatus(AppConstants.IS_WAITING);
                                storyModel.setBody(storyBody);
                                storyModel.setFile(file);
                                storyModel.setType(file_type);
                                storyModel.setDuration(duration);


                                StoriesModel storiesModel;
                                RealmQuery<StoriesModel> storiesModelRealmQuery = realm1.where(StoriesModel.class).equalTo("_id", storyOwnerId);
                                storiesModel = storiesModelRealmQuery.findAll().first();
                                storiesModel.set_id(storyOwnerId);
                                String name = UtilsPhone.getContactName(sender_phone);
                                if (name != null) {
                                    storiesModel.setUsername(name);
                                } else {
                                    storiesModel.setUsername(sender_phone);
                                }
                                if (sender_image != null)
                                    storiesModel.setUserImage(sender_image);
                                storiesModel.setDownloaded(false);
                                storiesModel.setPreview(file);
                                RealmList<StoryModel> stories = storiesModel.getStories();
                                stories.add(storyModel);
                                storiesModel.setStories(stories);
                                realm1.copyToRealmOrUpdate(storiesModel);


                            } catch (Exception e) {
                                AppHelper.LogCat("Exception  last id  " + e.getMessage());
                            }


                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_OLD_ROW, storyId));
                        });

                    } else {
                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("storyId", storyId);
                            updateMessage.put("ownerId", senderId);
                            updateMessage.put("recipientId", com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                        } catch (JSONException e) {
                            // e.printStackTrace();
                        }


                        Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                        if (mSocket != null) {

                            mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_SEEN, updateMessage, (Ack) args -> {

                                JSONObject data1 = (JSONObject) args[0];
                                try {
                                    if (data1.getBoolean("success")) {
                                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                                        AppHelper.LogCat("--> duplicate story Recipient mark story as  seen <--");
                                    } else {
                                        AppHelper.LogCat(" duplicate story   seen ");
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }

                            });

                        }
                    }

                }


            } else {


                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("storyId", storyId);
                    updateMessage.put("recipientId", com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                } catch (JSONException e) {
                    // e.printStackTrace();
                }
                Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                if (mSocket != null) {
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_FINISHED, updateMessage);
                }
            }


        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception MainService" + e.getMessage());
        }
        if (!realm.isClosed())
            realm.close();
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
    }


    public boolean checkIfSingleStoryExist(String storyId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long size;
        try {
            RealmQuery<StoryModel> query = realm.where(StoryModel.class)
                    .equalTo("_id", storyId)
                    .equalTo("deleted", false);
            size = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        AppHelper.LogCat("size " + size);
        return size != 0;
    }

    private boolean checkIfSeenUserStoryExist(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long size;
        try {
            RealmQuery<StoryModel> query = realm.where(StoryModel.class).equalTo("seenList._id", userId);
            size = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        AppHelper.LogCat("size " + size);
        return size != 0;
    }

    public static boolean checkIfSingleStoryWaitingExist(String storyId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long size;
        try {
            RealmQuery<StoryModel> query = realm.where(StoryModel.class).equalTo("_id", storyId).equalTo("status", AppConstants.IS_WAITING);
            size = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        AppHelper.LogCat("size waiting " + size);
        return size != 0;
    }


    public RealmList<StoryModel> getStoriesById(String ownerId) {


        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            return realm.where(StoriesModel.class).equalTo("_id", ownerId).findFirst().getStories();
        } catch (Exception e) {
            return null;
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public RealmList<StoryModel> getStoriesHeaderById(String ownerId) {


        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            return realm.where(StoriesHeaderModel.class).equalTo("_id", ownerId).findFirst().getStories();
        } catch (Exception e) {
            return null;
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public StoryModel getStoryById(String storyId) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            return realm.where(StoryModel.class).equalTo("_id", storyId).findFirst();
        } catch (Exception e) {
            return null;
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public void deleteExpiredStory(String storyId, String ownerId) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            if (StoriesController.getInstance().checkIfSingleStoryExist(storyId)) {
                WorkJobsManager.getInstance().sendDeletedStoryToServer();
                AppHelper.LogCat("mark story as deleted successfully  ");
            } else {

                if (ownerId.equals(com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))) {

                    APIHelper.initialApiUsersContacts().deleteStory(storyId).subscribe(statusResponse -> {
                        if (statusResponse.isSuccess()) {

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

                        } else {

                            AppHelper.LogCat("delete story failed  " + statusResponse.getMessage());

                        }


                    }, throwable -> {

                        AppHelper.LogCat("delete story failed  " + throwable.getMessage());
                    });
                } else {

                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("storyId", storyId);
                        updateMessage.put("recipientId", com.pefgloble.pefchate.helpers.PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }
                    if (mSocket != null) {
                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_STORY_AS_EXPIRED, updateMessage);
                    }
                }
            }

        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }
}