package com.pefgloble.pefchate.activities.stories;

import android.os.Build;
import android.os.Bundle;
import android.view.View;



import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.adapter.StoriesPagerAdapter;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.stories.StoriesModel;
import com.pefgloble.pefchate.ui.CustomViewPager;

import androidx.annotation.Nullable;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION;
import static android.view.WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;

/**
 * Created by Abderrahim El imame on 7/10/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesDetailsActivity extends BaseActivity {

    @BindView(R.id.viewpager)
    CustomViewPager pager;

    private final String KEY_SELECTED_PAGE = "KEY_SELECTED_PAGE";
    int selectedPage = 0;
    int currentStoryPosition = 0;
    private Realm realm;
    public RealmResults<StoriesModel> storyModels;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedPage = savedInstanceState.getInt(KEY_SELECTED_PAGE);
        }

        setContentView(R.layout.activity_story_details);
        ButterKnife.bind(this);
        pager = findViewById(R.id.viewpager);
        selectedPage = getIntent().getIntExtra("position", 0);
        if (getIntent().hasExtra("currentStoryPosition"))
            currentStoryPosition = getIntent().getIntExtra("currentStoryPosition", 0);
        String storyId = getIntent().getStringExtra("storyId");
        realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        StoriesPagerAdapter mAdapter;
        if (storyId.equals(PreferenceManager.getInstance().getID(this))) {
            mAdapter = new StoriesPagerAdapter(getSupportFragmentManager(), 1, storyId, currentStoryPosition);
        } else {
            storyModels = realm.where(StoriesModel.class).findAll().sort("downloaded", Sort.ASCENDING);
            mAdapter = new StoriesPagerAdapter(getSupportFragmentManager(), storyModels.size(), storyId);

        }


        pager.setAdapter(mAdapter);
        /// pager.setPageTransformer(false, new CubeOutTransformer());

        pager.setCurrentItem(selectedPage);
        pager.setOffscreenPageLimit(0);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_PAGE, pager.getCurrentItem());
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (!realm.isClosed()) realm.close();
    }


/*
    @BindView(R.id.viewpager)
    CustomViewPager pager;
    StoriesPagerAdapter mAdapter;
    private final String KEY_SELECTED_PAGE = "KEY_SELECTED_PAGE";
    int selectedPage = 0;
    int currentStoryPosition = 0;
    private Realm realm;
    public RealmResults<StoriesModel> storyModels;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            selectedPage = savedInstanceState.getInt(KEY_SELECTED_PAGE);
        }
        setContentView(R.layout.activity_story_details);
        ButterKnife.bind(this);
        pager = findViewById(R.id.viewpager);
        selectedPage = getIntent().getIntExtra("position", 0);
        if (getIntent().hasExtra("currentStoryPosition"))
            currentStoryPosition = getIntent().getIntExtra("currentStoryPosition", 0);
        String storyId = getIntent().getStringExtra("userId");
        if (storyId.equals(PreferenceManager.getInstance().getID(this))) {
            mAdapter = new StoriesPagerAdapter(getSupportFragmentManager(), 1, storyId, currentStoryPosition);
        } else {
            FirebaseFirestore fbStore= FirebaseFirestore.getInstance();
            List<CreateStoryModel> createStoryModelList=new ArrayList<>();
            String id=storyId;
            fbStore.collection("Stories").document(storyId).collection("AllStories").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        for (DocumentSnapshot documentSnapshot : task.getResult()) {
                            CreateStoryModel createStoryModel=documentSnapshot.toObject(CreateStoryModel.class);
                            createStoryModelList.add(createStoryModel);
                        }
                        mAdapter = new StoriesPagerAdapter(getSupportFragmentManager(), createStoryModelList.size(), storyId);
                        //   mStoryShowAdapter = new StoryPageAdapter(createStoryModelList, storyProgressView, StoriesViewer.this);
                    }

                }
            });

        }


        pager.setAdapter(mAdapter);
        /// pager.setPageTransformer(false, new CubeOutTransformer());

        pager.setCurrentItem(selectedPage);
        pager.setOffscreenPageLimit(0);

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(KEY_SELECTED_PAGE, pager.getCurrentItem());
    }

    @Override
    protected void onResume() {
        super.onResume();
        View decorView = getWindow().getDecorView();
        // Hide the status bar.
        int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().setFlags(FLAG_TRANSLUCENT_NAVIGATION, FLAG_TRANSLUCENT_NAVIGATION);
            getWindow().setFlags(FLAG_TRANSLUCENT_STATUS, FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
*/


}
