package com.pefgloble.pefchate.api;

import android.content.Context;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.EditUser;
import com.pefgloble.pefchate.JsonClasses.auth.JoinModelResponse;
import com.pefgloble.pefchate.JsonClasses.auth.LoginModel;
import com.pefgloble.pefchate.JsonClasses.calls.CallSaverModel;
import com.pefgloble.pefchate.JsonClasses.calls.CallsInfoModel;
import com.pefgloble.pefchate.JsonClasses.calls.CallsModel;
import com.pefgloble.pefchate.JsonClasses.contacts.BlockResponse;
import com.pefgloble.pefchate.JsonClasses.contacts.SyncContacts;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersBlockModel;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.messags.UpdateMessageModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.NetworkModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.SettingsResponse;
import com.pefgloble.pefchate.JsonClasses.status.EditStatus;
import com.pefgloble.pefchate.JsonClasses.status.NewStatus;
import com.pefgloble.pefchate.JsonClasses.status.StatusModel;
import com.pefgloble.pefchate.JsonClasses.status.StatusResponse;
import com.pefgloble.pefchate.RestAPI.APIContact;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.stories.CreateStoryModel;
import com.pefgloble.pefchate.stories.StoriesHeaderModel;
import com.pefgloble.pefchate.stories.StoriesModel;
import com.pefgloble.pefchate.stories.StoryModel;


import org.reactivestreams.Subscription;

import java.util.List;
import java.util.concurrent.TimeUnit;


import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class UsersService {
    private APIContact mApiContact;
    private Context mContext;
    private Realm realm;
    private APIService mApiService;
    private Subscription subscription;
    //private Box<UsersModel> usersModelBox;

    public UsersService(Realm realm, Context context, APIService mApiService) {
        this.mContext = context;
        this.realm = realm;
        this.mApiService = mApiService;
        //  usersModelBox = BoxManager.getStore().boxFor(UsersModel.class);
    }

    public UsersService(Context context, APIService mApiService) {
        this.mContext = context;
        this.mApiService = mApiService;

    }

    public UsersService() {
    }

    /**
     * method to initialize the api contact
     *
     * @return return value
     */
    public APIContact initializeApiContact() {
        if (mApiContact == null) {
            mApiContact = this.mApiService.RootService(APIContact.class, BuildConfig.BACKEND_BASE_URL);
        }
        return mApiContact;
    }


    /**
     * method to get all contacts
     *
     * @return return value
     */
    public Observable<RealmResults<UsersModel>> getAllContacts() {
        String[] fieldNames = {"activate", "username", "linked"};
        Sort sort[] = {Sort.DESCENDING, Sort.ASCENDING, Sort.DESCENDING};
        RealmResults<UsersModel> usersModels = realm.where(UsersModel.class)
                .notEqualTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                .equalTo("exist", true)
                .sort(fieldNames, sort).findAll();
        return Observable.just(usersModels);
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Observable<RealmResults<UsersModel>> getLinkedContacts() {

        String[] fieldNames = {"activate", "username", "linked"};
        Sort sort[] = {Sort.DESCENDING, Sort.ASCENDING, Sort.DESCENDING};
        RealmResults<UsersModel> usersModels = realm.where(UsersModel.class)
                .notEqualTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                .equalTo("exist", true)
                .equalTo("linked", true)
                .equalTo("activate", true)
                .sort(fieldNames, sort).findAll();
        return Observable.just(usersModels);
    }

    public int getLinkedContactsSize() {
        RealmResults<UsersModel> usersModels = realm.where(UsersModel.class)
                .notEqualTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                .equalTo("exist", true)
                .equalTo("linked", true)
                .equalTo("activate", true)
                .findAll();
        return usersModels.size();
    }

    /**
     * method to get linked contacts
     *
     * @return return value
     */
    public Observable<RealmResults<UsersBlockModel>> getBlockedContacts() {
        RealmResults<UsersBlockModel> usersModel = realm.where(UsersBlockModel.class).notEqualTo("usersModel._id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).equalTo("usersModel.linked", true).equalTo("usersModel.activate", true).sort("usersModel.username", Sort.ASCENDING).findAll();
        return Observable.just(usersModel).filter(RealmResults::isLoaded);
    }

    /**
     * method to update(syncing) contacts
     *
     * @param contacts
     * @return return value
     */
    public Observable<List<UsersModel>> updateContacts(List<UsersModel> contacts) {

        SyncContacts syncContacts = new SyncContacts();
        syncContacts.setUsersModelList(contacts);
        return initializeApiContact().contacts(syncContacts)
                .subscribeOn(Schedulers.computation())
                // Read results in Android Main Thread (UI)
                .observeOn(AndroidSchedulers.mainThread())
                .map(this::copyOrUpdateContacts);
    }

    /**
     * method to get general user information
     *
     * @param userID this is parameter  getContact for method
     * @return return value
     */
    public UsersModel getUser(String userID) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mContext);
        try {
            UsersModel usersModel = realm.where(UsersModel.class).equalTo("_id", userID).findFirst();
            if (usersModel != null)
                return usersModel;
            else
                return new UsersModel();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    public UsersModel getContactLocal(String userID) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mContext);
        UsersModel contactsModel = realm.where(UsersModel.class).equalTo("_id", userID).findFirst();
        if (!realm.isClosed()) realm.close();
        if (contactsModel != null)
            return contactsModel;
        else
            return new UsersModel();
    }

    /**
     * method to get user information from the server
     *
     * @param userID this is parameter for getContactInfo method
     * @return return  value
     */
    public Observable<UsersModel> getContactInfo(String userID) {
        /*
        return initializeApiContact().contact(userID)
                // Request API data on IO Scheduler
                .subscribeOn(Schedulers.io())
                // Write to Realm on Computation scheduler
                .observeOn(Schedulers.computation())
                .map(this::copyOrUpdateContactInfo)
                .observeOn(AndroidSchedulers.mainThread())
                .map(contactsModel -> getContactLocal(userID));*/
        return null;
    }


    public Observable<UsersModel> getUserLocalInfo(String userID) {
        // Read any cached results
        UsersModel cachedData = getUser(userID);
        return Observable.just(cachedData);
    }

    public Observable<UsersModel> getUserInfo(String userID) {
        if (AppHelper.isOnline(WhatsCloneApplication.getInstance())) {

            Observable<UsersModel> observable = initializeApiContact().getUser(userID)
                    // Request API data on IO Scheduler
                    .subscribeOn(Schedulers.io())
                    // Write to Realm on Computation scheduler
                    //.subscribeOn(Schedulers.computation())
                    // Read results in Android Main Thread (UI)
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(this::copyOrUpdateUserInfo);


            // Read any cached results
            UsersModel cachedData = getUser(userID);
            if (cachedData != null)
                // Merge with the observable from API
                return observable.mergeWith(Observable.just(cachedData));
            else
                return observable;
        } else {

            // Read any cached results
            UsersModel cachedData = getUser(userID);
            return Observable.just(cachedData);
        }

    }
/*
    public Observable<UsersModel> getContactServer(String userID) {


        return initializeApiContact().contact(userID)
                .delay(3L, TimeUnit.SECONDS)
                // Request API data on IO Scheduler
                .subscribeOn(Schedulers.io())
                // Write to Realm on Computation scheduler
                .observeOn(Schedulers.computation())
                .map(this::copyOrUpdateContactInfo);
    }*/

    /**
     * method to get all status
     *
     * @return return value
     */
    public RealmResults<StatusModel> getAllStatus() {
        // Realm realm = WhatsCloneApplication.getRealmDatabaseInstance();
        RealmResults<StatusModel> statusModels = realm.where(StatusModel.class).equalTo("userId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).sort("created", Sort.DESCENDING).findAll();
        //  if (!realm.isClosed()) realm.close();
        return statusModels;
    }

    /**
     * method to get user status from server
     *
     * @return return value
     */
    public Observable<List<StatusModel>> getUserStatus(String userId) {

        if (AppHelper.isOnline(WhatsCloneApplication.getInstance())) {

            Observable<List<StatusModel>> observable = initializeApiContact().status(userId)
                    // .delay(3, TimeUnit.SECONDS)
                    // Request API data on IO Scheduler
                    .subscribeOn(Schedulers.io())
                    // Write to Realm on Computation scheduler
                    //  .observeOn(Schedulers.computation())
                    .observeOn(AndroidSchedulers.mainThread())
                    .map(this::copyOrUpdateStatus);


            // Read any cached results
            List<StatusModel> cachedData = getAllStatus();
            if (cachedData != null)
                if (cachedData.size() == 0)
                    // Concat with the observable from API
                    return Observable.concat(observable, Observable.just(cachedData));
                else
                    // Merge with the observable from API
                    return observable.mergeWith(Observable.just(cachedData));
            else
                return observable;
        } else {

            // Read any cached results
            List<StatusModel> cachedData = getAllStatus();
            return Observable.just(cachedData);
        }


    }

    /**
     * method to delete user status
     *
     * @param status this is parameter for deleteStatus method
     * @return return  value
     */
    public Observable<StatusResponse> deleteStatus(String status) {
        return initializeApiContact().deleteStatus(status)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to delete all user status
     *
     * @return return value
     */
    public Observable<StatusResponse> deleteAllStatus() {
        return initializeApiContact().deleteAllStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to update user status
     *
     * @param statusID this is parameter for updateStatus method
     * @return return  value
     */
    public Observable<StatusResponse> updateStatus(String statusID, String currentStatusId) {
        EditStatus editStatus = new EditStatus();
        editStatus.setCurrentStatusId(currentStatusId);
        editStatus.setStatusId(statusID);
        return initializeApiContact().updateStatus(editStatus)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit user status
     *
     * @param newStatus this is the first parameter for editStatus method
     * @param statusID  this is the second parameter for editStatus method
     * @return return  value
     */
    public Observable<StatusResponse> editStatus(String newStatus, String statusID) {
        NewStatus newStatus1 = new NewStatus();
        newStatus1.setNewStatus(newStatus);
        newStatus1.setStatusId(statusID);
        return initializeApiContact().editStatus(newStatus1)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit username
     *
     * @param newName this is parameter for editUsername method
     * @return return  value
     */
/*    public Observable<StatusResponse> editUsername(String newName) {
        EditUser editUser = new EditUser();
        editUser.setUsername(newName);
        return initializeApiContact().editUsername(editUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);

    }*/


    /**
     * method to edit user image
     *
     * @param newImage this is parameter for editUsername method
     * @return return  value
     */
    public Observable<StatusResponse> editUserImage(String newImage) {
        EditUser editUser = new EditUser();
        editUser.setImage(newImage);
        return initializeApiContact().editUserImage(editUser)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to get current status fron local
     *
     * @return return value
     */
    public Observable<StatusModel> getCurrentStatusFromLocal() {
        StatusModel statusModels = realm.where(StatusModel.class).equalTo("userId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).equalTo("current", true).findFirst();
        if (statusModels != null)
            return Observable.just(statusModels).filter(statusFromLocal -> statusFromLocal.isLoaded()).switchIfEmpty(Observable.just(new StatusModel()));
        else
            return Observable.just(new StatusModel());
    }

    public Observable<StatusResponse> saveNewCall(CallSaverModel callSaverModel) {
        return initializeApiContact().saveNewCall(callSaverModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);

    }


/*    public Observable<BlockResponse> block(String userId) {
        return initializeApiContact().block(userId)
                .subsribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(usersResponse -> usersResponse);

    }*/

    public Observable<BlockResponse> unbBlock(String userId) {
        return initializeApiContact().unBlock(userId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(usersResponse -> usersResponse);

    }

    /**
     * method to delete user status
     *
     * @return return  value
     */
    public Observable<JoinModelResponse> deleteAccount(LoginModel loginModel) {
        return initializeApiContact().deleteAccount(loginModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(response -> response);
    }


    /**
     * method to copy or update user status
     *
     * @param statusModels this is parameter for copyOrUpdateStatus method
     * @return return  value
     */
    private List<StatusModel> copyOrUpdateStatus(List<StatusModel> statusModels) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mContext);
        realm.beginTransaction();
        List<StatusModel> statusModels1 = realm.copyToRealmOrUpdate(statusModels);
        realm.commitTransaction();
        if (!realm.isClosed()) realm.close();
        return statusModels1;
    }

    /**
     * method to copy or update contacts list
     *
     * @param mListContacts this is parameter for copyOrUpdateContacts method
     * @return return  value
     */
    private List<UsersModel> copyOrUpdateContacts(List<UsersModel> mListContacts) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mContext);
    /*    List<ContactsModel> finalList = checkContactList(mListContacts, realm);
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
        }*/


        realm.beginTransaction();
        List<UsersModel> contactsModels = realm.copyToRealmOrUpdate(mListContacts);
        realm.commitTransaction();
        if (!realm.isClosed()) realm.close();
        return contactsModels;
    }


    private List<UsersModel> checkContactList(List<UsersModel> contactsModelList, Realm realm) {
        if (contactsModelList.size() != 0) {
            realm.executeTransactionAsync(realm1 -> {
                RealmResults<UsersModel> contactsModels = realm1.where(UsersModel.class).findAll();
                contactsModels.deleteAllFromRealm();
            });
        }
        return contactsModelList;
    }

    private boolean checkIfContactExist(String id, Realm realm) {
        RealmQuery<UsersModel> query = realm.where(UsersModel.class).equalTo("_id", id);
        return query.count() != 0;

    }

    /**
     * method to copy or update user information
     *
     * @param usersModel this is parameter for copyOrUpdateContactInfo method
     * @return return  value
     */
    private UsersModel copyOrUpdateUserInfo(UsersModel usersModel) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mContext);
        UsersModel realmContact;
        try {

            if (UtilsPhone.checkIfContactExist(WhatsCloneApplication.getInstance(), usersModel.getPhone())) {
                realm.beginTransaction();
                usersModel.setExist(true);
                realmContact = realm.copyToRealmOrUpdate(usersModel);
                realm.commitTransaction();
            } else {
                realm.beginTransaction();
                usersModel.setExist(false);
                realmContact = realm.copyToRealmOrUpdate(usersModel);
                realm.commitTransaction();

            }
        } finally {
            if (!realm.isClosed()) realm.close();
        }


        return realmContact;
    }


    public Observable<SettingsResponse> getAppSettings() {
        return initializeApiContact().getAppSettings()
                .delay(3L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation());
    }


    /**
     * *
     * method to get all calls
     *
     * @return return value
     */
    public Observable<RealmResults<CallsModel>> getAllCalls() {
        RealmResults<CallsModel> callsModel = realm.where(CallsModel.class).sort("date", Sort.DESCENDING).findAll();
        return Observable.just(callsModel);
    }

    /**
     * *
     * method to get all calls details
     *
     * @return return value
     */
    public Observable<RealmResults<CallsInfoModel>> getAllCallsDetails(String callID) {
        RealmResults<CallsInfoModel> callsInfoModel = realm.where(CallsInfoModel.class)
                .equalTo("callId", callID)
                .sort("date", Sort.DESCENDING).findAll();

        return Observable.just(callsInfoModel);
    }

    /**
     * method to get general call information
     *
     * @param callID this is parameter  getContact for method
     * @return return value
     */
    public Observable<CallsModel> getCallDetails(String callID) {
        CallsModel callsModel = realm.where(CallsModel.class).equalTo("_id", callID).findFirst();
        if (callsModel != null)
            return Observable.just(callsModel).filter(callsModel1 -> callsModel1.isLoaded()).switchIfEmpty(Observable.just(new CallsModel()));
        else
            return Observable.just(new CallsModel());
    }


    public Observable<NetworkModel> checkIfUserSession() {
        return initializeApiContact().checkNetwork()
                .delay(3L, TimeUnit.SECONDS)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(networkModel -> networkModel);
    }
    public Observable<StatusResponse> deleteStory(String storyId) {
        return initializeApiContact().deleteStory(storyId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    public Observable<StatusResponse> sendMessage(UpdateMessageModel updateMessageModel) {

        return initializeApiContact().sendMessage(PreferenceManager.getInstance().getToken(AGApplication.getInstance()),updateMessageModel)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.computation())
                .map(statusResponse -> statusResponse);
    }

    public StoriesHeaderModel getMineStories() {
        return realm.where(StoriesHeaderModel.class)
                .equalTo("stories.deleted", false)
                .findFirst();
    }

    public Observable<RealmResults<StoriesModel>> getAllStories() {
        RealmResults<StoriesModel> storiesModels = realm.where(StoriesModel.class)
                .equalTo("stories.deleted", false)
                .sort("downloaded", Sort.ASCENDING).findAll();
        return Observable.just(storiesModels).filter(RealmResults::isLoaded);
    }

    public Observable<RealmResults<StoryModel>> getStories() {

        RealmResults<StoryModel> storyModels = realm.where(StoryModel.class)
                .equalTo("userId", PreferenceManager.getInstance().getID(AGApplication.getInstance()))
                .equalTo("deleted", false)
                .sort("date", Sort.ASCENDING).findAll();
        return Observable.just(storyModels).filter(RealmResults::isLoaded);
    }

    public Observable<RealmList<UsersModel>> getSeenList(String storyId) {

        StoryModel storyModel = realm.where(StoryModel.class)
                .equalTo("_id", storyId)
                .equalTo("deleted", false)
                .equalTo("userId", PreferenceManager.getInstance().getID(AGApplication.getInstance()))
                .sort("date", Sort.ASCENDING).findFirst();
        return Observable.just(storyModel.getSeenList()).filter(RealmList::isLoaded);
    }

    public Observable<StatusResponse> createStory(CreateStoryModel createStoryModel) {
        return initializeApiContact().createStory(createStoryModel)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupResponse -> groupResponse);

    }




    public Observable<StatusResponse> deleteAccountConfirmation(String code) {
        return initializeApiContact().deleteAccountConfirmation(code)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }


    public Observable<StatusResponse> deleteConversation(String conversationId) {
        return initializeApiContact().deleteConversation(conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    public Observable<StatusResponse> deleteMessage(String messageId) {
        return initializeApiContact().deleteMessage(messageId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

}
