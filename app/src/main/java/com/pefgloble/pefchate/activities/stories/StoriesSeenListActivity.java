package com.pefgloble.pefchate.activities.stories;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;


import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.adapter.SeenListAdapter;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.interfaces.LoadingData;
import com.pefgloble.pefchate.presenter.StoriesPresenter;
import com.pefgloble.pefchate.stories.StorySeenListModel;
import com.pefgloble.pefchate.ui.DragListener;
import com.pefgloble.pefchate.ui.DragToClose;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * Created by Abderrahim El imame on 7/27/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesSeenListActivity extends BaseActivity implements LoadingData {

    @BindView(R.id.drag_to_close)
    DragToClose dragToClose;

    @BindView(R.id.seenList)
    RecyclerView seenList;

    @BindView(R.id.card)
    View card;

    @BindView(R.id.empty_view)
    View empty_view;

    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private SeenListAdapter seenListAdapter;
    private StoriesPresenter storiesPresenter;

    @Override
    public void onCreate(Bundle bundle) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(bundle);
        setContentView(R.layout.activity_story_seen_list);
        ButterKnife.bind(this);
        EventBus.getDefault().register(this);
        initializeView();
        storiesPresenter = new StoriesPresenter(this, getIntent().getStringExtra("storyId"));
        storiesPresenter.onCreate();


    }

    private void initializeView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        seenList.setLayoutManager(mLinearLayoutManager);
        seenListAdapter = new SeenListAdapter(this);
        seenList.setAdapter(seenListAdapter);
        seenList.setItemAnimator(new DefaultItemAnimator());

        card.setOnClickListener(v -> {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("isReply", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

        });
        seenList.setOnClickListener(v -> {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("isReply", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

        });
        toolbar.setOnClickListener(v -> {

            Intent resultIntent = new Intent();
            resultIntent.putExtra("isReply", true);
            setResult(Activity.RESULT_OK, resultIntent);
            finish();

        });
        dragToClose.setCloseOnClick(true);
        dragToClose.setDragListener(new DragListener() {
            @Override
            public void onStartDraggingView() {
                AppHelper.LogCat("onStartDraggingView()");

            }

            @Override
            public void onDraggingView(float offset) {
                toolbar.setAlpha(offset);
                seenList.setAlpha(offset);
            }

            @Override
            public void onViewClosed() {
                AppHelper.LogCat("onViewClosed()");

                Intent resultIntent = new Intent();
                resultIntent.putExtra("isReply", true);
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    /**
     * method to show seen list
     *
     * @param usersModels this  parameter of ShowSeenList method
     */
    public void showSeenList(RealmList<UsersModel> usersModels) {
        if (usersModels.size() != 0) {
            empty_view.setVisibility(View.GONE);
            seenList.setVisibility(View.VISIBLE);

        } else {
            empty_view.setVisibility(View.VISIBLE);
            seenList.setVisibility(View.GONE);


        }
        seenListAdapter.setistUsersList(usersModels);
        setupToolbar();
    }


    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        getSupportActionBar().setHomeButtonEnabled(false);
        String title = String.format(getResources().getString(R.string.viewed_by) + " %s ", seenListAdapter.getItemCount());
        getSupportActionBar().setTitle(title);
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
            case AppConstants.EVENT_BUS_NEW_STORY_OWNER_OLD_ROW:
                seenListAdapter.notifyDataSetChanged();
                break;

        }
    }


    @Override
    public void onShowLoading() {

    }

    @Override
    public void onHideLoading() {

    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (storiesPresenter != null)
            storiesPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onBackPressed() {
        Intent resultIntent = new Intent();
        resultIntent.putExtra("isReply", true);
        setResult(Activity.RESULT_OK, resultIntent);
        super.onBackPressed();
    }
}
