package com.pefgloble.pefchate.presenter;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.AgoraVideo.openvcall.ui.MainActivity;
import com.pefgloble.pefchate.HomeScreens.ProfilePage;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.CompleteRegistrationActivity;
import com.pefgloble.pefchate.activities.EditProfileActivity;
import com.pefgloble.pefchate.activities.settings.EditUsernameActivity;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class EditProfilePresenter implements Presenter {
    private ProfilePage view;
    private EditUsernameActivity editUsernameActivity;
    private CompositeDisposable mDisposable;
    private CompleteRegistrationActivity completeRegistrationActivity;
    private Realm realm;
    private UsersService mUsersContacts;
    private boolean isEditUsername = false;
    private APIService mApiService;


    public APIService getmApiService() {
        return mApiService;
    }

    public EditProfilePresenter(CompleteRegistrationActivity completeRegistrationActivity) {
        this.completeRegistrationActivity = completeRegistrationActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
    }

    public EditProfilePresenter(ProfilePage editProfileActivity) {
        this.view = editProfileActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());


    }


    public EditProfilePresenter() {
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
    }

    public EditProfilePresenter(EditUsernameActivity editUsernameActivity, boolean b) {
        this.isEditUsername = b;
        this.editUsernameActivity = editUsernameActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {
        mDisposable = new CompositeDisposable();
        if (!isEditUsername) {
            if (completeRegistrationActivity != null) {
                APIService mApiService = APIService.with(completeRegistrationActivity);
                mUsersContacts = new UsersService(realm, completeRegistrationActivity, mApiService);
                getUserInfo();
            } else {
                mApiService = APIService.with(view.getContext());
                mUsersContacts = new UsersService(realm, view.getContext(), mApiService);
                loadData();
            }
        } else {
            mApiService = APIService.with(editUsernameActivity);
            this.mUsersContacts = new UsersService(realm, editUsernameActivity, mApiService);

        }

    }

    public void getUserInfo() {
        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(PreferenceManager.getInstance().getID(completeRegistrationActivity)).subscribe(usersModel -> {
            AppHelper.LogCat("usersModel " + usersModel.toString());
            completeRegistrationActivity.setInfo(usersModel.getImage(),usersModel.getUsername());

        }, throwable -> {
            AppHelper.LogCat("usersModel throwable" + throwable.getMessage());

        }));
    }

    public void loadData() {

        getContactLocal();
        getContactServer();


    }

    private void getContactLocal() {

        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(PreferenceManager.getInstance().getID(view.getContext())).subscribe(usersModel -> {
            AppHelper.LogCat("usersModel " + usersModel.toString());
            view.ShowContact(usersModel);

        }, throwable -> {
            AppHelper.LogCat("usersModel throwable" + throwable.getMessage());

        }));
       /* mDisposable.addAll(mUsersContacts.getContact(PreferenceManager.getInstance().getID(view)).subscribe(contactsModel -> {
            view.ShowContact(contactsModel);
        }, throwable -> {
            view.onErrorLoading(throwable);
        }))
        ;*/
    }

    private void getContactServer() {
/*        mDisposable.addAll(mUsersContacts.getContactInfo(PreferenceManager.getInstance().getID(view)).subscribe(contactsModel -> {
            view.ShowContact(contactsModel);
        }, throwable -> {
            view.onErrorLoading(throwable);
        }))

        ;*/
    }

    @Override
    public void onPause() {


    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        realm.close();
        if (mDisposable != null) mDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }

    @Override
    public void onRefresh() {

    }

    @Override
    public void onStop() {

    }


    public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {

            if (Permissions.hasAny(WhatsCloneApplication.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {

                switch (requestCode) {
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(activity, data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(activity, data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = activity.getApplicationContext().getContentResolver()
                                        .query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, MediaStore.Images.ImageColumns
                                                .DATE_TAKEN + " DESC");

                                if (cursor != null && cursor.moveToFirst()) {
                                    String imageLocation = cursor.getString(1);
                                    cursor.close();
                                    File imageFile = new File(imageLocation);
                                    if (imageFile.exists()) {
                                        imagePath = imageFile.getPath();
                                    }
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("error" + e);
                            }
                        }
                        break;
                }


                if (imagePath != null) {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_PROFILE_PATH, imagePath));
                } else {
                    AppHelper.LogCat("imagePath is null");
                }
            } else {
                Permissions.with(activity)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .ifNecessary()
                        .withRationaleDialog(activity.getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information),
                                R.drawable.ic_folder_white_24dp)
                        .onAnyResult(() -> {

                        })
                        .execute();
            }
        }

    }


    public void editCurrentImage(String image, boolean forComplete) {
        mDisposable.addAll(mUsersContacts.editUserImage(image).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                if (forComplete) {
                    AppHelper.Snackbar(completeRegistrationActivity.getBaseContext(), completeRegistrationActivity.findViewById(R.id.completeRegistrationLayout), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    PreferenceManager.getInstance().setIsNeedInfo(completeRegistrationActivity, false);


                    Intent intent = new Intent(completeRegistrationActivity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    completeRegistrationActivity.startActivity(intent);
                    completeRegistrationActivity.finish();
                    AnimationsUtil.setTransitionAnimation(completeRegistrationActivity);
                } else {
                    AppHelper.Snackbar(view.getContext(), view.getActivity().findViewById(R.id.editProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

                }
            } else {
                if (!forComplete) {
                    AppHelper.Snackbar(view.getContext(), view.getActivity().findViewById(R.id.editProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                } else {
                    AppHelper.Snackbar(completeRegistrationActivity.getBaseContext(), completeRegistrationActivity.findViewById(R.id.completeRegistrationLayout), completeRegistrationActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                }
            }
        }, AppHelper::LogCat));

    }

    /*public void editCurrentName(String name, boolean forComplete, boolean imageIsNull) {
        mDisposable.addAll(mUsersContacts.editUsername(name).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                if (forComplete) {
                    if (!imageIsNull) return;
                    AppHelper.Snackbar(completeRegistrationActivity.getBaseContext(), completeRegistrationActivity.findViewById(R.id.completeRegistrationLayout), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    PreferenceManager.getInstance().setIsNeedInfo(completeRegistrationActivity, false);

                    Intent intent = new Intent(completeRegistrationActivity, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    completeRegistrationActivity.startActivity(intent);
                    completeRegistrationActivity.finish();
                    AnimationsUtil.setTransitionAnimation(completeRegistrationActivity);
                } else {
                    AppHelper.Snackbar(editUsernameActivity.getBaseContext(), editUsernameActivity.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_USERNAME_PROFILE_UPDATED));
                    editUsernameActivity.finish();
                }
            } else {
                if (!forComplete) {
                    AppHelper.Snackbar(editUsernameActivity.getBaseContext(), editUsernameActivity.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                } else {
                    AppHelper.Snackbar(completeRegistrationActivity.getBaseContext(), completeRegistrationActivity.findViewById(R.id.completeRegistrationLayout), completeRegistrationActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                }
            }
        }, AppHelper::LogCat));

    }*/

}