package com.pefgloble.pefchate.activities.call;

import android.Manifest;
import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.calls.CallsInfoModel;
import com.pefgloble.pefchate.JsonClasses.calls.CallsModel;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.call.CallingApi;
import com.pefgloble.pefchate.helpers.files.RealmBackupRestore;
import com.pefgloble.pefchate.helpers.files.UniqueId;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.ui.CallAnswerDeclineButton;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.json.JSONObject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.fragment.app.FragmentActivity;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;


public class IncomingCallScreen extends FragmentActivity implements CallAnswerDeclineButton.AnswerDeclineListener {


    @BindView(R.id.tvAudioVideoCall)
    TextView tvAudioVideoCall;

    @BindView(R.id.userImage)
    AppCompatImageView userImage;


    @BindView(R.id.callerName)
    TextView tvCallerName;


    @BindView(R.id.answer_decline_button)
    CallAnswerDeclineButton callAnswerDeclineButton;
    boolean isVideoCall;
    String callType;
    String call_id;
    String callId;
    String caller_id;

    private boolean isAttendButtonIsClicked = false;

    Ringtone ringtone;
    CountDownTimer timer;

    long autoRejectDelay = 60 * 1000;

    private UsersModel currentUser;
    private Realm realm;
    private String currentUserImage;
    private String lastID;
    private boolean isSaved = false;

    @SuppressLint("StaticFieldLeak")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_incoming);
        ButterKnife.bind(this);
        Window window = getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD);
        window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED);
        window.addFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

    /*    if (PreferenceManager.getInstance().getKeyAppKilled(this)) {
            this.timer = new CountDownTimer(autoRejectDelay, 1000) {
                @Override
                public void onTick(long millisUntilFinished) {
                }

                @Override
                public void onFinish() {
                    if (!IncomingCallScreen.this.isAttendButtonIsClicked) {
                        //  Toast.makeText(IncomingCallScreen.this, "Timeout", Toast.LENGTH_LONG).show();
                        IncomingCallScreen.this.finish();
                    }
                }
            };
            this.timer.start();
        }*/


        callAnswerDeclineButton.setAnswerDeclineListener(this);
        callAnswerDeclineButton.setVisibility(View.VISIBLE);
        callAnswerDeclineButton.startRingingAnimation();
        EventBus.getDefault().register(this);


        if (getIntent().getExtras() != null) {
            this.call_id = getIntent().getExtras().getString("call_id", "");
            this.callId = getIntent().getExtras().getString("callId", "");
            this.caller_id = getIntent().getExtras().getString("call_from", "");
            this.callType = getIntent().getExtras().getString("callType", AppConstants.VOICE_CALL);
        }
        isVideoCall = callType.equals(AppConstants.VIDEO_CALL);
        realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        AppHelper.LogCat("caller_id " + caller_id);
        this.currentUser = realm.where(UsersModel.class).equalTo("_id", caller_id).findFirst();
        AppHelper.LogCat("currentUser " + currentUser.get_id());

        if (!(this.currentUser == null || this.currentUser.getImage() == null)) {
            try {

                currentUserImage = currentUser.getImage();
                String currentUserId = currentUser.get_id();

                if (currentUserImage != null) {
                    BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            userImage.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            userImage.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            userImage.setImageDrawable(placeholder);
                        }


                    };

                    Glide.with(this)
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + currentUserImage))
                            .signature(new ObjectKey(currentUserImage))
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.bg_circle_image_holder)
                            .error(R.drawable.bg_circle_image_holder)
                            .into(target);

                } else {
                    userImage.setImageDrawable(AppHelper.getDrawable(this, R.drawable.bg_circle_image_holder));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        try {
            this.ringtone = RingtoneManager.getRingtone(getApplicationContext(), RingtoneManager.getDefaultUri(1));
            this.ringtone.play();
        } catch (Exception e) {
            e.printStackTrace();
        }
        setupView();
        this.timer = new CountDownTimer(autoRejectDelay, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
            }

            @Override
            public void onFinish() {
                //  Toast.makeText(IncomingCallScreen.this, "Timeout", Toast.LENGTH_LONG).show();
                if (!IncomingCallScreen.this.isAttendButtonIsClicked) {
                    //  Toast.makeText(IncomingCallScreen.this, "Timeout", Toast.LENGTH_LONG).show();
                   IncomingCallScreen.this.finish();
                }
            }
        };
        this.timer.start();
        addNewCall();
    }

    private void setupView() {
        if (this.currentUser == null) {
            this.tvCallerName.setText(R.string.unknown);
        } else if (this.currentUser.getUsername() != null) {
            this.tvCallerName.setText(currentUser.getUsername());
        } else {
            this.tvCallerName.setText(currentUser.getPhone());
        }
        if (this.callType.contentEquals(AppConstants.VOICE_CALL)) {
            this.tvAudioVideoCall.setText(getResources().getString(R.string.voice_call));
        } else {
            this.tvAudioVideoCall.setText(getResources().getString(R.string.video_call));
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        try {
            this.timer.cancel();
            this.ringtone.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
        EventBus.getDefault().unregister(this);
        if (!realm.isClosed()) realm.close();
    }

    /**
     * method of EventBus
     *
     * @param object this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(JSONObject object) {
        try {

            AppHelper.LogCat("onEventMainThread " + "CallEventChange " + object.toString());
            if (object.getString("eventName").contentEquals("CallEventChange")) {
                String status = object.getString("status");
                if (status.contentEquals("Reject")) {
                    AppHelper.LogCat("Reject call incoming " + object.toString());
                    this.ringtone.stop();
                    finish();
                }
            }
        } catch (Exception e) {
            AppHelper.LogCat("onEventMainThread f " + "CallEventChange " + e.getMessage());
        }
    }

    private void addNewCall() {
        try {
            saveToDataBase();

        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
        }
    }


    @SuppressLint("CheckResult")
    public void saveToDataBase() {
        if (currentUser == null) return;
        String phone = currentUser.getPhone();
        String username = currentUser.getUsername();
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        DateTime current = new DateTime();
        String callTime = String.valueOf(current);

        String historyCallId = getHistoryCallId(currentUser.get_id(), PreferenceManager.getInstance().getID(this), isVideoCall, realm);

        try {

            if (historyCallId.equals("")) {
                realm.executeTransactionAsync(realm1 -> {
                    UsersModel contactsModel1 = realm1.where(UsersModel.class).equalTo("_id", caller_id).findFirst();

                    String lastID = RealmBackupRestore.getCallLastId();
                    CallsModel callsModel = realm1.createObject(CallsModel.class, UniqueId.generateUniqueId());
                    callsModel.set_id(lastID);
                    if (isVideoCall)
                        callsModel.setType(AppConstants.VIDEO_CALL);
                    else
                        callsModel.setType(AppConstants.VOICE_CALL);
                    callsModel.setContactsModel(contactsModel1);
                    callsModel.setPhone(phone);
                    callsModel.setUsername(username);
                    callsModel.setCounter(1);
                    callsModel.setFrom(contactsModel1.get_id());
                    callsModel.setTo(PreferenceManager.getInstance().getID(this));
                    callsModel.setDuration("00:00");
                    callsModel.setDate(callTime);
                    callsModel.setReceived(true);

                    CallsInfoModel callsInfoModel = realm1.createObject(CallsInfoModel.class, UniqueId.generateUniqueId());
                    RealmList<CallsInfoModel> callsInfoModelRealmList = new RealmList<CallsInfoModel>();

                    callsInfoModel.set_id(callId);
                    if (isVideoCall)
                        callsInfoModel.setType(AppConstants.VIDEO_CALL);
                    else
                        callsInfoModel.setType(AppConstants.VOICE_CALL);
                    callsInfoModel.setContactsModel(contactsModel1);
                    callsInfoModel.setPhone(phone);
                    callsInfoModel.setCallId(lastID);
                    callsInfoModel.setFrom(contactsModel1.get_id());
                    callsInfoModel.setTo(PreferenceManager.getInstance().getID(this));
                    callsInfoModel.setDuration("00:00");
                    callsInfoModel.setDate(callTime);
                    callsInfoModel.setReceived(true);
                    callsInfoModelRealmList.add(callsInfoModel);
                    callsModel.setCallsInfoModels(callsInfoModelRealmList);
                    realm1.copyToRealmOrUpdate(callsModel);
                    this.lastID = lastID;
                    //EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, lastID));
                }, () -> {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_CALL_NEW_ROW, this.lastID));
                });
            } else {

                realm.executeTransactionAsync(realm1 -> {
                    UsersModel contactsModel1 = realm1.where(UsersModel.class).equalTo("_id", caller_id).findFirst();

                    int callCounter;
                    CallsModel callsModel;
                    RealmQuery<CallsModel> callsModelRealmQuery = realm1.where(CallsModel.class).equalTo("_id", historyCallId);
                    callsModel = callsModelRealmQuery.findAll().first();

                    callCounter = callsModel.getCounter();
                    callCounter++;
                    callsModel.setDate(callTime);
                    callsModel.setCounter(callCounter);
                    callsModel.setDuration("00:00");
                    CallsInfoModel callsInfoModel = realm1.createObject(CallsInfoModel.class, UniqueId.generateUniqueId());
                    RealmList<CallsInfoModel> callsInfoModelRealmList = callsModel.getCallsInfoModels();
                    callsInfoModel.set_id(callId);
                    if (isVideoCall)
                        callsInfoModel.setType(AppConstants.VIDEO_CALL);
                    else
                        callsInfoModel.setType(AppConstants.VOICE_CALL);
                    callsInfoModel.setContactsModel(contactsModel1);
                    callsInfoModel.setPhone(phone);
                    callsInfoModel.setCallId(callsModel.get_id());
                    callsInfoModel.setFrom(contactsModel1.get_id());
                    callsInfoModel.setTo(PreferenceManager.getInstance().getID(this));
                    callsInfoModel.setDuration("00:00");
                    callsInfoModel.setDate(callTime);
                    callsInfoModel.setReceived(true);
                    callsInfoModelRealmList.add(callsInfoModel);
                    callsModel.setCallsInfoModels(callsInfoModelRealmList);

                    realm1.copyToRealmOrUpdate(callsModel);
                    //  EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
                }, () -> {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CALL_OLD_ROW, historyCallId));
                });
            }

        } finally {

            if (!realm.isClosed()) realm.close();
        }

    }


    private String getHistoryCallId(String fromId, String toId, boolean isVideoCall, Realm realm) {
        String type;
        CallsModel callsModel;
        if (isVideoCall)
            type = AppConstants.VIDEO_CALL;
        else
            type = AppConstants.VOICE_CALL;


        try {

            callsModel = realm.where(CallsModel.class)
                    .equalTo("from", fromId)
                    .equalTo("to", toId)
                    .equalTo("received", false)
                    .equalTo("type", type)
                    .findAll().first();
            return callsModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("call history id Exception " + e.getMessage());
            return "";
        }
    }

    @Override
    public void onAnswered() {
        AppHelper.LogCat("onAnswered");
        Permissions.with(this)
                .request(Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA)
                .ifNecessary()
                .withRationaleDialog(getString(R.string.to_answer_the_call_from_s_give_app_access_to_your_microphone, currentUser.getUsername()),
                        R.drawable.ic_mic_white_24dp, R.drawable.ic_videocam_white_24dp)
                .withPermanentDenialDialog(getString(R.string.app_requires_microphone_and_camera_permissions_in_order_to_make_or_receive_calls))
                .onAllGranted(() -> {
                    this.isAttendButtonIsClicked = true;
                    CallingApi.startCall(this, this.callType, this.call_id, true, caller_id, callId);
                    finish();
                })
                .onAnyDenied(() -> {
                    if (this.callType.equals(AppConstants.VIDEO_CALL))
                        CallingApi.sendCallEvent("Reject", currentUser.get_id(), this.call_id, true);
                    else
                        CallingApi.sendCallEvent("Reject", currentUser.get_id(), this.call_id, false);
                    try {
                        this.ringtone.stop();
                    } catch (Exception e2) {
                        e2.printStackTrace();
                    }
                    finish();
                })
                .execute();
    }

    @Override
    public void onDeclined() {
        AppHelper.LogCat("onDeclined");
        if (this.callType.equals(AppConstants.VIDEO_CALL))
            CallingApi.sendCallEvent("Reject", currentUser.get_id(), this.call_id, true);
        else
            CallingApi.sendCallEvent("Reject", currentUser.get_id(), this.call_id, false);
        try {
            this.ringtone.stop();
        } catch (Exception e2) {
            e2.printStackTrace();
        }
        finish();
    }
}
