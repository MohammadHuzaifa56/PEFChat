package com.pefgloble.pefchate.presenter;


import android.Manifest;
import android.os.Handler;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.NewConversationContactsActivity;
import com.pefgloble.pefchate.activities.stories.PrivacyContactsActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.ContactsFragment;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.ForegroundRuning;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.files.UsersPrivacyModel;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;

import java.util.List;

import androidx.appcompat.app.AlertDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsPresenter implements Presenter {
    private NewConversationContactsActivity newConversationContactsActivity;
    private PrivacyContactsActivity privacyContactsActivity;
    private ContactsFragment contactsFragment;
    private Realm realm;
    private UsersService mUsersContacts;

    private CompositeDisposable mDisposable;

    public ContactsPresenter(PrivacyContactsActivity privacyContactsActivity) {
        this.privacyContactsActivity = privacyContactsActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
    }

    public ContactsPresenter(NewConversationContactsActivity newConversationContactsActivity) {
        this.newConversationContactsActivity = newConversationContactsActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
    }

    public ContactsPresenter(ContactsFragment contactsFragment) {
        this.contactsFragment = contactsFragment;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
    }


    @Override
    public void onStart() {
    }

    @Override
    public void onCreate() {

        if (newConversationContactsActivity != null) {
            APIService mApiService = APIService.with(newConversationContactsActivity);
            mUsersContacts = new UsersService(realm, newConversationContactsActivity, mApiService);
            getContacts();
        } else if (privacyContactsActivity != null) {

            APIService mApiService = APIService.with(privacyContactsActivity);
            mUsersContacts = new UsersService(realm, privacyContactsActivity, mApiService);
            getContacts();
        } else {

            APIService mApiService = APIService.with(contactsFragment.getActivity());
            mUsersContacts = new UsersService(realm, contactsFragment.getActivity(), mApiService);
            getContactsFragment();
        }

    }

    public void getContactsFragment() {
        try {

            mDisposable.add(mUsersContacts.getAllContacts().subscribe(contactsModels -> {
                contactsFragment.updateContacts(contactsModels);
            }, throwable -> {
                contactsFragment.onErrorLoading(throwable);
            }, () -> {
                contactsFragment.onHideLoading();
            }))
            ;
            try {
                PreferenceManager.getInstance().setContactSize(contactsFragment.getActivity(), mUsersContacts.getLinkedContactsSize());
            } catch (Exception e) {
                AppHelper.LogCat(" Exception size contact fragment");
            }
        } catch (Exception e) {
            AppHelper.LogCat("getAllContacts Exception ContactsPresenter " + e.getMessage());
        }
        if (PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()) == 0) {
            loadDataFromServer();
        }
    }

    public void getContacts() {
        if (newConversationContactsActivity != null) {
            newConversationContactsActivity.onShowLoading();
            try {

                mDisposable.add(mUsersContacts.getAllContacts().subscribe(contactsModels -> {
                    newConversationContactsActivity.ShowContacts(contactsModels);
                }, throwable -> {
                    newConversationContactsActivity.onErrorLoading(throwable);
                }, () -> {
                    newConversationContactsActivity.onHideLoading();
                }))
                ;
                try {
                    PreferenceManager.getInstance().setContactSize(newConversationContactsActivity, mUsersContacts.getLinkedContactsSize());
                } catch (Exception e) {
                    AppHelper.LogCat(" Exception size contact fragment");
                }
            } catch (Exception e) {
                AppHelper.LogCat("getAllContacts Exception ContactsPresenter " + e.getMessage());
            }
            if (PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()) == 0) {
                loadDataFromServer();
            }


        } else if (privacyContactsActivity != null) {
            privacyContactsActivity.onShowLoading();
            try {

                mDisposable.add(mUsersContacts.getLinkedContacts().subscribe(contactsModels -> {
                    privacyContactsActivity.ShowContacts(contactsModels);
                }, throwable -> {
                    privacyContactsActivity.onErrorLoading(throwable);
                }, () -> {
                    privacyContactsActivity.onHideLoading();
                }))
                ;
                try {
                    PreferenceManager.getInstance().setContactSize(privacyContactsActivity, mUsersContacts.getLinkedContactsSize());
                } catch (Exception e) {
                    AppHelper.LogCat(" Exception size contact fragment");
                }
            } catch (Exception e) {
                AppHelper.LogCat("getAllContacts Exception ContactsPresenter " + e.getMessage());
            }
            if (PreferenceManager.getInstance().getContactSize(WhatsCloneApplication.getInstance()) == 0) {
                loadDataFromServer();
            }
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
        if (!realm.isClosed())
            realm.close();
        if (mDisposable != null)
            mDisposable.dispose();
    }

    @Override
    public void onLoadMore() {

    }


    @Override
    public void onRefresh() {
        if (newConversationContactsActivity != null) {
            if (Permissions.hasAny(newConversationContactsActivity, Manifest.permission.READ_CONTACTS, Manifest.permission.WRITE_CONTACTS)) {

                AppHelper.LogCat("Read contact data permission already granted.");
                newConversationContactsActivity.onShowLoading();
                mDisposable.addAll(Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {


                    try {
                        List<UsersModel> contactsModels = UtilsPhone.getInstance().GetPhoneContacts();
                        subscriber.onNext(contactsModels);
                        subscriber.onComplete();
                    } catch (Exception throwable) {
                        subscriber.onError(throwable);
                    }
                }).subscribeOn(Schedulers.computation()).subscribe(contacts -> {
                    AppHelper.LogCat("  size contact fragment " + contacts.size());
                    mUsersContacts.updateContacts(contacts).subscribe(contactsModelList -> {
                        // newConversationContactsActivity.ShowContacts(contactsModelList);

                        new Handler().postDelayed(() -> {
                            try {
                                mDisposable.addAll(mUsersContacts.getAllContacts().subscribe(usersModels -> {


                                    if (newConversationContactsActivity != null) {
                                        if (PreferenceManager.getInstance().getStoriesPrivacy(newConversationContactsActivity) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS) {

                                            realm.executeTransaction(realm1 -> {
                                                List<UsersPrivacyModel> usersPrivacyModels = realm1.where(UsersPrivacyModel.class).findAll();
                                                AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());
                                                RealmList<UsersPrivacyModel> usersPrivacyModelList = new RealmList<>();
                                                for (UsersModel usersModel : usersModels) {
                                                    if (!UsersController.getInstance().checkIfPrivacyUserExist(usersModel.get_id())) {
                                                        UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                                                        usersPrivacyModel.setId(usersModel.get_id());
                                                        usersPrivacyModel.setUsersModel(usersModel);
                                                        usersPrivacyModelList.add(usersPrivacyModel);
                                                    }
                                                }
                                                PreferenceManager.getInstance().setStoriesPrivacy(newConversationContactsActivity, AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS);
                                                realm1.copyToRealmOrUpdate(usersPrivacyModelList);
                                            });
                                            newConversationContactsActivity.ShowContacts(usersModels);

                                        }
                                    }
                                }, throwable -> {
                                    newConversationContactsActivity.onErrorLoading(throwable);
                                    newConversationContactsActivity.onHideLoading();

                                }, () -> {
                                    newConversationContactsActivity.onHideLoading();
                                }))
                                ;
                                try {
                                    PreferenceManager.getInstance().setContactSize(newConversationContactsActivity, mUsersContacts.getLinkedContactsSize());
                                } catch (Exception e) {
                                    AppHelper.LogCat(" Exception size contact fragment");
                                }
                            } catch (Exception e) {
                                AppHelper.LogCat("getAllContacts Exception ContactsPresenter ");
                            }
                            AppHelper.CustomToast(newConversationContactsActivity, newConversationContactsActivity.getString(R.string.success_response_contacts));
                            //  mDisposable.add(mUsersContacts.getUserInfo(PreferenceManager.getInstance().getID(newConversationContactsActivity)).subscribe(contactsModel -> AppHelper.LogCat("getContactInfo"), AppHelper::LogCat));

                        }, 2000);
                    }, throwable -> {
                        newConversationContactsActivity.onErrorLoading(throwable);

                        if (ForegroundRuning.get().isForeground()) {
                            try {
                                AlertDialog.Builder alert = new AlertDialog.Builder(newConversationContactsActivity.getApplicationContext());
                                alert.setMessage(throwable.getMessage());
                                alert.setPositiveButton(R.string.ok, (dialog, which) -> {
                                });
                                alert.setCancelable(false);
                                alert.show();
                            } catch (Exception e) {
                                AppHelper.LogCat("Exception " + e.getMessage());
                                AppHelper.CustomToast(newConversationContactsActivity, e.getMessage());
                            }
                        }
                    }, () -> {
                        newConversationContactsActivity.onHideLoading();
                    });
                }, throwable -> {
                    AppHelper.LogCat(" " + throwable.getMessage());
                }))
                ;

            } else {
                Permissions.with(newConversationContactsActivity)
                        .request(Manifest.permission.WRITE_CONTACTS, Manifest.permission.READ_CONTACTS)
                        .ifNecessary()
                        .withRationaleDialog(newConversationContactsActivity.getString(R.string.app__requires_contacts_permission_in_order_to_attach_contact_information),
                                R.drawable.ic_contacts_white_24dp)
                        .onAnyResult(() -> {

                        })
                        .execute();
            }
        } else if (contactsFragment != null) {

            mDisposable.addAll(mUsersContacts.getAllContacts().subscribe(contactsModels -> {
                contactsFragment.updateContacts(contactsModels);
            }, throwable -> {
                contactsFragment.onErrorLoading(throwable);
            }, () -> {
                contactsFragment.onHideLoading();
            }));
        }


    }

    @Override
    public void onStop() {

    }

    private void getContactInfo() {
        mDisposable.add(APIHelper.initialApiUsersContacts().getUserInfo(PreferenceManager.getInstance().getID(newConversationContactsActivity)).subscribe(contactsModel -> {
        }, throwable -> AppHelper.LogCat(throwable.getMessage())));
    }

    private void loadDataFromServer() {
        //   getContactInfo();
        if (newConversationContactsActivity != null)
            newConversationContactsActivity.onShowLoading();
        else if (privacyContactsActivity != null) {
            privacyContactsActivity.onShowLoading();
        }
        mDisposable.add(Observable.create((ObservableOnSubscribe<List<UsersModel>>) subscriber -> {
            try {
                List<UsersModel> contactsModels = UtilsPhone.getInstance().GetPhoneContacts();
                subscriber.onNext(contactsModels);
                subscriber.onComplete();
            } catch (Exception throwable) {
                subscriber.onError(throwable);
            }
        }).subscribeOn(Schedulers.computation()).subscribe(contacts -> {
            mUsersContacts.updateContacts(contacts).subscribe(contactsModelList -> {
                //newConversationContactsActivity.ShowContacts(contactsModelList);

                new Handler().postDelayed(() -> {
                    try {
                        if (privacyContactsActivity != null) {
                            mDisposable.addAll(mUsersContacts.getLinkedContacts().subscribe(usersModels -> {


                                if (newConversationContactsActivity != null) {
                                    if (PreferenceManager.getInstance().getStoriesPrivacy(newConversationContactsActivity) == AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS) {

                                        realm.executeTransaction(realm1 -> {
                                            List<UsersPrivacyModel> usersPrivacyModels = realm1.where(UsersPrivacyModel.class).findAll();
                                            AppHelper.LogCat("usersPrivacyModels " + usersPrivacyModels.size());

                                            RealmList<UsersPrivacyModel> usersPrivacyModelList = new RealmList<>();
                                            for (UsersModel usersModel : usersModels) {
                                                if (!UsersController.getInstance().checkIfPrivacyUserExist(usersModel.get_id()) && usersModel.isActivate() && usersModel.isLinked()) {
                                                    UsersPrivacyModel usersPrivacyModel = new UsersPrivacyModel();
                                                    usersPrivacyModel.setId(usersModel.get_id());
                                                    usersPrivacyModel.setUsersModel(usersModel);
                                                    usersPrivacyModelList.add(usersPrivacyModel);
                                                }
                                            }
                                            PreferenceManager.getInstance().setStoriesPrivacy(newConversationContactsActivity, AppConstants.StoriesConstants.STORIES_PRIVACY_ALL_CONTACTS);
                                            realm1.copyToRealmOrUpdate(usersPrivacyModelList);
                                        });
                                    }
                                    newConversationContactsActivity.ShowContacts(usersModels);

                                } else if (privacyContactsActivity != null) {
                                    privacyContactsActivity.ShowContacts(usersModels);
                                } else
                                    contactsFragment.updateContacts(usersModels);
                            }, throwable -> {
                                if (newConversationContactsActivity != null) {
                                    newConversationContactsActivity.onErrorLoading(throwable);
                                    newConversationContactsActivity.onHideLoading();
                                } else if (privacyContactsActivity != null) {

                                    privacyContactsActivity.onErrorLoading(throwable);
                                    privacyContactsActivity.onHideLoading();
                                } else
                                    contactsFragment.onErrorLoading(throwable);
                            }, () -> {
                                if (newConversationContactsActivity != null)
                                    newConversationContactsActivity.onHideLoading();
                                else if (privacyContactsActivity != null) {
                                    privacyContactsActivity.onHideLoading();
                                } else
                                    contactsFragment.onHideLoading();
                            }));
                        } else {
                            mDisposable.addAll(mUsersContacts.getAllContacts().subscribe(contactsModels -> {
                                if (newConversationContactsActivity != null)
                                    newConversationContactsActivity.ShowContacts(contactsModels);
                                else if (privacyContactsActivity != null) {
                                    privacyContactsActivity.ShowContacts(contactsModels);
                                } else
                                    contactsFragment.updateContacts(contactsModels);
                            }, throwable -> {
                                if (newConversationContactsActivity != null) {
                                    newConversationContactsActivity.onErrorLoading(throwable);
                                    newConversationContactsActivity.onHideLoading();
                                } else if (privacyContactsActivity != null) {

                                    privacyContactsActivity.onErrorLoading(throwable);
                                    privacyContactsActivity.onHideLoading();
                                } else
                                    contactsFragment.onErrorLoading(throwable);
                            }, () -> {
                                if (newConversationContactsActivity != null)
                                    newConversationContactsActivity.onHideLoading();
                                else if (privacyContactsActivity != null) {
                                    privacyContactsActivity.onHideLoading();
                                } else
                                    contactsFragment.onHideLoading();
                            }));
                        }
                        try {
                            if (newConversationContactsActivity != null)
                                PreferenceManager.getInstance().setContactSize(newConversationContactsActivity, mUsersContacts.getLinkedContactsSize());
                            else if (privacyContactsActivity != null) {
                                PreferenceManager.getInstance().setContactSize(privacyContactsActivity, mUsersContacts.getLinkedContactsSize());
                            } else
                                PreferenceManager.getInstance().setContactSize(contactsFragment.getActivity(), mUsersContacts.getLinkedContactsSize());
                        } catch (Exception e) {
                            AppHelper.LogCat(" Exception size contact fragment");
                        }
                    } catch (Exception e) {
                        AppHelper.LogCat("getAllContacts Exception ContactsPresenter ");
                    }
                    if (newConversationContactsActivity != null)
                        AppHelper.CustomToast(newConversationContactsActivity, newConversationContactsActivity.getString(R.string.success_response_contacts));

                }, 2000);

            }, throwable -> {
                if (newConversationContactsActivity != null)
                    newConversationContactsActivity.onErrorLoading(throwable);
                else if (privacyContactsActivity != null) {
                    privacyContactsActivity.onErrorLoading(throwable);
                } else
                    contactsFragment.onErrorLoading(throwable);
            }, () -> {

            });
        }, throwable -> {
            AppHelper.LogCat(" " + throwable.getMessage());
        }));

    }



}