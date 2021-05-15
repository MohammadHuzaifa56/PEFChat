package com.pefgloble.pefchate.AgoraVideo.openvcall;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;

import com.pefgloble.pefchate.AgoraVideo.openvcall.model.AGEventHandler;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.CurrentUserSettings;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.EngineConfig;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.MyEngineEventHandler;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.helpers.ForegroundRuning;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.jobs.GcmTopicSubscribe;
import com.pefgloble.pefchate.jobs.HandlerUnsentMessagesWorker;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.jobs.WorkJobsManager;
import com.pefgloble.pefchate.recievers.BootCompleteReceiver;
import com.pefgloble.pefchate.recievers.MessagesReceiverBroadcast;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import androidx.appcompat.app.AppCompatActivity;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;

public class AGApplication extends Application {
    private CurrentUserSettings mVideoSettings = new CurrentUserSettings();
    public static volatile Handler applicationHandler = null;
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    private RtcEngine mRtcEngine;
    private EngineConfig mConfig;
    private MyEngineEventHandler mEventHandler;
    BroadcastReceiver receiver;
    MessagesReceiverBroadcast myReciever;
    BootCompleteReceiver bootCompleteReceiver;

    static AGApplication mInstance;

    public RtcEngine rtcEngine() {
        return mRtcEngine;
    }

    public EngineConfig config() {
        return mConfig;
    }

    public CurrentUserSettings userSettings() {
        return mVideoSettings;
    }

    public void addEventHandler(AGEventHandler handler) {
        mEventHandler.addEventHandler(handler);
    }

    public void remoteEventHandler(AGEventHandler handler) {
        mEventHandler.removeEventHandler(handler);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        setmInstance(this);
        createRtcEngine();
        applicationHandler = new Handler(getApplicationContext().getMainLooper());


        if (PreferenceManager.getInstance().getToken(mInstance) != null) {
            Log.d("SocTag","Create Called");
            SocketConnectionManager.getInstance().connectSocket(mInstance);
            IntentFilter filter=new IntentFilter();
            filter.addAction("new_user_notification_whatsclone");
            myReciever=new MessagesReceiverBroadcast();
            registerReceiver(myReciever,filter);

            bootCompleteReceiver=new BootCompleteReceiver();
            IntentFilter filter1=new IntentFilter();
            filter1.addAction("android.intent.action.BOOT_COMPLETED");
            registerReceiver(bootCompleteReceiver,filter1);

        }
        ForegroundRuning.init(this);
        WorkJobsManager.getInstance().pruneWork();
       // SocketConnectionManager.getInstance().checkSocketConnection();
        initializerApplication();
    }

    public void setmInstance(AGApplication mInstance) {
        AGApplication.mInstance = mInstance;
    }

    public static synchronized AGApplication getInstance() {
        return mInstance;
    }

    private void createRtcEngine() {
        Context context = getApplicationContext();
        String appId = context.getString(R.string.agora_app_id);
        if (TextUtils.isEmpty(appId)) {
            throw new RuntimeException("NEED TO use your App ID, get your own ID at https://dashboard.agora.io/");
        }

        mEventHandler = new MyEngineEventHandler();
        try {
            // Creates an RtcEngine instance
            mRtcEngine = RtcEngine.create(context, appId, mEventHandler);
        } catch (Exception e) {
            log.error(Log.getStackTraceString(e));
            throw new RuntimeException("NEED TO check rtc sdk init fatal error\n" + Log.getStackTraceString(e));
        }

        /*
          Sets the channel profile of the Agora RtcEngine.
          The Agora RtcEngine differentiates channel profiles and applies different optimization
          algorithms accordingly. For example, it prioritizes smoothness and low latency for a
          video call, and prioritizes video quality for a video broadcast.
         */
        mRtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_COMMUNICATION);
        // Enables the video module.
        mRtcEngine.enableVideo();
        /*
          Enables the onAudioVolumeIndication callback at a set time interval to report on which
          users are speaking and the speakers' volume.
          Once this method is enabled, the SDK returns the volume indication in the
          onAudioVolumeIndication callback at the set time interval, regardless of whether any user
          is speaking in the channel.
         */
        mRtcEngine.enableAudioVolumeIndication(200, 3, false);

        mConfig = new EngineConfig();
    }

    public void initializerApplication() {
        // JobsUtil.isWorkScheduled(HandlerUnsentMessagesWorker.TAG);
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {
            SocketConnectionManager.getInstance().checkSocketConnection();
            WorkJobsManager.getInstance().initializerApplicationService();
            WorkJobsManager.getInstance().syncingContactsWithServerWorker();
            WorkJobsManager.getInstance().sendUserMessagesToServer();
            WorkJobsManager.getInstance().sendUserStoriesToServer();
            WorkJobsManager.getInstance().sendDeliveredStatusToServer();
            WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
            WorkJobsManager.getInstance().sendDeletedStoryToServer();
            GcmTopicSubscribe.start(this);
        }
    }


    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}
