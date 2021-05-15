package com.pefgloble.pefchate.activities.group;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.BaseActivity;
import com.pefgloble.pefchate.adapter.AddNewMembersToGroupAdapter;
import com.pefgloble.pefchate.api.APIService;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.presenter.AddNewMembersToGroupPresenter;

import java.util.List;

import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;

/**
 * Created by Abderrahim El imame on 20/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupActivity extends BaseActivity {
    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.ParentLayoutAddNewMembers)
    LinearLayout ParentLayoutAddContact;
    @BindView(R.id.app_bar)
    Toolbar toolbar;

    private RealmList<UsersModel> mContactsModelList;
    private AddNewMembersToGroupPresenter mAddMembersToGroupPresenter;
    private String groupID;
    private Realm realm;
    private APIService mApiService;
    private AddNewMembersToGroupAdapter mAddMembersToGroupListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_new_members_to_group);
        ButterKnife.bind(this);

        mApiService = new APIService(this);
        realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        if (getIntent().hasExtra("groupID")) {
            groupID = getIntent().getExtras().getString("groupID");
        }
        mAddMembersToGroupPresenter = new AddNewMembersToGroupPresenter(this);
        initializeView();
        setupToolbar();
    }

    /**
     * method to initialize the view
     */
    private void initializeView() {
        mAddMembersToGroupPresenter.onCreate();
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getApplicationContext());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        mAddMembersToGroupListAdapter = new AddNewMembersToGroupAdapter(this, mContactsModelList, groupID, mAddMembersToGroupPresenter.getRealm());
        ContactsList.setAdapter(mAddMembersToGroupListAdapter);
        // this is the default; this call is actually only necessary with custom ItemAnimators
        ContactsList.setItemAnimator(new DefaultItemAnimator());


    }

    /**
     * method to show contacts
     *
     * @param contactsModelList this  parameter of ShowContacts method
     */
    public void ShowContacts(List<UsersModel> contactsModelList) {
        RealmList<UsersModel> contactsModels1 = new RealmList<UsersModel>();
        contactsModels1.addAll(contactsModelList);
        mContactsModelList = contactsModels1;
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        mAddMembersToGroupPresenter.onDestroy();
        if (!realm.isClosed())
            realm.close();

    }


    /**
     * method to setup the toolbar
     */
    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(R.string.title_activity_add_new_members_to_group);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        // Set up SearchView
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_contacts).getActionView();
        searchView.setIconified(true);
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        searchView.setOnQueryTextListener(mQueryTextListener);
        searchView.setQueryHint("Search ...");
        return true;
    }

    private SearchView.OnQueryTextListener mQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        public boolean onQueryTextSubmit(String s) {
            return false;
        }

        @Override
        public boolean onQueryTextChange(String s) {
            mAddMembersToGroupListAdapter.setString(s);
            Search(s);

            return true;
        }
    };


    /**
     * method to start searching
     *
     * @param string this  is parameter for Search method
     */
    public void Search(String string) {

        List<UsersModel> filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            mAddMembersToGroupListAdapter.animateTo(filteredModelList);
            ContactsList.scrollToPosition(0);
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

        List<UsersModel> contactsModels = realm.where(UsersModel.class)
                .equalTo("exist", true)
                .equalTo("linked", true)
                .notEqualTo("_id", PreferenceManager.getInstance().getID(this))
                .beginGroup()
                .contains("phone", query, Case.INSENSITIVE)
                .or()
                .contains("username", query, Case.INSENSITIVE)
                .endGroup()
                .findAll();

        realm.close();
        return contactsModels;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();

    }


}
