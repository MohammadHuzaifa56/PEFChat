package com.pefgloble.pefchate.presenter;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.status.StatusModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.staus.EditStatusActivity;
import com.pefgloble.pefchate.staus.StatusActivity;
import com.pefgloble.pefchate.staus.StatusDelete;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusPresenter implements Presenter {

    private StatusActivity view;
    private EditStatusActivity editStatusActivity;
    private StatusDelete viewDelete;
    private Realm realm;
    private UsersService mUsersContacts;
    private APIService mApiService;
    private CompositeDisposable mDisposable;

    public StatusPresenter(StatusActivity statusActivity) {
        this.view = statusActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        this.mApiService = APIService.with(this.view);
        this.mUsersContacts = new UsersService(this.realm, this.view, this.mApiService);
        mDisposable = new CompositeDisposable();
    }

    public StatusPresenter(StatusDelete statusDelete) {
        this.viewDelete = statusDelete;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();

    }

    public StatusPresenter(EditStatusActivity editStatusActivity) {
        this.editStatusActivity = editStatusActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        this.mApiService = APIService.with(this.editStatusActivity);
        this.mUsersContacts = new UsersService(this.realm, this.editStatusActivity, this.mApiService);
        mDisposable = new CompositeDisposable();
    }


    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (!EventBus.getDefault().isRegistered(view)) EventBus.getDefault().register(view);

        this.mApiService = APIService.with(view);
        mUsersContacts = new UsersService(realm, view, this.mApiService);
        getStatusFromServer();
        getCurrentStatus();

    }


    private void getStatusFromServer() {
        mDisposable.addAll(mUsersContacts.getUserStatus(PreferenceManager.getInstance().getID(view)).subscribe(view::ShowStatus, view::onErrorLoading));
    }

    public void getCurrentStatus() {
        try {
            mDisposable.addAll(mUsersContacts.getCurrentStatusFromLocal().subscribe(view::ShowCurrentStatus, throwable -> AppHelper.LogCat(" " + throwable.getMessage())))
            ;
        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }
    }

    @Override
    public void onPause() {

    }

    @Override
    public void onResume() {
    }

    @Override
    public void onDestroy() {
        if (view != null) {
            EventBus.getDefault().unregister(view);
        }
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

    public void DeleteAllStatus() {
        AppHelper.showDialog(view, "Delete All Status");
        mDisposable.addAll(mUsersContacts.deleteAllStatus().subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                realm.executeTransaction(realm1 -> realm1.where(StatusModel.class).equalTo("userId", PreferenceManager.getInstance().getID(view)).findAll().deleteAllFromRealm());
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                view.startActivity(view.getIntent());
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            }
        }, throwable -> {
            AppHelper.LogCat("Delete Status Error StatusPresenter ");
            AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus),view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);


            AppHelper.hideDialog();
        }))
        ;
    }


    public void DeleteStatus(String statusID) {
        APIService mApiServiceDelete = APIService.with(viewDelete);
        UsersService mUsersContactsDelete = new UsersService(realm, viewDelete, mApiServiceDelete);
        AppHelper.showDialog(viewDelete, "Deleting");
        mDisposable.add(mUsersContactsDelete.deleteStatus(statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_DELETE_STATUS, statusID));
                viewDelete.finish();
            } else {
                AppHelper.hideDialog();
                AppHelper.LogCat("delete  status " + statusResponse.getMessage());
                AppHelper.CustomToast(viewDelete, viewDelete.getString(R.string.oops_something));
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("delete  status " + throwable.getMessage());
            AppHelper.CustomToast(viewDelete, viewDelete.getString(R.string.oops_something));
        }))
        ;
    }

    public void UpdateCurrentStatus(String status, String statusID, String currentStatusId) {
        AppHelper.showDialog(view, "Updating Status");
        mDisposable.add(mUsersContacts.updateStatus(statusID, currentStatusId).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_STATUS, status));


            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("update current status " + throwable.getMessage());
            AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

        }))
        ;

    }


    public void EditCurrentStatus(String status, String statusID) {
        mDisposable.add(mUsersContacts.editStatus(status, statusID).subscribe(statusResponse -> {
            if (statusResponse.isSuccess()) {
                AppHelper.hideDialog();
                AppHelper.Snackbar(editStatusActivity.getBaseContext(), editStatusActivity.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_STATUS, status));
                editStatusActivity.finish();
            } else {
                AppHelper.hideDialog();
                AppHelper.Snackbar(editStatusActivity.getBaseContext(), editStatusActivity.findViewById(R.id.layout_container), statusResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
            }
        }, throwable -> {
            AppHelper.hideDialog();
            AppHelper.LogCat("update current status " + throwable.getMessage());
            AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), editStatusActivity.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

        }))
        ;

    }

    public void onEventPush(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_DELETE_STATUS:
                String id = pusher.getStatusID();
                realm.executeTransactionAsync(realm1 -> {
                    realm1.where(StatusModel.class).equalTo("_id", id).findFirst().deleteFromRealm();
                }, () -> {
                    view.deleteStatus(id);
                    AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), "Your Status Updated Successfully", AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    getStatusFromServer();
                    getCurrentStatus();
                }, error -> {
                    AppHelper.LogCat(error.getMessage());
                    AppHelper.Snackbar(view.getBaseContext(), view.findViewById(R.id.ParentLayoutStatus), view.getString(R.string.oops_something), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                });
                break;
            case AppConstants.EVENT_BUS_UPDATE_STATUS:
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPDATE_CURRENT_SATUS));
                if (pusher.getData() != null)
                    view.ShowCurrentStatus(pusher.getData());
                getStatusFromServer();
                getCurrentStatus();
                break;
        }
    }
}