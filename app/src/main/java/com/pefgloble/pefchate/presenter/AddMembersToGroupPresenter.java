package com.pefgloble.pefchate.presenter;



import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.activities.group.AddMembersToGroupActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 26/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupPresenter implements Presenter {
    private final AddMembersToGroupActivity view;
    private final Realm realm;
    private CompositeDisposable mDisposable;


    public AddMembersToGroupPresenter(AddMembersToGroupActivity addMembersToGroupActivity) {
        this.view = addMembersToGroupActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        mDisposable = new CompositeDisposable();
        mDisposable.add(APIHelper.initialApiUsersContacts().getLinkedContacts().subscribe(usersModels->{
            AppHelper.LogCat("usersModels "+usersModels);
            view.ShowContacts(usersModels);
        }, throwable -> AppHelper.LogCat("AddMembersToGroupPresenter " + throwable.getMessage())));

    }


    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {

    }

    @Override
    public void onDestroy() {
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
}