package com.pefgloble.pefchate.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.calls.CallSaverModel;
import com.pefgloble.pefchate.JsonClasses.calls.CallsInfoModel;
import com.pefgloble.pefchate.JsonClasses.calls.CallsModel;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersBlockModel;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.activities.call.CallAlertActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.call.CallingApi;
import com.pefgloble.pefchate.helpers.files.RealmBackupRestore;
import com.pefgloble.pefchate.helpers.files.UniqueId;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.jobs.WorkJobsManager;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Random;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class CallsController {

    private static volatile CallsController Instance = null;
    private boolean isSaved = false;

    public CallsController() {
    }

    public static CallsController getInstance() {

       CallsController localInstance = Instance;
        if (localInstance == null) {
            synchronized (CallsController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new CallsController();
                }
            }
        }
        return localInstance;

    }


    private boolean checkIfCallExist(String callId, Realm realm) {
        RealmQuery<CallsModel> query = realm.where(CallsModel.class).equalTo("_id", callId);
        return query.count() != 0;

    }


    public void updateSeenCallStatus(String callId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            realm.executeTransaction(realm1 -> {


                CallsInfoModel callsInfoModel1 = realm1.where(CallsInfoModel.class)
                        .equalTo("_id", callId)
                        .findFirst();
                if (callsInfoModel1 != null) {
                    callsInfoModel1.setStatus(AppConstants.IS_SEEN);
                    realm1.copyToRealmOrUpdate(callsInfoModel1);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, callId));
                }


            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no story unRead MessagesPresenter ");
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


    public void sendUserCallToServer(Context context, String callId, String isUpdate) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        try {
            AppHelper.LogCat("Job emit call to. callId : " + callId);
            CallsInfoModel callsInfoModel = realm.where(CallsInfoModel.class).equalTo("_id", callId).findFirst();
            if (callsInfoModel != null) {
                if (callsInfoModel.getStatus() != AppConstants.IS_SEEN) {


                    CallSaverModel callSaverModel = new CallSaverModel();
                    callSaverModel.setFromId(callsInfoModel.getFrom());
                    callSaverModel.setToId(callsInfoModel.getTo());
                    callSaverModel.setDate(callsInfoModel.getDate());
                    callSaverModel.setDuration(callsInfoModel.getDuration());
                    callSaverModel.setIsVideo(callsInfoModel.getType());
                    if (isUpdate.equals("true")) {
                        callSaverModel.setCallId(callId);
                    }
                    callSaverModel.setUpdate(isUpdate);

                    APIHelper.initialApiUsersContacts().saveNewCall(callSaverModel).subscribe(response -> {

                        if (response.isSuccess()) {
                            if (isUpdate.equals("false"))
                                CallsController.getInstance().makeCallAsSent(context, callId, response.getCallId());
                        } else {
                            AppHelper.LogCat("response " + response.getMessage());
                        }
                    }, throwable -> {
                        AppHelper.LogCat("throwable " + throwable.getMessage());
                    });

                } else {

                    AppHelper.LogCat(" failed ");
                }


            } else {
                AppHelper.LogCat("this call is already sent failed ");
            }
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }

    /**
     * method to make call as sent
     */
    public void makeCallAsSent(Context context, String callId, String newCallId) {
        updateStatusAsSentBySender(context, callId, newCallId);
    }


    /**
     * method to update status for the send message by sender  (as sent message ) in realm  database
     *
     * @param callId    this is the first parameter for updateStatusAsSentBySender method
     * @param newCallId
     */
    private void updateStatusAsSentBySender(Context context, String callId, String newCallId) {
        AppHelper.LogCat("heheheh ");

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            realm.executeTransaction(realm1 -> {

                CallsInfoModel callsInfoModel = realm1.where(CallsInfoModel.class)
                        .equalTo("_id", callId)
                        .equalTo("status", AppConstants.IS_WAITING)
                        .findFirst();

                callsInfoModel.setStatus(AppConstants.IS_SENT);
                callsInfoModel.set_id(newCallId);
                CallsInfoModel callsInfoModel1 = realm1.copyFromRealm(callsInfoModel);
                realm1.copyToRealmOrUpdate(callsInfoModel);

                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, newCallId));
                JSONObject updateMessage = new JSONObject();


                UsersModel usersModel = realm1.where(UsersModel.class)
                        .equalTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                        .findFirst();
                UsersModel owner = realm1.copyFromRealm(usersModel);
                String roomId = randomString();
                try {


                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("username",owner.getUsername());
                    jsonObject.put("phone",owner.getPhone());

                    updateMessage.put("callId", newCallId);
                    updateMessage.put("call_from", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    updateMessage.put("call_id", roomId);
                    updateMessage.put("callType", callsInfoModel1.getType());
                    updateMessage.put("status", "init_call");
                    updateMessage.put("date", callsInfoModel1.getDate());
                    updateMessage.put("owner", jsonObject);
                    updateMessage.put("recipientId", callsInfoModel1.getTo());


                } catch (JSONException e) {
                    e.printStackTrace();
                }
                //emit by socket to other user
                AppHelper.runOnUIThread(() -> {

                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                    if (mSocket != null) {

                        if (mSocket.connected()) {

                            CallingApi.startCall(context, callsInfoModel1.getType(), roomId, false, callsInfoModel1.getFrom(), newCallId);
                            PreferenceManager.getInstance().putRoomID(WhatsCloneApplication.getInstance(), roomId);
                            mSocket.emit(AppConstants.SocketConstants.SOCKET_NEW_USER_CALL_TO_SERVER, updateMessage);
                        } else {
                            WhatsCloneApplication.getInstance().startActivity(new Intent(WhatsCloneApplication.getInstance(), CallAlertActivity.class));
                        }

                    }

                });
            });

        } catch (Exception e) {
            AppHelper.LogCat(" Is sent calls Realm Error" + e.getMessage());
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }


    public static String randomString() {
        char[] chars = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 20; i++) {
            sb.append(chars[random.nextInt(chars.length)]);
        }
        sb.append("PnPLabs3Embed");
        return sb.toString();
    }

    /**
     * method to update status as seen by sender (if recipient have been seen the call)  in realm database
     */
    public void updateSeenStatus(String callId, String recipientId) {
        //   ArrayList<String> usersList = new ArrayList<String>();

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {


            if (checkIfSingleCallExist(callId)) {
                AppHelper.LogCat("Seen callId " + callId);
                realm.executeTransaction(realm1 -> {


                    CallsInfoModel callsInfoModel = realm1.where(CallsInfoModel.class).equalTo("_id", callId)
                            .findFirst();
                    if (callsInfoModel != null) {
                        callsInfoModel.setStatus(AppConstants.IS_SEEN);
                        realm1.copyToRealmOrUpdate(callsInfoModel);
                        AppHelper.LogCat("Seen successfully");


                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("callId", callId);
                            updateMessage.put("recipientId", recipientId);
                        } catch (JSONException e) {
                            // e.printStackTrace();
                        }
                        Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                        if (mSocket != null) {
                            mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_AS_FINISHED, updateMessage);
                        }

                    } else {
                        AppHelper.LogCat("Seen failed ");
                    }


                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, callId));

                });
            } else {
                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("callId", callId);
                } catch (JSONException e) {
                    // e.printStackTrace();
                }


                Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                if (mSocket != null) {
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_EXIST_AS_FINISHED, updateMessage);
                }
            }
        } finally {

            if (!realm.isClosed())
                realm.close();
        }
    }

    /*for update message status*/

    public void saveCallToLocalDB(Context context, String recipientId, String type) {
        boolean isVideoCall;
        isVideoCall = type.equals(AppConstants.VIDEO_CALL);
        DateTime current = new DateTime();
        String callTime = String.valueOf(current);
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            String historyCallId = getHistoryCallId(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()), recipientId, isVideoCall, realm);

            if (historyCallId == null) {
                realm.executeTransaction(realm1 -> {
                    UsersModel contactsModel1;

                    contactsModel1 = realm1.where(UsersModel.class).equalTo("_id", recipientId).findFirst();
                    String lastID = RealmBackupRestore.getCallLastId();
                    CallsModel callsModel = realm1.createObject(CallsModel.class, UniqueId.generateUniqueId());
                    callsModel.set_id(lastID);
                    callsModel.setType(type);

                    callsModel.setContactsModel(contactsModel1);
                    callsModel.setPhone(contactsModel1.getPhone());
                    callsModel.setUsername(contactsModel1.getUsername());
                    callsModel.setFrom(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    callsModel.setTo(contactsModel1.get_id());
                    callsModel.setDuration("00:00");
                    callsModel.setCounter(1);
                    callsModel.setDate(callTime);
                    callsModel.setReceived(false);


                    RealmList<CallsInfoModel> callsInfoModelRealmList = new RealmList<CallsInfoModel>();
                    String lastInfoID = RealmBackupRestore.getCallInfoLastId();

                    CallsInfoModel callsInfoModel = realm1.createObject(CallsInfoModel.class, UniqueId.generateUniqueId());
                    callsInfoModel.set_id(lastInfoID);
                    callsInfoModel.setType(type);
                    callsInfoModel.setStatus(AppConstants.IS_WAITING);
                    callsInfoModel.setContactsModel(contactsModel1);

                    callsInfoModel.setPhone(contactsModel1.getPhone());
                    callsInfoModel.setFrom(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    callsInfoModel.setCallId(lastID);
                    callsInfoModel.setTo(contactsModel1.get_id());
                    callsInfoModel.setDuration("00:00");
                    callsInfoModel.setDate(callTime);
                    callsInfoModel.setReceived(false);
                    callsInfoModelRealmList.add(callsInfoModel);
                    callsModel.setCallsInfoModels(callsInfoModelRealmList);
                    realm1.copyToRealmOrUpdate(callsModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, lastID));
                    sendUserCallToServer(context, lastInfoID, "false");
                });
            } else {

                realm.executeTransaction(realm1 -> {
                    UsersModel contactsModel1;
                    contactsModel1 = realm1.where(UsersModel.class).equalTo("_id", recipientId).findFirst();

                    int callCounter;
                    CallsModel callsModel;
                    RealmQuery<CallsModel> callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("_id", historyCallId);
                    callsModel = callsModelRealmQuery.findAll().first();

                    callCounter = callsModel.getCounter();
                    callCounter++;
                    callsModel.setDate(callTime);
                    callsModel.setCounter(callCounter);

                    RealmList<CallsInfoModel> callsInfoModelRealmList = callsModel.getCallsInfoModels();
                    String lastInfoID = RealmBackupRestore.getCallInfoLastId();

                    CallsInfoModel callsInfoModel = realm1.createObject(CallsInfoModel.class, UniqueId.generateUniqueId());
                    callsInfoModel.set_id(lastInfoID);
                    callsInfoModel.setType(type);
                    callsInfoModel.setStatus(AppConstants.IS_WAITING);
                    callsInfoModel.setContactsModel(contactsModel1);
                    callsInfoModel.setPhone(contactsModel1.getPhone());
                    callsInfoModel.setFrom(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    callsInfoModel.setTo(contactsModel1.get_id());
                    callsInfoModel.setCallId(callsModel.get_id());
                    callsInfoModel.setDuration("00:00");
                    callsInfoModel.setDate(callTime);
                    callsInfoModel.setReceived(false);
                    callsInfoModelRealmList.add(callsInfoModel);
                    callsModel.setCallsInfoModels(callsInfoModelRealmList);

                    realm1.copyToRealmOrUpdate(callsModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
                    sendUserCallToServer(context, lastInfoID, "false");
                });
            }

        } finally {

            if (!realm.isClosed())
                realm.close();

        }

    }

    @SuppressLint("CheckResult")
    public void saveToDataBase(String caller_id, String callId, boolean isVideoCall, DateTime date, String phone, String username) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {


            String callTime = String.valueOf(date);

            String historyCallId = getHistoryCallIdReceived(caller_id, PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()), isVideoCall, realm);

            if (historyCallId == null) {
                realm.executeTransaction(realm1 -> {
                    UsersModel contactsModel1 = realm1.where(UsersModel.class).equalTo("_id", caller_id).findFirst();
                    String lastID = RealmBackupRestore.getCallLastId();
                    CallsModel callsModel = realm1.createObject(CallsModel.class, UniqueId.generateUniqueId());
                    callsModel.set_id(lastID);
                    if (isVideoCall)
                        callsModel.setType(AppConstants.VIDEO_CALL);
                    else
                        callsModel.setType(AppConstants.VOICE_CALL);
                    callsModel.setContactsModel(contactsModel1);
                    callsModel.setPhone(phone);
                    callsModel.setUsername(username);
                    callsModel.setCounter(1);
                    callsModel.setFrom(contactsModel1.get_id());
                    callsModel.setTo(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    callsModel.setDuration("00:00");
                    callsModel.setDate(callTime);
                    callsModel.setReceived(true);

                    CallsInfoModel callsInfoModel = realm1.createObject(CallsInfoModel.class, UniqueId.generateUniqueId());
                    RealmList<CallsInfoModel> callsInfoModelRealmList = new RealmList<CallsInfoModel>();
                    //  String lastInfoID = RealmBackupRestore.getCallInfoLastId();
                    callsInfoModel.set_id(callId);
                    if (isVideoCall)
                        callsInfoModel.setType(AppConstants.VIDEO_CALL);
                    else
                        callsInfoModel.setType(AppConstants.VOICE_CALL);
                    callsInfoModel.setContactsModel(contactsModel1);

                    callsInfoModel.setStatus(AppConstants.IS_WAITING);
                    callsInfoModel.setPhone(phone);
                    callsInfoModel.setCallId(lastID);
                    callsInfoModel.setFrom(contactsModel1.get_id());
                    callsInfoModel.setTo(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    callsInfoModel.setDuration("00:00");
                    callsInfoModel.setDate(callTime);
                    callsInfoModel.setReceived(true);
                    callsInfoModelRealmList.add(callsInfoModel);
                    callsModel.setCallsInfoModels(callsInfoModelRealmList);
                    realm1.copyToRealmOrUpdate(callsModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, callId));
                });
            } else {

                realm.executeTransaction(realm1 -> {
                    UsersModel contactsModel1 = realm1.where(UsersModel.class).equalTo("_id", caller_id).findFirst();

                    int callCounter;
                    CallsModel callsModel;
                    RealmQuery<CallsModel> callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("_id", historyCallId);
                    callsModel = callsModelRealmQuery.findAll().first();

                    callCounter = callsModel.getCounter();
                    callCounter++;
                    callsModel.setDate(callTime);
                    callsModel.setCounter(callCounter);
                    callsModel.setDuration("00:00");
                    CallsInfoModel callsInfoModel = realm1.createObject(CallsInfoModel.class, UniqueId.generateUniqueId());
                    RealmList<CallsInfoModel> callsInfoModelRealmList = callsModel.getCallsInfoModels();

                    callsInfoModel.set_id(callId);
                    if (isVideoCall)
                        callsInfoModel.setType(AppConstants.VIDEO_CALL);
                    else
                        callsInfoModel.setType(AppConstants.VOICE_CALL);
                    callsInfoModel.setContactsModel(contactsModel1);

                    callsInfoModel.setStatus(AppConstants.IS_WAITING);
                    callsInfoModel.setPhone(phone);
                    callsInfoModel.setCallId(callsModel.get_id());
                    callsInfoModel.setFrom(contactsModel1.get_id());
                    callsInfoModel.setTo(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));
                    callsInfoModel.setDuration("00:00");
                    callsInfoModel.setDate(callTime);
                    callsInfoModel.setReceived(true);
                    callsInfoModelRealmList.add(callsInfoModel);
                    callsModel.setCallsInfoModels(callsInfoModelRealmList);

                    realm1.copyToRealmOrUpdate(callsModel);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
                });
            }


        } finally {
            if (!realm.isClosed()) realm.close();
            WorkJobsManager.getInstance().sendSeenCallToServer(callId);
        }


    }


    private String getHistoryCallIdReceived(String fromId, String toId, boolean isVideoCall, Realm realm) {
        String type;
        CallsModel callsModel;
        if (isVideoCall)
            type = AppConstants.VIDEO_CALL;
        else
            type = AppConstants.VOICE_CALL;


        try {

            callsModel = realm.where(CallsModel.class)
                    .equalTo("from", fromId)
                    .equalTo("to", toId)
                    .equalTo("received", true)
                    .equalTo("type", type)
                    .findFirst();
            return callsModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("call history id Exception MainService" + e.getMessage());
            return null;
        }
    }

    private String getHistoryCallId(String fromId, String toId, boolean isVideoCall, Realm realm) {
        String type;
        CallsModel callsModel;
        if (isVideoCall)
            type = AppConstants.VIDEO_CALL;
        else
            type = AppConstants.VOICE_CALL;


        try {

            callsModel = realm.where(CallsModel.class)
                    .equalTo("from", fromId)
                    .equalTo("to", toId)
                    .equalTo("received", false)
                    .equalTo("type", type)
                    .findFirst();
            return callsModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("call history id Exception MainService" + e.getMessage());
            return null;
        }
    }


    public boolean checkIfSingleCallExist(String callId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long size;
        try {
            RealmQuery<CallsInfoModel> query = realm.where(CallsInfoModel.class).equalTo("_id", callId);
            size = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        AppHelper.LogCat("size " + size);
        return size != 0;
    }


}