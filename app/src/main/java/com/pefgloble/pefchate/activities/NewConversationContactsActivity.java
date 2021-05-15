package com.pefgloble.pefchate.activities;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.group.AddMembersToGroupActivity;
import com.pefgloble.pefchate.adapter.ContactsAdapter;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.interfaces.LoadingData;
import com.pefgloble.pefchate.presenter.ContactsPresenter;
import com.pefgloble.pefchate.ui.RecyclerViewFastScroller;
import com.pefgloble.pefchate.util.PreCachingLayoutManager;


import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by abderrahimelimame on 6/9/16.
 * Email : abderrahim.elimame@gmail.com
 */

public class NewConversationContactsActivity extends BaseActivity implements LoadingData {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.app_bar)
    Toolbar toolbar;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;
    private ContactsAdapter mSelectContactsAdapter;
    private ContactsPresenter mContactsPresenter;

    @BindView(R.id.toolbar_progress_bar)
    ProgressBar toolbarProgressBar;

    @BindView(R.id.close_btn_search_view)
    ImageView closeBtn;
    @BindView(R.id.search_input)
    TextInputEditText searchInput;
    @BindView(R.id.clear_btn_search_view)
    ImageView clearBtn;
    @BindView(R.id.app_bar_search_view)
    View searchView;

    @BindView(R.id.new_group)
    View new_group;

    @BindView(R.id.new_contact)
    View new_contact;

    @BindView(R.id.main_view)
    LinearLayout MainView;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_conversation);
        ButterKnife.bind(this);
        new_group.setEnabled(false);
        searchInput.setFocusable(true);
        initializerSearchView(searchInput, clearBtn);
        initializerView();
        mContactsPresenter = new ContactsPresenter(this);
        mContactsPresenter.onCreate();
    }


    /**
     * method to initialize the view
     */
    private void initializerView() {

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.title_select_contacts));

        }
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getApplicationContext());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(this));//fix preload image before appears
        mSelectContactsAdapter = new ContactsAdapter(false);
        ContactsList.setLayoutManager(layoutManager);
        ContactsList.setAdapter(mSelectContactsAdapter);

        //fix slow recyclerview start
        ContactsList.setHasFixedSize(true);
        ContactsList.setItemViewCacheSize(30);
        ContactsList.setDrawingCacheEnabled(true);
        ContactsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        // set recyclerView to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

        closeBtn.setOnClickListener(v -> closeSearchView());
        clearBtn.setOnClickListener(v -> clearSearchView());
    }

    /**
     * method to initialize the search view
     */
    public void initializerSearchView(TextInputEditText searchInput, ImageView clearSearchBtn) {

        final Context context = this;
        searchInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                InputMethodManager inputManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            }

        });
        searchInput.addTextChangedListener(new TextWatcherAdapter() {
            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                clearSearchBtn.setVisibility(View.GONE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mSelectContactsAdapter.setString(s.toString());
                Search(s.toString().trim());
                clearSearchBtn.setVisibility(View.VISIBLE);
            }

            @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void afterTextChanged(Editable s) {

                if (s.length() == 0) {
                    clearSearchBtn.setVisibility(View.GONE);
                    mContactsPresenter.getContacts();
                }
            }
        });

    }


    @SuppressWarnings("unused")
    @OnClick(R.id.new_group)
    public void newGroup() {
        startActivity(new Intent(this, AddMembersToGroupActivity.class));
        finish();
        AnimationsUtil.setTransitionAnimation(this);
    }

    @OnClick(R.id.new_contact)
    public void newContact(){
        try {
            Intent mIntent = new Intent(Intent.ACTION_INSERT);
            mIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
            startActivityForResult(mIntent, AppConstants.SELECT_ADD_NEW_CONTACT);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    /**
     * method to close the searchview with animation
     */
    @SuppressWarnings("unused")
    @OnClick(R.id.close_btn_search_view)
    public void closeSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_for_button_animtion_exit);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    private void launcherSearchView() {
        final Animation animation = AnimationUtils.loadAnimation(this, R.anim.scale_for_button_animtion_enter);
        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                searchView.setVisibility(View.VISIBLE);
                toolbar.setVisibility(View.GONE);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        searchView.startAnimation(animation);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search_contacts:
                launcherSearchView();
                break;
            case R.id.refresh_contacts:
                mContactsPresenter.onRefresh();
                break;
            case android.R.id.home:
                finish();

                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
        AnimationsUtil.setTransitionAnimation(this);
    }

    /**
     * method to show contacts list
     *
     * @param contactsModels this is parameter for ShowContacts  method
     */
    public void ShowContacts(List<UsersModel> contactsModels) {
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_REFRESH_CONTACTS));
        new_group.setEnabled(true);
        if (getSupportActionBar() != null)
            getSupportActionBar().setSubtitle(String.format(Locale.getDefault(), "%s " + getResources().getString(R.string.of) + "%s", PreferenceManager.getInstance().getContactSize(this), contactsModels.size()));
        if (contactsModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
            RealmList<UsersModel> usersModelRealmList = new RealmList<>();
            usersModelRealmList.addAll(contactsModels);

            mSelectContactsAdapter.setContacts(usersModelRealmList);
            /*
            mContactsModelList = usersModelRealmList;
            mSelectContactsAdapter.setMembers(usersModelRealmList);*/
        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        MainView.setVisibility(View.GONE);

    }

    @Override
    protected void onPause() {
        super.onPause();
        MainView.setVisibility(View.VISIBLE);
    }


    @Override
    public void onShowLoading() {
        toolbarProgressBar.setVisibility(View.VISIBLE);
        toolbarProgressBar.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(this, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void onHideLoading() {
        toolbarProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onErrorLoading(Throwable throwable) {
        AppHelper.LogCat("Contacts Fragment " + throwable.getMessage());

        toolbarProgressBar.setVisibility(View.GONE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.contacts_menu, menu);
        return true;
    }

    /**
     * method to clear/reset the search view
     */
    public void clearSearchView() {
        if (searchInput.getText() != null) {
            searchInput.setText("");
            mContactsPresenter.getContacts();
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
        }
    }

    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        List<UsersModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);
            mSelectContactsAdapter.animateTo(filteredModelList);
            ContactsList.scrollToPosition(0);
        } else {
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
        }
    }

    /**
     * method to filter the list of contacts
     *
     * @param query this parameter for FilterList  method
     * @return this for what method will return
     */
    private List<UsersModel> FilterList(String query) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        List<UsersModel> usersModels = realm.where(UsersModel.class)
                .equalTo("exist", true)
                .notEqualTo("_id", PreferenceManager.getInstance().getID(this))
                .beginGroup()
                .contains("phone", query, Case.INSENSITIVE)
                .or()
                .contains("username", query, Case.INSENSITIVE)
                .endGroup()
                .findAll();
        List<UsersModel> contactsModelList = new ArrayList<>(usersModels);

        if (!realm.isClosed())
            realm.close();
        return contactsModelList;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mContactsPresenter != null)
            mContactsPresenter.onDestroy();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

    }


}
