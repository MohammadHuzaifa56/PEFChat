package com.pefgloble.pefchate.jobs;

import android.content.Context;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.concurrent.CountDownLatch;

import androidx.annotation.NonNull;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import io.reactivex.disposables.CompositeDisposable;
import io.socket.client.Socket;

/**
 * Created by Abderrahim El imame on 5/8/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class InitializerApplicationService extends Worker {

    public static final String TAG = InitializerApplicationService.class.getSimpleName();
    private int mPendingMessages = 0;
    private CountDownLatch latch;
    private CompositeDisposable mDisposable;

    public InitializerApplicationService(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }

    @NonNull
    @Override
    public Result doWork() {
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null) {
            mDisposable = new CompositeDisposable();
            runMethods(AGApplication.getInstance());
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

        if (mDisposable != null)
            mDisposable.dispose();
        boolean needsReschedule = (mPendingMessages > 0);
        AppHelper.LogCat("Job stopped. Needs reschedule: " + needsReschedule);
        if (!needsReschedule) {
            WorkManager.getInstance(getApplicationContext()).cancelAllWorkByTag(TAG);
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
        AppHelper.LogCat("Job finished. : " + mPendingMessages);
        if (!needsReschedule)
            WorkManager.getInstance().cancelAllWorkByTag(TAG);


    }


    private void runMethods(Context mContext) {
        mPendingMessages = 4;
        latch = new CountDownLatch(4);

        notifyOtherUser(mContext);
        getContactInfo(mContext);
        checkIfUserSession();
       // getAppSettings(mContext);
    }

    /**
     * method to send notification if i'm new  to the app
     */

    private void notifyOtherUser(Context context) {
        if (PreferenceManager.getInstance().isNewUser(context)) {


            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("senderId", PreferenceManager.getInstance().getID(context));
                jsonObject.put("phone", PreferenceManager.getInstance().getMobileNumber(context));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Socket mSocket = SocketConnectionManager.getInstance().getSocket();
            if (mSocket != null) {
                mSocket.emit(AppConstants.SocketConstants.SOCKET_NEW_USER_JOINED, jsonObject);
                PreferenceManager.getInstance().setIsNewUser(getApplicationContext(), false);
            } else {
                mPendingMessages--;
                checkCompletion();
            }
        } else {
            mPendingMessages--;
            checkCompletion();
        }
    }


    private void getContactInfo(Context context) {
        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(PreferenceManager.getInstance().getID(context)).subscribe(contactsModel -> {

            mPendingMessages--;
            checkCompletion();
        }, throwable -> {
            AppHelper.LogCat(throwable.getMessage());


            checkCompletion();
        }));
    }


    public void getAppSettings(Context context) {
        mDisposable.add(APIHelper.initialApiUsersContacts().getAppSettings().subscribe(settingsResponse -> {

            PreferenceManager.getInstance().setPublisherId(context, settingsResponse.getPublisherId());
            PreferenceManager.getInstance().setUnitBannerAdsID(context, settingsResponse.getUnitBannerID());


            PreferenceManager.getInstance().setShowBannerAds(context, settingsResponse.isAdsBannerStatus());
            PreferenceManager.getInstance().setShowVideoAds(context, settingsResponse.isAdsVideoStatus());
            PreferenceManager.getInstance().setShowInterstitialAds(context, settingsResponse.isAdsInterstitialStatus());

            PreferenceManager.getInstance().setUnitVideoAdsID(context, settingsResponse.getUnitVideoID());
            PreferenceManager.getInstance().setAppVideoAdsID(context, settingsResponse.getAppID());

            PreferenceManager.getInstance().setUnitInterstitialAdID(context, settingsResponse.getUnitInterstitialID());

            PreferenceManager.getInstance().setGiphyKey(context, settingsResponse.getGiphyKey());
            PreferenceManager.getInstance().setPrivacyLink(context, settingsResponse.getPrivacyLink());


           /* int currentAppVersion;
            if (PreferenceManager.getInstance().getVersionApp(AGApplication.getInstance()) != 0) {
                currentAppVersion = PreferenceManager.getInstance().getVersionApp(AGApplication.getInstance());
            } else {
                currentAppVersion = Integer.parseInt(AppHelper.getAppVersion(AGApplication.getInstance()));
            }
            if (currentAppVersion != 0 && currentAppVersion < settingsResponse.getAppVersion()) {
                PreferenceManager.getInstance().setVersionApp(context, currentAppVersion);
                PreferenceManager.getInstance().setIsOutDate(context, true);
            } else {
                PreferenceManager.getInstance().setIsOutDate(context, false);
            }*/
            mPendingMessages--;
            checkCompletion();
            latch.countDown();
        }, throwable -> {
            checkCompletion();
            AppHelper.LogCat("Error get settings info Welcome " + throwable.getMessage());
        }));
    }


    public void checkIfUserSession() {
        mDisposable.add(APIHelper.initialApiUsersContacts().checkIfUserSession().subscribe(networkModel -> {
            if (!networkModel.isConnected()) {
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_SESSION_EXPIRED));

            }
            mPendingMessages--;
            checkCompletion();
        }, throwable -> {
            checkCompletion();
            AppHelper.LogCat("checkIfUserSession MainActivity " + throwable.getMessage());
        }))
        ;
    }
}