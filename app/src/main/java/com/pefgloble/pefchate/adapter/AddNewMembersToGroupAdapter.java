package com.pefgloble.pefchate.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;

import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.presenter.MessagesController;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;


/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddNewMembersToGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private RealmList<UsersModel> mUsersModel;
    private LayoutInflater mInflater;
    private Realm realm;
    private String groupID;
    private String SearchQuery;

    public AddNewMembersToGroupAdapter(Activity mActivity, RealmList<UsersModel> mUsersModel, String groupID, Realm realm) {
        this.mActivity = mActivity;
        this.mUsersModel = mUsersModel;
        mInflater = LayoutInflater.from(mActivity);
        this.realm = realm;
        this.groupID = groupID;

    }

    public void setContacts(RealmList<UsersModel> mContactsModels) {
        this.mUsersModel = mContactsModels;
        notifyDataSetChanged();
    }


    public RealmList<UsersModel> getContacts() {
        return mUsersModel;
    }


    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(List<UsersModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(List<UsersModel> newModels) {
        int arraySize = mUsersModel.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final UsersModel model = mUsersModel.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final UsersModel model = newModels.get(i);
            if (!mUsersModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final UsersModel model = newModels.get(toPosition);
            final int fromPosition = mUsersModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private UsersModel removeItem(int position) {
        final UsersModel model = mUsersModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, UsersModel model) {
        mUsersModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final UsersModel model = mUsersModel.remove(fromPosition);
        mUsersModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_add_members_group, parent, false);
        return new ContactsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final UsersModel usersModel = this.mUsersModel.get(position);
        try {
            if (contactsViewHolder.checkIfMemberExist(usersModel.get_id(), groupID)) {
                contactsViewHolder.itemView.setEnabled(false);
                contactsViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorGray2));
            } else {
                contactsViewHolder.itemView.setEnabled(true);
                contactsViewHolder.username.setTextColor(mActivity.getResources().getColor(R.color.colorBlack));
            }
            String username;
                String name = UtilsPhone.getContactName(usersModel.getPhone());
                if (name != null) {
                    username = name;
                } else {
                    username = usersModel.getPhone();
                }


            SpannableString recipientUsername = SpannableString.valueOf(username);
            if (SearchQuery == null) {
                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
            } else {
                int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                if (index >= 0) {
                    recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
            }


            if (usersModel.getStatus() != null) {
                contactsViewHolder.setStatus(usersModel.getStatus().getBody());
            }

            contactsViewHolder.setUserImage(usersModel.getImage(), usersModel.get_id(), username);

        } catch (Exception e) {
            AppHelper.LogCat("Exception" + e.getMessage());
        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        if (mUsersModel != null) {
            return mUsersModel.size();
        } else {
            return 0;
        }
    }


    class ContactsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        @BindView(R.id.user_image)
        ImageView userImage;

        @BindView(R.id.username)
        TextView username;

        @BindView(R.id.status)
        TextView status;

        @BindView(R.id.select_icon)
        LinearLayout selectIcon;

        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);
            itemView.setOnClickListener(this);

        }


        boolean checkIfMemberExist(String memberID, String groupID) {
            RealmQuery<MembersModel> query = realm.where(MembersModel.class)
                    .equalTo("owner._id", memberID)
                    .equalTo("groupId", groupID)
                    .equalTo("left", false);
            return query.count() != 0;
        }


        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId, String username) {

            Drawable drawable = AppHelper.getDrawable(mActivity, R.drawable.useric);

            if (ImageUrl != null) {

                DrawableImageViewTarget target = new DrawableImageViewTarget(userImage) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        userImage.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        userImage.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        userImage.setImageDrawable(placeHolderDrawable);
                    }
                };

                Glide.with(mActivity.getApplicationContext())
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))

                        .signature(new ObjectKey(ImageUrl))
                        .centerCrop()
                        .apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            } else {
                userImage.setImageDrawable(drawable);
            }
        }


        void setStatus(String Status) {
            String statu = UtilsString.unescapeJava(Status);
            status.setText(statu);
        }

        @Override
        public void onClick(View view) {
            UsersModel usersModel = mUsersModel.get(getAdapterPosition());
            String theName;
            String name = UtilsPhone.getContactName(usersModel.getPhone());
            if (name != null) {
                theName = name;
            } else {
                theName = usersModel.getPhone();
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
            builder.setMessage(mActivity.getString(R.string.add_to_group) + theName + mActivity.getString(R.string.member_to_group))
                    .setPositiveButton(mActivity.getString(R.string.add_new_member), (dialog, which) -> {
                        AddMembersToGroup(usersModel.get_id());
                    }).setNegativeButton(mActivity.getString(R.string.cancel), null).show();
        }


        @SuppressLint("CheckResult")
        private void AddMembersToGroup(String id) {
            AppHelper.showDialog(mActivity, mActivity.getString(R.string.adding_member));
            APIHelper.initializeApiGroups().addMembers(groupID, id).subscribe(groupResponse -> {

                if (groupResponse.isSuccess()) {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_SUCCESS, AppConstants.TEXT_COLOR);
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ADD_MEMBER, groupID));
                    MessagesController.getInstance().sendMessageGroupActions(groupID, AppHelper.getCurrentTime(), AppConstants.ADD_STATE);
                    mActivity.finish();

                } else {
                    AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), groupResponse.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);
                }
            }, throwable -> {

                AppHelper.Snackbar(mActivity, mActivity.findViewById(R.id.ParentLayoutAddNewMembers), throwable.getMessage(), AppConstants.MESSAGE_COLOR_ERROR, AppConstants.TEXT_COLOR);

            }, AppHelper::hideDialog);
        }
    }


}

