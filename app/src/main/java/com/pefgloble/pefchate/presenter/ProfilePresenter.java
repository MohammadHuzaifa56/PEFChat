package com.pefgloble.pefchate.presenter;



import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModel;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.ProfileActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.GroupsService;
import com.pefgloble.pefchate.api.MessagesService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.DocumentsFragment;
import com.pefgloble.pefchate.fragments.LinksFragment;
import com.pefgloble.pefchate.fragments.MediaFragment;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ProfilePresenter implements Presenter {
    private ProfileActivity profileActivity;
    private MediaFragment mediaFragment;
    private DocumentsFragment documentFragment;
    private LinksFragment linksFragment;
    private final Realm realm;
    private String groupID;
    private String userID;
    private GroupsService mGroupsService;
    private MessagesService mMessagesService;
    private APIService mApiService;
    private CompositeDisposable mDisposable;
    private UsersService mUsersContacts;


    public ProfilePresenter(ProfileActivity profileActivity) {
        this.profileActivity = profileActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }

    public ProfilePresenter(MediaFragment mediaFragment) {
        this.mediaFragment = mediaFragment;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }

    public ProfilePresenter(DocumentsFragment documentFragment) {
        this.documentFragment = documentFragment;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }

    public ProfilePresenter(LinksFragment linksFragment) {
        this.linksFragment = linksFragment;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();

        mMessagesService = new MessagesService(realm);
        if (profileActivity != null) {
            if (!EventBus.getDefault().isRegistered(profileActivity))
                EventBus.getDefault().register(profileActivity);
            mApiService = APIService.with(profileActivity);
            if (profileActivity.getIntent().hasExtra("userID")) {
                userID = profileActivity.getIntent().getExtras().getString("userID");
                loadContactData();
                try {
                    loadUserMediaData(userID);
                } catch (Exception e) {
                    AppHelper.LogCat("Media Execption");
                }

            }


            if (profileActivity.getIntent().hasExtra("groupID")) {
                groupID = profileActivity.getIntent().getExtras().getString("groupID");
                loadGroupData();
                try {
                    loadGroupMediaData(groupID);
                } catch (Exception e) {
                    AppHelper.LogCat("Media Execption");
                }
            }
        } else {

            if (mediaFragment != null) {
                mApiService = APIService.with(mediaFragment.getActivity());

                if (mediaFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = mediaFragment.getActivity().getIntent().getExtras().getString("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (mediaFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = mediaFragment.getActivity().getIntent().getExtras().getString("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            } else if (documentFragment != null) {
                mApiService = APIService.with(documentFragment.getActivity());

                if (documentFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = documentFragment.getActivity().getIntent().getExtras().getString("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (documentFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = documentFragment.getActivity().getIntent().getExtras().getString("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            } else if (linksFragment != null) {
                mApiService = APIService.with(linksFragment.getActivity());

                if (linksFragment.getActivity().getIntent().hasExtra("userID")) {
                    userID = linksFragment.getActivity().getIntent().getExtras().getString("userID");
                    try {
                        loadUserMediaData(userID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
                if (linksFragment.getActivity().getIntent().hasExtra("groupID")) {
                    groupID = linksFragment.getActivity().getIntent().getExtras().getString("groupID");
                    try {
                        loadGroupMediaData(groupID);
                    } catch (Exception e) {
                        AppHelper.LogCat("Media Execption");
                    }

                }
            }


        }


    }


    private void loadUserMediaData(String userID) {
        if (profileActivity != null)
            mMessagesService.getUserMedia(userID, PreferenceManager.getInstance().getID(profileActivity)).subscribe(profileActivity::ShowMedia, profileActivity::onErrorLoading);
        if (mediaFragment != null)
            mMessagesService.getUserMedia(userID, PreferenceManager.getInstance().getID(mediaFragment.getActivity())).subscribe(mediaFragment::ShowMedia, mediaFragment::onErrorLoading);
        else if (documentFragment != null)
            mMessagesService.getUserDocuments(userID, PreferenceManager.getInstance().getID(documentFragment.getActivity())).subscribe(documentFragment::ShowMedia, documentFragment::onErrorLoading);
        else if (linksFragment != null)
            mMessagesService.getUserLinks(userID, PreferenceManager.getInstance().getID(linksFragment.getActivity())).subscribe(linksFragment::ShowMedia, linksFragment::onErrorLoading);

    }

    private void loadGroupMediaData(String groupID) {
        if (profileActivity != null)
            mMessagesService.getGroupMedia(groupID).subscribe(profileActivity::ShowMedia, profileActivity::onErrorLoading);
        else if (mediaFragment != null)
            mMessagesService.getGroupMedia(groupID).subscribe(mediaFragment::ShowMedia, mediaFragment::onErrorLoading);
        else if (documentFragment != null)
            mMessagesService.getGroupDocuments(groupID).subscribe(documentFragment::ShowMedia, documentFragment::onErrorLoading);
        else if (linksFragment != null)
            mMessagesService.getGroupLinks(groupID).subscribe(linksFragment::ShowMedia, linksFragment::onErrorLoading);
    }

    private void loadContactData() {

        mUsersContacts = new UsersService(realm, profileActivity, mApiService);

        getContactLocal();
        getContactServer();

    }

    private void getContactLocal() {
/*        mDisposable.addAll(mUsersContacts.getContact(userID).subscribe(contactsModel -> {
            profileActivity.ShowContact(contactsModel);
        }, throwable -> {
            profileActivity.onErrorLoading(throwable);
        }))
        ;*/
        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(userID).subscribe(usersModel -> {
            AppHelper.LogCat("usersModel " + usersModel.toString());
            profileActivity.ShowContact(usersModel);

        }, throwable -> {
            AppHelper.LogCat("usersModel throwable" + throwable.getMessage());

        }));
    }

    private void getContactServer() {
       /* mDisposable.addAll(mUsersContacts.getContactInfo(userID).subscribe(contactsModel -> {
            profileActivity.ShowContact(contactsModel);
        }, throwable -> {
            profileActivity.onErrorLoading(throwable);
        }))
        ;*/
    }

    private void loadGroupData() {
        mGroupsService = new GroupsService(realm, profileActivity, mApiService);
        getGroupLocal();
        // getGroupServer();


    }


    private void getGroupLocal() {


        mDisposable.add(APIHelper.initializeApiGroups().getGroupInfo(groupID).subscribe(groupModel -> {
            AppHelper.LogCat("groupModel " + groupModel.toString());
            profileActivity.ShowGroup(groupModel);
            //  Response response = new Response(Response.STATUS_SUCCESS, groupModel.toString());
            //   responseObserver.onChanged(response);

        }, throwable -> {
            AppHelper.LogCat("groupModel throwable" + throwable.getMessage());
            // Response response = new Response(Response.STATUS_FAIL, throwable.getMessage());
            //  responseObserver.onChanged(response);
        }));
   /*     mDisposable.addAll(mGroupsService.getGroup(groupID).subscribe(groupsModel -> {
            profileActivity.ShowGroup(groupsModel);
        }, throwable -> {
            profileActivity.onErrorLoading(throwable);
        }))
        ;*/
    }

    private void getGroupServer() {
   /*     mDisposable.addAll(mGroupsService.getGroupInfo(groupID).subscribe(groupsModel -> {
            profileActivity.ShowGroup(groupsModel);
        }, throwable -> {
            profileActivity.onErrorLoading(throwable);
        }))
        ;*/
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    public Realm getRealm() {
        return realm;
    }

    @Override
    public void onDestroy() {
        if (profileActivity != null)
            EventBus.getDefault().unregister(profileActivity);
        if (!realm.isClosed())
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

    public void updateUIGroupData(String groupID) {
        mDisposable.addAll(mGroupsService.getGroupInfo(groupID).subscribe(groupsModel -> {
            profileActivity.UpdateGroupUI(groupsModel);
        }, throwable -> {
            profileActivity.onErrorLoading(throwable);
        }))
        ;

    }

    public void ExitGroup() {
        MembersModel membersModel = realm.where(MembersModel.class).equalTo("owner._id", PreferenceManager.getInstance().getID(profileActivity)).findFirst();
        if (membersModel == null) {
            AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), profileActivity.getString(R.string.failed_exit_group), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

        } else {
            mDisposable.add(mGroupsService.ExitGroup(groupID, membersModel.get_id()).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {
                    realm.executeTransactionAsync(realm1 -> {

                        MembersModel membersGroupModel = realm1.where(MembersModel.class).equalTo("owner._id", PreferenceManager.getInstance().getID(profileActivity)).findFirst();
                        membersGroupModel.setLeft(true);
                        membersGroupModel.setAdmin(false);
                        realm1.copyToRealmOrUpdate(membersGroupModel);
                    }, () -> {
                        AppHelper.hideDialog();
                        AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                        MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.LEFT_STATE);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_EXIT_THIS_GROUP, groupID));
                    }, error -> {
                        AppHelper.hideDialog();
                        AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), profileActivity.getString(R.string.failed_exit_group), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

                        AppHelper.LogCat("error while exiting group" + error.getMessage());
                    });
                } else {
                    AppHelper.hideDialog();
                    AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                try {
                    AppHelper.hideDialog();
                    profileActivity.onErrorExiting();
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }
            }));
        }

    }

    public void DeleteGroup() {
        MembersModel membersModel = realm.where(MembersModel.class).equalTo("owner._id", PreferenceManager.getInstance().getID(profileActivity)).findFirst();
        if (membersModel == null) {
            AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), profileActivity.getString(R.string.failed_to_delete_this_group_check_connection), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);

        } else {

            ConversationModel conversationsModel = realm.where(ConversationModel.class).equalTo("group._id", groupID).findFirst();
            String conversationId = conversationsModel.get_id();
            AppHelper.LogCat("conversationId "+conversationId);
          /*  mDisposable.add(mGroupsService.DeleteGroup(groupID, membersModel.get_id(), conversationId).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {


                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationId));
                    realm.executeTransactionAsync(realm1 -> {
                        RealmResults<MessageModel> messagesModel1 = realm1.where(MessageModel.class).equalTo("conversationId", conversationId).findAll();
                        messagesModel1.deleteAllFromRealm();
                    }, () -> {
                        AppHelper.LogCat("Message Deleted  successfully  ProfilePresenter");


                        realm.executeTransactionAsync(realm1 -> {
                            ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationId).findFirst();
                            conversationsModel1.deleteFromRealm();
                            GroupModel groupsModel = realm1.where(GroupModel.class).equalTo("_id", groupID).findFirst();
                            groupsModel.deleteFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Conversation deleted successfully ProfilePresenter");

                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_GROUP, statusResponse.getMessage()));
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                            NotificationsManager.getInstance().SetupBadger(profileActivity);
                        }, error -> {
                            AppHelper.LogCat("Delete conversation failed  ProfilePresenter" + error.getMessage());

                        });
                    }, error -> {

                        AppHelper.LogCat("Delete message failed ProfilePresenter" + error.getMessage());

                    });


                } else {
                    AppHelper.Snackbar(profileActivity, profileActivity.findViewById(R.id.containerProfile), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {
                try {
                    profileActivity.onErrorDeleting();
                } catch (Exception e) {
                    AppHelper.LogCat(e);
                }

            }, AppHelper::hideDialog));*/
        }
    }


}