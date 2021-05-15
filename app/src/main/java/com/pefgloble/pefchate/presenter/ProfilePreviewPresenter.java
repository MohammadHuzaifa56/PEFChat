package com.pefgloble.pefchate.presenter;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.activities.ProfilePreviewActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.GroupsService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/02/2016. Email : abderrahim.elimame@gmail.com
 */
public class ProfilePreviewPresenter implements Presenter {
    private ProfilePreviewActivity profilePreviewActivity;
    private Realm realm;
    private UsersService mUsersContacts;
    private CompositeDisposable mDisposable;
    private String userID;
    private String groupID;
    private GroupsService mGroupsService;

    public ProfilePreviewPresenter(ProfilePreviewActivity profilePreviewActivity) {
        this.profilePreviewActivity = profilePreviewActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }


    @Override
    public void onStart() {

    }

    @Override
    public void
    onCreate() {

        mDisposable = new CompositeDisposable();
        if (profilePreviewActivity != null) {
            APIService mApiService = APIService.with(profilePreviewActivity);

            if (profilePreviewActivity.getIntent().hasExtra("userID")) {
                userID = profilePreviewActivity.getIntent().getExtras().getString("userID");
                mUsersContacts = new UsersService(realm, profilePreviewActivity, mApiService);
                getContactLocal();
                //getContactServer();

            }

            if (profilePreviewActivity.getIntent().hasExtra("groupID")) {
                mGroupsService = new GroupsService(realm, profilePreviewActivity, mApiService);
                groupID = profilePreviewActivity.getIntent().getExtras().getString("groupID");

                getGroupLocal();
                getGroupServer();

            }
        }
    }

    private void getGroupLocal() {
        /*mDisposable.addAll(mGroupsService.getGroup(groupID).subscribe(groupsModel -> {
            profilePreviewActivity.ShowGroup(groupsModel);
        }, throwable -> {
            profilePreviewActivity.onErrorLoading(throwable);
        }))
        ;*/


        mDisposable.add(APIHelper.initializeApiGroups().getGroupInfo(groupID).subscribe(groupModel -> {
            AppHelper.LogCat("groupModel " + groupModel.toString());
            //  Response response = new Response(Response.STATUS_SUCCESS, groupModel.toString());
            //   responseObserver.onChanged(response);
            profilePreviewActivity.ShowGroup(groupModel);
        }, throwable -> {
            AppHelper.LogCat("groupModel throwable" + throwable.getMessage());
            // Response response = new Response(Response.STATUS_FAIL, throwable.getMessage());
            //  responseObserver.onChanged(response);
        }));
    }

    private void getGroupServer() {
      /*  mDisposable.addAll(mGroupsService.getGroupInfo(groupID).subscribe(groupsModel -> {
            profilePreviewActivity.ShowGroup(groupsModel);
            AppHelper.LogCat("groupsModel " + groupsModel.getName());
        }, throwable -> {
            profilePreviewActivity.onErrorLoading(throwable);
        }))
        ;*/
    }


    private void getContactLocal() {

        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(userID).subscribe(usersModel -> {
            AppHelper.LogCat("usersModel " + usersModel.toString());

            profilePreviewActivity.ShowContact(usersModel);
        }, throwable -> {
            AppHelper.LogCat("usersModel throwable" + throwable.getMessage());

        }));
       /* mDisposable.addAll(mUsersContacts.getContact(userID).subscribe(contactsModel -> {
            profilePreviewActivity.ShowContact(contactsModel);
        }, throwable -> {
            profilePreviewActivity.onErrorLoading(throwable);
        }))
        ;*/
    }
/*
    private void getContactServer() {
        mDisposable.addAll(mUsersContacts.getContactInfo(userID).subscribe(contactsModel -> {
            profilePreviewActivity.ShowContact(contactsModel);
        }, throwable -> {
            profilePreviewActivity.onErrorLoading(throwable);
        }))
        ;
    }*/

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
    }

    @Override
    public void onDestroy() {
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

}