package com.pefgloble.pefchate.fragments;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.material.internal.TextWatcherAdapter;
import com.google.android.material.textfield.TextInputEditText;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.adapter.ContactsAdapter;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.interfaces.LoadingData;
import com.pefgloble.pefchate.presenter.ContactsPresenter;
import com.pefgloble.pefchate.ui.CustomProgressView;
import com.pefgloble.pefchate.ui.RecyclerViewFastScroller;
import com.pefgloble.pefchate.util.PreCachingLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_REFRESH_CONTACTS;
/**
 * Created by Abderrahim El imame on 02/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ContactsFragment extends Fragment implements LoadingData {

    @BindView(R.id.ContactsList)
    RecyclerView ContactsList;
    @BindView(R.id.fastscroller)
    RecyclerViewFastScroller fastScroller;
    @BindView(R.id.empty)
    LinearLayout emptyContacts;


    @BindView(R.id.progress_bar_load)
    CustomProgressView progressBarLoad;


    private ContactsAdapter mContactsAdapter;
    private ContactsPresenter mContactsPresenter;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View mView = inflater.inflate(R.layout.fragment_contacts, container, false);
        ButterKnife.bind(this, mView);
        EventBus.getDefault().register(this);
        initializerView();

        askForPermissions();
        return mView;
    }

    private void askForPermissions() {
        if( ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.READ_CONTACTS ) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.READ_CONTACTS}, 123);
        }
        else {
            mContactsPresenter = new ContactsPresenter(this);
            mContactsPresenter.onCreate();
        }
    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        LinearLayoutManager mLinearLayoutManager = new LinearLayoutManager(getActivity());
        mLinearLayoutManager.setOrientation(RecyclerView.VERTICAL);
        mContactsAdapter = new ContactsAdapter(true);
        setHasOptionsMenu(true);
        ContactsList.setLayoutManager(mLinearLayoutManager);
        ContactsList.setAdapter(mContactsAdapter);
        ContactsList.setItemAnimator(new DefaultItemAnimator());
        ContactsList.getItemAnimator().setChangeDuration(0);

        //fix slow recyclerview start
        ContactsList.setHasFixedSize(true);
        ContactsList.setItemViewCacheSize(10);
        ContactsList.setDrawingCacheEnabled(true);
        ContactsList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end
        // set recycler view to fastScroller
        fastScroller.setRecyclerView(ContactsList);
        fastScroller.setViewsToUse(R.layout.contacts_fragment_fast_scroller, R.id.fastscroller_bubble, R.id.fastscroller_handle);

    }


    /**
     * method to update contacts
     *
     * @param contactsModelList this is parameter for  updateContacts method
     */
    public void updateContacts(List<UsersModel> contactsModelList) {
        RealmList<UsersModel> contactsModels = new RealmList<UsersModel>();
        contactsModels.addAll(contactsModelList);

        mContactsAdapter.setContacts(contactsModels);

        if (contactsModels.size() != 0) {
            fastScroller.setVisibility(View.VISIBLE);
            ContactsList.setVisibility(View.VISIBLE);
            emptyContacts.setVisibility(View.GONE);

        } else {
            fastScroller.setVisibility(View.GONE);
            ContactsList.setVisibility(View.GONE);
            emptyContacts.setVisibility(View.VISIBLE);
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

            case EVENT_BUS_REFRESH_CONTACTS:
                mContactsPresenter.onRefresh();
                break;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        EventBus.getDefault().unregister(this);
        mContactsPresenter.onDestroy();

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
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==123 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            mContactsPresenter = new ContactsPresenter(this);
            mContactsPresenter.onCreate();
        }
    }
}