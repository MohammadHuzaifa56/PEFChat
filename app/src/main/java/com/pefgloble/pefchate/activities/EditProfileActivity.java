package com.pefgloble.pefchate.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ProgressBar;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.ProfileResponse;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.activities.settings.EditUsernameActivity;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.BottomSheetEditProfile;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.ImageUtils;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.interfaces.LoadingData;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.presenter.EditProfilePresenter;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.SharedElementCallback;
import androidx.core.widget.NestedScrollView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.socket.client.Socket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS;


/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditProfileActivity extends BaseActivity implements LoadingData {

    @BindView(R.id.userAvatar)
    AppCompatImageView userAvatar;
    @BindView(R.id.addAvatar)
    FloatingActionButton addAvatar;
    @BindView(R.id.username)
    AppCompatTextView username;
    @BindView(R.id.status)
    EmojiTextView status;
    @BindView(R.id.numberPhone)
    AppCompatTextView numberPhone;
    @BindView(R.id.editProfile)
    NestedScrollView mView;
    @BindView(R.id.progress_bar_edit_profile)
    ProgressBar progressBar;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    private CompositeDisposable mDisposable;
    private UsersModel mContactsModel;
    private EditProfilePresenter mEditProfilePresenter;

    private String PicturePath;

    private Drawable drawable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        mDisposable = new CompositeDisposable();
        EventBus.getDefault().register(this);
        initializerView();


        //mEditProfilePresenter = new EditProfilePresenter(this);
        //mEditProfilePresenter.onCreate();
        ActivityCompat.setEnterSharedElementCallback(this, new SharedElementCallback() {
            @Override
            public void onSharedElementEnd(List<String> sharedElementNames, List<View> sharedElements, List<View> sharedElementSnapshots) {
                super.onSharedElementEnd(sharedElementNames, sharedElements, sharedElementSnapshots);
                addAvatar.setVisibility(View.GONE);
                final Animation animation = AnimationUtils.loadAnimation(EditProfileActivity.this, R.anim.scale_for_button_animtion_enter);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        addAvatar.setVisibility(View.VISIBLE);
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });

                addAvatar.startAnimation(animation);

            }
        });

    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        drawable = AppHelper.getDrawable(this, R.drawable.holder_user);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        addAvatar.setOnClickListener(v -> {
            BottomSheetEditProfile bottomSheetEditProfile = new BottomSheetEditProfile();
            bottomSheetEditProfile.show(getSupportFragmentManager(), bottomSheetEditProfile.getTag());
        });
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.statusLayout)
    public void launchStatus() {
      /*  Intent mIntent = new Intent(this, StatusActivity.class);
        startActivity(mIntent);*/
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.editUsernameBtn)
    public void launchEditUsername() {
        if (mContactsModel.getUsername() != null) {
            Intent mIntent = new Intent(this, EditUsernameActivity.class);
            mIntent.putExtra("currentUsername", mContactsModel.getUsername());
            startActivity(mIntent);
        } else {
            Intent mIntent = new Intent(this, EditUsernameActivity.class);
            mIntent.putExtra("currentUsername", "");
            startActivity(mIntent);
        }
    }

    /**
     * method to show contact info
     *
     * @param mContactsModel this is parameter for ShowContact  method
     */
    public void ShowContact(UsersModel mContactsModel) {
        final String finalName;
        String name = UtilsPhone.getContactName(mContactsModel.getPhone());
        if (name != null) {
            finalName = name;
        } else {
            finalName = mContactsModel.getPhone();
        }
        this.mContactsModel = mContactsModel;
        if (mContactsModel.getPhone() != null) {
            numberPhone.setText(mContactsModel.getPhone());
        }
        if (mContactsModel.getStatus() != null) {
            String state = UtilsString.unescapeJava(mContactsModel.getStatus().getBody());
            status.setText(state);
        } else {
            status.setText(getString(R.string.no_status));
        }
        if (mContactsModel.getUsername() != null) {
            username.setText(mContactsModel.getUsername());
        } else {
            username.setText(getString(R.string.no_username));
        }


        String ImageUrl = mContactsModel.getImage();
        String recipientId = mContactsModel.get_id();

        if (ImageUrl != null) {

            BitmapImageViewTarget target = new BitmapImageViewTarget(userAvatar) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    userAvatar.setImageBitmap(resource);


                }


                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    userAvatar.setImageDrawable(errorDrawable);
                }

                @Override
                public void onLoadStarted(Drawable placeHolderDrawable) {
                    super.onLoadStarted(placeHolderDrawable);
                    userAvatar.setImageDrawable(placeHolderDrawable);
                }
            };
            Glide.with(EditProfileActivity.this)
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))

                    .signature(new ObjectKey(ImageUrl))
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.EDIT_PROFILE_IMAGE_SIZE, AppConstants.EDIT_PROFILE_IMAGE_SIZE)
                    .into(target);
        } else {
            userAvatar.setImageDrawable(drawable);
        }

        userAvatar.setOnClickListener(v -> {
            if (mContactsModel.getImage() != null) {
                if (FilesManager.isFilePhotoProfileExists(this, FilesManager.getProfileImage(mContactsModel.getImage()))) {
                    AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE, mContactsModel.getImage(), mContactsModel.get_id());
                } else {
                    AppHelper.LaunchImagePreviewActivity(EditProfileActivity.this, AppConstants.PROFILE_IMAGE_FROM_SERVER, mContactsModel.getImage(), mContactsModel.get_id());
                }
            }
        });
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                final Animation animation = AnimationUtils.loadAnimation(EditProfileActivity.this, R.anim.scale_for_button_animtion_exit);
                animation.setAnimationListener(new Animation.AnimationListener() {
                    @Override
                    public void onAnimationStart(Animation animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animation animation) {
                        addAvatar.setVisibility(View.GONE);
                        if (AppHelper.isAndroid5()) {
                            ActivityCompat.finishAfterTransition(EditProfileActivity.this);
                        } else {
                            finish();


                        }
                    }

                    @Override
                    public void onAnimationRepeat(Animation animation) {

                    }
                });
                addAvatar.startAnimation(animation);

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        final Animation animation = AnimationUtils.loadAnimation(EditProfileActivity.this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                addAvatar.setVisibility(View.GONE);

                if (AppHelper.isAndroid5()) {
                    ActivityCompat.finishAfterTransition(EditProfileActivity.this);
                } else {
                    finish();

                }

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        addAvatar.startAnimation(animation);

    }

    @Override
    protected void onPause() {
        super.onPause();
        mEditProfilePresenter.onPause();
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mEditProfilePresenter.onDestroy();
        EventBus.getDefault().unregister(this);
        if (mDisposable != null) mDisposable.dispose();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mEditProfilePresenter.onActivityResult(this, requestCode, resultCode, data);
    }

    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case EVENT_BUS_IMAGE_PROFILE_PATH:
                progressBar.setVisibility(View.VISIBLE);
                PicturePath = String.valueOf(pusher.getData());
                if (PicturePath != null) {
                    try {
                        new UploadFileToServer().execute();
                    } catch (Exception e) {
                        AppHelper.LogCat(e);
                        AppHelper.CustomToast(EditProfileActivity.this, getString(R.string.oops_something));
                    }

                }
                break;
            case EVENT_BUS_UPDATE_CURRENT_SATUS:
                mEditProfilePresenter.loadData();
                break;
            case AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED:
                mEditProfilePresenter.loadData();
                break;
        }

    }


    @SuppressLint("CheckResult")
    private void setImage(String ImageUrl) {


        mEditProfilePresenter.editCurrentImage(ImageUrl, false);
        BitmapImageViewTarget target = new BitmapImageViewTarget(userAvatar) {


            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                userAvatar.setImageBitmap(resource);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MINE_IMAGE_PROFILE_UPDATED));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("ownerId", PreferenceManager.getInstance().getID(EditProfileActivity.this));
                    jsonObject.put("is_group", false);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                if (mSocket != null)
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_IMAGE_PROFILE_UPDATED, jsonObject);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                userAvatar.setImageDrawable(errorDrawable);
            }


            @Override
            public void onLoadStarted(Drawable placeholder) {
                super.onLoadStarted(placeholder);
                userAvatar.setImageDrawable(placeholder);
            }
        };

        Glide.with(EditProfileActivity.this)
                .asBitmap()

                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + PreferenceManager.getInstance().getID(this) + "/" + ImageUrl))
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(drawable)
                .error(drawable)
                .override(AppConstants.EDIT_PROFILE_IMAGE_SIZE, AppConstants.EDIT_PROFILE_IMAGE_SIZE)
                .into(target);
    }


    @Override
    protected void onResume() {
        super.onResume();

    }


    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable.getMessage());
    }


    /**
     * Uploading the image  to server
     */
    @SuppressLint("StaticFieldLeak")
    private class UploadFileToServer extends AsyncTask<Void, Integer, ProfileResponse> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            AppHelper.LogCat("onPreExecute  image ");
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            AppHelper.LogCat("progress image " + (int) (progress[0]));
        }

        @Override
        protected ProfileResponse doInBackground(Void... params) {
            return uploadFile();
        }

        private ProfileResponse uploadFile() {

            RequestBody requestFile;
            final ProfileResponse profileResponse = null;
            if (PicturePath != null) {
                byte[] imageByte = ImageUtils.compressImage(PicturePath);
                // create RequestBody instance from file
                requestFile = RequestBody.create(MediaType.parse("image/*"), imageByte);
            } else {
                requestFile = null;
            }
            EditProfileActivity.this.runOnUiThread(() -> AppHelper.showDialog(EditProfileActivity.this, "Updating ... "));
            if (requestFile == null) {
                AppHelper.CustomToast(EditProfileActivity.this, getString(R.string.oops_something));
            } else {
                File file = new File(PicturePath);
                mDisposable.add(APIHelper.initializeUploadFiles().uploadUserImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile), PreferenceManager.getInstance().getID(EditProfileActivity.this)).subscribe(response -> {
                    if (response.isSuccess()) {
                        if (PicturePath != null) {
                            file.delete();
                        }
                        runOnUiThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                            realm.executeTransactionAsync(realm1 -> {
                                UsersModel contactsModel = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(EditProfileActivity.this)).findFirst();
                                contactsModel.setImage(response.getFilename());
                                realm1.copyToRealmOrUpdate(contactsModel);

                            }, () -> new Handler().postDelayed(() -> {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.hideDialog();
                                AppHelper.CustomToast(EditProfileActivity.this, response.getMessage());
                                setImage(response.getFilename());
                            }, 700), error -> AppHelper.LogCat("error update group image in group model " + error.getMessage()));
                            realm.close();
                        });
                    } else {
                        AppHelper.CustomToast(EditProfileActivity.this, response.getMessage());
                        AppHelper.hideDialog();
                    }
                }, throwable -> {
                    AppHelper.hideDialog();
                    AppHelper.CustomToast(EditProfileActivity.this, getString(R.string.failed_upload_image));
                    AppHelper.LogCat("Failed  upload your image " + throwable.getMessage());
                    runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                }))
                ;
            }
            return profileResponse;
        }


        @Override
        protected void onPostExecute(ProfileResponse response) {
            super.onPostExecute(response);
            // AppHelper.LogCat("Response from server: " + response);
        }
    }
}
