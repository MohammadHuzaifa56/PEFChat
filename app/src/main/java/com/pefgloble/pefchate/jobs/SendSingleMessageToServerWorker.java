package com.pefgloble.pefchate.jobs;

import android.content.Context;
import android.content.Intent;
import android.os.IBinder;


import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.JsonClasses.messags.UpdateMessageModel;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.presenter.MessagesController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
public class SendSingleMessageToServerWorker extends JobIntentService {


    private CompositeDisposable compositeDisposable;


    private int mPendingMessages = 0;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";
    public static void start(Context context) {
        Intent starter = new Intent(context, SendSingleMessageToServerWorker.class);
        SendSingleMessageToServerWorker.enqueueWork(context, starter);
    }
    /**
     * Unique job ID for this service.
     */
    public static final int TAG = 1122122;

    /**
     * Convenience method for enqueuing work in to this service.
     */
    private static void enqueueWork(Context context, Intent intent) {
        enqueueWork(context, SendSingleMessageToServerWorker.class, TAG, intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        AppHelper.LogCat("  has Created");
        mRequestQue=Volley.newRequestQueue(this);
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            compositeDisposable = new CompositeDisposable();
            unSentMessages();
        } else {
            stopSelf();
        }
    }

    private void unSentMessages() {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            List<MessageModel> messagesModelsList = realm.where(MessageModel.class)
                    .equalTo("status", AppConstants.IS_WAITING)
                    //.equalTo("is_group", false)
                    .equalTo("file_upload", true)
                    .equalTo("sender._id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                    .sort("created", Sort.ASCENDING).findAll();

            mPendingMessages = messagesModelsList.size();
            AppHelper.LogCat("Job unSentMessages: " + mPendingMessages);
            if (messagesModelsList.size() != 0) {

                for (MessageModel messagesModel : messagesModelsList) {
                    UpdateMessageModel updateMessageModel = new UpdateMessageModel();
                    updateMessageModel.setSenderId(messagesModel.getSender().get_id());
                    if (messagesModel.isIs_group()) {
                        updateMessageModel.setGroupId(messagesModel.getGroup().get_id());
                        int arraySize = messagesModel.getGroup().getMembers().size();
                        if (arraySize == 0) return;
                        List<String> ids = new ArrayList<>();
                        for (int x = 0; x <= arraySize - 1; x++) {
                            if (!messagesModel.getGroup().getMembers().get(x).getOwner().get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                                ids.add(messagesModel.getGroup().getMembers().get(x).getOwner().get_id());
                        }
                        updateMessageModel.setMembers_ids(ids);

                        //  AppHelper.LogCat("ids "+ids);
                    } else {

                        updateMessageModel.setOtherUserId(messagesModel.getRecipient().get_id());
                    }
                    updateMessageModel.setState(messagesModel.getState());
                    updateMessageModel.setMessageId(messagesModel.get_id());
                    updateMessageModel.setConversationId(messagesModel.getConversationId());
                    updateMessageModel.setMessage(messagesModel.getMessage());
                    updateMessageModel.setCreated(messagesModel.getCreated());
                    updateMessageModel.setFile(messagesModel.getFile());
                    updateMessageModel.setFile_type(messagesModel.getFile_type());
                    updateMessageModel.setDuration_file(messagesModel.getDuration_file());
                    updateMessageModel.setFile_size(messagesModel.getFile_size());
                    updateMessageModel.setLatitude(messagesModel.getLatitude());
                    updateMessageModel.setLongitude(messagesModel.getLongitude());

                    updateMessageModel.setReply_id(messagesModel.getReply_id());
                    updateMessageModel.setReply_message(messagesModel.isReply_message());
                    updateMessageModel.setDocument_name(messagesModel.getDocument_name());
                    updateMessageModel.setDocument_type(messagesModel.getDocument_type());

                    if (!messagesModel.isFile_upload()) break;


                    compositeDisposable.add(APIHelper.initialApiUsersContacts().sendMessage(updateMessageModel).subscribe(response -> {
                        if (response.isSuccess()) {
                            sendNotification(updateMessageModel.getOtherUserId());
                            MessagesController.getInstance().makeMessageAsSent(updateMessageModel.getSenderId(), updateMessageModel.getMessageId(), response.getMessageId(), updateMessageModel.getConversationId(), response.getConversationId());
                            mPendingMessages--;
                            checkCompletion();
                        } else {
                            checkCompletion();
                        }
                    }, throwable -> {
                        checkCompletion();
                    }));
                }
            } else {
                checkCompletion();
            }
        } finally {

            if (!realm.isClosed())
                realm.close();
        }
    }

    private void sendNotification(String otherUserId) {
        JSONObject json = new JSONObject();
        try {
            json.put("to","/topics/"+otherUserId);
            JSONObject notificationObj = new JSONObject();
            notificationObj.put("title","any title");
            notificationObj.put("body","any body");

            json.put("data",notificationObj);
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.POST, URL,
                    json,
                    new Response.Listener<JSONObject>() {
                        @Override
                        public void onResponse(JSONObject response) {
                            AppHelper.LogCat("FCM Noti Send Successfull");
                        }
                    }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    AppHelper.LogCat("FCM Noti Error "+error.getMessage());
                }
            }
            ){
                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    Map<String,String> header = new HashMap<>();
                    header.put("content-type","application/json");
                    header.put("authorization","key=AAAAIu72OZg:APA91bF-JXo_YUTV2mbJQ7T6Df_jFB5hIeMv8MgPynu001GgeAmCmk-ZAZFI1_CkrAQIpv3KTZSf2sKXsFtYrFF3tE92HZSi8br5qXM03l_ewb_VQct5UTXd63iRbA6TWrF7Z87sS7s1");
                    return header;
                }
            };
            mRequestQue.add(request);
        }
        catch (JSONException e)

        {
            e.printStackTrace();
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mPendingMessages = 0;
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


    // returns whether an attempt was made to send every message at least once
    private boolean needsReschedule() {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        RealmResults<MessageModel> messageModels;
        int size = 0;
        try {
            messageModels = realm.where(MessageModel.class)
                    .equalTo("status", AppConstants.IS_WAITING)
                    // .equalTo("is_group", false)
                    .equalTo("file_upload", true)
                    .equalTo("sender._id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                    .sort("created", Sort.ASCENDING).findAll();
            size = messageModels.size();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }

        return size != 0;
    }

    @Override
    public boolean onStopCurrentWork() {
        //return super.onStopCurrentWork();
        return !needsReschedule();
    }

    /**
     * Decides whether the job can be stopped, and whether it needs to be rescheduled in case of
     * pending messages to send.
     */
    private void checkCompletion() {
        if (needsReschedule()) {
            return;
        }

        //  if any sending is not successful, reschedule job for remaining files
        boolean needsReschedule = (mPendingMessages > 0);
        AppHelper.LogCat("checkCompletion files: " + mPendingMessages);
        //jobFinished(parameters, needsReschedule);
        if (!needsReschedule)
            stopSelf();
    }



}