package com.pefgloble.pefchate.activities.media;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Bundle;
import android.widget.Toast;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.CropFragment;
import com.pefgloble.pefchate.fragments.PhotoEditorFragment;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.files.RealmBackupRestore;
import com.pefgloble.pefchate.helpers.files.UniqueId;
import com.pefgloble.pefchate.jobs.PendingFilesTask;
import com.pefgloble.pefchate.stories.StoriesHeaderModel;
import com.pefgloble.pefchate.stories.StoryModel;

import org.greenrobot.eventbus.EventBus;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;


public class ImageEditActivity extends BaseActivity implements PhotoEditorFragment.OnFragmentInteractionListener, CropFragment.OnFragmentInteractionListener {


    private Rect cropRect;
    private boolean forStory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_edit);

        String imagePath = getIntent().getStringExtra(AppConstants.MediaConstants.EXTRA_IMAGE_PATH);
        forStory = getIntent().getBooleanExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, false);
        if (imagePath != null) {

            addFragment(this, R.id.fragment_container,
                    PhotoEditorFragment.newInstance(imagePath));
        }
    }

    @Override
    public void onCropClicked(Bitmap bitmap) {
        addFragment(this, R.id.fragment_container, CropFragment.newInstance(bitmap, cropRect));
    }

    private String getStoryId(String userId, Realm realm) {
        try {
            StoriesHeaderModel storiesHeaderModel = realm.where(StoriesHeaderModel.class)
                    .equalTo("_id", userId)
                    .findFirst();
            return storiesHeaderModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("Get storyId id Exception  " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onDoneClicked(String imagePath, String message) {
        if (imagePath == null) {
            AppHelper.CustomToast(getApplicationContext(), getString(R.string.oops_something));
        } else {
            AppHelper.LogCat("imagePath " + imagePath);
            AppHelper.LogCat("message " + message);
            if (forStory) {

                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                try {
                    String storyId = getStoryId(PreferenceManager.getInstance().getID(this), realm);
                    if (storyId == null) {

                        String lastID = RealmBackupRestore.getStoryLastId();
                        realm.executeTransactionAsync(realm1 -> {

                            UsersModel storyOwner = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(this)).findFirst();


                            StoryModel storyModel = realm1.createObject(StoryModel.class, UniqueId.generateUniqueId());
                            storyModel.set_id(lastID);
                            storyModel.setUserId(PreferenceManager.getInstance().getID(this));
                            storyModel.setDate(AppHelper.getCurrentTime());
                            storyModel.setDownloaded(true);
                            storyModel.setUploaded(false);
                            storyModel.setDeleted(false);
                            storyModel.setStatus(AppConstants.IS_WAITING);
                            storyModel.setFile(imagePath);
                            storyModel.setBody(message);

                            storyModel.setType("image");
                            storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));


                            RealmList<StoryModel> stories = new RealmList<>();
                            stories.add(storyModel);
                            StoriesHeaderModel storiesHeaderModel = realm1.createObject(StoriesHeaderModel.class, UniqueId.generateUniqueId());
                            storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));


                            String name = "Ali"; /*UtilsPhone.getContactName(storyOwner.getPhone());*/
                            if (name != null) {
                                storiesHeaderModel.setUsername(name);
                            } else {
                                storiesHeaderModel.setUsername(storyOwner.getPhone());
                            }

                            storiesHeaderModel.setUserImage(storyOwner.getImage());
                            storiesHeaderModel.setDownloaded(true);
                            storiesHeaderModel.setStories(stories);
                            realm1.copyToRealmOrUpdate(storiesHeaderModel);
                        }, () -> {

                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_NEW_ROW, PreferenceManager.getInstance().getID(this)));

                            // Create the task, set the listener, add to the task controller, and run
                              PendingFilesTask.initUploadListener(lastID);

                        }, error -> AppHelper.LogCat("Error  story id  " + error.getMessage()));


                    } else {
                        String lastID = RealmBackupRestore.getStoryLastId();
                        realm.executeTransactionAsync(realm1 -> {
                            try {
                                UsersModel storyOwner = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(this)).findFirst();
                                StoryModel storyModel = realm1.createObject(StoryModel.class, UniqueId.generateUniqueId());
                                storyModel.set_id(lastID);
                                storyModel.setUserId(storyId);
                                storyModel.setDate(AppHelper.getCurrentTime());
                                storyModel.setDownloaded(true);
                                storyModel.setUploaded(false);
                                storyModel.setDeleted(false);
                                storyModel.setStatus(AppConstants.IS_WAITING);
                                storyModel.setFile(imagePath);
                                storyModel.setBody(message);
                                storyModel.setType("image");
                                storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));

                                StoriesHeaderModel storiesHeaderModel;
                                RealmQuery<StoriesHeaderModel> storiesHeaderModelRealmQuery = realm1.where(StoriesHeaderModel.class).equalTo("_id", storyId);
                                storiesHeaderModel = storiesHeaderModelRealmQuery.findAll().first();
                                storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));
                                storiesHeaderModel.setUsername(storyOwner.getUsername());
                                storiesHeaderModel.setUserImage(storyOwner.getImage());
                                storiesHeaderModel.setDownloaded(true);
                                RealmList<StoryModel> stories = storiesHeaderModel.getStories();
                                stories.add(storyModel);
                                storiesHeaderModel.setStories(stories);
                                realm1.copyToRealmOrUpdate(storiesHeaderModel);
                            } catch (Exception e) {
                                AppHelper.LogCat("Exception  last id  " + e.getMessage());
                            }
                        }, () -> {
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(this)));
                            // Create the task, set the listener, add to the task controller, and run
                            PendingFilesTask.initUploadListener(lastID);
                        }, error -> AppHelper.LogCat("Error  last id   " + error.getMessage()));
                    }
                } finally {
                    if (!realm.isClosed())
                        realm.close();

                    finish();
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
                }


            } else {

                Intent intent = new Intent();
                intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITED_PATH, imagePath);
                intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITOR_MESSAGE, message);
                setResult(Activity.RESULT_OK, intent);

                finish();
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
            }
        }

    }

    @Override
    public void onImageCropped(Bitmap bitmap, Rect cropRect) {
        this.cropRect = cropRect;
        PhotoEditorFragment photoEditorFragment = (PhotoEditorFragment) getFragmentByTag(this, PhotoEditorFragment.class.getSimpleName());
        if (photoEditorFragment != null) {
            photoEditorFragment.setImageWithRect(bitmap);
            photoEditorFragment.reset();
            removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));

        }
    }

    @Override
    public void onCancelCrop() {
        removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));
    }

    @Override
    public void onBackPressed() {
        if (getFragmentByTag(this, CropFragment.class.getSimpleName()) != null) {
            removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));
        } else {
            super.onBackPressed();
        }
    }


    public void addFragment(AppCompatActivity activity, int contentId, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        transaction.add(contentId, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }

    public void removeFragment(AppCompatActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }


    public Fragment getFragmentByTag(AppCompatActivity appCompatActivity, String tag) {
        return appCompatActivity.getSupportFragmentManager().findFragmentByTag(tag);
    }


/*    private Rect cropRect;
    private boolean forStory;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_image_edit);

        String imagePath = getIntent().getStringExtra(AppConstants.MediaConstants.EXTRA_IMAGE_PATH);
        forStory = getIntent().getBooleanExtra(AppConstants.MediaConstants.EXTRA_FOR_STORY, false);
        if (imagePath != null) {

            addFragment(this, R.id.fragment_container,
                    PhotoEditorFragment.newInstance(imagePath));
        }
    }

    @Override
    public void onCropClicked(Bitmap bitmap) {
        addFragment(this, R.id.fragment_container, CropFragment.newInstance(bitmap, cropRect));
    }

    private String getStoryId(String userId, Realm realm) {
        try {
            StoriesHeaderModel storiesHeaderModel = realm.where(StoriesHeaderModel.class)
                    .equalTo("_id", userId)
                    .findFirst();
            return storiesHeaderModel.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("Get storyId id Exception  " + e.getMessage());
            return null;
        }
    }

    @Override
    public void onDoneClicked(String imagePath, String message) {
        if (imagePath == null) {
            Toast.makeText(getApplicationContext(),"Image Path is null",Toast.LENGTH_SHORT).show();
            //    AppHelper.CustomToast(getApplicationContext(), getString(R.string.oops_something));
        } else {
            AppHelper.LogCat("imagePath " + imagePath);
            AppHelper.LogCat("message " + message);



            *//*if (forStory) {

                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(getApplicationContext());
                try {
                    String storyId = getStoryId(PreferenceManager.getInstance().getID(this), realm);
                    if (storyId == null) {

                        String lastID = RealmBackupRestore.getStoryLastId();
                        realm.executeTransactionAsync(realm1 -> {

                            UsersModel storyOwner = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(this)).findFirst();


                            StoryModel storyModel = realm1.createObject(StoryModel.class, UniqueId.generateUniqueId());
                            storyModel.set_id(lastID);
                            storyModel.setUserId(PreferenceManager.getInstance().getID(this));
                            storyModel.setDate(AppHelper.getCurrentTime());
                            storyModel.setDownloaded(true);
                            storyModel.setUploaded(false);
                            storyModel.setDeleted(false);
                            storyModel.setStatus(AppConstants.IS_WAITING);
                            storyModel.setFile(imagePath);
                            storyModel.setBody(message);

                            storyModel.setType("image");
                            storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));


                            RealmList<StoryModel> stories = new RealmList<>();
                            stories.add(storyModel);
                            StoriesHeaderModel storiesHeaderModel = realm1.createObject(StoriesHeaderModel.class, UniqueId.generateUniqueId());
                            storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));


                            String name = UtilsPhone.getContactName(storyOwner.getPhone());
                            if (name != null) {
                                storiesHeaderModel.setUsername(name);
                            } else {
                                storiesHeaderModel.setUsername(storyOwner.getPhone());
                            }

                            storiesHeaderModel.setUserImage(storyOwner.getImage());
                            storiesHeaderModel.setDownloaded(true);
                            storiesHeaderModel.setStories(stories);
                            realm1.copyToRealmOrUpdate(storiesHeaderModel);
                        }, () -> {

                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_NEW_ROW, PreferenceManager.getInstance().getID(this)));

                            // Create the task, set the listener, add to the task controller, and run
                            PendingFilesTask.initUploadListener(lastID);

                        }, error -> AppHelper.LogCat("Error  story id  " + error.getMessage()));


                    } else {
                        String lastID = RealmBackupRestore.getStoryLastId();
                        realm.executeTransactionAsync(realm1 -> {
                            try {


                                UsersModel storyOwner = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(this)).findFirst();


                                StoryModel storyModel = realm1.createObject(StoryModel.class, UniqueId.generateUniqueId());
                                storyModel.set_id(lastID);
                                storyModel.setUserId(storyId);
                                storyModel.setDate(AppHelper.getCurrentTime());
                                storyModel.setDownloaded(true);
                                storyModel.setUploaded(false);
                                storyModel.setDeleted(false);
                                storyModel.setStatus(AppConstants.IS_WAITING);
                                storyModel.setFile(imagePath);
                                storyModel.setBody(message);
                                storyModel.setType("image");
                                storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));


                                StoriesHeaderModel storiesHeaderModel;
                                RealmQuery<StoriesHeaderModel> storiesHeaderModelRealmQuery = realm1.where(StoriesHeaderModel.class).equalTo("_id", storyId);
                                storiesHeaderModel = storiesHeaderModelRealmQuery.findAll().first();
                                storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));
                                storiesHeaderModel.setUsername(storyOwner.getUsername());
                                storiesHeaderModel.setUserImage(storyOwner.getImage());
                                storiesHeaderModel.setDownloaded(true);
                                RealmList<StoryModel> stories = storiesHeaderModel.getStories();
                                stories.add(storyModel);
                                storiesHeaderModel.setStories(stories);
                                realm1.copyToRealmOrUpdate(storiesHeaderModel);


                            } catch (Exception e) {
                                AppHelper.LogCat("Exception  last id  " + e.getMessage());
                            }
                        }, () -> {

                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(this)));


                            // Create the task, set the listener, add to the task controller, and run
                            PendingFilesTask.initUploadListener(lastID);
                        }, error -> AppHelper.LogCat("Error  last id   " + error.getMessage()));
                    }

                } finally {
                    if (!realm.isClosed())
                        realm.close();

                    finish();
                    LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                    localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
                }


            } else {

                Intent intent = new Intent();
                intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITED_PATH, imagePath);
                intent.putExtra(AppConstants.MediaConstants.EXTRA_EDITOR_MESSAGE, message);
                setResult(Activity.RESULT_OK, intent);

                finish();
                LocalBroadcastManager localBroadcastManager = LocalBroadcastManager.getInstance(this);
                localBroadcastManager.sendBroadcast(new Intent(getPackageName() + "closePickerActivity"));
            }*//*
        }

    }

    @Override
    public void onImageCropped(Bitmap bitmap, Rect cropRect) {
        this.cropRect = cropRect;
        PhotoEditorFragment photoEditorFragment = (PhotoEditorFragment) getFragmentByTag(this, PhotoEditorFragment.class.getSimpleName());
        if (photoEditorFragment != null) {
            photoEditorFragment.setImageWithRect(bitmap);
            photoEditorFragment.reset();
            removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));

        }
    }

    @Override
    public void onCancelCrop() {
        removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));
    }

    @Override
    public void onBackPressed() {
        if (getFragmentByTag(this, CropFragment.class.getSimpleName()) != null) {
            removeFragment(this, getFragmentByTag(this, CropFragment.class.getSimpleName()));
        } else {
            super.onBackPressed();
        }
    }


    public void addFragment(AppCompatActivity activity, int contentId, Fragment fragment) {
        FragmentTransaction transaction = activity.getSupportFragmentManager().beginTransaction();

        transaction.add(contentId, fragment, fragment.getClass().getSimpleName());
        transaction.commit();
    }

    public void removeFragment(AppCompatActivity activity, Fragment fragment) {
        activity.getSupportFragmentManager().beginTransaction()
                .remove(fragment)
                .commit();
    }


    public Fragment getFragmentByTag(AppCompatActivity appCompatActivity, String tag) {
        return appCompatActivity.getSupportFragmentManager().findFragmentByTag(tag);
    }*/
}
