package com.pefgloble.pefchate.presenter;

import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.stories.StoriesListActivity;
import com.pefgloble.pefchate.activities.stories.StoriesSeenListActivity;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.api.UsersService;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.fragments.StoriesFragment;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.stories.StoryModel;

import org.greenrobot.eventbus.EventBus;

import io.reactivex.disposables.CompositeDisposable;
import io.realm.Realm;
import io.realm.RealmResults;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_DELETE_STORIES_ITEM;


/**
 * Created by Abderrahim El imame on 12/20/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesPresenter implements Presenter {


    private StoriesListActivity storiesListActivity;
    private StoriesSeenListActivity storiesSeenListActivity;
    private StoriesFragment storiesFragment;
    private Realm realm;
    private UsersService mUsersContacts;
    private CompositeDisposable mDisposable;
    private String storyId;

    public StoriesPresenter(StoriesSeenListActivity storiesSeenListActivity, String storyId) {
        this.storiesSeenListActivity = storiesSeenListActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
        this.storyId = storyId;
    }

    public StoriesPresenter(StoriesListActivity storiesListActivity) {
        this.storiesListActivity = storiesListActivity;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
    }

    public StoriesPresenter(StoriesFragment storiesFragment) {
        this.storiesFragment = storiesFragment;
        this.realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mDisposable = new CompositeDisposable();
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onCreate() {
        if (storiesListActivity != null) {

            APIService mApiService = APIService.with(storiesListActivity);
            mUsersContacts = new UsersService(realm, storiesListActivity, mApiService);
            getStories();
        } else if (storiesFragment != null) {

            APIService mApiService = APIService.with(storiesFragment.getContext());
            mUsersContacts = new UsersService(realm, storiesFragment.getContext(), mApiService);
            getAllStories();
        } else {

            APIService mApiService = APIService.with(storiesSeenListActivity);
            mUsersContacts = new UsersService(realm, storiesSeenListActivity, mApiService);
            getSeenList(storyId);

        }
    }

    private void getAllStories() {
        mDisposable.add(mUsersContacts.getAllStories().subscribe(storiesModels -> {
            storiesFragment.UpdateStories(storiesModels, mUsersContacts.getMineStories());
        }, throwable -> {
            storiesFragment.onErrorLoading(throwable);
        }, () -> {
            storiesFragment.onHideLoading();
        }))
        ;
    }

    private void getSeenList(String storyId) {

        mDisposable.add(mUsersContacts.getSeenList(storyId).subscribe(usersModels -> {
            storiesSeenListActivity.showSeenList(usersModels);
        }, throwable -> {
            storiesSeenListActivity.onErrorLoading(throwable);
        }, () -> {
            storiesSeenListActivity.onHideLoading();
        }))
        ;
    }

    private void getStories() {

        mDisposable.add(mUsersContacts.getStories().subscribe(storyModels -> {
            storiesListActivity.showStories(storyModels);
        }, throwable -> {
            storiesListActivity.onErrorLoading(throwable);
        }, () -> {
            storiesListActivity.onHideLoading();
        }))
        ;
    }

    public void deleteStory(StoryModel storyModel, int currentPosition1) {
        String storyId = storyModel.get_id();
        storiesListActivity.storiesListAdapter.removeStoryItem(currentPosition1);
        if (storyModel.getStatus() == AppConstants.IS_WAITING) {
            realm.executeTransactionAsync(realm1 -> {


                StoryModel storyModel1 = realm1.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                storyModel1.setDeleted(true);
                realm1.copyToRealmOrUpdate(storyModel1);
               /* StoryModel storyModel1 = realm1.where(StoryModel.class).equalTo("_id", storyId).findFirst();
                storyModel1.deleteFromRealm();*/
            }, () -> {
                AppHelper.LogCat("story deleted successfully  ");

                RealmResults<StoryModel> storyModels = realm.where(StoryModel.class)
                        .equalTo("userId", PreferenceManager.getInstance().getID(storiesListActivity))
                        .equalTo("deleted",false)
                        .findAll();
                if (storyModels.size() == 0) {

                    storiesListActivity.finish();
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(storiesListActivity)));
                   /* realm.executeTransactionAsync(realm1 -> {
                        StoriesHeaderModel storiesHeaderModel = realm1.where(StoriesHeaderModel.class).equalTo("_id", PreferenceManager.getInstance().getID(storiesListActivity)).findFirst();
                        storiesHeaderModel.deleteFromRealm();
                    }, () -> {
                        AppHelper.LogCat("stories deleted successfully  ");
                        storiesListActivity.finish();
                        EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(storiesListActivity)));

                    }, error -> {
                        AppHelper.LogCat("delete stories failed  " + error.getMessage());

                    });*/
                } else {
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(storiesListActivity)));
                }
            }, error -> {
                AppHelper.LogCat("delete story failed  " + error.getMessage());

            });
        } else {
            mDisposable.add(APIHelper.initialApiUsersContacts().deleteStory(storyModel.get_id()).subscribe(statusResponse -> {
                if (statusResponse.isSuccess()) {
                    realm.executeTransactionAsync(realm1 -> {

                        StoryModel storyModel1 = realm1.where(StoryModel.class).equalTo("_id", storyId).equalTo("userId", PreferenceManager.getInstance().getID(storiesListActivity)).findFirst();
                        storyModel1.setDeleted(true);
                        realm1.copyToRealmOrUpdate(storyModel1);
                       /* StoryModel storyModel1 = realm1.where(StoryModel.class).equalTo("_id", storyId).equalTo("userId", PreferenceManager.getInstance().getID(storiesListActivity)).findFirst();
                        storyModel1.deleteFromRealm();*/
                    }, () -> {
                        AppHelper.LogCat("story deleted successfully  ");

                        RealmResults<StoryModel> storyModels = realm.where(StoryModel.class)
                                .equalTo("userId", PreferenceManager.getInstance().getID(storiesListActivity))
                                .equalTo("deleted",false)
                                .findAll();
                        if (storyModels.size() == 0) {

                            storiesListActivity.finish();
                            EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(storiesListActivity)));
                   /*         realm.executeTransactionAsync(realm1 -> {
                                StoriesHeaderModel storiesHeaderModel = realm1.where(StoriesHeaderModel.class).equalTo("_id", PreferenceManager.getInstance().getID(storiesListActivity)).findFirst();
                                storiesHeaderModel.deleteFromRealm();
                            }, () -> {
                                AppHelper.LogCat("stories deleted successfully  ");
                                storiesListActivity.finish();
                                EventBus.getDefault().post(new Pusher(EVENT_BUS_DELETE_STORIES_ITEM, PreferenceManager.getInstance().getID(storiesListActivity)));

                            }, error -> {
                                AppHelper.LogCat("delete stories failed  " + error.getMessage());

                            });*/
                        } else {
                            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW, PreferenceManager.getInstance().getID(storiesListActivity)));
                        }
                    }, error -> {
                        AppHelper.LogCat("delete story failed  " + error.getMessage());

                    });
                } else {
                    AppHelper.CustomToast(storiesListActivity, storiesListActivity.getString(R.string.oops_something));
                }


            }, throwable -> {
                AppHelper.LogCat("delete story failed  " + throwable.getMessage());
                AppHelper.CustomToast(storiesListActivity, storiesListActivity.getString(R.string.oops_something));
            }));
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
        if (storiesFragment != null) {

            APIService mApiService = APIService.with(storiesFragment.getContext());
            mUsersContacts = new UsersService(realm, storiesFragment.getContext(), mApiService);
            getAllStories();
        }
    }

    @Override
    public void onStop() {

    }


}
