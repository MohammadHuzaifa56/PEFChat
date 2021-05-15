package com.pefgloble.pefchate.api;

import android.content.Context;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.RestAPI.APIContact;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;

import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;


/**
 * Created by Abderrahim El imame on 20/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsService {
    private Realm realm;
    private APIContact apiContact;
    private APIService mApiService;


    public ConversationsService(Realm realm, APIService mApiService) {
        this.realm = realm;
        this.mApiService = mApiService;
    }


    private APIContact initializeApiConversations() {
        if (apiContact == null) {
            apiContact = this.mApiService.RootService(APIContact.class, BuildConfig.BACKEND_BASE_URL);
        }
        return apiContact;
    }

    /**
     * method to get Conversations list
     *
     * @return return value
     */
/*    public Observable<RealmResults<ConversationsModel>> getConversations() {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();
        RealmResults<ConversationsModel> conversationsModels = realm.where(ConversationsModel.class).sort("created", Sort.DESCENDING).findAll();
        if (!realm.isClosed()) realm.close();
        return Observable.just(conversationsModels).filter(RealmResults::isLoaded)
                .filter(RealmResults::isValid)
                .subscribeOn(AndroidSchedulers.mainThread())
                .observeOn(AndroidSchedulers.mainThread());
    }*/
    private RealmResults<ConversationModel> getLocalConversations() {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        RealmResults<ConversationModel> conversationsModel;
        try {
            conversationsModel = realm.where(ConversationModel.class).sort("created", Sort.DESCENDING).findAll();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        return conversationsModel;

    }

    /**
     * method to get user conversations from server
     *
     * @return return value
     */
    public Observable<List<ConversationModel>> getConversations() {

       /* if (!unSentMessagesExist(WhatsCloneApplication.getInstance())) {
            if (AppHelper.isOnline(WhatsCloneApplication.getInstance())) {

            Observable<List<ConversationModel>> conversationListApi = initializeApiConversations().getConversationsList()

                        // Request API data on IO Scheduler
                        //  .subscribeOn(Schedulers.io())
                        // Write to Realm on Computation scheduler
                        .subscribeOn(Schedulers.computation())
                        // Read results in Android Main Thread (UI)
                        .observeOn(AndroidSchedulers.mainThread())
                        .map(this::copyOrUpdateConversations);


                // Read any cached results
                List<ConversationModel> cachedData = getLocalConversations();
                if (cachedData != null)
                    if (cachedData.size() == 0)
                        // Concat with the observable from API
                        return Observable.concat(conversationListApi, Observable.just(cachedData));
                    else
                        // Merge with the observable from API
                        return conversationListApi.mergeWith(Observable.just(cachedData));
                else
                    return conversationListApi;
            } else {

                // Read any cached results
                List<ConversationModel> cachedData = getLocalConversations();
                return Observable.just(cachedData);
            }

      //  } else {*/

        // Read any cached results
        List<ConversationModel> cachedData = getLocalConversations();
        //
        return Observable.just(cachedData);
        /// }

    }

    private List<ConversationModel> copyOrUpdateConversations(List<ConversationModel> conversationsModels) {

      /*  realm.beginTransaction();
        realm.copyToRealmOrUpdate(conversationsModels);
        realm.commitTransaction();*/
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        // if (page == 1) {
        List<ConversationModel> finalList = checkConversations(conversationsModels, realm);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }


        realm.beginTransaction();
        List<ConversationModel> conversationsModelList = realm.copyToRealmOrUpdate(conversationsModels);
        realm.commitTransaction();
        /*}else {

            realm.beginTransaction();
            realm.copyToRealmOrUpdate(conversationsModels);
            realm.commitTransaction();
        }*//*
        if (!realm.isClosed()) realm.close();*/
        /*for (ConversationModel conversationModel : conversationsModels) {
            AppHelper.LogCat("conversationModel " + conversationModel.toString());
            conversationModel.setLatestMessageRelation();
            if (conversationModel.isIs_group()) {
                conversationModel.setGroupRelation();
            } else {
                conversationModel.setOwnerRelation();
                conversationModel.getOwner().setStatusRelation();
            }


            if (conversationModel.getLatestMessage().isIs_group()) {
                conversationModel.getLatestMessage().setGroupRelation();
            } else {
                conversationModel.getLatestMessage().setRecipientRelation();
                conversationModel.getLatestMessage().getRecipient().setStatusRelation();
            }

            conversationModel.getLatestMessage().setSenderRelation();
            conversationModel.getLatestMessage().getSender().setStatusRelation();
            conversationModelBox.put(conversationModel);
        }
*/
        //   AppHelper.LogCat("conversationListApi " + conversationModelBox.getAll());
        return conversationsModelList;
    }

    private List<ConversationModel> checkConversations(List<ConversationModel> conversationsModels, Realm realm) {
        if (conversationsModels.size() != 0) {
            realm.executeTransactionAsync(realm1 -> {
                RealmResults<ConversationModel> conversationsModels1 = realm1.where(ConversationModel.class).findAll();
                conversationsModels1.deleteAllFromRealm();
                RealmResults<MessageModel> messagesModels = realm1.where(MessageModel.class).findAll();
                messagesModels.deleteAllFromRealm();

            });
        }
        return conversationsModels;
    }

    private static boolean unSentMessagesExist(Context mContext) {


        int size = Observable.create((ObservableOnSubscribe<Integer>) subscriber -> {
            try {
                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

                List<MessageModel> messagesModelsList = realm.where(MessageModel.class)
                        .equalTo("status", AppConstants.IS_WAITING)
                        .equalTo("file_upload", true)
                        .equalTo("sender._id", PreferenceManager.getInstance().getID(mContext))
                        .sort("created", Sort.ASCENDING).findAll();


                AppHelper.LogCat("size " + messagesModelsList.size());
                subscriber.onNext(messagesModelsList.size());


                if (!realm.isClosed())
                    realm.close();
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).blockingFirst();


        return size != 0;
    }
}
