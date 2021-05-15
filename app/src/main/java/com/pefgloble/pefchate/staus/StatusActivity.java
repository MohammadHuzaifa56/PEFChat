package com.pefgloble.pefchate.staus;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;


import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.status.StatusModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.adapter.StatusAdapter;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.presenter.StatusPresenter;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


/**
 * Created by Abderrahim El imame on 28/04/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class StatusActivity extends BaseActivity  {


    @BindView(R.id.currentStatus)
    TextView currentStatus;
    @BindView(R.id.editCurrentStatusBtn)
    AppCompatImageView editCurrentStatusBtn;
    @BindView(R.id.StatusList)
    RecyclerView StatusList;
    @BindView(R.id.ParentLayoutStatus)
    LinearLayout ParentLayoutStatus;


    private StatusAdapter mStatusAdapter;
    private StatusPresenter mStatusPresenter;
    private String statusID;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);
        ButterKnife.bind(this);


        initializerView();
        mStatusPresenter = new StatusPresenter(this);
        mStatusPresenter.onCreate();
        setupToolbar();


    }



    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.app_bar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    /**
     * method to initialize the view
     */
    public void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mStatusAdapter = new StatusAdapter(this);
        StatusList.setLayoutManager(mLinearLayoutManager);
        StatusList.setAdapter(mStatusAdapter);
    }

    @SuppressWarnings("unused")
    @OnClick(R.id.editCurrentStatusBtn)
    void launchEditStatus(View v) {
        Intent mIntent = new Intent(this, EditStatusActivity.class);
        mIntent.putExtra("statusID", statusID);
        mIntent.putExtra("currentStatus", currentStatus.getText().toString().trim());
        startActivity(mIntent);
    }

    /**
     * method to show status list
     *
     * @param statusModels this is parameter for  ShowStatus   method
     */
    public void ShowStatus(List<StatusModel> statusModels) {
        mStatusAdapter.setStatus(statusModels);
        for (StatusModel statusModel : statusModels) {
            if (statusModel.isCurrent()) {
                String status = UtilsString.unescapeJava(statusModel.getBody());
                currentStatus.setText(status);
                break;
            }
        }
        mStatusPresenter.getCurrentStatus();
    }


    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        mStatusPresenter.onEventPush(pusher);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.status_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();

                break;
            case R.id.deleteStatus:
                mStatusPresenter.DeleteAllStatus();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mStatusPresenter.onDestroy();

    }

    /**
     * method to show the current status
     *
     * @param statusModel this is parameter for  ShowCurrentStatus   method
     */
    public void ShowCurrentStatus(String statusModel) {
        String status = UtilsString.unescapeJava(statusModel);
        currentStatus.setText(status);
    }

    /**
     * method to show the current status
     *
     * @param statusModel this is parameter for  ShowCurrentStatus   method
     */
    public void ShowCurrentStatus(StatusModel statusModel) {
        statusID = statusModel.get_id();
        String status = UtilsString.unescapeJava(statusModel.getBody());
        currentStatus.setText(status);

    }

    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("status error" + throwable.getMessage());
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mStatusPresenter.onResume();

    }


    public void deleteStatus(String statusID) {
        mStatusAdapter.DeleteStatusItem(statusID);
    }


}
