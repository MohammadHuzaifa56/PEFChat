package com.pefgloble.pefchate.activities;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.animation.RevealAnimation;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.call.CallManager;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.presenter.ProfilePreviewPresenter;
import com.vanniktech.emoji.EmojiTextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.AppCompatImageView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfilePreviewActivity extends Activity {

    @BindView(R.id.userProfileName)
    TextView userProfileName;
    @BindView(R.id.ContactBtn)
    AppCompatImageView ContactBtn;
    @BindView(R.id.AboutBtn)
    AppCompatImageView AboutBtn;
    @BindView(R.id.CallBtn)
    AppCompatImageView CallBtn;
    @BindView(R.id.CallVideoBtn)
    AppCompatImageView CallVideoBtn;
    @BindView(R.id.userProfilePicture)
    AppCompatImageView userProfilePicture;
    @BindView(R.id.actionProfileArea)
    LinearLayout actionProfileArea;
    @BindView(R.id.invite)
    TextView actionProfileInvite;
    @BindView(R.id.containerProfile)
    FrameLayout containerProfile;
    @BindView(R.id.containerProfileInfo)
    LinearLayout containerProfileInfo;

    private String groupname = null;
    public String userID;
    public String groupID;
    public String conversationID;
    private boolean isGroup;
    private long Duration = 500;
    private Intent mIntent;
    private boolean isImageLoaded = false;
    private String ImageUrl;
    private String ImageUrlFile;
    private ProfilePreviewPresenter mProfilePresenter;
    private String finalName;
    private RevealAnimation revealAnimation;
    /*
     */
    /**/

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (AppHelper.isAndroid5()) {
            getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            getWindow().setStatusBarColor(AppHelper.getColor(this, R.color.colorPrimaryDark));
        }

        // Make us non-modal, so that others can receive touch events.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL, WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL);
        // but notify us that it happened.
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH, WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH);

        setContentView(R.layout.activity_profile_preview);
        ButterKnife.bind(this);
        initializerView();

        userProfileName.setSelected(true);

        isGroup = getIntent().getExtras().getBoolean("isGroup");
        userID = getIntent().getExtras().getString("userID");

        if (getIntent().hasExtra("groupID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            groupID = getIntent().getExtras().getString("groupID");
            conversationID = getIntent().getExtras().getString("conversationID");
        }
        mProfilePresenter = new ProfilePreviewPresenter(this);
        mProfilePresenter.onCreate();

        if (getIntent() != null) {
            revealAnimation = new RevealAnimation(containerProfileInfo, getIntent(), this);
            revealAnimation.setCustomAnimatorListener(new RevealAnimation.CustomAnimatorListener() {
                @Override
                public void onAnimationStart() {

                }

                @Override
                public void onAnimationEnd() {

                }
            });
        }
        /*if (AppHelper.isAndroid5()) {
            containerProfileInfo.post(() -> AnimationsUtil.show(containerProfileInfo, Duration));
        }*/
        if (isGroup) {
            CallBtn.setVisibility(View.GONE);
            CallVideoBtn.setVisibility(View.GONE);
        } else {
            CallBtn.setVisibility(View.VISIBLE);
            CallVideoBtn.setVisibility(View.VISIBLE);
        }
    }
    /**
     * method to initialize the view
     */
    private void initializerView() {
        if (AppHelper.isAndroid5()) {
            userProfilePicture.setTransitionName(getString(R.string.user_image_transition));
            userProfileName.setTransitionName(getString(R.string.user_name_transition));
        }
        ContactBtn.setOnClickListener(v -> {
            if (isGroup) {
                Intent messagingIntent = new Intent(this, MessagesActivity.class);
                messagingIntent.putExtra("conversationID", conversationID);
                messagingIntent.putExtra("groupID", groupID);
                messagingIntent.putExtra("isGroup", true);
                startActivity(messagingIntent);
                AnimationsUtil.setTransitionAnimation(this);
                finish();
            } else {
                Intent messagingIntent = new Intent(this, MessagesActivity.class);
                //  messagingIntent.putExtra("conversationID", "");
                messagingIntent.putExtra("recipientID", userID);
                messagingIntent.putExtra("isGroup", false);
                startActivity(messagingIntent);
                AnimationsUtil.setTransitionAnimation(this);
                finish();
            }
        });
        AboutBtn.setOnClickListener(v -> {
            if (isGroup) {
            /*    if (AppHelper.isAndroid5()) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userProfilePicture, "userAvatar"), new Pair<>(userProfileName, "userName"));
                    mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("groupID", groupID);
                    mIntent.putExtra("isGroup", true);
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {*/
                mIntent = new Intent(this, ProfileActivity.class);
                mIntent.putExtra("groupID", groupID);
                mIntent.putExtra("isGroup", true);
                startActivity(mIntent);
                AnimationsUtil.setTransitionAnimation(this);
                finish();
                // }
            } else {/*
                if (AppHelper.isAndroid5()) {
                    ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(this, new Pair<>(userProfilePicture, "userAvatar"), new Pair<>(userProfileName, "userName"));
                    Intent mIntent = new Intent(this, ProfileActivity.class);
                    mIntent.putExtra("userID", userID);
                    mIntent.putExtra("isGroup", false);
                    startActivity(mIntent, options.toBundle());
                    finish();
                } else {*/
                mIntent = new Intent(this, ProfileActivity.class);
                mIntent.putExtra("userID", userID);
                mIntent.putExtra("isGroup", false);
                startActivity(mIntent);
                AnimationsUtil.setTransitionAnimation(this);
                finish();
                //}
            }

        });
        CallBtn.setOnClickListener(v -> {
            if (!isGroup) {
                CallManager.callContact(this, false, userID);
            }
        });
        CallVideoBtn.setOnClickListener(v -> {
            if (!isGroup) {
                CallManager.callContact(this, true, userID);
            }
        });
        containerProfile.setOnClickListener(v -> {
          /*  if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();*/
            revealAnimation.unRevealActivity();
        });
        containerProfileInfo.setOnClickListener(v -> {
           /* if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();*/
            revealAnimation.unRevealActivity();
        });
        userProfilePicture.setOnClickListener(v -> {
            if (isImageLoaded) {
                if (ImageUrlFile != null) {
                    if (FilesManager.isFilePhotoProfileExists(this, FilesManager.getProfileImage(ImageUrlFile))) {
                        AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE, ImageUrlFile,userID);
                    } else {
                        AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE_FROM_SERVER, ImageUrlFile,userID);
                    }
                }
            }
        });

    }


    /**
     * method to show user information
     *
     * @param usersModel this is parameter for  ShowContact method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ShowContact(UsersModel usersModel) {

        try {
            UpdateUI(usersModel, null);
        } catch (Exception e) {
            AppHelper.LogCat(" Profile preview Exception" + e.getMessage());
        }
    }

    /**
     * method to show group information
     *
     * @param groupsModel this is parameter for   ShowGroup method
     */
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    public void ShowGroup(GroupModel groupsModel) {

        try {
            UpdateUI(null, groupsModel);
        } catch (Exception e) {
            AppHelper.LogCat(" Profile preview Exception" + e.getMessage());
        }

    }

    /**
     * method to update the UI
     *
     * @param mContactsModel this is the first parameter for  UpdateUI  method
     * @param mGroupsModel   this is the second parameter for   UpdateUI  method
     */
    @SuppressLint("StaticFieldLeak")
    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private void UpdateUI(UsersModel mContactsModel, GroupModel mGroupsModel) {



        if (isGroup) {
            ImageUrlFile = mGroupsModel.getImage();
            ImageUrl = EndPoints.ROWS_GROUP_IMAGE_URL +  ImageUrlFile;


            RequestBuilder<Drawable> thumbnailRequest = Glide.with(this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrl))
                    .override(AppConstants.PROFILE_PREVIEW_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_IMAGE_SIZE)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);
            if (mGroupsModel.getName() != null) {
                groupname = UtilsString.unescapeJava(mGroupsModel.getName());
                userProfileName.setText(groupname);

            }


            Drawable drawable = AppHelper.getDrawable(this, R.drawable.gropic);

            if (ImageUrlFile != null) {
                DrawableImageViewTarget target = new DrawableImageViewTarget(userProfilePicture) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        userProfilePicture.setImageDrawable(resource);
                        isImageLoaded = true;
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        userProfilePicture.setImageDrawable(errorDrawable);
                        isImageLoaded = false;
                    }

                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userProfilePicture.setImageDrawable(placeHolderDrawable);
                    }
                };
                Glide.with(getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrl))
                        .signature(new ObjectKey(ImageUrlFile))
                        .thumbnail(thumbnailRequest)
                        .centerCrop()
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.PROFILE_PREVIEW_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_IMAGE_SIZE)
                        .into(target);

            } else {

                userProfilePicture.setImageDrawable(drawable);
                isImageLoaded = false;
            }
            actionProfileArea.setVisibility(View.VISIBLE);

        } else {


            if (mContactsModel.isLinked() && mContactsModel.isActivate()) {
                actionProfileArea.setVisibility(View.VISIBLE);
                actionProfileInvite.setVisibility(View.GONE);
            } else {
                actionProfileArea.setVisibility(View.GONE);
                actionProfileInvite.setVisibility(View.VISIBLE);
            }
            ImageUrlFile = mContactsModel.getImage();
            ImageUrl = EndPoints.ROWS_IMAGE_URL + mContactsModel.get_id() + "/" + ImageUrlFile;


            RequestBuilder<Drawable> thumbnailRequest = Glide.with(this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrl))
                    .override(AppConstants.PROFILE_PREVIEW_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_IMAGE_SIZE)
                    .diskCacheStrategy(DiskCacheStrategy.ALL);

                finalName = UtilsPhone.getContactName(mContactsModel.getPhone());

                if (finalName != null) {
                    userProfileName.setText(finalName);
                } else {
                    userProfileName.setText(mContactsModel.getPhone());
                    finalName = mContactsModel.getPhone();
                }

            Drawable drawable = AppHelper.getDrawable(this, R.drawable.useric);

            if (ImageUrlFile != null) {

                DrawableImageViewTarget target = new DrawableImageViewTarget(userProfilePicture) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        userProfilePicture.setImageDrawable(resource);

                        isImageLoaded = true;

                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        userProfilePicture.setImageDrawable(errorDrawable);
                        isImageLoaded = false;
                    }

                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userProfilePicture.setImageDrawable(placeHolderDrawable);
                    }
                };
                Glide.with(getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(ImageUrl))
                        .signature(new ObjectKey(ImageUrlFile))
                        .thumbnail(thumbnailRequest)
                        .centerCrop()
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.PROFILE_PREVIEW_IMAGE_SIZE, AppConstants.PROFILE_PREVIEW_IMAGE_SIZE)
                        .into(target);


            } else {

                userProfilePicture.setImageDrawable(drawable);
                isImageLoaded = false;
            }


        }
    }


    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable.getMessage());
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mProfilePresenter != null)
            mProfilePresenter.onDestroy();
    }


    @Override
    public void onBackPressed() {
      /*  if (AppHelper.isAndroid5())
            containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
        else
            finish();*/
        revealAnimation.unRevealActivity();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        // If we've received a touch notification that the user has touched
        // outside the app, finish the activity.
        if (MotionEvent.ACTION_OUTSIDE == event.getAction()) {
            /*if (AppHelper.isAndroid5())
                containerProfileInfo.post(() -> AnimationsUtil.hide(this, containerProfileInfo, Duration));
            else
                finish();*/
            revealAnimation.unRevealActivity();
            return true;
        }

        return super.onTouchEvent(event);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }
}
