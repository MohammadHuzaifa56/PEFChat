package com.pefgloble.pefchate.presenter;




import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.GroupsService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesPresenter implements Presenter {
    private final MessagesActivity messagesActivity;
    private String RecipientID = null, ConversationID = null, GroupID = null;
    private Boolean isGroup=false;
    private CompositeDisposable mDisposable;
    // private MessagesService messagesService;
    //  private UsersService mUsersContacts;
    // private GroupsService groupsService;
    private Realm realm;
    // private UserViewModel mViewModel;
    //private GroupViewModel mGroupViewModel;
    //private MessagesListViewModel messagesListViewModel;


    public MessagesPresenter(MessagesActivity messagesActivity) {
        this.messagesActivity = messagesActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());


        // mViewModel = ViewModelProviders.of(this.messagesActivity).get(UserViewModel.class);
        // mGroupViewModel = ViewModelProviders.of(this.messagesActivity).get(GroupViewModel.class);
        //  messagesListViewModel = ViewModelProviders.of(this.messagesActivity).get(MessagesListViewModel.class);
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(messagesActivity))
            EventBus.getDefault().register(messagesActivity);
        if (messagesActivity.getIntent().getExtras() != null) {
            if (messagesActivity.getIntent().hasExtra("conversationID")) {
                ConversationID = messagesActivity.getIntent().getExtras().getString("conversationID");
            }
            if (messagesActivity.getIntent().hasExtra("recipientID")) {
                RecipientID = messagesActivity.getIntent().getExtras().getString("recipientID");
            }

            if (messagesActivity.getIntent().hasExtra("groupID")) {
                GroupID = messagesActivity.getIntent().getExtras().getString("groupID");
            }

            if (messagesActivity.getIntent().hasExtra("isGroup")) {
                isGroup = messagesActivity.getIntent().getExtras().getBoolean("isGroup");
            }

        }

        AppHelper.LogCat("oncreate increment ");
        mDisposable = new CompositeDisposable();
        //  messagesService = new MessagesService(realm);
        //   APIService mApiService = APIService.with(messagesActivity);
        // mUsersContacts = new UsersService(realm, messagesActivity, mApiService);

        getContactSenderLocal();
        if (isGroup) {
           // groupsService = new GroupsService(realm, messagesActivity, mApiService);
            getGroupLocal();
        } else {
            getContactRecipientLocal();
        }

        getData();
    }


    public void getData() {

        if (isGroup) {
            loadLocalGroupData();
        } else {
            loadLocalData();
        }
    }

    private void getGroupLocal() {

/*
        mDisposable.add(APIHelper.initializeApiGroups().getGroupInfoLocal(GroupID).subscribe(groupModel -> {
            AppHelper.LogCat("groupModel " + groupModel.toString());
            messagesActivity.updateGroupInfo(groupModel);
        }, throwable -> {
            AppHelper.LogCat("groupModel throwable" + throwable.getMessage());
        }));*/
        mDisposable.add(APIHelper.initializeApiGroups().getGroupInfo(GroupID).subscribe(groupModel -> {
            AppHelper.LogCat("groupModel " + groupModel.toString());
            messagesActivity.updateGroupInfo(groupModel);
        }, throwable -> {
            AppHelper.LogCat("groupModel throwable" + throwable.getMessage());
        }));
    }

    private void getGroupMembersServer() {/*
        mDisposable.add(groupsService.updateGroupMembers(GroupID).subscribeWith(new DisposableObserver<List<MembersGroupModel>>() {


            @Override
            public void onNext(List<MembersGroupModel> membersGroupModels) {
                messagesActivity.ShowGroupMembers(membersGroupModels);
            }

            @Override
            public void onError(Throwable e) {
                messagesActivity.onErrorLoading(e);
            }

            @Override
            public void onComplete() {

            }
        }));*/
    }

    private void getGroupServer() {
       /* mDisposable.add(groupsService.getGroupInfo(GroupID).subscribeWith(new DisposableObserver<GroupsModel>() {


            @Override
            public void onNext(GroupsModel groupsModel) {
                messagesActivity.updateGroupInfo(groupsModel);
            }

            @Override
            public void onError(Throwable e) {
                messagesActivity.onErrorLoading(e);
            }

            @Override
            public void onComplete() {


            }
        }));*/


    }


    public void getContactRecipientLocal() {


        // mViewModel.refreshUser(RecipientID);
       /* mViewModel.getUser(RecipientID).observe(messagesActivity, usersModels -> {
            AppHelper.LogCat("getContactRecipientLocal usersModels " + usersModels.get(0).getPhone());
            if (!usersModels.get(0).get_id().equals(RecipientID)) return;
            messagesActivity.updateContactRecipient(usersModels.get(0));


        });*/
        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(RecipientID).subscribe(usersModel -> {
            AppHelper.LogCat("usersModel " + usersModel.toString());

            messagesActivity.updateContactRecipient(usersModel);

        }, throwable -> {
            AppHelper.LogCat("usersModel throwable" + throwable.getMessage());

        }));
        /*mDisposable.add(mUsersContacts.getContact(RecipientID).subscribeWith(new DisposableObserver<ContactsModel>() {

            @Override
            public void onNext(ContactsModel contactsModel) {
                messagesActivity.updateContactRecipient(contactsModel);
            }

            @Override
            public void onError(Throwable e) {
                messagesActivity.onErrorLoading(e);
            }

            @Override
            public void onComplete() {

                getContactRecipientServer();
            }
        }));*/
    }

    private void getContactRecipientServer() {
       /* mDisposable.add(mUsersContacts.getContactInfo(RecipientID).subscribeWith(new DisposableObserver<ContactsModel>() {

            @Override
            public void onNext(ContactsModel contactsModel) {
                messagesActivity.updateContactRecipient(contactsModel);
            }

            @Override
            public void onError(Throwable e) {
                messagesActivity.onErrorLoading(e);
            }

            @Override
            public void onComplete() {


            }
        }))
        ;*/
    }

    private void getContactSenderLocal() {
        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(PreferenceManager.getInstance().getID(messagesActivity)).subscribe(usersModel -> {
            AppHelper.LogCat("usersModel " + usersModel.toString());

        }, throwable -> {
            AppHelper.LogCat("usersModel throwable" + throwable.getMessage());

        }));
        ;
        //  messagesActivity.updateContact(mViewModel.getCurrentUser());
        //   mViewModel.refreshUser(PreferenceManager.getInstance().getID(messagesActivity));

      /*  mViewModel.getUser(PreferenceManager.getInstance().getID(messagesActivity)).observe(messagesActivity, usersModels -> {
            AppHelper.LogCat("getContactSenderLocal usersModels " + usersModels.get(0).getPhone());
            if (!usersModels.get(0).get_id().equals(PreferenceManager.getInstance().getID(messagesActivity)))
                return;
            messagesActivity.updateContact(usersModels.get(0));

        });*/
      /*  mDisposable.add(mUsersContacts.getContact(PreferenceManager.getInstance().getID(messagesActivity)).subscribeWith(new DisposableObserver<ContactsModel>() {

            @Override
            public void onNext(ContactsModel contactsModel) {
                messagesActivity.updateContact(contactsModel);
            }

            @Override
            public void onError(Throwable e) {
                messagesActivity.onErrorLoading(e);
            }

            @Override
            public void onComplete() {

                getContactSenderServer();
            }
        }));*/
    }

    private void getContactSenderServer() {
       /* mDisposable.add(mUsersContacts.getContactInfo(PreferenceManager.getInstance().getID(messagesActivity)).subscribeWith(new DisposableObserver<ContactsModel>() {

            @Override
            public void onNext(ContactsModel contactsModel) {
                messagesActivity.updateContact(contactsModel);
            }

            @Override
            public void onError(Throwable e) {
                messagesActivity.onErrorLoading(e);
            }

            @Override
            public void onComplete() {

            }
        }));*/
    }

    private void loadLocalGroupData() {
        if (NotificationsManager.getInstance().getManager())
            NotificationsManager.getInstance().cancelNotification(GroupID);
      /*  messagesListViewModel.loadGroupMessages(GroupID);
        messagesListViewModel.getGroupMessagesList(GroupID).observe(messagesActivity, messageModels -> {
            messagesActivity.onHideLoading();
            AppHelper.LogCat("messagesModels " + messageModels.size());
            messagesActivity.ShowMessages(messageModels);
        });*/
        mDisposable.add(APIHelper.initializeMessagesService().getConversation(GroupID).subscribe(messagesModels -> {
            messagesActivity.onHideLoading();
            AppHelper.LogCat("messagesModels " + messagesModels.size());
            messagesActivity.ShowMessages(messagesModels);
        }, throwable -> {
            messagesActivity.onErrorLoading(throwable);
            messagesActivity.onHideLoading();
        }));

    }

    private void loadLocalData() {
        if (NotificationsManager.getInstance().getManager())
            NotificationsManager.getInstance().cancelNotification(RecipientID);
        mDisposable.add(APIHelper.initializeMessagesService().getConversation(ConversationID, RecipientID, PreferenceManager.getInstance().getID(messagesActivity)).subscribe(messagesModels -> {
            messagesActivity.onHideLoading();
            AppHelper.LogCat("messagesModels " + messagesModels.size());

            messagesActivity.ShowMessages(messagesModels);
        }, throwable -> {
            messagesActivity.onErrorLoading(throwable);
            messagesActivity.onHideLoading();
        }));
       /* messagesListViewModel.loadMessages(RecipientID);
        messagesListViewModel.getMessagesList(RecipientID, ConversationID).observe(messagesActivity, messageModels -> {
            messagesActivity.onHideLoading();
            AppHelper.LogCat("messagesModels " + messageModels.size());

            messagesActivity.ShowMessages(messageModels);
        });*/
    }

    @Override
    public void onPause() {
    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(messagesActivity);
        if (!realm.isClosed())
            realm.close();
        if (mDisposable != null && !mDisposable.isDisposed())
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


    public void deleteConversation(String conversationID) {
        RealmResults<MessageModel> messagesModelWaiting = realm.where(MessageModel.class)
                .equalTo("conversationId", conversationID)
                .findAll();
        RealmResults<MessageModel> messagesModelAll = realm.where(MessageModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("conversationId", conversationID)
                .findAll();

        if (messagesModelWaiting.size() == messagesModelAll.size()) {
            realm.executeTransactionAsync(realm1 -> {
                RealmResults<MessageModel> messagesModel1 = realm1.where(MessageModel.class).equalTo("conversationId", conversationID).findAll();
                messagesModel1.deleteAllFromRealm();
            }, () -> {
                AppHelper.LogCat("Message Deleted  successfully  MessagesPopupActivity");

                RealmResults<MessageModel> messagesModel1 = realm.where(MessageModel.class).equalTo("conversationId", conversationID).findAll();
                if (messagesModel1.size() == 0) {
                    realm.executeTransactionAsync(realm1 -> {
                        ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationID).findFirst();
                        conversationsModel1.deleteFromRealm();
                    }, () -> {
                        AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity");

                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                        NotificationsManager.getInstance().SetupBadger(messagesActivity);
                        messagesActivity.finish();
                    }, error -> {
                        AppHelper.LogCat("Delete conversation failed  MessagesPopupActivity" + error.getMessage());

                    });
                } else {
                    MessageModel lastMessage = realm.where(MessageModel.class).equalTo("conversationId", conversationID).findAll().last();
                    realm.executeTransactionAsync(realm1 -> {
                        ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationID).findFirst();
                        conversationsModel1.setLatestMessage(lastMessage);
                        realm1.copyToRealmOrUpdate(conversationsModel1);
                    }, () -> {
                        AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity ");
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                        NotificationsManager.getInstance().SetupBadger(messagesActivity);
                        messagesActivity.finish();
                    }, error -> {
                        AppHelper.LogCat("Delete conversation failed  MessagesPopupActivity" + error.getMessage());

                    });
                }

            }, error -> {
                AppHelper.LogCat("Delete message failed MessagesPopupActivity" + error.getMessage());

            });
        } else {
            mDisposable.add(APIHelper.initialApiUsersContacts().deleteConversation(conversationID).subscribe(statusResponse -> {
                realm.executeTransactionAsync(realm1 -> {
                    RealmResults<MessageModel> messagesModel1 = realm1.where(MessageModel.class).equalTo("conversationId", conversationID).findAll();
                    messagesModel1.deleteAllFromRealm();
                }, () -> {
                    AppHelper.LogCat("Message Deleted  successfully  MessagesPopupActivity");

                    RealmResults<MessageModel> messagesModel1 = realm.where(MessageModel.class).equalTo("conversationId", conversationID).findAll();
                    if (messagesModel1.size() == 0) {
                        realm.executeTransactionAsync(realm1 -> {
                            ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationID).findFirst();
                            conversationsModel1.deleteFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity");

                            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                            NotificationsManager.getInstance().SetupBadger(messagesActivity);
                            messagesActivity.finish();
                        }, error -> {
                            AppHelper.LogCat("Delete conversation failed  MessagesPopupActivity" + error.getMessage());

                        });
                    } else {
                        MessageModel lastMessage = realm.where(MessageModel.class).equalTo("conversationId", conversationID).findAll().last();
                        realm.executeTransactionAsync(realm1 -> {
                            ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationID).findFirst();
                            conversationsModel1.setLatestMessage(lastMessage);
                            realm1.copyToRealmOrUpdate(conversationsModel1);
                        }, () -> {
                            AppHelper.LogCat("Conversation deleted successfully MessagesPopupActivity ");
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                            NotificationsManager.getInstance().SetupBadger(messagesActivity);
                            messagesActivity.finish();
                        }, error -> {
                            AppHelper.LogCat("Delete conversation failed  MessagesPopupActivity" + error.getMessage());

                        });
                    }
                }, error -> {
                    AppHelper.LogCat("Delete message failed MessagesPopupActivity" + error.getMessage());

                });
            }, throwable -> {
                AppHelper.LogCat("Delete message failed MessagesPopupActivity" + throwable.getMessage());
            }));
        }
    }

    public void deleteMessage(MessageModel messagesModel, int currentPosition1) {
        String messageId = messagesModel.get_id();
        if (messagesModel.getState().equals(AppConstants.NORMAL_STATE)) {
            messagesActivity.mMessagesAdapter.removeMessageItem(currentPosition1);
            if (messagesModel.getStatus() == AppConstants.IS_WAITING) {
                realm.executeTransactionAsync(realm1 -> {
                    MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).findFirst();
                    messagesModel1.deleteFromRealm();
                }, () -> {
                    AppHelper.LogCat("Message deleted successfully MessagesActivity ");

                    RealmResults<MessageModel> messagesModel1 = realm.where(MessageModel.class).equalTo("conversationId", ConversationID).findAll();
                    if (messagesModel1.size() == 0) {
                        realm.executeTransactionAsync(realm1 -> {
                            ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", ConversationID).findFirst();
                            conversationsModel1.deleteFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                            messagesActivity.finish();
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, ConversationID));
                            NotificationsManager.getInstance().SetupBadger(messagesActivity);

                        }, error -> {
                            AppHelper.LogCat("delete conversation failed MessagesActivity " + error.getMessage());

                        });
                    } else {
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, ConversationID));
                        NotificationsManager.getInstance().SetupBadger(messagesActivity);
                    }
                }, error -> {
                    AppHelper.LogCat("delete message failed  MessagesActivity" + error.getMessage());

                });
            } else {
                mDisposable.add(APIHelper.initialApiUsersContacts().deleteMessage(messagesModel.get_id()).subscribe(statusResponse -> {
                    if (statusResponse.isSuccess()) {
                        realm.executeTransactionAsync(realm1 -> {
                            MessageModel messagesModel1 = realm1.where(MessageModel.class).equalTo("_id", messageId).equalTo("conversationId", ConversationID).findFirst();
                            messagesModel1.deleteFromRealm();
                        }, () -> {
                            AppHelper.LogCat("Message deleted successfully MessagesActivity ");

                            RealmResults<MessageModel> messagesModel1 = realm.where(MessageModel.class).equalTo("conversationId", ConversationID).findAll();
                            if (messagesModel1.size() == 0) {
                                realm.executeTransactionAsync(realm1 -> {
                                    ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", ConversationID).findFirst();
                                    conversationsModel1.deleteFromRealm();
                                }, () -> {
                                    AppHelper.LogCat("Conversation deleted successfully MessagesActivity ");
                                    messagesActivity.finish();
                                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, ConversationID));
                                    NotificationsManager.getInstance().SetupBadger(messagesActivity);

                                }, error -> {
                                    AppHelper.LogCat("delete conversation failed MessagesActivity " + error.getMessage());

                                });
                            } else {
                                EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                                EventBus.getDefault().post(new Pusher(EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, ConversationID));
                                NotificationsManager.getInstance().SetupBadger(messagesActivity);
                            }
                        }, error -> {
                            AppHelper.LogCat("delete message failed  MessagesActivity" + error.getMessage());

                        });
                    } else {
                        AppHelper.CustomToast(messagesActivity, messagesActivity.getString(R.string.oops_something));
                    }
                }, throwable -> {
                    AppHelper.LogCat("delete message failed  MessagesActivity" + throwable.getMessage());
                }));
            }
        }
    }
}

