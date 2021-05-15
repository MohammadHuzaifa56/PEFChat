package com.pefgloble.pefchate.jobs;

import android.content.Context;


import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;

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

public class SendDeliveredGroupStatusToServer extends Worker {

    public static final String TAG = SendDeliveredGroupStatusToServer.class.getSimpleName();
    private int mPendingMessages = 0;
    private CountDownLatch latch;

    public SendDeliveredGroupStatusToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {


            updateStatusDeliveredOffline(WhatsCloneApplication.getInstance());
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


    @Override
    public void onStopped() {
        super.onStopped();

        boolean needsReschedule = (mPendingMessages > 0);
        AppHelper.LogCat("Job stopped. Needs reschedule: " + needsReschedule);
        if (!needsReschedule) {
            WorkManager.getInstance().cancelAllWorkByTag(TAG);
            mPendingMessages = 0;
        }
    }

    // returns whether an attempt was made to send every message at least once
    private boolean isComplete() {
        return mPendingMessages == 0;
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
        AppHelper.LogCat("Job finished. Pending files: " + mPendingMessages);
        if (!needsReschedule)
            WorkManager.getInstance().cancelAllWorkByTag(TAG);


    }

    /**
     * method to  update status delivered when user was offline and come online
     * and he has a new messages (unread)
     *
     * @param mContext
     */

    private  void updateStatusDeliveredOffline(Context mContext) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());

        try {
            List<MessageModel> messagesModels = realm.where(MessageModel.class)
                    .notEqualTo("sender._id", PreferenceManager.getInstance().getID(mContext))
                    .equalTo("is_group", true)
                    .equalTo("status", AppConstants.IS_SENT).findAll();

            latch = new CountDownLatch(messagesModels.size());
            mPendingMessages = messagesModels.size();
            if (messagesModels.size() != 0) {
                for (MessageModel messagesModel1 : messagesModels) {
                    String messageId = messagesModel1.get_id();
                    String ownerId = messagesModel1.getSender().get_id();


                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", ownerId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_DELIVERED, updateMessage, (Ack) args -> {

                            JSONObject data = (JSONObject) args[0];
                            try {
                                if (data.getBoolean("success")) {
                                    Realm realm2 = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                                    try {
                                        realm2.executeTransaction(realm1 -> {
                                            MessageModel messagesModel = realm1.where(MessageModel.class).equalTo("_id", messageId)
                                                    .equalTo("status", AppConstants.IS_SENT)
                                                    .findFirst();
                                            if (messagesModel != null) {
                                                messagesModel.setStatus(AppConstants.IS_DELIVERED);
                                                realm1.copyToRealmOrUpdate(messagesModel);
                                                AppHelper.LogCat("RecipientMarkMessageAsDelivered successfully");
                                            } else {
                                                AppHelper.LogCat("RecipientMarkMessageAsDelivered failed ");
                                            }
                                        });
                                    } finally {
                                        if (!realm2.isClosed())
                                            realm2.close();
                                    }


                                    mPendingMessages--;
                                    checkCompletion();
                                    AppHelper.LogCat("--> Recipient mark message as  delivered <--");
                                } else {
                                    checkCompletion();
                                    AppHelper.LogCat("RecipientMarkMessageAsDelivered failed ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });
                    } else {

                        checkCompletion();
                        AppHelper.LogCat("RecipientMarkMessageAsDelivered failed ");
                    }


                }

            }
        } catch (Exception e) {
            AppHelper.LogCat("RecipientMarkMessageAsDelivered failed " + e.getMessage());
        } finally {
            if (!realm.isClosed()) realm.close();
            latch.countDown();
        }

    }


}
