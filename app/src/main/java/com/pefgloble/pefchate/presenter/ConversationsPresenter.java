package com.pefgloble.pefchate.presenter;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.ConversationsService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.ConversationsFragment;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmResults;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsPresenter implements Presenter {
    private final ConversationsFragment conversationsFragmentView;
    private final Realm realm;
    private CompositeDisposable mDisposable;
    private int currentPage = 1;
    private ConversationsService conversationsService;

    //private ConversationsListViewModel mViewModel;

    public ConversationsPresenter(ConversationsFragment conversationsFragment) {
        this.conversationsFragmentView = conversationsFragment;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        conversationsService = new ConversationsService(WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance()), APIService.with(WhatsCloneApplication.getInstance()));
        // mViewModel = ViewModelProviders.of(conversationsFragment).get(ConversationsListViewModel.class);
    }

    public Realm getRealm() {
        return realm;
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(conversationsFragmentView))
            EventBus.getDefault().register(conversationsFragmentView);

        mDisposable = new CompositeDisposable();
        loadData(false);


    }


    private void loadData(boolean isRefresh) {
        if (isRefresh)
            conversationsFragmentView.onShowLoading();
        //getConversationFromLocal(isRefresh);
        getConversationFromServer();
        getMostConversations();
    }

    private void getMostConversations() {
        RealmResults<ConversationModel> conversationModelsList=realm.where(ConversationModel.class).findAll().sort("created");
        conversationsFragmentView.UpdateRecentUser(conversationModelsList);
    }
    private void getConversationFromServer() {
/*
        mViewModel.getConversationList().observe(conversationsFragmentView, conversationModels -> {
            AppHelper.LogCat("conversationsModels " + conversationModels.size());
            conversationsFragmentView.UpdateConversation(conversationModels);
            conversationsFragmentView.onHideLoading();

        });*/
        mDisposable.add(conversationsService.getConversations().subscribe(conversationsModels -> {
            //EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
            AppHelper.LogCat("conversationsModels " + conversationsModels.size());
            conversationsFragmentView.UpdateConversation(conversationsModels);
            conversationsFragmentView.onHideLoading();
        }, conversationsFragmentView::onErrorLoading, conversationsFragmentView::onHideLoading));
    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
        //  mViewModel.refreshConversations();
    }

    @Override
    public void onDestroy() {
        EventBus.getDefault().unregister(conversationsFragmentView);
        if (!realm.isClosed())
            realm.close();
        if (mDisposable != null) {
            mDisposable.dispose();
        }

    }


    public String loadLastItem() {
        ConversationModel conversationsModel = realm.where(ConversationModel.class).findAll().last();
        if (conversationsModel.get_id() != null)
            return conversationsModel.get_id();
        else
            return null;
    }

    @Override
    public void onLoadMore() {
/*
        if (loadLastItem() != 0) {
            setCurrentPage(loadLastItem());
        }*/

        //loadData(false);
    }

    @Override
    public void onRefresh() {
        setCurrentPage(1);
        loadData(true);

    }

    @Override
    public void onStop() {

    }


    public int getCurrentPage() {
        return currentPage;
    }

    public void setCurrentPage(int currentPage) {
        this.currentPage = currentPage;
    }

    public void deleteConversation(String conversationID, int currentPosition) {

        RealmResults<MessageModel> messagesModelWaiting = realm.where(MessageModel.class)
                .equalTo("conversationId", conversationID)
                .findAll();
        RealmResults<MessageModel> messagesModelAll = realm.where(MessageModel.class)
                .equalTo("status", AppConstants.IS_WAITING)
                .equalTo("conversationId", conversationID)
                .findAll();
        conversationsFragmentView.mConversationsAdapter.removeConversationItem(currentPosition);
        if (messagesModelWaiting.size() == messagesModelAll.size()) {
            realm.executeTransactionAsync(realm1 -> {
                RealmResults<MessageModel> messagesModel1 = realm1.where(MessageModel.class).equalTo("conversationId", conversationID).findAll();
                messagesModel1.deleteAllFromRealm();
            }, () -> {
                AppHelper.LogCat("Message Deleted  successfully  ConversationsFragment");

                realm.executeTransactionAsync(realm1 -> {
                    ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationID).findFirst();
                    conversationsModel1.deleteFromRealm();
                }, () -> {
                    AppHelper.LogCat("Conversation deleted successfully ConversationsFragment");
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                    NotificationsManager.getInstance().SetupBadger(conversationsFragmentView.getActivity());
                }, error -> {
                    AppHelper.LogCat("Delete conversation failed  ConversationsFragment" + error.getMessage());

                });
            }, error -> {
                AppHelper.LogCat("Delete message failed ConversationsFragment" + error.getMessage());

            });

        } else {
            mDisposable.add(APIHelper.initialApiUsersContacts().deleteConversation(conversationID).subscribe(statusResponse -> {

                realm.executeTransactionAsync(realm1 -> {
                    RealmResults<MessageModel> messagesModel1 = realm1.where(MessageModel.class).equalTo("conversationId", conversationID).findAll();
                    messagesModel1.deleteAllFromRealm();
                }, () -> {
                    AppHelper.LogCat("Message Deleted  successfully  ConversationsFragment");
                    realm.executeTransactionAsync(realm1 -> {
                        ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationID).findFirst();
                        conversationsModel1.deleteFromRealm();
                    }, () -> {
                        AppHelper.LogCat("Conversation deleted successfully ConversationsFragment");
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_CONVERSATION_ITEM, conversationID));
                        NotificationsManager.getInstance().SetupBadger(conversationsFragmentView.getActivity());
                    }, error -> {
                        AppHelper.LogCat("Delete conversation failed  ConversationsFragment" + error.getMessage());

                    });
                }, error -> {
                    AppHelper.LogCat("Delete message failed ConversationsFragment" + error.getMessage());

                });
            }, throwable -> {
                AppHelper.LogCat("Delete message failed ConversationsFragment" + throwable.getMessage());
            }));
        }
    }
}
