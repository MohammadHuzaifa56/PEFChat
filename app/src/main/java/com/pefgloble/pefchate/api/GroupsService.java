package com.pefgloble.pefchate.api;

import android.content.Context;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.groups.EditGroup;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupRequest;
import com.pefgloble.pefchate.JsonClasses.groups.GroupResponse;
import com.pefgloble.pefchate.JsonClasses.groups.MemberRequest;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModel;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.JsonClasses.status.StatusResponse;
import com.pefgloble.pefchate.RestAPI.APIGroups;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;


import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmQuery;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class GroupsService {
    private APIGroups mApiGroups;
    private Context mContext;
    private Realm realm;
    private APIService mApiService;

    public GroupsService(Realm realm, Context context, APIService mApiService) {
        this.mContext = context;
        this.realm = realm;
        this.mApiService = mApiService;

    }

    /**
     * method to initialize the api groups
     *
     * @return return value
     */
    private APIGroups initializeApiGroups() {
        if (mApiGroups == null) {
            mApiGroups = mApiService.RootService(APIGroups.class, BuildConfig.BACKEND_BASE_URL);
        }
        return mApiGroups;
    }

    /**
     * method to get all groups list
     *
     * @return return value
     */
    public Observable<List<GroupModel>> getGroups() {
        List<GroupModel> groups = realm.where(GroupModel.class).findAll();
        return Observable.just(groups);
    }


    /**
     * method to get single group information
     *
     * @param groupID this is parameter for  getGroupInfo method
     * @return return value
     */
    /* public Observable<GroupsModel> getGroupInfo(String groupID) {
     */

    /**
     * Load data from api layer and save it to DB.
     *//*
        return initializeApiGroups().getGroup(groupID)
                // Request API data on IO Scheduler
                .subscribeOn(Schedulers.io())
                // Write to Realm on Computation scheduler
                .observeOn(Schedulers.computation())
                //.map(this::copyOrUpdateGroup)
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupsModel -> getGroupLocal(groupID));

    }*/
    public Observable<GroupModel> getGroupInfoLocal(String groupID) {
        // Read any cached results
        GroupModel cachedData = getGroup(groupID);
        return Observable.just(cachedData);
    }

    public Observable<GroupModel> getGroupInfo(String groupID) {

/*
        return realm.where(GroupModel.class).equalTo("_id", groupID).findFirst().asFlowable()
                // We only want the list once it is loaded.
                // .filter(RealmResults::isLoaded)
                // .switchMap(Flowable::fromIterable)

                // get GitHub statistics.
                .flatMap(user -> initializeApiGroups().getGroup(groupID))

                // Map Network model to our View model
                .map(this::copyOrUpdateGroup)

                // Retrofit put us on a worker thread. Move back to UI
                .observeOn(AndroidSchedulers.mainThread());*/
        if (AppHelper.isOnline(AGApplication.getInstance())) {

            Observable<GroupModel> observable =
                    initializeApiGroups().getGroup(groupID)
                            // Request API data on IO Scheduler
                            //  .subscribeOn(Schedulers.io())
                            // Write to Realm on Computation scheduler
                            .subscribeOn(Schedulers.computation())
                            // Read results in Android Main Thread (UI)
                            .observeOn(AndroidSchedulers.mainThread())
                            .map(this::copyOrUpdateGroup);


            // Read any cached results
            GroupModel cachedData = getGroup(groupID);
            if (cachedData != null)
                // Merge with the observable from API
                return observable.mergeWith(Observable.just(cachedData));
            else
                return observable;
        } else {

            // Read any cached results
            GroupModel cachedData = getGroup(groupID);
           return Observable.just(cachedData);
        }

    }

    /**
     * method to get group information from local
     *
     * @param groupID this is parameter for getGroup method
     * @return return value
     */
    public GroupModel getGroup(String groupID) {


        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        GroupModel groupsModel;
        try {
            groupsModel = realm.where(GroupModel.class).equalTo("_id", groupID).findFirst();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        return groupsModel;

    }

    /**
     * method to get group information from local
     *
     * @param groupID this is parameter for getGroup method
     * @return return value
     */
    public GroupModel getGroupLocal(String groupID) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        GroupModel groupsModel = realm.where(GroupModel.class).equalTo("_id", groupID).findFirst();
        if (!realm.isClosed()) realm.close();
        if (groupsModel != null)
            return groupsModel;
        else
            return new GroupModel();
    }


    /**
     * method to copy or update a single group
     *
     * @param groupsModel this is parameter for copyOrUpdateGroup method
     * @return return value
     */
    private GroupModel copyOrUpdateGroup(GroupModel groupsModel) {

        realm.beginTransaction();
        GroupModel groupsModel1 = realm.copyToRealmOrUpdate(groupsModel);
        realm.commitTransaction();
        return groupsModel1;
    }

    /**
     * method to check if a group conversation exist
     *
     * @param groupID this is parameter for checkIfGroupConversationExist method
     * @return return value
     */
    private boolean checkIfGroupConversationExist(int groupID, Realm realm) {
        RealmQuery<ConversationModel> query = realm.where(ConversationModel.class).equalTo("groupId", groupID);
        return query.count() != 0;
    }

    /**
     * method to check for id 0
     *
     * @return return value
     */
    public boolean checkIfZeroExist(Realm realm) {
        RealmQuery<MessageModel> query = realm.where(MessageModel.class).equalTo("_id", 0);
        return query.count() != 0;
    }


    /**
     * methods for get group members
     *
     * @param groupID this is parameter for getGroupMembers method
     * @return return value
     */
    public List<MembersModel> getGroupMembers(String groupID) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        List<MembersModel> membersGroupModels = realm.where(MembersModel.class).equalTo("groupId", groupID).equalTo("deleted", false).equalTo("left", false).findAll();
        if (!realm.isClosed()) realm.close();
        return membersGroupModels;
    }

    public Observable<List<MembersModel>> getGroupMembersLocal(String groupID) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        List<MembersModel> membersGroupModels = realm.where(MembersModel.class).equalTo("groupId", groupID).equalTo("deleted", false).equalTo("left", false).findAll();
        if (!realm.isClosed()) realm.close();
        if (membersGroupModels.size() > 0)
            return Observable.just(membersGroupModels);
        else
            return Observable.just(new ArrayList<>());
    }


    public Observable<GroupResponse> createGroup(GroupRequest groupRequest) {
        return initializeApiGroups().createGroup(groupRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupResponse -> groupResponse);

    }


    public Observable<GroupResponse> addMembers(String groupId,
                                                String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupId);
        memberRequest.setUserId(userId);
        return initializeApiGroups().addMembers(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(groupResponse -> groupResponse);

    }

    /**
     * method to exit a group
     *
     * @param groupID this is parameter for ExitGroup method
     * @return return value
     */
    public Observable<GroupResponse> ExitGroup(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().exitGroup(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }


    /**
     * method to edit group name
     *
     * @param newName this is the first parameter for editGroupName method
     * @param groupID this is the second parameter for editGroupName method
     * @return return  value
     */
    public Observable<StatusResponse> editGroupName(String newName, String groupID) {
        EditGroup editGroupName = new EditGroup();
        editGroupName.setName(newName);
        editGroupName.setGroupId(groupID);
        return initializeApiGroups().editGroupName(editGroupName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to edit group name
     *
     * @param newImage this is the first parameter for editGroupImage method
     * @param groupID  this is the second parameter for editGroupImage method
     * @return return  value
     */
    public Observable<StatusResponse> editGroupImage(String newImage, String groupID) {
        EditGroup editGroupName = new EditGroup();
        editGroupName.setImage(newImage);
        editGroupName.setGroupId(groupID);
        return initializeApiGroups().editGroupImage(editGroupName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(statusResponse -> statusResponse);
    }

    /**
     * method to delete group
     *
     * @param groupID this is parameter for DeleteGroup method
     * @return return value
     */
    public Observable<GroupResponse> DeleteGroup(String groupID, String userId, String conversationId) {
        return initializeApiGroups().deleteGroup(groupID, userId, conversationId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * method to make user as member
     *
     * @param groupID this is parameter for makeAdminMember method
     * @param userId  this is parameter for makeAdminMember method
     * @return return value
     */
    public Observable<GroupResponse> makeAdminMember(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().makeAdminMember(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * method to make user as admin
     *
     * @param groupID this is parameter for makeMemberAdmin method
     * @param userId  this is parameter for makeMemberAdmin method
     * @return return value
     */
    public Observable<GroupResponse> makeMemberAdmin(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().makeMemberAdmin(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    /**
     * method to remove member from group
     *
     * @param groupID this is parameter for removeMember method
     * @param userId  this is parameter for removeMember method
     * @return return value
     */
    public Observable<GroupResponse> removeMember(String groupID, String userId) {
        MemberRequest memberRequest = new MemberRequest();
        memberRequest.setGroupId(groupID);
        memberRequest.setUserId(userId);
        return initializeApiGroups().removeMember(memberRequest)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }
}
