package com.pefgloble.pefchate.fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Fragment;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.TextView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.interfaces.OnCallVideoEvents;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import jp.wasabeef.glide.transformations.BlurTransformation;


public class VideoCallFragment extends Fragment {
    private View controlView;
    private long countUp;
    public String time = null;


    boolean isFrontCamera = false;
    boolean isMute = false;

    private UsersModel currentUser;


    private String currentUserId;
    private String currentUserName;
    private Realm realm;
    private String currentUserImage;
    private String caller_id;

    @BindView(R.id.animation_view)
    public
    LottieAnimationView animation_view;

    @BindView(R.id.callerName)
    TextView callerName;

    @BindView(R.id.userImage)
    AppCompatImageView userImage;

    @BindView(R.id.chrono)
    Chronometer chronometer;

    @BindView(R.id.stopWatch)
    TextView stopWatch;

    @SuppressLint("StaticFieldLeak")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        controlView = inflater.inflate(R.layout.newlayoutvideo, container, false);
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
            callerName.setText(currentUserName);

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
                            .placeholder(R.drawable.bg_circle_image_holder)
                            .error(R.drawable.bg_circle_image_holder)
                            .into(target);

                } else {
                    userImage.setImageDrawable(AppHelper.getDrawable(getActivity(), R.drawable.bg_circle_image_holder));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

        } else {
            this.currentUser = realm.where(UsersModel.class).equalTo("_id", caller_id).findFirst();
            if (this.currentUser == null) {
                callerName.setText(R.string.unknown);
            } else if (this.currentUser.getUsername() != null) {
                callerName.setText(this.currentUser.getUsername());
            } else {
                callerName.setText(currentUser.getPhone());
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
      /*  this.mute.setOnClickListener(v -> {
           *//* callEvents.onMute();
            isMute = !isMute;
            if (isMute) {
                mute.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_videocam_off_white_24dp));
            } else {
                mute.setImageDrawable(AppHelper.getVectorDrawable(getActivity(), R.drawable.ic_videocam_white_24dp));
            }*//*
        });*/
        return controlView;
    }


    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
      //  this.callEvents = (OnCallVideoEvents) activity;
    }

    public void startStopWatch() {
        AppHelper.LogCat("startStopWatch");
        userImage.setVisibility(View.GONE);
        chronometer.setOnChronometerTickListener(chronometer -> {
            countUp = (SystemClock.elapsedRealtime() - chronometer.getBase()) / 1000;
            time = String.format(Locale.getDefault(), "%02d:%02d:%02d", countUp / 3600, countUp / 60, countUp % 60);
            stopWatch.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", countUp / 3600, countUp / 60, countUp % 60));

        });
        chronometer.setBase(SystemClock.elapsedRealtime());
        chronometer.start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) realm.close();
    }
}
