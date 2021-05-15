package com.pefgloble.pefchate.jobs;



import android.content.Context;
import android.os.Build;

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
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.presenter.MessagesController;

import org.joda.time.DateTime;
import org.joda.time.Hours;
import org.joda.time.Minutes;
import org.joda.time.Seconds;
import org.json.JSONException;
import org.json.JSONObject;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import androidx.work.BackoffPolicy;
import androidx.work.Constraints;
import androidx.work.Data;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.WorkRequest;
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
public class WorkJobsManager {

    private static volatile WorkJobsManager Instance = null;
    private int mPendingMessages = 0;
    private CompositeDisposable compositeDisposable;
    private RequestQueue mRequestQue;
    private String URL = "https://fcm.googleapis.com/fcm/send";

    private WorkJobsManager() {

    }

    public static WorkJobsManager getInstance() {

        WorkJobsManager localInstance = Instance;
        if (localInstance == null) {
            synchronized (WorkJobsManager.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new WorkJobsManager();
                }
            }
        }
        return localInstance;

    }


    /**
     * Job to send seen status to other user
     *
     * @param senderId
     */
    public void sendSeenStatusToServer(String senderId, String conversationId, Context context) {
        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("senderId", senderId);
        dataBuilder.putString("conversationId", conversationId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenStatusToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenStatusToServer.TAG)
                .build();
        //WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(SendSeenStatusToServer.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }

    /**
     * Job to send delivered status when current was offline
     */
    public void sendDeliveredStatusToServer() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendDeliveredStatusToServer.class)
                .setConstraints(new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendDeliveredStatusToServer.TAG)
                .build();
        //WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(SendDeliveredStatusToServer.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }

    /**
     * Job to send seen group status to other user
     *
     * @param groupId
     */
    public void sendSeenGroupStatusToServer(String groupId, String conversationId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("groupId", groupId);
        dataBuilder.putString("conversationId", conversationId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenGroupStatusToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenGroupStatusToServer.TAG)
                .build();
        //WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(SendSeenGroupStatusToServer.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }

    /**
     * Job to send delivered group status when current was offline
     */
    public void sendDeliveredGroupStatusToServer() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendDeliveredGroupStatusToServer.class)
                .setConstraints(new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendDeliveredGroupStatusToServer.TAG)
                .build();
        //WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(SendDeliveredGroupStatusToServer.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }


    public void sendUserMessagesToServer() {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(HandlerUnsentMessagesWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(HandlerUnsentMessagesWorker.TAG)
                .build();
         // WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
            WorkManager.getInstance(AGApplication.getInstance()).beginUniqueWork(HandlerUnsentMessagesWorker.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }
    public void unSentMessages() {
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
        mRequestQue= Volley.newRequestQueue(AGApplication.getInstance());
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

    public void sendUserStoriesToServer() {
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(HandlerUnsentStoriesWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(HandlerUnsentStoriesWorker.TAG)
                .build();
        //WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(HandlerUnsentStoriesWorker.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }

    public void downloadFileToServer(String uploadId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("messageId", uploadId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(DownloadSingleFileFromServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new androidx.work.Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(DownloadSingleFileFromServerWorker.TAG + "_" + uploadId)
                .build();
        //   WorkManager.getInstance().beginUniqueWork(DownloadSingleFileFromServerWorker.TAG + "_" + uploadId, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }

    public void uploadFileToServer(String uploadId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("messageId", uploadId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadSingleFileToServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(UploadSingleFileToServerWorker.TAG + "_" + uploadId)
                .build();
        // WorkManager.getInstance().beginUniqueWork(UploadSingleFileToServerWorker.TAG + "_" + uploadId, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
    }


    public void uploadFileStoryToServer(String uploadId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("storyId", uploadId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(UploadSingleStoryFileToServerWorker.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(UploadSingleStoryFileToServerWorker.TAG + "_" + uploadId)
                .build();
        // WorkManager.getInstance().beginUniqueWork(UploadSingleFileToServerWorker.TAG + "_" + uploadId, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
        WorkManager.getInstance(AGApplication.getInstance()).enqueue(workRequest);
    }

    public void cancelJob(String tag) {
        // WorkManager.getInstance().cancelUniqueWork(tag);
        WorkManager.getInstance(AGApplication.getInstance()).cancelAllWorkByTag(tag);
    }

    public void cancelAllJob() {
        AppHelper.LogCat("cancelAllJob");
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).cancelAllWork();
        // WorkManager.getInstance().pruneWork();//cleanup all the completed jobs from the database.
    }

    public void pruneWork() {
        AppHelper.LogCat("pruneWork");
        WorkManager.getInstance(AGApplication.getInstance()).pruneWork();
    }
    /**
     * Job to send seen call to other user
     *
     * @param callId
     */
    public void sendSeenCallToServer(String callId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("callId", callId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenCallToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenCallToServer.TAG + "_" + callId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance().beginUniqueWork(SendSeenCallToServer.TAG + "_" + storyId, ExistingWorkPolicy.KEEP, workRequest).enqueue();
    }

    /**
     * Job to send seen story to other user
     *
     *
     */
    public void sendSeenStoryToServer(String senderId, String storyId) {

        Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("senderId", senderId);
        dataBuilder.putString("storyId", storyId);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendSeenStoryToServer.class)
                .setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendSeenStoryToServer.TAG + "_" + storyId)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance().beginUniqueWork(SendSeenStoryToServer.TAG + "_" + storyId, ExistingWorkPolicy.KEEP, workRequest).enqueue();
    }

    public void sendDeletedStoryToServer() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SendDeletedStoryToServer.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SendDeletedStoryToServer.TAG)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(SendDeletedStoryToServer.TAG, ExistingWorkPolicy.KEEP, workRequest).enqueue();
    }

    public void expireStoryWorker(String storyId, String date) {
        //calculate difference between 24 with the story date

        DateTime startDateValue = new DateTime();
        DateTime endDateValue = UtilsTime.getCorrectDate(date).plusHours(24);
        int hours = Hours.hoursBetween(startDateValue, endDateValue).getHours();
        int minutes = Minutes.minutesBetween(startDateValue, endDateValue).getMinutes();
        int seconds = Seconds.secondsBetween(startDateValue, endDateValue).getSeconds();

        AppHelper.LogCat("left time " + hours + ":" + minutes + ":" + seconds);// TODO: 1/10/19 i will need it for destorying messages

     /*   Data.Builder dataBuilder = new Data.Builder();
        dataBuilder.putString("storyId", storyId);
        PeriodicWorkRequest.Builder dayWorkBuilder = new PeriodicWorkRequest.Builder(ExpireStoryWorker.class, seconds, TimeUnit.SECONDS, 5, TimeUnit.MINUTES);
        dayWorkBuilder.setInputData(dataBuilder.build())
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build());
        // Add Tag to workBuilder
        dayWorkBuilder.addTag(ExpireStoryWorker.TAG + "_" + storyId);
        // Create the actual work object:
        PeriodicWorkRequest dayWork = dayWorkBuilder.build();
        // Then enqueue the recurring task:
        WorkManager.getInstance().enqueue(dayWork);*/

    }

    /**
     * Job to get initializer settings
     */
    public void initializerApplicationService() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(InitializerApplicationService.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(InitializerApplicationService.TAG)
                .build();
        //WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(InitializerApplicationService.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();
    }

    public void syncingContactsWithServerWorker() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SyncingContactsWithServerWorker.class, 24, TimeUnit.HOURS, 5, TimeUnit.MINUTES)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SyncingContactsWithServerWorker.TAG)
                .build();
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).enqueue(workRequest);
        // WorkManager.getInstance().enqueueUniquePeriodicWork(SyncingContactsWithServerWorker.TAG, ExistingPeriodicWorkPolicy.KEEP, workRequest);
    }

    public void syncingContactsWithServerWorkerInit() {

        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(SyncingContactsWithServerWorker.class)
                .setConstraints(new Constraints.Builder()
                        .setRequiredNetworkType(NetworkType.CONNECTED)
                        .build())
                .addTag(SyncingContactsWithServerWorker.TAG)
                .build();
        // WorkManager.getInstance().enqueue(workRequest);
        WorkManager.getInstance(WhatsCloneApplication.getInstance()).beginUniqueWork(SyncingContactsWithServerWorker.TAG, ExistingWorkPolicy.REPLACE, workRequest).enqueue();


    }


}
