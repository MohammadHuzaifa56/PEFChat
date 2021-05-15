package com.pefgloble.pefchate.HomeScreens;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.socket.client.IO;
import io.socket.client.Socket;
import io.socket.emitter.Emitter;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;

import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

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
import com.pefgloble.pefchate.RegistrationScreens.SignIn_Methods;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.activities.EditProfileActivity;
import com.pefgloble.pefchate.activities.media.PickerBuilder;
import com.pefgloble.pefchate.activities.settings.EditUsernameActivity;
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
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.presenter.EditProfilePresenter;
import com.pefgloble.pefchate.staus.StatusActivity;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

import static android.app.Activity.RESULT_OK;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS;

/**
 * A simple {@link Fragment} subclass.

 * create an instance of this fragment.
 */
public class ProfilePage extends Fragment {
    AppCompatImageView btnEditName,btnEditDesign,userAvatar;
    AppCompatTextView tvName,tvDesig,tvPhone,tvStatus;
    EditProfilePresenter editProfilePresenter;
    UsersModel mContactsModel;
    ProgressBar progressBar;
    Button btnLog;
    Drawable drawable;
    CompositeDisposable mCompositeDisposable;
    private String PicturePath;
    FloatingActionButton fabAdd;

    public ProfilePage() {

    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view=inflater.inflate(R.layout.fragment_profile_page, container, false);
        EventBus.getDefault().register(this);
        mCompositeDisposable=new CompositeDisposable();
        drawable = ContextCompat.getDrawable(getContext(),R.drawable.useric);
        btnEditName=view.findViewById(R.id.editUsernameBtn);
        btnEditDesign=view.findViewById(R.id.editUserDesig);
        tvName=view.findViewById(R.id.username);
        tvDesig=view.findViewById(R.id.userdesig);
        fabAdd=view.findViewById(R.id.addAvatar);
        btnLog=view.findViewById(R.id.btnLog);
        tvStatus=view.findViewById(R.id.status);
        tvPhone=view.findViewById(R.id.numberPhone);
        progressBar=view.findViewById(R.id.progress_bar_edit_profile);
        userAvatar=view.findViewById(R.id.userAvatar);

        String name=PreferenceManager.getInstance().getName(getContext());
        String desig=PreferenceManager.getInstance().getUserDesig(getContext());

        editProfilePresenter = new EditProfilePresenter(this);
        editProfilePresenter.onCreate();
        //tvName.setText(name);
        //tvDesig.setText(desig);

        btnEditName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getContext(), EditUsernameActivity.class);
                mIntent.putExtra("currentUsername", name);
                startActivity(mIntent);
            }
        });

        tvStatus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getContext(), StatusActivity.class);
                startActivity(mIntent);
            }
        });

        btnLog.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              PreferenceManager.getInstance().setRegistered(AGApplication.getInstance(),false);
              startActivity(new Intent(getContext(),SignIn_Methods.class));
              getActivity().finish();
            }
        });

        btnEditDesign.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent mIntent = new Intent(getContext(), EditUsernameActivity.class);
                mIntent.putExtra("currentDesignation", desig);
                startActivity(mIntent);
            }
        });
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppHelper.LogCat("Read data permission already granted.");
                new PickerBuilder(getActivity(), PickerBuilder.SELECT_FROM_GALLERY)
                        .setOnImageReceivedListener(imageUri -> {
                            Intent data = new Intent();
                            data.setData(imageUri);
                            AppHelper.LogCat("new image SELECT_FROM_GALLERY" + imageUri);
                            editProfilePresenter.onActivityResult(getActivity(), AppConstants.SELECT_PROFILE_PICTURE, RESULT_OK, data);

                        })
                        .setImageName(getActivity().getString(R.string.app_name))
                        .setImageFolderName(getActivity().getString(R.string.app_name))
                        .setCropScreenColor(R.color.colorPrimary)
                        .withTimeStamp(false)
                        .setOnPermissionRefusedListener(() -> {
                            Toast.makeText(getActivity(), getString(R.string.all_permission_required), Toast.LENGTH_LONG).show();
                        })
                        .start();
            }
        });

        return view;
    }
    public void ShowContact(UsersModel mContactsModel) {
        final String finalName;
        String name = UtilsPhone.getContactName(mContactsModel.getPhone());
        this.mContactsModel=mContactsModel;
        if (mContactsModel.getPhone() != null) {
            tvPhone.setText(mContactsModel.getPhone());
        }
        else {
            tvPhone.setText("Phone Number");
        }
        if (mContactsModel.getStatus() != null) {
            String state = UtilsString.unescapeJava(mContactsModel.getStatus().getBody());
            tvStatus.setText(state);
        } else {
            tvStatus.setText(getString(R.string.no_status));
        }
        if (mContactsModel.getUsername() != null) {
            tvName.setText(mContactsModel.getUsername());
        } else {
            tvName.setText(getString(R.string.no_username));
        }
        if (mContactsModel.getDesignation()!=null){
            tvDesig.setText(mContactsModel.getDesignation());
        }
        else {
            tvDesig.setText("Designation");
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
            Glide.with(getContext())
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
                if (FilesManager.isFilePhotoProfileExists(getContext(), FilesManager.getProfileImage(mContactsModel.getImage()))) {
                    AppHelper.LaunchImagePreviewActivity(getActivity(), AppConstants.PROFILE_IMAGE, mContactsModel.getImage(), mContactsModel.get_id());
                } else {
                    AppHelper.LaunchImagePreviewActivity(getActivity(), AppConstants.PROFILE_IMAGE_FROM_SERVER, mContactsModel.getImage(), mContactsModel.get_id());
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
        if (mCompositeDisposable != null) mCompositeDisposable.dispose();
    }
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
                        AppHelper.CustomToast(getContext(), getString(R.string.oops_something));
                    }

                }
                break;
            case EVENT_BUS_UPDATE_CURRENT_SATUS:
                editProfilePresenter.loadData();
                break;
            case AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED:
                editProfilePresenter.loadData();
                break;
        }

    }
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
            getActivity().runOnUiThread(() -> AppHelper.showDialog(getContext(), "Updating ... "));
            if (requestFile == null) {
                AppHelper.CustomToast(getContext(), getString(R.string.oops_something));
            } else {
                File file = new File(PicturePath);
                mCompositeDisposable.add(APIHelper.initializeUploadFiles().uploadUserImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile), PreferenceManager.getInstance().getID(getContext())).subscribe(response -> {
                    if (response.isSuccess()) {
                        if (PicturePath != null) {
                            file.delete();
                        }
                        getActivity().runOnUiThread(() -> {
                            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                            realm.executeTransactionAsync(realm1 -> {
                                UsersModel contactsModel = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(getContext())).findFirst();
                                contactsModel.setImage(response.getFilename());
                                PreferenceManager.getInstance().setImageUrl(AGApplication.getInstance(),response.getFilename());

                                        JSONObject jsonObject = new JSONObject();
                                        try {
                                            jsonObject.put("ownerId", PreferenceManager.getInstance().getID(getContext()));
                                            jsonObject.put("is_group", false);
                                            jsonObject.put("desig",PreferenceManager.getInstance().getUserDesig(AGApplication.getInstance()));
                                            jsonObject.put("image",response.getFilename());
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                        Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                                        if (mSocket != null)
                                            mSocket.emit(AppConstants.SocketConstants.SOCKET_IMAGE_PROFILE_UPDATED, jsonObject);

                                realm1.copyToRealmOrUpdate(contactsModel);



                            }, () -> new Handler().postDelayed(() -> {
                                progressBar.setVisibility(View.GONE);
                                AppHelper.hideDialog();
                                AppHelper.CustomToast(getContext(), response.getMessage());
                                PreferenceManager.getInstance().setImageUrl(getContext(),response.getFilename());
                                setImage(response.getFilename());
                            }, 700), error -> AppHelper.LogCat("error update group image in group model " + error.getMessage()));
                            realm.close();
                        });
                    } else {
                        AppHelper.CustomToast(getContext(), response.getMessage());
                        AppHelper.hideDialog();
                    }
                }, throwable -> {
                    AppHelper.hideDialog();
                    AppHelper.CustomToast(getContext(), getString(R.string.failed_upload_image));
                    AppHelper.LogCat("Failed  upload your image " + throwable.getMessage());
                    getActivity().runOnUiThread(() -> progressBar.setVisibility(View.GONE));
                }));
            }
            return profileResponse;
        }


        @Override
        protected void onPostExecute(ProfileResponse response) {
            super.onPostExecute(response);
            // AppHelper.LogCat("Response from server: " + response);
        }
    }

    @SuppressLint("CheckResult")
    private void setImage(String ImageUrl) {


        editProfilePresenter.editCurrentImage(ImageUrl, false);
        BitmapImageViewTarget target = new BitmapImageViewTarget(userAvatar) {


            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                userAvatar.setImageBitmap(resource);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MINE_IMAGE_PROFILE_UPDATED));
                JSONObject jsonObject = new JSONObject();
                try {
                    jsonObject.put("ownerId", PreferenceManager.getInstance().getID(getContext()));
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

        Glide.with(getContext())
                .asBitmap()

                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + PreferenceManager.getInstance().getID(getContext()) + "/" + ImageUrl))
                .centerCrop()
                .apply(RequestOptions.circleCropTransform())
                .placeholder(drawable)
                .error(drawable)
                .override(AppConstants.EDIT_PROFILE_IMAGE_SIZE, AppConstants.EDIT_PROFILE_IMAGE_SIZE)
                .into(target);
    }
}