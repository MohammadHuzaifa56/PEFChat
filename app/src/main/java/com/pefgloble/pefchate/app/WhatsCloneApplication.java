package com.pefgloble.pefchate.app;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.StrictMode;
import android.text.TextUtils;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.danikula.videocache.HttpProxyCacheServer;
import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.AGEventHandler;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.CurrentUserSettings;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.EngineConfig;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.MyEngineEventHandler;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ForegroundRuning;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UserAgentHeadersInjector;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.jobs.WorkJobsManager;

import org.greenrobot.eventbus.EventBus;
import org.slf4j.LoggerFactory;

import java.util.Locale;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.multidex.MultiDexApplication;
import io.agora.rtc.Constants;
import io.agora.rtc.RtcEngine;
import io.realm.Realm;
import io.realm.RealmConfiguration;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class WhatsCloneApplication extends MultiDexApplication {
    public static volatile Handler applicationHandler = null;
    static Context mInstance;
    public Context context;
    private static RealmConfiguration realmConfiguration;
    private HttpProxyCacheServer proxy;


    public static Context getInstance() {
        return AGApplication.getInstance();
    }

    public  Context returnContext(){
        return WhatsCloneApplication.this;
    }

    private CurrentUserSettings mVideoSettings = new CurrentUserSettings();

    private final org.slf4j.Logger log = LoggerFactory.getLogger(this.getClass());
    private RtcEngine mRtcEngine;
    private EngineConfig mConfig;
    private MyEngineEventHandler mEventHandler;

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

       /* if (AppConstants.CRASH_LYTICS)
            WhatsCloneApplication.setupCrashlytics();*/
        if (PreferenceManager.getInstance().getToken(mInstance) != null) {
            initRealm();
            checkIfUserSession();
        }

        if (AppConstants.DEBUGGING_MODE) {
            PrettyFormatStrategy.newBuilder()
                    .showThreadInfo(true)  // (Optional) Whether to show thread info or not. Default true
                    //    .methodCount(0)         // (Optional) How many method line to show. Default 2
                    //.methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                    //  .logStrategy(customLog) // (Optional) Changes the log strategy to print out. Default LogCat
                    .tag(AppConstants.TAG)   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                    .build();
            Logger.addLogAdapter(new AndroidLogAdapter() {
                @Override
                public boolean isLoggable(int priority, String tag) {
                    return true;
                }
            });
            //   strictMode();
        }
        applicationHandler = new Handler(getApplicationContext().getMainLooper());

        WorkJobsManager.getInstance().pruneWork();
        initializerApplication();
/*        ForegroundRuning.get().addListener(new ForegroundRuning.Listener() {
            @Override
            public void onBecameForeground() {
                AppHelper.LogCat("onBecameForeground ");
                initializerApplication();
            }

            @Override
            public void onBecameBackground() {
                AppHelper.LogCat("onBecameBackground ");
            WorkJobsManager.getInstance().cancelAllJob();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                initializerApplication();
            }
        });*/


        if (PreferenceManager.getInstance().getToken(mInstance) != null && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {
        //    SocketConnectionManager.getInstance().connectSocket(mInstance);

        }
 /*       if (AppConstants.ENABLE_CRASH_HANDLER)
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());*/

        if (!PreferenceManager.getInstance().getLanguage(this).equals(""))
            setDefaultLocale(this, new Locale(PreferenceManager.getInstance().getLanguage(this)));
        else {
            if (Locale.getDefault().toString().startsWith("en_")) {
                PreferenceManager.getInstance().setLanguage(this, "en");
            }
        }
       // EmojiManager.install(new IosEmojiProvider());

        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);




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


  /*  public static void setupCrashlytics() {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder()
                        .disabled(AppConstants.DEBUGGING_MODE)
                        .build())
                .build();
        Fabric.with(mInstance, crashlyticsKit, new Crashlytics());
        Crashlytics.setUserEmail(PreferenceManager.getInstance().getMobileNumber(getInstance()));
        Crashlytics.setUserName(PreferenceManager.getInstance().getMobileNumber(getInstance()));
        Crashlytics.setUserIdentifier(String.valueOf(PreferenceManager.getInstance().getID(getInstance())));

    }*/

    protected void strictMode() {
        /**
         * Doesn't enable anything on the main thread that related
         * to resource access.
         */
        StrictMode.setThreadPolicy(new StrictMode.ThreadPolicy.Builder()
                .permitDiskWrites()
                .permitDiskReads()
                .detectNetwork()
                .detectCustomSlowCalls()
                .penaltyLog()
                .penaltyFlashScreen()
                .penaltyDeath()
                .build());

        /**
         * Doesn't enable any leakage of the application's components.
         */
        final StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            builder.detectLeakedRegistrationObjects();
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            builder.detectFileUriExposure();
        }
        builder.detectLeakedClosableObjects()
                .detectLeakedSqlLiteObjects()
                .penaltyLog()
                .penaltyDeath();
        StrictMode.setVmPolicy(builder.build());
    }


    @SuppressWarnings("deprecation")
    protected void setDefaultLocale(Context context, Locale locale) {
        Locale.setDefault(locale);
        Configuration appConfig = new Configuration();
        appConfig.locale = locale;
        context.getResources().updateConfiguration(appConfig, context.getResources().getDisplayMetrics());

    }
    public void initializerApplication() {
        //   JobsUtil.isWorkScheduled(HandlerUnsentMessagesWorker.TAG);
        if (PreferenceManager.getInstance().getToken(getApplicationContext()) != null && !PreferenceManager.getInstance().isNeedProvideInfo(this)) {
            SocketConnectionManager.getInstance().checkSocketConnection();
            WorkJobsManager.getInstance().initializerApplicationService();
            WorkJobsManager.getInstance().syncingContactsWithServerWorker();
            WorkJobsManager.getInstance().sendUserMessagesToServer();
            //  WorkJobsManager.getInstance().sendUserStoriesToServer();
            WorkJobsManager.getInstance().sendDeliveredStatusToServer();
            WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
         //   WorkJobsManager.getInstance().sendDeletedStoryToServer();
            // GcmTopicSubscribe.start(this);
        }

    }

    @SuppressLint("DefaultLocale")
    public static void initializeRealmConfig() {

         String DatabaseName = "PEFChat" + PreferenceManager.getInstance().getToken(getInstance()) + "_db" + ".realm";

        if (realmConfiguration == null) {
            AppHelper.LogCat("Initializing Realm configuration.");
            setRealmConfiguration(new RealmConfiguration
                    .Builder()
                    .name(DatabaseName)
                    .schemaVersion(AppConstants.DatabaseVersion)
                    // .migration(new RealmMigrations())
                    .deleteRealmIfMigrationNeeded()
                    .build());
        }
    }

    public static void setRealmConfiguration(RealmConfiguration realmConfiguration) {
        WhatsCloneApplication.realmConfiguration = realmConfiguration;
        Realm.setDefaultConfiguration(realmConfiguration);
    }

    public static Realm getRealmDatabaseInstance(Context mContext) {

        initRealm();
        return Realm.getDefaultInstance();
    }

    private static void initNewRealm(Context mContext) {
        Realm.init(mContext);

        String DatabaseName = "PEFChat" + PreferenceManager.getInstance().getToken(AGApplication.getInstance()) + "_db" + ".realm";

        if (realmConfiguration == null) {
            AppHelper.LogCat("Initializing Realm configuration.");
            setRealmConfiguration(new RealmConfiguration
                    .Builder()
                    .name(DatabaseName)
                    .schemaVersion(AppConstants.DatabaseVersion)
                    // .migration(new RealmMigrations())
                    .deleteRealmIfMigrationNeeded()
                    .build());
        }

        //initializeRealmConfig();
    }

    public static void DeleteRealmDatabaseInstance() {
        Realm.getDefaultInstance().deleteAll();
        Realm.deleteRealm(WhatsCloneApplication.realmConfiguration);
    }

    public static void initRealm() {
        Realm.init(getInstance());
        initializeRealmConfig();
    }


    public static void setmInstance(Context mInstance) {
        WhatsCloneApplication.mInstance = mInstance;
    }

    public static HttpProxyCacheServer getProxy(Context context) {
        WhatsCloneApplication app = (WhatsCloneApplication) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(this)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for video cache
                .headerInjector(new UserAgentHeadersInjector())
                .cacheDirectory(FilesManager.getFileDataCached(getInstance(), "cache"))
                .build();
    }


    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }


    @SuppressLint("CheckResult")
    public void checkIfUserSession() {
        APIHelper.initialApiUsersContacts().checkIfUserSession().subscribe(networkModel -> {
            if (!networkModel.isConnected()) {
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_SESSION_EXPIRED));

            }
        }, throwable -> {
            AppHelper.LogCat("checkIfUserSession MainActivity " + throwable.getMessage());
        })
        ;
    }


}
