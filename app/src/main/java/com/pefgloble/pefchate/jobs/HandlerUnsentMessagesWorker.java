package com.pefgloble.pefchate.jobs;

import android.content.Context;
import android.util.Log;


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
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.Sort;

/**
 * Created by Abderrahim El imame on 10/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class HandlerUnsentMessagesWorker extends Worker {


    public static final String TAG = HandlerUnsentMessagesWorker.class.getSimpleName();
    private int mPendingMessages = 0;
    private CompositeDisposable compositeDisposable;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    public HandlerUnsentMessagesWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }


    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
            try {
                List<MessageModel> messagesModelsList = realm.where(MessageModel.class)
                        .equalTo("status", AppConstants.IS_WAITING)
                        // .equalTo("is_group", false)
                        .equalTo("file_upload", true)
                        .equalTo("sender._id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                        .sort("created", Sort.ASCENDING).findAll();

                AppHelper.LogCat("Job unSentMessages: " + messagesModelsList.size());
                if (messagesModelsList.size() > 0) {
                    AppHelper.LogCat("Job jjb: " + messagesModelsList.size());
                    if (!AppHelper.isServiceRunning(SendSingleMessageToServerWorker.class)) {
                        AppHelper.LogCat("Job isServiceRunning: " + messagesModelsList.size());
                        SendSingleMessageToServerWorker.start(getApplicationContext());
                        try {
                            Thread.sleep(500);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        return Result.success();
                    } else {
                        AppHelper.LogCat("Job not isServiceRunning: " + messagesModelsList.size());
                        unSentMessages();
                        return Result.retry();
                    }


                } else {
                    return Result.failure();
                }

            }
            finally {
                if (!realm.isClosed())
                    realm.close();
            }
        } else {
            return Result.failure();
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
                    compositeDisposable=new CompositeDisposable();
                    compositeDisposable.add(APIHelper.initialApiUsersContacts().sendMessage(updateMessageModel).subscribe(response -> {
                        if (response.isSuccess()) {
                            sendNotification(updateMessageModel.getOtherUserId());
                            MessagesController.getInstance().makeMessageAsSent(updateMessageModel.getSenderId(), updateMessageModel.getMessageId(), response.getMessageId(), updateMessageModel.getConversationId(), response.getConversationId());
                            mPendingMessages--;
                            //checkCompletion();
                        } else {
                           // checkCompletion();
                        }
                    }, throwable -> {
                        //checkCompletion();
                    }));
                }
            } else {
                //checkCompletion();
            }
        } finally {

            if (!realm.isClosed())
                realm.close();
        }
    }
    private void sendNotification(String otherUserId) {
        mRequestQue= Volley.newRequestQueue(getApplicationContext());
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
}