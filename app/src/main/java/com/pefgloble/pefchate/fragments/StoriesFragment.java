package com.pefgloble.pefchate.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.stories.CreateStoriesText;
import com.pefgloble.pefchate.adapter.StoriesAdapter;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.interfaces.LoadingData;
import com.pefgloble.pefchate.presenter.StoriesPresenter;
import com.pefgloble.pefchate.stories.StoriesHeaderModel;
import com.pefgloble.pefchate.stories.StoriesModel;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 12/3/16.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class StoriesFragment extends Fragment implements LoadingData {

    @BindView(R.id.storiesList)
    RecyclerView storiesList;

    @BindView(R.id.empty)
    LinearLayout emptyLayout;

    private StoriesAdapter storiesAdapter;

    @BindView(R.id.swipeStories)
    SwipeRefreshLayout mSwipeRefreshLayout;


    private StoriesPresenter storiesPresenter;
    FloatingActionButton fabCamera;
    @BindView(R.id.fabStory)
    FloatingActionButton fabStory;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_stories, container, false);
        fabCamera=mView.findViewById(R.id.fabCamera);
        ButterKnife.bind(this, mView);
        EventBus.getDefault().register(this);
        storiesPresenter = new StoriesPresenter(this);



        fabCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                  FilesManager.capturePhoto((AppCompatActivity) getActivity(), AppConstants.PICK_CAMERA_GALLERY_STORY, true);

            }
        });


        fabStory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(),CreateStoriesText.class));
            }
        });

        return mView;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializerView();

        storiesPresenter.onCreate();
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setHasOptionsMenu(true);
        storiesAdapter = new StoriesAdapter((AppCompatActivity) getActivity(), storiesList);
        storiesList.setLayoutManager(new LinearLayoutManager(getActivity()));
        storiesList.setAdapter(storiesAdapter);
        storiesList.setItemAnimator(new DefaultItemAnimator());
        storiesList.getItemAnimator().setChangeDuration(0);

        //fix slow recyclerview start
        storiesList.setHasFixedSize(true);
        storiesList.setItemViewCacheSize(10);
        storiesList.setDrawingCacheEnabled(true);
        storiesList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorGreenLight);
        mSwipeRefreshLayout.setOnRefreshListener(() -> storiesPresenter.onRefresh());


    }


    /**
     * method to show stories list
     *
     * @param storiesModels this is parameter for  UpdateStoies  method
     */
    public void UpdateStories(List<StoriesModel> storiesModels, StoriesHeaderModel storiesHeaderModel) {


        if (storiesModels.size() == 0 && storiesHeaderModel == null) {
            storiesList.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {
            storiesList.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);

            RealmList<StoriesModel> modelRealmList = new RealmList<>();
            modelRealmList.addAll(storiesModels);
            storiesAdapter.setStoriesModelList(modelRealmList);
            storiesAdapter.setStoriesHeaderModel(storiesHeaderModel);

        }

    }


    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        switch (pusher.getAction()) {
            case AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_NEW_ROW:
                // new Handler().postDelayed(() -> addStoryEventMainThread(pusher.getStoryId()), 500);
                //    break;
            case AppConstants.EVENT_BUS_NEW_MESSAGE_STORY_OLD_ROW:
                //   new Handler().postDelayed(() -> storiesAdapter.updateStoryItem(pusher.getStoryId()), 500);
                //  break;


            case AppConstants.EVENT_BUS_NEW_STORY_OWNER_NEW_ROW:
            case AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW:
/*
                new Handler().postDelayed(() -> {
                    storiesAdapter.updateStoryMineItem(pusher.getStoryId());
                    showEmptyView();

                }, 500);
                break;*/
            case AppConstants.EVENT_BUS_DELETE_STORIES_ITEM:
               /* new Handler().postDelayed(() -> {
                    storiesAdapter.updateStoryMineItem(pusher.getStoryId());
                    showEmptyView();
                }, 500);*/
                storiesPresenter.onRefresh();

                break;
        }
    }

    private void showEmptyView() {
        if (storiesAdapter.getItemCount() == 0) {
            storiesList.setVisibility(View.GONE);
            emptyLayout.setVisibility(View.VISIBLE);
        } else {

            storiesList.setVisibility(View.VISIBLE);
            emptyLayout.setVisibility(View.GONE);
        }
    }


    /**
     * method to add a new story to list stories
     *
     * @param storyId this is the parameter for addStoryEventMainThread
     */

    private void addStoryEventMainThread(String storyId) {
        storiesAdapter.addStoryItem(storyId);
        storiesList.setVisibility(View.VISIBLE);
        emptyLayout.setVisibility(View.GONE);
        storiesList.scrollToPosition(0);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        /*if (mCallsPresenter != null)
            mCallsPresenter.onDestroy();*/
    }

    @Override
    public void onShowLoading() {
        if (!mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(true);

    }

    @Override
    public void onHideLoading() {
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat(throwable);
        if (mSwipeRefreshLayout.isRefreshing())
            mSwipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
