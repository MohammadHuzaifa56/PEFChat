package com.pefgloble.pefchate.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.interfaces.OnCallAudioEvents;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class AudioCallFragment extends Fragment {

    private View controlView;
    long countUp;
    public String time;

   // private OnCallAudioEvents callEvents;
    boolean isMute = false;
    boolean isSpeaker = false;
    private String currentUserId;
    private String currentUserName;

    private OnCallAudioEvents callEvents;

    private String currentUserImage;

    private Realm realm;
    String caller_id;

    @BindView(R.id.chrono)
    Chronometer stopWatch;

    @BindView(R.id.stopWatch)
    TextView tvStopWatch;

    @BindView(R.id.thumbnail)
    AppCompatImageView userImage;

    @BindView(R.id.diconnect_btn)
    AppCompatImageView cancelButton;

    @BindView(R.id.mute)
    AppCompatImageView mute;

    @BindView(R.id.speaker)
    AppCompatImageView speaker;

    @BindView(R.id.callerName)
    TextView tvCallerName;


    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controlView = inflater.inflate(R.layout.fargment_voice_call, container, false);
        ButterKnife.bind(this, controlView);

        caller_id = getActivity().getIntent().getStringExtra("caller_id");
        realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        UsersModel user = realm.where(UsersModel.class).equalTo("_id", caller_id).findFirst();

        if (user != null) {
            currentUserId = user.get_id();
            if (user.getUsername() != null)
                currentUserName = user.getUsername();
            else
                currentUserName = user.getPhone();

            currentUserImage = user.getImage();
        }

        if (caller_id.equals(currentUserId)) {

            tvCallerName.setText(currentUserName);
            try {

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

                    Glide.with(getActivity())
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + currentUserImage))
                            .signature(new ObjectKey(currentUserImage))
                            .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.useric)
                            .error(R.drawable.useric)
                            .into(target);

                } else {
                    userImage.setImageDrawable(AppHelper.getDrawable(getActivity(), R.drawable.useric));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else {

            UsersModel currentUser = realm.where(UsersModel.class).equalTo("_id", caller_id).findFirst();
            if (currentUser == null) {
                tvCallerName.setText(R.string.unknown);
            } else if (currentUser.getUsername() != null) {
                tvCallerName.setText(currentUser.getUsername());
            } else {
                tvCallerName.setText(currentUser.getPhone());
            }

            currentUserImage = currentUser.getImage();

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

                Glide.with(getActivity())
                        .asBitmap()
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + currentUserId + "/" + currentUserImage))
                        .signature(new ObjectKey(currentUserImage))
                        .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(R.drawable.bg_circle_image_holder)
                        .error(R.drawable.bg_circle_image_holder)
                        .into(target);

            } else {
                userImage.setImageDrawable(AppHelper.getDrawable(getActivity(), R.drawable.bg_circle_image_holder));
            }

        }
       // cancelButton.setOnClickListener(v -> callEvents.onCallHangUp(true));
        mute.setOnClickListener(v -> {
            callEvents.onMute();
            isMute = !isMute;
            if (isMute) {
                mute.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_mic_off_white_24dp));
            } else {
                mute.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_mic_white_24dp));
            }
        });
        speaker.setOnClickListener(v -> {
            callEvents.onSpeaker();
            isSpeaker = !isSpeaker;
            if (isSpeaker) {
                speaker.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_volume_off_white_24dp));
            } else {
                speaker.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_volume_up_white_24dp));
            }
        });


        return controlView;

    }


    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        this.callEvents = (OnCallAudioEvents) activity;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) realm.close();
    }

    public void startStopWatch() {

        stopWatch.setOnChronometerTickListener(chronometer -> {
            countUp = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
            time = String.format(Locale.getDefault(), "%02d:%02d:%02d", countUp / 3600, countUp / 60, countUp % 60);
            tvStopWatch.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", countUp / 3600, countUp / 60, countUp % 60));
        });
        stopWatch.setBase(SystemClock.elapsedRealtime());
        stopWatch.start();
        speaker.performClick();
        speaker.performClick();
    }
}
