package com.pefgloble.pefchate.activities;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModel;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.group.AddNewMembersToGroupActivity;
import com.pefgloble.pefchate.activities.group.EditGroupActivity;
import com.pefgloble.pefchate.activities.media.MediaActivity;
import com.pefgloble.pefchate.adapter.GroupMembersAdapter;
import com.pefgloble.pefchate.adapter.MediaProfileAdapter;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.BottomSheetEditGroupImage;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.ImageCompressionAsyncTask;
import com.pefgloble.pefchate.helpers.ImageLoader;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.helpers.call.CallManager;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.presenter.ProfilePresenter;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.joda.time.DateTime;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;
import androidx.palette.graphics.Palette;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;
import io.socket.client.Socket;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;


/**
 * Created by Abderrahim El imame on 27/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfileActivity extends BaseActivity {

    @BindView(R.id.cover)
    ImageView UserCover;
    @BindView(R.id.anim_toolbar)
    Toolbar toolbar;/*
    @BindView(R.id.appbar)
    AppBarLayout AppBarLayout;
    @BindView(R.id.collapsing_toolbar)
    CollapsingToolbarLayout collapsingToolbar;*/
    @BindView(R.id.containerProfile)
    LinearLayout containerProfile;
    @BindView(R.id.created_title)
    TextView mCreatedTitle;
    @BindView(R.id.group_container_title)
    LinearLayout GroupTitleContainer;
    @BindView(R.id.group_edit)
    FloatingActionButton EditGroupBtn;
    @BindView(R.id.statusPhoneContainer)
    CardView statusPhoneContainer;
    @BindView(R.id.status)
    TextView status;
    @BindView(R.id.numberPhone)
    TextView numberPhone;
    @BindView(R.id.status_date)
    TextView status_date;
    @BindView(R.id.send_message)
    ImageView sendMessageBtn;
    @BindView(R.id.call_video)
    ImageView callVideoBtn;
    @BindView(R.id.call_voice)
    ImageView callVoiceBtn;
    @BindView(R.id.MembersList)
    RecyclerView MembersList;
    @BindView(R.id.participantContainer)
    CardView participantContainer;
    @BindView(R.id.participantContainerExit)
    LinearLayout participantContainerExit;
    @BindView(R.id.participantContainerDelete)
    LinearLayout participantContainerDelete;
    @BindView(R.id.participantCounter)
    TextView participantCounter;
    @BindView(R.id.media_counter)
    TextView mediaCounter;
    @BindView(R.id.media_section)
    CardView mediaSection;

    @BindView(R.id.mediaProfileList)
    RecyclerView mediaList;
    @BindView(R.id.shareBtn)
    FloatingActionButton shareBtn;

    private CompositeDisposable mDisposable;
    private MediaProfileAdapter mMediaProfileAdapter;
    private GroupMembersAdapter mGroupMembersAdapter;
    private UsersModel mContactsModel;
    private GroupModel mGroupsModel;
    public String userID;
    public String groupID;
    private boolean isGroup;
    private int mutedColor;
    private int mutedColorStatusBar;
    int numberOfColors = 24;
    private ProfilePresenter mProfilePresenter;
    private boolean isAnAdmin;
    private boolean isLeft;
    private APIService mApiService;
    private String PicturePath;
    private Intent mIntent;
    private String name = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ButterKnife.bind(this);
        initializerView();

        mDisposable = new CompositeDisposable();
        if (getIntent().hasExtra("userID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            userID = getIntent().getExtras().getString("userID");
        }


        if (getIntent().hasExtra("groupID")) {
            isGroup = getIntent().getExtras().getBoolean("isGroup");
            groupID = getIntent().getExtras().getString("groupID");
        }
        mApiService = new APIService(this);
        mProfilePresenter = new ProfilePresenter(this);
        mProfilePresenter.onCreate();

        participantContainerExit.setOnClickListener(v -> {

            String name = UtilsString.unescapeJava(mGroupsModel.getName());
            if (name.length() > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.exit_group) + name.substring(0, 10) + "... " + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.exit), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.exiting_group_dialog));
                            mProfilePresenter.ExitGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.exit_group) + name + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.exit), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.exiting_group_dialog));
                            mProfilePresenter.ExitGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            }


        });

        participantContainerDelete.setOnClickListener(v -> {
            String name = UtilsString.unescapeJava(mGroupsModel.getName());
            if (name.length() > 10) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete) + name.substring(0, 10) + "... " + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.deleting_group_dialog));
                            mProfilePresenter.DeleteGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(getString(R.string.delete) + name + "" + getString(R.string.group_ex))
                        .setPositiveButton(getString(R.string.delete), (dialog, which) -> {
                            AppHelper.showDialog(this, getString(R.string.deleting_group_dialog));
                            mProfilePresenter.DeleteGroup();
                        }).setNegativeButton(getString(R.string.cancel), null).show();
            }
        });
        callVideoBtn.setOnClickListener(view -> makeCall(true));
        callVoiceBtn.setOnClickListener(view -> makeCall(false));

        EditGroupBtn.setOnClickListener(view -> launchEditGroupName());
        sendMessageBtn.setOnClickListener(view -> sendMessage(mContactsModel));
        shareBtn.setOnClickListener(view -> shareContact(mContactsModel));

    }


    private void makeCall(boolean isVideoCall) {
        if (!isVideoCall) {
            CallManager.callContact(ProfileActivity.this, false, userID);
        } else {
            CallManager.callContact(ProfileActivity.this, true, userID);
        }
    }


    /**
     * method to initialize group members view
     */
    private void initializerGroupMembersView() {

        participantContainer.setVisibility(View.VISIBLE);
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mGroupMembersAdapter = new GroupMembersAdapter(this, mProfilePresenter.getRealm());
        MembersList.setLayoutManager(mLinearLayoutManager);
        MembersList.setAdapter(mGroupMembersAdapter);
        //checkIfIsAnAdmin();

    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setHomeButtonEnabled(true);
        }
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        mediaList.setLayoutManager(linearLayoutManager);
        mMediaProfileAdapter = new MediaProfileAdapter(this);
        mediaList.setAdapter(mMediaProfileAdapter);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (isGroup) {
            if (isAnAdmin) {
                getMenuInflater().inflate(R.menu.profile_menu_group_add, menu);
            }/* else {
                if (!left)
                    getMenuInflater().inflate(R.menu.profile_menu_group, menu);
            }*/

        } else {
            if (mContactsModel != null)
                if (UtilsPhone.checkIfContactExist(this, mContactsModel.getPhone())) {
                    if (userID.equals(PreferenceManager.getInstance().getID(this))) {
                        getMenuInflater().inflate(R.menu.profile_menu_mine, menu);
                    } else {
                        getMenuInflater().inflate(R.menu.profile_menu, menu);
                    }
                } else if (userID.equals(PreferenceManager.getInstance().getID(this))) {
                    getMenuInflater().inflate(R.menu.profile_menu_mine, menu);
                } else {
                    getMenuInflater().inflate(R.menu.profile_menu_user_not_exist, menu);
                }

        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();

        } else if (item.getItemId() == R.id.add_contact) {
            Intent mIntent = new Intent(this, AddNewMembersToGroupActivity.class);
            mIntent.putExtra("groupID", groupID);
            mIntent.putExtra("profileAdd", "add");
            startActivity(mIntent);
        } else if (item.getItemId() == R.id.edit_contact) {
            editContact(mContactsModel);
        } else if (item.getItemId() == R.id.view_contact) {
            viewContact(mContactsModel);
        } else if (item.getItemId() == R.id.add_new_contact) {
            addNewContact();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.media_selection)
    public void launchMediaActivity() {

        if (isGroup) {
            mIntent = new Intent(this, MediaActivity.class);
            mIntent.putExtra("groupID", groupID);
            mIntent.putExtra("isGroup", true);
            mIntent.putExtra("Username", mGroupsModel.getImage());
            startActivity(mIntent);
            AnimationsUtil.setTransitionAnimation(this);

        } else {
            String finalName;
            if (mContactsModel.getUsername() != null) {
                finalName = mContactsModel.getUsername();
            } else {
                String name = UtilsPhone.getContactName(mContactsModel.getPhone());
                if (name != null) {
                    finalName = name;
                } else {
                    finalName = mContactsModel.getPhone();
                }
            }
            mIntent = new Intent(this, MediaActivity.class);
            mIntent.putExtra("userID", userID);
            mIntent.putExtra("isGroup", false);
            mIntent.putExtra("Username", finalName);
            startActivity(mIntent);
            AnimationsUtil.setTransitionAnimation(this);

        }
    }

    private void addNewContact() {
        try {
            Intent mIntent = new Intent(Intent.ACTION_INSERT);
            mIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            mIntent.putExtra(ContactsContract.Intents.Insert.PHONE, mContactsModel.getPhone());
            startActivityForResult(mIntent, AppConstants.SELECT_ADD_NEW_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    private void launchEditGroupName() {
        try {

            Intent mIntent = new Intent(this, EditGroupActivity.class);
            if (mGroupsModel.getName() == null || mGroupsModel.getName().equals("null"))
                mIntent.putExtra("currentGroupName", "");
            else
                mIntent.putExtra("currentGroupName", mGroupsModel.getName());
            mIntent.putExtra("groupID", mGroupsModel.get_id());
            startActivity(mIntent);

        } catch (Exception e) {
            AppHelper.LogCat("Error   UI Exception " + e.getMessage());
        }
    }

    public void ShowContact(UsersModel contactsModel) {
        mContactsModel = contactsModel;
        try {
            updateUI(null, contactsModel);
        } catch (Exception e) {
            AppHelper.LogCat("Error ContactsModel in profile UI Exception " + e.getMessage());
        }
    }

    public void ShowMedia(List<MessageModel> messagesModel) {
        if (messagesModel.size() != 0) {
            mediaSection.setVisibility(View.VISIBLE);
            mediaCounter.setText(String.valueOf(messagesModel.size()));
            mMediaProfileAdapter.setMessages(messagesModel);

        } else {
            mediaSection.setVisibility(View.GONE);
        }

    }

    public void ShowGroup(GroupModel groupsModel) {
        mGroupsModel = groupsModel;
        try {
            updateUI(mGroupsModel, null);
        } catch (Exception e) {
            AppHelper.LogCat("Error GroupsModel in profile UI Exception " + e.getMessage());
        }
    }

    @SuppressLint({"StaticFieldLeak", "CheckResult"})
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void updateUI(GroupModel mGroupsModel, UsersModel mContactsModel) {


        if (isGroup) {
            GroupTitleContainer.setVisibility(View.VISIBLE);
            statusPhoneContainer.setVisibility(View.GONE);
            shareBtn.setVisibility(View.GONE);
            for (MembersModel membersModel : mGroupsModel.getMembers()) {

                if (membersModel.getOwner().get_id().equals(PreferenceManager.getInstance().getID(this))) {
                    isAnAdmin = membersModel.isAdmin();
                    isLeft = membersModel.isLeft();
                    break;
                }
            }
            ShowGroupMembers(mGroupsModel.getMembers());

            if (isAnAdmin) {
                EditGroupBtn.setVisibility(View.VISIBLE);
                if (isLeft) {
                    participantContainerExit.setVisibility(View.GONE);
                    participantContainerDelete.setVisibility(View.VISIBLE);
                } else {
                    participantContainerExit.setVisibility(View.VISIBLE);
                    participantContainerDelete.setVisibility(View.GONE);
                }
            } else {
                if (isLeft) {
                    participantContainerExit.setVisibility(View.GONE);
                    participantContainerDelete.setVisibility(View.VISIBLE);
                    EditGroupBtn.setVisibility(View.GONE);
                } else {
                    participantContainerExit.setVisibility(View.VISIBLE);
                    participantContainerDelete.setVisibility(View.GONE);
                    EditGroupBtn.setVisibility(View.VISIBLE);
                }
            }


            DateTime messageDate = UtilsTime.getCorrectDate(mGroupsModel.getCreated());
            String groupDate = UtilsTime.convertDateToStringFormat(this, messageDate);
            if (mGroupsModel.getOwner().get_id().equals(PreferenceManager.getInstance().getID(this))) {
                mCreatedTitle.setText(String.format(getString(R.string.created_by_you_at) + " %s", groupDate));
            } else {
                String name = UtilsPhone.getContactName(mGroupsModel.getOwner().getPhone());
                if (name != null) {
                    mCreatedTitle.setText(String.format(getString(R.string.created_by) + " %s " + getString(R.string.group_at) + " %s ", name, groupDate));
                } else {
                    mCreatedTitle.setText(String.format(getString(R.string.created_by) + " %s " + getString(R.string.group_at) + " %s ", mGroupsModel.getOwner().getPhone(), groupDate));
                }
            }
            String name = UtilsString.unescapeJava(mGroupsModel.getName());
            if (name.length() > 10)
                getSupportActionBar().setTitle(name.substring(0, 10) + "... " + "");
            else
                getSupportActionBar().setTitle(name);


            String ImageUrl = mGroupsModel.getImage();
            String groupId = mGroupsModel.get_id();


            Drawable drawable;
            drawable = AppHelper.getDrawable(this, R.drawable.holder_group_simple);

            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED, groupID));
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    UserCover.setImageDrawable(drawable);
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                    UserCover.setImageDrawable(drawable);

                }
            };

            if (!ProfileActivity.this.isFinishing()) {
                try {
                    Glide.with(ProfileActivity.this)
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .centerCrop()
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                            .into(target);

                } catch (Exception e) {
                    AppHelper.LogCat(e.getMessage());
                }


            }


            UserCover.setOnClickListener(view -> {
                BottomSheetEditGroupImage bottomSheetEditGroupImage = new BottomSheetEditGroupImage();
                bottomSheetEditGroupImage.show(getSupportFragmentManager(), bottomSheetEditGroupImage.getTag());
            });


            //    APIHelper.initializeApiGroups().updateGroupMembers(mGroupsModel.get_id()).subscribe(this::ShowGroupMembers, this::onErrorLoading);


        } else {
            EditGroupBtn.setVisibility(View.GONE);
            if (userID.equals(PreferenceManager.getInstance().getID(this))) {
                sendMessageBtn.setVisibility(View.GONE);
                callVideoBtn.setVisibility(View.GONE);
                callVoiceBtn.setVisibility(View.GONE);
                shareBtn.setVisibility(View.GONE);
            } else {
                sendMessageBtn.setVisibility(View.VISIBLE);
                callVideoBtn.setVisibility(View.VISIBLE);
                callVoiceBtn.setVisibility(View.VISIBLE);
                shareBtn.setVisibility(View.VISIBLE);
            }

          /*  if (mContactsModel.getUsername() != null) {
                name = mContactsModel.getUsername();
            } else {*/
                name = UtilsPhone.getContactName(mContactsModel.getPhone());
                if (name == null) {
                    name = mContactsModel.getPhone();
                }
          //  }
            getSupportActionBar().setTitle(name);
            GroupTitleContainer.setVisibility(View.GONE);
            statusPhoneContainer.setVisibility(View.VISIBLE);
            String Status = UtilsString.unescapeJava(mContactsModel.getStatus().getBody());

            status.setText(Status);
            numberPhone.setText(mContactsModel.getPhone());

            DateTime messageDate = UtilsTime.getCorrectDate(mContactsModel.getStatus().getCreated());
            status_date.setText(UtilsTime.convertDateToStringFormat(this, messageDate));

            String userImageUrl = mContactsModel.getImage();
            String userId = mContactsModel.get_id();


            Drawable drawable;
            drawable = AppHelper.getDrawable(this, R.drawable.holder_user_simple);

            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });


                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);

                    UserCover.setImageDrawable(drawable);

                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);

                    UserCover.setImageDrawable(drawable);

                }
            };


            Glide.with(ProfileActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + userId + "/" + userImageUrl))
                    .signature(new ObjectKey(userImageUrl))
                    .centerCrop()
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                    .into(target);


            if (userImageUrl != null) {
                if (FilesManager.isFilePhotoProfileExists(this, FilesManager.getProfileImage(userImageUrl))) {
                    UserCover.setOnClickListener(view -> AppHelper.LaunchImagePreviewActivity(this, AppConstants.PROFILE_IMAGE, userImageUrl, userId));
                } else {
                    UserCover.setOnClickListener(view -> AppHelper.LaunchImagePreviewActivity(ProfileActivity.this, AppConstants.PROFILE_IMAGE_FROM_SERVER, userImageUrl, userId));
                }
            }


        }


    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mProfilePresenter.onDestroy();

    }


    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Profile throwable " + throwable.getMessage());
    }

    public void onErrorDeleting() {
        AppHelper.Snackbar(this, containerProfile, getString(R.string.failed_to_delete_this_group_check_connection), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

    }

    public void onErrorExiting() {
        AppHelper.Snackbar(this, containerProfile, getString(R.string.failed_to_exit_this_group_check_connection), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

    }

    /**
     * method to show group members list
     *
     * @param membersGroupModels this is parameter for ShowGroupMembers  method
     */
    public void ShowGroupMembers(RealmList<MembersModel> membersGroupModels) {
        AppHelper.LogCat("membersGroupModels " + membersGroupModels.size());
        for (MembersModel membersModel : membersGroupModels) {
            AppHelper.LogCat("membersGroupModels membersModel " + membersModel.getOwner().get_id());
        }
        if (membersGroupModels.size() != 0) {
            initializerGroupMembersView();
            mGroupMembersAdapter.setMembers(membersGroupModels);
            participantCounter.setText(String.valueOf(membersGroupModels.size()));
        }


    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        String imagePath = null;
        if (resultCode == Activity.RESULT_OK) {

            if (Permissions.hasAny(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                AppHelper.LogCat("Read storage data permission already granted.");
                switch (requestCode) {
                    case AppConstants.SELECT_ADD_NEW_CONTACT:
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_CONTACT_ADDED));
                        break;
                    case AppConstants.SELECT_PROFILE_PICTURE:
                        imagePath = FilesManager.getPath(this, data.getData());
                        break;
                    case AppConstants.SELECT_PROFILE_CAMERA:
                        if (data.getData() != null) {
                            imagePath = FilesManager.getPath(this, data.getData());
                        } else {
                            try {
                                String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA, MediaStore
                                        .Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images
                                        .ImageColumns.MIME_TYPE};
                                final Cursor cursor = this.getContentResolver()
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
                AppHelper.LogCat("Please request Read contact data permission.");
            }

        }
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
            case AppConstants.EVENT_BUS_DELETE_GROUP:
                AppHelper.Snackbar(this, containerProfile, pusher.getData(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                new Handler().postDelayed(this::finish, 500);
                break;
            case AppConstants.EVENT_BUS_PATH_GROUP:
                PicturePath = pusher.getData();
                try {
                    @SuppressLint("StaticFieldLeak")
                    ImageCompressionAsyncTask imageCompression = new ImageCompressionAsyncTask() {
                        @Override
                        protected void onPostExecute(byte[] imageBytes) {

                            // image here is compressed & ready to be sent to the server
                            // create RequestBody instance from file
                            RequestBody requestFile;
                            if (imageBytes == null)
                                requestFile = null;
                            else
                                requestFile = RequestBody.create(MediaType.parse("image*//*"), imageBytes);
                            if (requestFile == null) {
                                AppHelper.LogCat("requestFile is null "+requestFile);
                                AppHelper.CustomToast(ProfileActivity.this, getString(R.string.oops_something));
                            } else {
                                File file = new File(PicturePath);
                                mDisposable.add(APIHelper.initializeUploadFiles().uploadGroupImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile)).subscribe(filesResponse -> {
                                    AppHelper.hideDialog();
                                    if (filesResponse.isSuccess()) {
                                        String groupId = groupID;

                                        mDisposable.addAll(APIHelper.initializeApiGroups().editGroupImage(filesResponse.getFilename(), groupId).subscribe(statusResponse -> {
                                            if (statusResponse.isSuccess()) {


                                                JSONObject jsonObject = new JSONObject();
                                                try {
                                                    jsonObject.put("ownerId", PreferenceManager.getInstance().getID(ProfileActivity.this));
                                                    jsonObject.put("is_group", true);
                                                } catch (JSONException e) {
                                                    e.printStackTrace();
                                                }
                                                Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                                                if (mSocket != null)
                                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_IMAGE_PROFILE_UPDATED, jsonObject);
                                                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                                                try {

                                                    realm.executeTransactionAsync(realm1 -> {
                                                                GroupModel groupsModel = realm1.where(GroupModel.class).equalTo("_id", groupId).findFirst();
                                                                groupsModel.setImage(filesResponse.getFilename());
                                                                realm1.copyToRealmOrUpdate(groupsModel);

                                                            }, () -> {
                                                                setImage(filesResponse.getFilename(), groupId);
                                                                AppHelper.CustomToast(ProfileActivity.this, filesResponse.getMessage());
                                                                ConversationModel conversationsModel = realm.where(ConversationModel.class).equalTo("group._id", groupId).findFirst();
                                                                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW, conversationsModel.get_id()));
                                                            },
                                                            error -> AppHelper.LogCat("error update group image in group model " + error.getMessage()));
                                                } finally {

                                                    if (!realm.isClosed())
                                                        realm.close();
                                                }
                                            } else {
                                                AppHelper.CustomToast(ProfileActivity.this, filesResponse.getMessage());
                                            }

                                        }, throwable -> {
                                            AppHelper.LogCat(throwable.getMessage());
                                        }));

                                    } else {
                                        AppHelper.CustomToast(ProfileActivity.this, filesResponse.getMessage());
                                    }
                                }, throwable -> {

                                    AppHelper.hideDialog();
                                    AppHelper.LogCat("Failed  upload your image " + throwable.getMessage());
                                    AppHelper.CustomToast(ProfileActivity.this, getString(R.string.oops_something));
                                }));
                            }
                        }
                    };
                    imageCompression.execute(PicturePath);
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                    AppHelper.CustomToast(this, getString(R.string.oops_something));
                }
                break;
            case AppConstants.EVENT_BUS_ADD_MEMBER:
                new Handler().postDelayed(() -> mProfilePresenter.updateUIGroupData(pusher.getGroupID()), 500);
                break;
            case AppConstants.EVENT_BUS_EXIT_THIS_GROUP:
                participantContainerExit.setVisibility(View.GONE);
                participantContainerDelete.setVisibility(View.VISIBLE);
                new Handler().postDelayed(() -> mProfilePresenter.updateUIGroupData(pusher.getGroupID()), 500);
                break;
            case AppConstants.EVENT_BUS_UPDATE_GROUP_NAME:
                new Handler().postDelayed(() -> mProfilePresenter.updateUIGroupData(pusher.getGroupID()), 500);
                break;

        }


    }


    private void editContact(UsersModel mContactsModel) {
        if (userID.equals(PreferenceManager.getInstance().getID(this))) {
            AppHelper.LaunchActivity(this, EditProfileActivity.class);
        } else {
            long ContactID = UtilsPhone.getContactID(this, mContactsModel.getPhone());
            try {
                if (ContactID != 0) {
                    Intent intent = new Intent(Intent.ACTION_EDIT);
                    intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
                    startActivity(intent);
                }
            } catch (Exception e) {
                AppHelper.LogCat("error edit contact " + e.getMessage());
            }
        }
    }

    private void viewContact(UsersModel mContactsModel) {
        long ContactID = UtilsPhone.getContactID(this, mContactsModel.getPhone());
        try {
            if (ContactID != 0) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, ContactID));
                startActivity(intent);
            }
        } catch (Exception e) {
            AppHelper.LogCat("error view contact " + e.getMessage());
        }
    }

    private void sendMessage(UsersModel mContactsModel) {
        Intent messagingIntent = new Intent(this, MessagesActivity.class);
        //  messagingIntent.putExtra("conversationID", "");
        messagingIntent.putExtra("recipientID", mContactsModel.get_id());
        messagingIntent.putExtra("isGroup", false);
        startActivity(messagingIntent);
        finish();
    }


    private void shareContact(UsersModel mContactsModel) {
        if (mContactsModel == null) return;
        Intent shareIntent = new Intent();
        shareIntent.setAction(Intent.ACTION_SEND);
        shareIntent.setType("text/*");
        String subject = null;
        if (mContactsModel.getUsername() != null) {
            subject = mContactsModel.getUsername();
        }
        if (mContactsModel.getPhone() != null) {
            if (subject != null) {
                subject = subject + " " + mContactsModel.getPhone();
            } else {
                subject = mContactsModel.getPhone();
            }
        }
        if (subject != null) {
            shareIntent.putExtra(Intent.EXTRA_TEXT, subject);
        }

        startActivity(Intent.createChooser(shareIntent, getString(R.string.shareContact)));
    }


    @SuppressLint("StaticFieldLeak")
    private void setImage(String ImageUrl, String groupId) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("groupId", groupID);
        } catch (JSONException e) {
            e.printStackTrace();
        }


        String groupImage = mGroupsModel.getImage();
        Bitmap holderBitmap = ImageLoader.GetCachedBitmapImage(groupImage, ProfileActivity.this, groupID, AppConstants.GROUP, AppConstants.FULL_PROFILE);
        if (holderBitmap != null) {
            Drawable drawable;
            drawable = new BitmapDrawable(getResources(), holderBitmap);
            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);

                    // AnimationsUtil.expandToolbar(containerProfile, holderBitmap, AppBarLayout);
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    UserCover.setImageBitmap(holderBitmap);
                    Palette.from(holderBitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception ex) {
                                AppHelper.LogCat(" " + ex.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);

                    UserCover.setImageBitmap(holderBitmap);
                    Palette.from(holderBitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }
            };
            Glide.with(ProfileActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                    .centerCrop()
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                    .into(target);

        } else {
            Drawable drawable;
            drawable = AppHelper.getDrawable(this, R.drawable.holder_group_simple);
            DrawableImageViewTarget target = new DrawableImageViewTarget(UserCover) {

                @Override
                public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                    super.onResourceReady(resource, transition);
                    Bitmap bitmap = AppHelper.convertToBitmap(resource, AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE);
                    UserCover.setImageBitmap(bitmap);
                    Palette.from(bitmap).maximumColorCount(numberOfColors).generate(palette -> {
                        Palette.Swatch swatchColorDark = palette.getDarkVibrantSwatch();
                        if (swatchColorDark != null) {
                            try {
                                mutedColor = swatchColorDark.getRgb();
                                toolbar.setBackgroundColor(mutedColor);
                                if (AppHelper.isAndroid5()) {

                                    float hsv[] = new float[3];
                                    Color.colorToHSV(swatchColorDark.getRgb(), hsv);
                                    hsv[2] = 0.2f;
                                    mutedColorStatusBar = Color.HSVToColor(hsv);
                                    getWindow().setStatusBarColor(mutedColorStatusBar);
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat(" " + e.getMessage());
                            }
                        } else {
                            List<Palette.Swatch> swatches = palette.getSwatches();
                            for (Palette.Swatch swatch : swatches) {
                                if (swatch != null) {
                                    mutedColor = swatch.getRgb();
                                    toolbar.setBackgroundColor(mutedColor);
                                    if (AppHelper.isAndroid5()) {
                                        float hsv[] = new float[3];
                                        Color.colorToHSV(swatch.getRgb(), hsv);
                                        hsv[2] = 0.2f;
                                        mutedColorStatusBar = Color.HSVToColor(hsv);
                                        getWindow().setStatusBarColor(mutedColorStatusBar);
                                    }
                                    break;
                                }
                            }

                        }
                    });
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    UserCover.setImageDrawable(errorDrawable);
                }

                @Override
                public void onLoadStarted(Drawable placeholder) {
                    super.onLoadStarted(placeholder);
                    UserCover.setImageDrawable(placeholder);
                }
            };
            Glide.with(ProfileActivity.this)
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + ImageUrl))
                    .centerCrop()
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.PROFILE_IMAGE_SIZE, AppConstants.PROFILE_IMAGE_SIZE)
                    .into(target);
        }

    }


    public void UpdateGroupUI(GroupModel groupsModel) {
        try {
            updateUI(groupsModel, null);
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }

    }


    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

}
