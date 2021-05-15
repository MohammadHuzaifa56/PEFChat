package com.pefgloble.pefchate.activities.group;

import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.RelativeLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModelJson;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.adapter.AddMembersToGroupAdapter;
import com.pefgloble.pefchate.adapter.AddMembersToGroupSelectorAdapter;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.presenter.AddMembersToGroupPresenter;
import com.pefgloble.pefchate.ui.RecyclerViewFastScroller;


import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GestureDetectorCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupActivity extends BaseActivity implements RecyclerView.OnItemTouchListener, View.OnClickListener {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.ParentLayoutAddContact)
    RelativeLayout ParentLayoutAddContact;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.fab)
    FloatingActionButton floatingActionButton;
    @BindView(R.id.ContactsListHeader)
    RecyclerView ContactsListHeader;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;

    private List<UsersModel> mContactsModelList;
    private AddMembersToGroupAdapter mAddMembersToGroupListAdapter;
    private AddMembersToGroupSelectorAdapter mAddMembersToGroupSelectorAdapter;
    private GestureDetectorCompat gestureDetector;
    private AddMembersToGroupPresenter mAddMembersToGroupPresenter;
    private Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_members_to_group);
        ButterKnife.bind(this);
        realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        mAddMembersToGroupPresenter = new AddMembersToGroupPresenter(this);
        initializeView();
        setupToolbar();
        EventBus.getDefault().register(this);
        PreferenceManager.getInstance().clearMembers(this);
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        mAddMembersToGroupPresenter.onCreate();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new AddMembersToGroupAdapter(this, mContactsModelList);
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(this, new RecyclerViewBenOnGestureListener());
        floatingActionButton.setOnClickListener(this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        ContactsListHeader.setLayoutManager(linearLayoutManager);
        mAddMembersToGroupSelectorAdapter = new AddMembersToGroupSelectorAdapter(this);
        ContactsListHeader.setAdapter(mAddMembersToGroupSelectorAdapter);
    }

    /**
     * method to show contacts
     *
     * @param contactsModels this  parameter of ShowContacts method
     */
    public void ShowContacts(List<UsersModel> contactsModels) {
        mContactsModelList = contactsModels;
    }


    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_members_to_group);
        String title = String.format(" %s " + getResources().getString(R.string.of) + " %s " + getResources().getString(R.string.selected), mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);
    }

    @Override
    public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
        gestureDetector.onTouchEvent(e);
        return false;
    }

    @Override
    public void onTouchEvent(RecyclerView rv, MotionEvent e) {

    }

    @Override
    public void onRequestDisallowInterceptTouchEvent(boolean disallowIntercept) {

    }

    /**
     * method to toggle the selection
     *
     * @param position this is parameter of ToggleSelection method
     */
    private void ToggleSelection(int position) {
        mAddMembersToGroupListAdapter.toggleSelection(position);
        String title = String.format(" %s " + getResources().getString(R.string.of) + " %s " + getResources().getString(R.string.selected), mAddMembersToGroupListAdapter.getSelectedItemCount(), mAddMembersToGroupListAdapter.getContacts().size());
        toolbar.setSubtitle(title);

    }


    @Override
    public void onClick(View v) {
        try {
            if (v.getId() == R.id.container_list_item) {

                int position = ContactsList.getChildAdapterPosition(v);
                ToggleSelection(position);


            } else if (v.getId() == R.id.fab) {
                if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                    int arraySize = mAddMembersToGroupListAdapter.getSelectedItems().size();
                    for (int x = 0; x < arraySize; x++) {
                        int position = mAddMembersToGroupListAdapter.getSelectedItems().get(x);
                        MembersModelJson membersGroupModel = new MembersModelJson();
                        membersGroupModel.setUserId(mAddMembersToGroupListAdapter.getContacts().get(position).get_id());
                        membersGroupModel.setAdmin(false);
                        membersGroupModel.setLeft(false);
                        membersGroupModel.setDeleted(false);
                        PreferenceManager.getInstance().addMember(this, membersGroupModel);
                    }
                    AppHelper.LaunchActivity(this, CreateGroupActivity.class);
                    finish();
                } else {
                    AppHelper.Snackbar(this, ParentLayoutAddContact, getString(R.string.select_one_at_least), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

                }


            }
        } catch (Exception e) {
            AppHelper.LogCat(" Touch Exception AddMembersToGroupActivity " + e.getMessage());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = ContactsList.findChildViewUnder(e.getX(), e.getY());
            onClick(view);
            return super.onSingleTapConfirmed(e);
        }

    }

    /**
     * method to scroll to the bottom of recyclerView
     */
    private void scrollToBottom() {
        ContactsListHeader.scrollToPosition(mAddMembersToGroupSelectorAdapter.getItemCount() - 1);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
                mAddMembersToGroupListAdapter.clearSelections();
            }
            PreferenceManager.getInstance().clearMembers(this);
            finish();


        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();


        AnimationsUtil.setTransitionAnimation(this);
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
            case AppConstants.EVENT_BUS_REMOVE_CREATE_MEMBER:
                mAddMembersToGroupSelectorAdapter.remove(pusher.getContactsModel());
                if (mAddMembersToGroupSelectorAdapter.getContacts().size() == 0) {
                    ContactsListHeader.setVisibility(View.GONE);
                }
                break;
            case AppConstants.EVENT_BUS_ADD_CREATE_MEMBER:
                ContactsListHeader.setVisibility(View.VISIBLE);
                mAddMembersToGroupSelectorAdapter.add(pusher.getContactsModel());
                scrollToBottom();
                break;
            case AppConstants.EVENT_BUS_DELETE_CREATE_MEMBER:
                int position = mAddMembersToGroupListAdapter.getItemPosition(pusher.getContactsModel());
                ToggleSelection(position);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        if (mAddMembersToGroupListAdapter.getSelectedItemCount() != 0) {
            mAddMembersToGroupListAdapter.clearSelections();
        }
        PreferenceManager.getInstance().clearMembers(this);

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddMembersToGroupPresenter.onDestroy();
        EventBus.getDefault().unregister(this);
        if (!realm.isClosed())
            realm.close();
    }


}
