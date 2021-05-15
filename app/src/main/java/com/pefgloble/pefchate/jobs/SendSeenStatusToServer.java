package com.pefgloble.pefchate.jobs;

import android.content.Context;
import android.util.Log;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.presenter.MessagesController;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;
import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.realm.Realm;
import io.socket.client.Ack;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class SendSeenStatusToServer extends Worker {

    public static final String TAG = SendSeenStatusToServer.class.getSimpleName();


    private boolean needsReschedule = false;
    private CountDownLatch latch;

    public SendSeenStatusToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        String senderId = getInputData().getString("senderId");
        String conversationId = getInputData().getString("conversationId");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            emitMessageSeen(WhatsCloneApplication.getInstance(), senderId, conversationId);
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return Result.success();

        } else {
            return Result.failure();
        }
    }


    /**
     * Decides whether the job can be stopped, and whether it needs to be rescheduled in case of
     * pending messages to send.
     */
    private void checkCompletion() {

        //  if any sending is not successful, reschedule job for remaining files
        boolean needsReschedule = (this.needsReschedule);
        AppHelper.LogCat("Job finished. Pending : " + this.needsReschedule);
        if (!needsReschedule) WorkManager.getInstance().cancelAllWorkByTag(TAG);
    }

    /**
     * method to emit that message are seen by user
     *
     * @param context
     * @param senderId
     * @param conversationId
     */
    public void emitMessageSeen(Context context, String senderId, String conversationId) {
        Log.d("SeenTag","emit fun called");
        latch = new CountDownLatch(1);
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            Log.d("SeenTag","try fun called");
            List<MessageModel> messagesModelsRealm = realm.where(MessageModel.class)
                    .equalTo("recipient._id", PreferenceManager.getInstance().getID(context))
                    .equalTo("sender._id", senderId)
                    .equalTo("is_group", false)
                    .beginGroup()
                    .equalTo("status", AppConstants.IS_SENT)
                    .or()
                    .equalTo("status", AppConstants.IS_DELIVERED)
                    .endGroup()
                    .findAll();
            AppHelper.LogCat("size messagesModelsRealm " + messagesModelsRealm.size());
            if (messagesModelsRealm.size() != 0) {
                Log.d("SeenTag","msgs model not null");

                for (MessageModel messagesModel1 : messagesModelsRealm) {
                    String messageId = messagesModel1.get_id();
                    String ownerId = messagesModel1.getSender().get_id();
                    String recipientId = messagesModel1.getRecipient().get_id();

                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", ownerId);
                        updateMessage.put("recipientId", recipientId);
                        updateMessage.put("is_group", false);

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                    if (mSocket != null) {
                        Log.d("SeenTag","emit has been called");
                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data = (JSONObject) args[0];
                            try {
                                if (data.getBoolean("success")) {
                                    MessagesController.getInstance().updateConversationStatus(conversationId, senderId);
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> Recipient mark message as  seen <--");
                                    needsReschedule = false;
                                    checkCompletion();

                                } else {
                                    needsReschedule = true;
                                    checkCompletion();
                                    AppHelper.LogCat("RecipientMarkMessageAsDelivered seen ");
                                }
                            } catch (JSONException e) {
                                Log.d("SeenTag","boolEx "+e.getMessage());
                                e.printStackTrace();
                            }

                        });

                    } else {
                        needsReschedule = true;
                        checkCompletion();
                        AppHelper.LogCat("RecipientMarkMessageAsDelivered failed ");
                    }


                }

            } else {
                needsReschedule = false;
                Log.d("SeenTag","msgac model is null");
                checkCompletion();
            }

        } finally {
            if (!realm.isClosed()) realm.close();
            latch.countDown();

        }

    }
}
