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

public class SendSeenGroupStatusToServer extends Worker {

    public static final String TAG = SendSeenGroupStatusToServer.class.getSimpleName();


    private boolean needsReschedule = false;
    private CountDownLatch latch;

    public SendSeenGroupStatusToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        String groupId = getInputData().getString("groupId");
        String conversationId = getInputData().getString("conversationId");

        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            emitMessageSeen(WhatsCloneApplication.getInstance(), groupId, conversationId);
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
     * @param groupId
     * @param conversationId
     */
    public void emitMessageSeen(Context context, String groupId, String conversationId) {
        latch = new CountDownLatch(1);
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
        try {



            List<MessageModel> messagesModelsRealm = realm.where(MessageModel.class)
                    .notEqualTo("sender._id", PreferenceManager.getInstance().getID(AGApplication   .getInstance()))
                    .equalTo("group._id", groupId)
                    .equalTo("is_group", true)
                    .beginGroup()
                    .equalTo("status", AppConstants.IS_SENT)
                    .or()
                    .equalTo("status", AppConstants.IS_DELIVERED)
                    .endGroup()
                    .findAll();

            AppHelper.LogCat("size messagesModelsRealm " + messagesModelsRealm.size());

            if (messagesModelsRealm.size() != 0) {

                for (MessageModel messagesModel1 : messagesModelsRealm) {
                    String messageId = messagesModel1.get_id();
                    String ownerId = messagesModel1.getSender().get_id();
                    String recipientId = PreferenceManager.getInstance().getID(context);

                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", ownerId);
                        updateMessage.put("recipientId", recipientId);
                        updateMessage.put("is_group", true);

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data = (JSONObject) args[0];
                            try {
                                if (data.getBoolean("success")) {
                                    MessagesController.getInstance().updateGroupConversationStatus(conversationId, groupId);
                                   // mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> Recipient mark message as  seen <--");
                                    needsReschedule = false;
                                    checkCompletion();

                                } else {
                                    needsReschedule = true;
                                    checkCompletion();
                                    AppHelper.LogCat("RecipientMarkMessageAsDelivered seen ");
                                }
                            } catch (JSONException e) {
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
                checkCompletion();
            }

        } finally {
            if (!realm.isClosed()) realm.close();
            latch.countDown();

        }


    }


}
