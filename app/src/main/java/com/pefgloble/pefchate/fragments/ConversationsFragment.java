package com.pefgloble.pefchate.fragments;


import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.ActionMode;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;


import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.ConversationsAdapter;
import com.pefgloble.pefchate.activities.NewConversationContactsActivity;
import com.pefgloble.pefchate.adapter.RecentUserAdapter;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.RateHelper;
import com.pefgloble.pefchate.interfaces.LoadingData;
import com.pefgloble.pefchate.presenter.ConversationsPresenter;
import com.pefgloble.pefchate.util.PreCachingLayoutManager;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmList;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_ACTION_MODE_FINISHED;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_DELETE_CONVERSATION_ITEM;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_IMAGE_PROFILE_UPDATED;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_IS_READ;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_UPDATE_CONVERSATION_OLD_ROW;


/**
 * Created by Abderrahim El imame  on 20/01/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class ConversationsFragment extends Fragment implements LoadingData, RecyclerView.OnItemTouchListener, ActionMode.Callback {

    @BindView(R.id.ConversationsList)
    RecyclerView ConversationList;
    @BindView(R.id.empty)
    LinearLayout emptyConversations;

    @BindView(R.id.fabAddGroup)
    FloatingActionButton fabAdd;

    @BindView(R.id.srchConv)
    EditText edtSrch;

    @BindView(R.id.friendRecycler)
    RecyclerView friendRecyler;

    @BindView(R.id.swipeConversations)
    SwipeRefreshLayout mSwipeRefreshLayout;


    public ConversationsAdapter mConversationsAdapter;
    public RecentUserAdapter recentUserAdapter;
    private ConversationsPresenter mConversationsPresenter;
    private GestureDetectorCompat gestureDetector;
    private ActionMode actionMode;


    private View mView;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        mView = inflater.inflate(R.layout.chat_page, container, false);
        ButterKnife.bind(this, mView);
        mConversationsPresenter = new ConversationsPresenter(this);
        initializerView();
        initializerSearchView();
        mConversationsPresenter.onCreate();


        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                RateHelper.significantEvent(getContext());
                AppHelper.LaunchActivity(getActivity(), NewConversationContactsActivity.class);
            }
        });

        return mView;
    }

    private void initializerSearchView() {
     edtSrch.setOnFocusChangeListener(new View.OnFocusChangeListener() {
         @Override
         public void onFocusChange(View view, boolean b) {
             if (!b) {
                 InputMethodManager inputManager = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                 inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
             }
         }
     });
     edtSrch.addTextChangedListener(new TextWatcher() {
         @Override
         public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

         }

         @Override
         public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
             mConversationsAdapter.setString(charSequence.toString());
             Search(charSequence.toString().trim());
         }

         @Override
         public void afterTextChanged(Editable editable) {
         }
     });
    }
    public void Search(String string) {

        final List<ConversationModel> filteredModelList;
        filteredModelList = FilterList(string);
        if (filteredModelList.size() != 0) {
            //searchList.setVisibility(View.VISIBLE);
            emptyConversations.setVisibility(View.GONE);
            // mConversationsAdapter.animateTo(filteredModelList);
           // searchList.scrollToPosition(0);
        } else {
            //searchList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }
    private List<ConversationModel> FilterList(String query) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        return realm.where(ConversationModel.class)
                .beginGroup()
                .contains("owner.username", query, Case.INSENSITIVE)
                .or()
                .contains("latestMessage.message", query, Case.INSENSITIVE)
                .endGroup()
                .findAll();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

    }

    /**
     * method to initialize the view
     */
    private void initializerView() {
        setHasOptionsMenu(true);
        PreCachingLayoutManager layoutManager = new PreCachingLayoutManager(getActivity());
        layoutManager.setOrientation(RecyclerView.VERTICAL);
        layoutManager.setExtraLayoutSpace(AppHelper.getScreenHeight(getActivity()));//fix preload image before appears
        mConversationsAdapter = new ConversationsAdapter(ConversationList, mConversationsPresenter.getRealm());
        mConversationsAdapter.setHasStableIds(true);//avoid blink item when notify adapter
        ConversationList.setLayoutManager(layoutManager);
        ConversationList.setAdapter(mConversationsAdapter);

        recentUserAdapter=new RecentUserAdapter(getContext());
        PreCachingLayoutManager preCachingLayoutManager=new PreCachingLayoutManager(getActivity());
        preCachingLayoutManager.setOrientation(RecyclerView.HORIZONTAL);
        recentUserAdapter.setHasStableIds(true);
        friendRecyler.setLayoutManager(preCachingLayoutManager);
        friendRecyler.setAdapter(recentUserAdapter);

        /*
        ConversationList.setItemAnimator(new DefaultItemAnimator());
        ConversationList.getItemAnimator().setChangeDuration(0);*/
        ((SimpleItemAnimator) ConversationList.getItemAnimator()).setSupportsChangeAnimations(false);
        //fix slow recyclerview start
        ConversationList.setHasFixedSize(true);
        ConversationList.setItemViewCacheSize(30);
        ConversationList.setDrawingCacheEnabled(true);
        ConversationList.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        ///fix slow recyclerview end
        ConversationList.addOnItemTouchListener(this);
        gestureDetector = new GestureDetectorCompat(getActivity(), new RecyclerViewBenOnGestureListener());
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorAccent, R.color.colorAccent, R.color.colorGreenLight);
        mSwipeRefreshLayout.setOnRefreshListener(() -> {
            //   endlessOnScrollListener.resetState();
            mConversationsPresenter.onRefresh();
        });

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
     * @param position
     */
    private void ToggleSelection(int position) {
        mConversationsAdapter.toggleSelection(position);
        String title = String.format(" " + getString(R.string.selected_items), mConversationsAdapter.getSelectedItemCount());
        actionMode.setTitle(title);


    }

    @Override
    public void onResume() {
        super.onResume();
        mConversationsPresenter.onResume();
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.select_conversation_menu, menu);
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_STARTED));
        return true;
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @SuppressLint("CheckResult")
    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {


        switch (item.getItemId()) {
            // TODO: 11/7/18 nzid group actions
            case R.id.delete_conversations:

                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());


                builder.setMessage(R.string.alert_message_delete_conversation);

                builder.setPositiveButton(R.string.Yes, (dialog, whichButton) -> {
                    int arraySize = mConversationsAdapter.getSelectedItems().size();
                    Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                    AppHelper.LogCat("start delete " + arraySize);
                    try {

                        if (arraySize != 0) {
                            AppHelper.showDialog(getActivity(), getString(R.string.deleting_chat));
                            for (int x = 0; x < arraySize; x++) {
                                int currentPosition = mConversationsAdapter.getSelectedItems().get(x);
                                try {
                                    ConversationModel conversationsModel = mConversationsAdapter.getItem(currentPosition);
                                    mConversationsPresenter.deleteConversation(conversationsModel.get_id(), currentPosition);
                                } catch (Exception e) {
                                    AppHelper.LogCat(e);
                                }
                            }
                            AppHelper.LogCat("finish delete");
                            AppHelper.hideDialog();
                        } else {
                            AppHelper.CustomToast(getActivity(), "Delete conversation failed  ");
                        }
                        if (actionMode != null) {
                            mConversationsAdapter.clearSelections();
                            actionMode.finish();
                        }

                    } finally {
                        if (!realm.isClosed())
                            realm.close();
                    }

                });


                builder.setNegativeButton(R.string.No, (dialog, whichButton) -> {

                });

                builder.show();
                return true;
            default:
                return false;
        }
    }


    @Override
    public void onDestroyActionMode(ActionMode mode) {
        this.actionMode = null;
        mConversationsAdapter.clearSelections();
        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ACTION_MODE_DESTROYED));
    }


    private class RecyclerViewBenOnGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            View view = ConversationList.findChildViewUnder(e.getX(), e.getY());
            int currentPosition = ConversationList.getChildAdapterPosition(view);
            try {
                ConversationModel conversationModel = mConversationsAdapter.getItem(currentPosition);

                if (actionMode != null) {
                    if (!conversationModel.isIs_group())
                        ToggleSelection(currentPosition);
                    boolean hasCheckedItems = mConversationsAdapter.getSelectedItems().size() > 0;//Check if any items are already selected or not
                    if (!hasCheckedItems && actionMode != null) {
                        // there no selected items, finish the actionMode
                        actionMode.finish();
                    }

                }

            } catch (Exception ex) {
                AppHelper.LogCat(" onSingleTapConfirmed " + ex.getMessage());
            }
            return super.onSingleTapConfirmed(e);
        }

        @Override
        public void onLongPress(MotionEvent e) {
            try {

                View view = ConversationList.findChildViewUnder(e.getX(), e.getY());
                int currentPosition = ConversationList.getChildAdapterPosition(view);
                if (actionMode != null) {
                    return;
                }
                ConversationModel conversationModel = mConversationsAdapter.getItem(currentPosition);

                if (!conversationModel.isIs_group()) {

                    actionMode = getActivity().startActionMode(ConversationsFragment.this);
                    if (actionMode != null) {
                        ToggleSelection(currentPosition);

                    }
                }
                super.onLongPress(e);
            } catch (Exception e1) {
                AppHelper.LogCat(" onLongPress " + e1.getMessage());
            }


        }


    }

    /**
     * method to show conversation list
     *
     * @param conversationsModels this is parameter for  ShowConversation  method
     */
    public void UpdateConversation(List<ConversationModel> conversationsModels) {

        if (conversationsModels.size() != 0) {
            RealmList<ConversationModel> conversationsModels1 = new RealmList<ConversationModel>();
            conversationsModels1.addAll(conversationsModels);
            mConversationsAdapter.setConversations(conversationsModels1);

            ConversationList.setVisibility(View.VISIBLE);
            emptyConversations.setVisibility(View.GONE);
            ConversationList.setAdapter(mConversationsAdapter);
        } else {
            ConversationList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }

    public void UpdateRecentUser(List<ConversationModel> conversationModels){
        if (conversationModels.size()!=0) {
            RealmList<ConversationModel> conversationsModels2 = new RealmList<ConversationModel>();
            conversationsModels2.addAll(conversationModels);
            recentUserAdapter.setConversations(conversationsModels2);
            friendRecyler.setVisibility(View.VISIBLE);
            friendRecyler.setAdapter(recentUserAdapter);
        }
        else {
            friendRecyler.setVisibility(View.GONE);
        }

    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mConversationsPresenter != null)
            mConversationsPresenter.onDestroy();

    }

    @Override
    public void onStop() {
        super.onStop();

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

    /**
     * method to add a new message to list messages
     *
     * @param conversationId this is the parameter for addConversationEventMainThread
     */

    private void addConversationEventMainThread(String conversationId) {
        mConversationsAdapter.addConversationItem(conversationId);
        ConversationList.setVisibility(View.VISIBLE);
        emptyConversations.setVisibility(View.GONE);
        ConversationList.smoothScrollToPosition(0);
    }
    /**
     * method of EventBus
     *
     * @param pusher this is parameter of onEventMainThread method
     */
    @SuppressWarnings("unused")
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onEventMainThread(Pusher pusher) {
        String messageId = pusher.getMessageId();
        switch (pusher.getAction()) {
            case EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW:
                new Handler().postDelayed(() -> addConversationEventMainThread(pusher.getConversationId()), 500);
                break;
            case EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW:
                new Handler().postDelayed(() -> mConversationsAdapter.updateConversationItem(pusher.getConversationId()), 500);
                break;

            case EVENT_BUS_MESSAGE_IS_READ:
            case EVENT_UPDATE_CONVERSATION_OLD_ROW:
            case EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS:
            case EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS:
            case EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS:
                new Handler().postDelayed(() -> mConversationsAdapter.updateStatusConversationItem(pusher.getConversationId()), 500);
                break;
            case EVENT_BUS_DELETE_CONVERSATION_ITEM:
                mConversationsAdapter.DeleteConversationItem(pusher.getConversationId());
                showEmptyView();
                break;
            case EVENT_BUS_IMAGE_GROUP_UPDATED:
                new Handler().postDelayed(() -> mConversationsAdapter.updateStatusConversationItem(getConversationGroupId(pusher.getGroupID())), 500);
                break;

            case EVENT_BUS_IMAGE_PROFILE_UPDATED:
                new Handler().postDelayed(() ->
                        mConversationsAdapter.updateStatusConversationItem(getConversationId(pusher.getOwnerID())), 500);
                break;
            case EVENT_BUS_ACTION_MODE_FINISHED:
                if (actionMode != null) {
                    mConversationsAdapter.clearSelections();
                    actionMode.finish();
                  /*  if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                        ((AppCompatActivity) getActivity()).getSupportActionBar().show();*/
                }
                break;


            case AppConstants.EVENT_BUS_MEMBER_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_TYPING, pusher.getSenderID(), pusher.getGroupID(), true);

                break;

            case AppConstants.EVENT_BUS_MEMBER_STOP_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_STOP_TYPING, pusher.getSenderID(), pusher.getGroupID(), true);
                break;
            case AppConstants.EVENT_BUS_USER_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_TYPING, pusher.getSenderID(), null, false);

                break;

            case AppConstants.EVENT_BUS_USER_STOP_TYPING:
                mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_STOP_TYPING, pusher.getSenderID(), null, false);
                break;

            case AppConstants.EVENT_BUS_UPDATE_USER_STATE:
                if (pusher.getData().equals(AppConstants.EVENT_BUS_USER_IS_ONLINE)) {
                    mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_CONNECTED, pusher.getSenderID(), null, false);
                } else if (pusher.getData().equals(AppConstants.EVENT_BUS_USER_IS_OFFLINE)) {
                    mConversationsAdapter.updateUserStatus(AppConstants.STATUS_USER_DISCONNECTED, pusher.getSenderID(), null, false);
                }
                break;

            default:
                throw new IllegalStateException("Unexpected value: " + pusher.getAction());
        }
    }

    private String getConversationGroupId(String GroupID) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        String conversationId = null;
        try {
            ConversationModel conversationsModel = realm.where(ConversationModel.class).equalTo("group._id", GroupID).findFirst();
            conversationId = conversationsModel.get_id();
            if (!realm.isClosed()) realm.close();
            return conversationId;
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception ContactFragment" + e.getMessage());
            return conversationId;
        }
    }


    private String getConversationId(String ownerId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        String conversationId = null;
        try {
            ConversationModel conversationsModel = realm.where(ConversationModel.class).equalTo("owner._id", ownerId).findFirst();
            conversationId = conversationsModel.get_id();
            if (!realm.isClosed()) realm.close();
            return conversationId;
        } catch (Exception e) {
            AppHelper.LogCat("Conversation id Exception ContactFragment" + e.getMessage());
            return conversationId;
        }
    }

    private void showEmptyView() {
        if (mConversationsAdapter.getItemCount() == 0) {
            ConversationList.setVisibility(View.GONE);
            emptyConversations.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        EventBus.getDefault().unregister(this);
    }
}
