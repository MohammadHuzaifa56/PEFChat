package com.pefgloble.pefchate.activities.group;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.Transition;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.SyncContacts;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupRequest;
import com.pefgloble.pefchate.JsonClasses.groups.GroupResponse;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModel;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModelJson;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.APIContact;
import com.pefgloble.pefchate.RestAPI.APIGroups;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.adapter.CreateGroupMembersToGroupAdapter;
import com.pefgloble.pefchate.adapter.CustomAdapter;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.ImageCompressionAsyncTask;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.helpers.files.RealmBackupRestore;
import com.pefgloble.pefchate.helpers.files.UniqueId;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.jobs.WorkJobsManager;
import com.pefgloble.pefchate.ui.InputGeneralPanel;
import com.vanniktech.emoji.EmojiEditText;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.google.GoogleEmojiProvider;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;


/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class CreateGroupActivity extends BaseActivity implements InputGeneralPanel.Listener {



    @BindView(R.id.layout_container)
    LinearLayout container;

    @BindView(R.id.bottom_panel)
    InputGeneralPanel inputPanel;


    @BindView(R.id.embedded_text_editor)
    EditText composeText;


    @BindView(R.id.group_image)
    ImageView groupImage;
    @BindView(R.id.add_image_group)
    ImageView addImageGroup;
    @BindView(R.id.fab)
    FloatingActionButton doneBtn;

    @BindView(R.id.create_group_pro_bar)
    ProgressBar progressBarGroup;

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.participantCounter)
    TextView participantCounter;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private CreateGroupMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private String selectedImagePath = null;
    private Realm realm;
    private String lastConversationID;
    private EmojiPopup emojiPopup;
    private CompositeDisposable mDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_group);
        ButterKnife.bind(this);
        realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
        initializeView();
        setupToolbar();
        loadData();
    }


    /**
     * method to loadCircleImage members form shared preference
     */
    private void loadData() {
        List<UsersModel> usersModels = new ArrayList<>();
        if (PreferenceManager.getInstance().getMembers(this) == null) return;
        int arraySize = PreferenceManager.getInstance().getMembers(this).size();

        String id;
        for (int x = 0; x < arraySize; x++) {
            id = PreferenceManager.getInstance().getMembers(this).get(x).getUserId();
            UsersModel usersModel = realm.where(UsersModel.class).equalTo("_id", id).findFirst();
            usersModels.add(usersModel);
        }
        mAddMembersToGroupListAdapter.setContacts(usersModels);

        String text = String.format(getString(R.string.participants) + " %s/%s ", mAddMembersToGroupListAdapter.getItemCount(), PreferenceManager.getInstance().getContactSize(this));
        participantCounter.setText(text);
    }

    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_members_to_group);
    }


    /**
     * method to initialize  the view
     */
    private void initializeView() {
        GridLayoutManager mLinearLayoutManager = new GridLayoutManager(getApplicationContext(), 4);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new CreateGroupMembersToGroupAdapter(this);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        doneBtn.setOnClickListener(v -> createGroupOffline());
        addImageGroup.setOnClickListener(v -> launchImageChooser());
        if (AppHelper.isAndroid5()) {
            Transition enterTrans = new Fade();
            getWindow().setEnterTransition(enterTrans);
            enterTrans.setDuration(300);
        } else {
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            params.setMargins(0, 0, 16, 0);
            params.gravity = Gravity.RIGHT | Gravity.BOTTOM;
            doneBtn.setLayoutParams(params);
        }

        composeText.setHint(getString(R.string.type_group_subject_here));
        inputPanel.setListener(this);
        EmojiManager.install(new GoogleEmojiProvider());
        emojiPopup = EmojiPopup.Builder.fromRootView(container).setOnEmojiPopupDismissListener(() -> inputPanel.setToEmoji()).setOnEmojiPopupShownListener(() -> inputPanel.setToIme()).build(composeText);
    }

    /**
     * method to select an image
     */
    private void launchImageChooser() {
        Intent mIntent = new Intent();
        mIntent.setType("image/*");
        mIntent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(
                Intent.createChooser(mIntent, getString(R.string.select_picture)),
                AppConstants.UPLOAD_PICTURE_REQUEST_CODE);
    }

    /**
     * method to create group in offline mode
     */
    private void createGroupOffline() {

        String groupName = UtilsString.escapeJava(composeText.getText().toString().trim());
        if (groupName.length() <= 3) {
            setProgressBarGroup();
            composeText.setError(getString(R.string.name_is_too_short));
        } else {
            getProgressBarGroup();
            DateTime current = new DateTime();
            String createTime = String.valueOf(current);

            int arraySize = PreferenceManager.getInstance().getMembers(CreateGroupActivity.this).size();
            List<String> ids = new ArrayList<>();
            for (int x = 0; x <= arraySize - 1; x++) {
                ids.add(PreferenceManager.getInstance().getMembers(CreateGroupActivity.this).get(x).getUserId());
            }
            ids.add(PreferenceManager.getInstance().getID(this));
            AppHelper.LogCat("ids Create " + ids);

            GroupRequest groupRequest = new GroupRequest();
            groupRequest.setCreateTime(createTime);
            groupRequest.setIds(ids);
            if (selectedImagePath != null)
                groupRequest.setImage(selectedImagePath);
            else
                groupRequest.setImage("null");
            groupRequest.setName(groupName);
            mDisposable.add(APIHelper.initializeApiGroups().createGroup(groupRequest).subscribe(groupResponse -> {
                if (groupResponse.isSuccess()) {
                    setProgressBarGroup();

                    AppHelper.LogCat("group id created 2 e " + groupResponse.toString());

                    String lastConversationID = RealmBackupRestore.getConversationLastId();
                    String lastID = RealmBackupRestore.getConversationLastId();
                    realm.executeTransactionAsync(realm1 -> {

                        UsersModel usersModelSender = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(this)).findFirst();


                        GroupModel groupModel = realm1.createObject(GroupModel.class, groupResponse.getGroupId());
                        RealmList<MembersModel> membersModelList = new RealmList<>();

                        for (int x=0;x<=ids.size()-1;x++){
                            UsersModel usersModel=realm1.where(UsersModel.class).equalTo("_id",ids.get(x)).findFirst();
                            MembersModel membersModel=realm1.createObject(MembersModel.class,groupResponse.getMembersModels().get(x).get_id());
                            if (ids.get(0).equals(PreferenceManager.getInstance().getID(AGApplication.getInstance()))){
                                membersModel.setAdmin(true);
                            }
                            else {
                                membersModel.setAdmin(false);
                            }
                            membersModel.setOwner(usersModel);
                            membersModel.setDeleted(false);
                            membersModel.setGroupId(groupResponse.getGroupId());
                            membersModelList.add(membersModel);
                        }

               /*         for (MembersModelJson membersModelJson : groupResponse.getMembersModels()) {
                            MembersModel membersModel = realm1.createObject(MembersModel.class, membersModelJson.get_id());
                            membersModel.setAdmin(membersModelJson.isAdmin());
                            membersModel.setDeleted(membersModelJson.isDeleted());
                            membersModel.setLeft(membersModelJson.isLeft());
                            membersModel.setGroupId(membersModelJson.getGroupId());
                            membersModel.setOwner();
                            membersModelList.add(membersModel);
                        }*/

                        groupModel.setMembers(membersModelList);
                        if (groupResponse.getGroupImage() != null)
                            groupModel.setImage(groupResponse.getGroupImage());
                        else
                            groupModel.setImage("null");
                        groupModel.setName(groupName);
                        groupModel.setOwner(usersModelSender);
                        realm1.copyToRealmOrUpdate(groupModel);

                        MessageModel messagesModel = realm1.createObject(MessageModel.class, UniqueId.generateUniqueId());
                        messagesModel.set_id(lastID);
                        messagesModel.setConversationId(lastConversationID);
                        messagesModel.setCreated(createTime);
                        messagesModel.setStatus(AppConstants.IS_WAITING);
                        messagesModel.setGroup(groupModel);
                        messagesModel.setSender(usersModelSender);
                        messagesModel.setIs_group(true);
                        messagesModel.setMessage("null");
                        messagesModel.setLatitude("null");
                        messagesModel.setLongitude("null");
                        messagesModel.setFile("null");
                        messagesModel.setFile_type("null");
                        messagesModel.setState(AppConstants.CREATE_STATE);
                        messagesModel.setFile_size("0");
                        messagesModel.setDuration_file("0");
                        messagesModel.setReply_id("null");
                        messagesModel.setReply_message(true);
                        messagesModel.setDocument_name("null");
                        messagesModel.setDocument_type("null");
                        messagesModel.setFile_upload(true);
                        messagesModel.setFile_downLoad(true);
                        ConversationModel conversationsModel1 = realm1.createObject(ConversationModel.class, UniqueId.generateUniqueId());
                        conversationsModel1.set_id(lastConversationID);
                        conversationsModel1.setGroup(groupModel);
                        conversationsModel1.setIs_group(true);
                        conversationsModel1.setLatestMessage(messagesModel);
                        conversationsModel1.setCreated(createTime);
                        conversationsModel1.setUnread_message_counter(0);

                    }, () -> {
                        new Handler().postDelayed(() -> {
                            // EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));
                            //  EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ADD_MEMBER, lastConversationID));

                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, lastConversationID));
                            PreferenceManager.getInstance().clearMembers(CreateGroupActivity.this);
                            AppHelper.CustomToast(CreateGroupActivity.this, groupResponse.getMessage());
                            if (emojiPopup.isShowing()) emojiPopup.dismiss();
                            WorkJobsManager.getInstance().sendUserMessagesToServer();
                            // new Handler().postDelayed(() -> JobsManager.getInstance().sendGroupMessagesToServer(), 500);
                            finish();

                        }, 200);

                    });
                    /*, error -> {
                        setProgressBarGroup();
                        AppHelper.LogCat("Realm Error create group offline CreateGroupActivity " + error.getMessage());
                        AppHelper.Snackbar(this, findViewById(R.id.layout_container), getString(R.string.create_group_failed), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                    });*/

                } else {
                    setProgressBarGroup();
                    AppHelper.CustomToast(CreateGroupActivity.this, groupResponse.getMessage());
                }
            }, throwable -> {
                AppHelper.LogCat(throwable.getMessage());

                setProgressBarGroup();
                AppHelper.CustomToast(CreateGroupActivity.this, CreateGroupActivity.this.getString(R.string.oops_something));
            }));

        }


    }

    void getProgressBarGroup() {
        progressBarGroup.setVisibility(View.VISIBLE);
        doneBtn.setVisibility(View.GONE);
        doneBtn.setEnabled(false);
    }

    void setProgressBarGroup() {
        progressBarGroup.setVisibility(View.GONE);
        doneBtn.setVisibility(View.VISIBLE);
        doneBtn.setEnabled(true);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }


    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == AppConstants.UPLOAD_PICTURE_REQUEST_CODE) {

                if (Permissions.hasAny(this, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    AppHelper.LogCat("Read contact data permission already granted.");
                    selectedImagePath = FilesManager.getPath(this, data.getData());
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
                                AppHelper.CustomToast(CreateGroupActivity.this, getString(R.string.oops_something));
                            } else {
                                File file = new File(selectedImagePath);
                                mDisposable.add(APIHelper.initializeUploadFiles().uploadGroupImage(MultipartBody.Part.createFormData("file", file.getName(), requestFile))
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread()).subscribe(filesResponse -> {
                                            if (filesResponse.isSuccess()) {
                                                AppHelper.LogCat("throwable " + filesResponse.getFilename());
                                                selectedImagePath = filesResponse.getFilename();
                                                Drawable drawable = AppHelper.getDrawable(CreateGroupActivity.this, R.drawable.holder_user);
                                                Glide.with(CreateGroupActivity.this)
                                                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + filesResponse.getFilename()))
                                                        .apply(RequestOptions.circleCropTransform())
                                                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                                        .placeholder(drawable)
                                                        .error(drawable)
                                                        .into(groupImage);
                                                if (groupImage.getVisibility() != View.VISIBLE) {
                                                    groupImage.setVisibility(View.VISIBLE);
                                                }
                                            } else {
                                                AppHelper.LogCat("throwable hjjh ");
                                                selectedImagePath = null;
                                                AppHelper.CustomToast(CreateGroupActivity.this, filesResponse.getMessage());
                                            }

                                        }, throwable -> {
                                            AppHelper.LogCat("throwable " + throwable);
                                            AppHelper.CustomToast(CreateGroupActivity.this, getString(R.string.oops_something));
                                        })
                                );

                            }
                        }
                    };
                    imageCompression.execute(selectedImagePath);

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


            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        realm.close();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getContacts() != null && mAddMembersToGroupListAdapter.getContacts().size() != 0) {
                PreferenceManager.getInstance().clearMembers(this);
                mAddMembersToGroupListAdapter.getContacts().clear();
            }
            finish();


        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    protected void onResume() {
        super.onResume();

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
        else {

            if (mAddMembersToGroupListAdapter.getContacts().size() != 0) {
                PreferenceManager.getInstance().clearMembers(this);
                mAddMembersToGroupListAdapter.getContacts().clear();

            }
            super.onBackPressed();
        }
    }

}
