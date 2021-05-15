package com.pefgloble.pefchate.jobs;

import android.content.Context;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.calls.CallsInfoModel;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.presenter.CallsController;

import org.json.JSONException;
import org.json.JSONObject;

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

public class SendSeenCallToServer extends Worker {

    public static final String TAG = SendSeenCallToServer.class.getSimpleName();


    private boolean needsReschedule = false;
    private CountDownLatch latch;
    private String callId;

    public SendSeenCallToServer(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        AppHelper.LogCat("onStartJob: " + "jobStarted");
        String senderId = getInputData().getString("senderId");
        callId = getInputData().getString("callId");


        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            emitCallSeen(callId);
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
        if (!needsReschedule) WorkManager.getInstance().cancelAllWorkByTag(TAG + "_" + callId);
    }

    /**
     * method to emit that call are seen by user
     *
     * @param callId
     */
    public void emitCallSeen(String callId) {
        latch = new CountDownLatch(1);
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        try {
            AppHelper.LogCat("Job . callId : " + callId);
            CallsInfoModel callsInfoModel = realm.where(CallsInfoModel.class).equalTo("_id", callId).findFirst();
            if (callsInfoModel != null) {

                if (callsInfoModel.getStatus() != AppConstants.IS_SEEN) {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("callId", callId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data = (JSONObject) args[0];
                            try {
                                if (data.getBoolean("success")) {
                                    CallsController.getInstance().updateSeenCallStatus(callId);
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_CALL_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> Recipient mark call as  seen <--");

                                    needsReschedule = false;
                                    checkCompletion();


                                } else {
                                    needsReschedule = true;
                                    checkCompletion();
                                    AppHelper.LogCat("RecipientMarkMessageAs seen ");
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
                } else {

                    needsReschedule = false;
                    checkCompletion();
                    AppHelper.LogCat("this call is already seen failed ");
                }
            } else {
                needsReschedule = false;
                checkCompletion();
                AppHelper.LogCat("this call is already exist seen failed ");
            }
        } finally {
            if (!realm.isClosed())
                realm.close();
            latch.countDown();
        }


    }
}
