package com.pefgloble.pefchate.activities.stories;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.files.RealmBackupRestore;
import com.pefgloble.pefchate.helpers.files.UniqueId;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.jobs.PendingFilesTask;
import com.pefgloble.pefchate.stories.StoriesHeaderModel;
import com.pefgloble.pefchate.stories.StoryModel;
import com.pefgloble.pefchate.ui.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;

public class CreateStoriesText extends BaseActivity implements InputGeneralPanel.Listener {


    @BindView(R.id.send_button)
    AppCompatImageButton sendButton;


    @BindView(R.id.story_text_background)
    AppCompatImageButton story_text_background;

    @BindView(R.id.story_text_style)
    AppCompatTextView story_text_style;


    @BindView(R.id.embedded_text_editor)
    EditText story_input;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    //   @BindView(R.id.story_scroll_view)
    //  ScrollView story_scroll_view;


    @BindView(R.id.story_create_view)
    LinearLayout story_create_view;

    @BindView(R.id.story_bottom)
    LinearLayout story_bottom;
    private EmojiPopup emojiPopup;

    ArrayList<String> colorsArray = new ArrayList<String>();
    ArrayList<String> fontstyleArray = new ArrayList<String>();

    int selectedcolor = 0;
    int selectedtextsize = 0;
    int selectedfontstyle = 0;
    String strfontstyle = "n";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_story_text);
        ButterKnife.bind(this);
        //

        colorsArray.add("#3B5663");
        colorsArray.add("#FF5722");
        colorsArray.add("#E00034");
        colorsArray.add("#ff000000");
        colorsArray.add("#E91E63");
        colorsArray.add("#00E676");

        fontstyleArray.add("fonts/MyriadPro_Bold.otf");
        fontstyleArray.add("fonts/Redressed.ttf");
        fontstyleArray.add("fonts/bevan.ttf");
        fontstyleArray.add("fonts/gotham_bold.otf");
        fontstyleArray.add("fonts/AlexRegular.ttf");
        fontstyleArray.add("fonts/RockSalt.ttf");

        strfontstyle = fontstyleArray.get(selectedfontstyle);
        setTypeface(strfontstyle);


        story_create_view.setBackgroundColor(Color.parseColor(colorsArray.get(selectedcolor)));

        story_text_style.setOnClickListener(v -> {
            selectedfontstyle++;
            if (selectedfontstyle >= fontstyleArray.size()) {
                selectedfontstyle = 0;
            }
            strfontstyle = fontstyleArray.get(selectedfontstyle);
            setTypeface(strfontstyle);

        });


        sendButton.setOnClickListener(v -> {


            if (Permissions.hasAny(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                if (story_input.getText().toString().trim().length() > 0) {
                    hideKeyboard();
                    getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
                    story_input.setCursorVisible(false);
                    story_bottom.setVisibility(View.GONE);
                    if (emojiPopup.isShowing())
                        emojiPopup.dismiss();
                    // TODO: 7/23/18 toast like whatsapp
                    new Handler().postDelayed(this::takeScreenshot, 200);


                }
            } else {
                Permissions.with(this)
                        .request(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)
                        .ifNecessary()
                        .withRationaleDialog(getString(R.string.app__requires_storage_permission_in_order_to_attach_media_information),
                                R.drawable.ic_folder_white_24dp)
                        .onAnyResult(() -> {

                        })
                        .execute();
            }


        });

        story_text_background.setOnClickListener(v -> {
            selectedcolor++;
            if (selectedcolor >= colorsArray.size()) {
                selectedcolor = 0;
            }
            story_create_view.setBackgroundColor(Color.parseColor(colorsArray.get(selectedcolor)));
        });


        inputPanel.setListener(this);
        EmojiManager.install(new GoogleEmojiProvider());
        emojiPopup = EmojiPopup.Builder.fromRootView(story_create_view).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(story_input);
        story_input.setEnabled(true);
        story_input.setFocusableInTouchMode(true);
        story_input.setFocusable(true);
        //    story_input.setEnableSizeCache(false);
        story_input.setMovementMethod(null);
        //    story_input.setMinTextSize(13);
        // can be added after layout inflation; it doesn't have to be fixed
        // value
        story_input.setMaxHeight(330);
        showKeyboard();
    }

    private void takeScreenshot() {

        Bitmap bitmap;

        bitmap = ScreenshotUtils.getScreenShot(this);


        if (bitmap != null) {

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd'T'HHmmss");
            String timeStamp = dateFormat.format(new Date());
            String imageFileName = "picture_" + timeStamp + ".jpg";


            File saveFile = ScreenshotUtils.getMainDirectoryName(this);//get the path to save screenshot
            File file = ScreenshotUtils.store(bitmap, "" + imageFileName + ".jpg", saveFile);//save the screenshot to selected path

            Log.e("file.getAbsolutePath()", "" + file.getAbsolutePath());


            DateTime current = new DateTime();
            String createTime = String.valueOf(current);

            sendStory(file.getAbsolutePath());

            // TODO: 7/23/18 create story offline and send it to server
            //finish();

        } else {
            //If bitmap is null show toast message
        }

    }

    private void sendStory(String imagePath) {

        if (imagePath == null) {
            AppHelper.CustomToast(getApplicationContext(), getString(R.string.oops_something));
        } else {
            AppHelper.LogCat("imagePath " + imagePath);

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
                        storyModel.setBody(null);

                        storyModel.setType("image");
                        storyModel.setDuration(String.valueOf(AppConstants.MediaConstants.MAX_STORY_DURATION_FOR_IMAGE));


                        RealmList<StoryModel> stories = new RealmList<>();
                        stories.add(storyModel);
                        StoriesHeaderModel storiesHeaderModel = realm1.createObject(StoriesHeaderModel.class, UniqueId.generateUniqueId());
                        storiesHeaderModel.set_id(PreferenceManager.getInstance().getID(this));


                        String name =  UtilsPhone.getContactName(storyOwner.getPhone());
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
                            storyModel.setBody(null);
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
        }
    }


    private void setTypeface(String path) {
        Typeface face = Typeface.createFromAsset(getAssets(), path);
        story_input.setTypeface(face);
        story_text_style.setTypeface(face);
    }


    /**
     * Hide keyboard from phoneEdit field
     */
    public void hideKeyboard() {
        InputMethodManager inputMethodManager = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(story_input.getWindowToken(), 0);
    }

    public void showKeyboard() {
        InputMethodManager imm = (InputMethodManager) getApplicationContext().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(story_input, InputMethodManager.SHOW_IMPLICIT);
    }


    @Override
    public void onEmojiToggle() {

        if (!emojiPopup.isShowing())
            emojiPopup.toggle();
        else
            emojiPopup.dismiss();
    }


    @Override
    public void onBackPressed() {
        if (emojiPopup.isShowing()) emojiPopup.dismiss();
        else
            super.onBackPressed();
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
}