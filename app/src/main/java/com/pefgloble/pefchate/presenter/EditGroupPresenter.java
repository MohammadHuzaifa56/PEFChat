package com.pefgloble.pefchate.presenter;


import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.provider.MediaStore;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.group.EditGroupActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.BottomSheetEditGroupImage;
import com.pefgloble.pefchate.helpers.AppHelper;
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
public class EditGroupPresenter implements Presenter {
    private EditGroupActivity view;
    private BottomSheetEditGroupImage bottomSheetEditGroupImage;
    private Realm realm;
    private UsersService mUsersContacts;
    private APIService mApiService;

    private CompositeDisposable mDisposable;

    public EditGroupPresenter(EditGroupActivity editGroupActivity) {
        this.view = editGroupActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }

    public EditGroupPresenter(BottomSheetEditGroupImage bottomSheetEditGroupImage) {
        this.bottomSheetEditGroupImage = bottomSheetEditGroupImage;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {

        mDisposable = new CompositeDisposable();
        if (view != null) {

            this.mApiService = APIService.with(view);
            this.mUsersContacts = new UsersService(this.realm, view, this.mApiService);
        } else if (bottomSheetEditGroupImage != null) {
            this.mApiService = APIService.with(bottomSheetEditGroupImage.getActivity());
            this.mUsersContacts = new UsersService(this.realm, bottomSheetEditGroupImage.getActivity(), this.mApiService);
        }
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
        if (mDisposable != null)
            mDisposable.dispose();
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


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {
            if (bottomSheetEditGroupImage != null) {

                if (Permissions.hasAny(WhatsCloneApplication.getInstance(), Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AppHelper.LogCat("Read storage data permission already granted.");
                    switch (requestCode) {
                        case AppConstants.SELECT_ADD_NEW_CONTACT:
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                            break;
                        case AppConstants.SELECT_PROFILE_PICTURE:
                            imagePath = FilesManager.getPath(bottomSheetEditGroupImage.getActivity(), data.getData());
                            break;
                        case AppConstants.SELECT_PROFILE_CAMERA:
                            if (data.getData() != null) {
                                imagePath = FilesManager.getPath(bottomSheetEditGroupImage.getActivity(), data.getData());
                            } else {
                                try {
                                    String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                            .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                            .ImageColumns.MIME_TYPE};
                                    final Cursor cursor = bottomSheetEditGroupImage.getActivity().getContentResolver()
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
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_PATH_GROUP, imagePath));
                    } else {
                        AppHelper.LogCat("imagePath is null");
                    }
                } else {

                    Permissions.with(bottomSheetEditGroupImage.getActivity())
                            .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                            .ifNecessary()
                            .withRationaleDialog(bottomSheetEditGroupImage.getActivity().getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information),
                                    R.drawable.ic_folder_white_24dp)
                            .onAnyResult(() -> {

                            })
                            .execute();
                }
            }

        }

    }


    public void EditCurrentName(String name, String groupID) {
        mDisposable.addAll(APIHelper.initializeApiGroups().editGroupName(name, groupID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                realm.executeTransactionAsync(realm1 -> {
                            GroupModel groupsModel = realm1.where(GroupModel.class).equalTo("_id", groupID).findFirst();
                            groupsModel.setName(name);
                            realm1.copyToRealmOrUpdate(groupsModel);
                        }, () -> {
                            AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_GROUP_NAME, groupID));
                            MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.EDITED_STATE);
                            view.finish();
                        }, error -> AppHelper.LogCat("error update group name in group model  " + error.getMessage()));
            } else {
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, AppHelper::LogCat));

    }

}