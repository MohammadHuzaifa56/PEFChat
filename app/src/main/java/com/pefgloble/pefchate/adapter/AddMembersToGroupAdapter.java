package com.pefgloble.pefchate.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
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
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.vanniktech.emoji.EmojiTextView;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;


/**
 * Created by Abderrahim El imame on 11/03/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class AddMembersToGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<UsersModel> mContactsModels;
    private LayoutInflater mInflater;
    private SparseBooleanArray selectedItems;


    public AddMembersToGroupAdapter(Activity mActivity, List<UsersModel> mContactsModels) {
        this.mActivity = mActivity;
        this.mContactsModels = mContactsModels;
        this.selectedItems = new SparseBooleanArray();
        mInflater = LayoutInflater.from(mActivity);

    }


    public void setContacts(List<UsersModel> mContactsModels) {
        this.mContactsModels = mContactsModels;
        notifyDataSetChanged();
    }


    public List<UsersModel> getContacts() {
        return mContactsModels;
    }


    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_add_members_group, parent, false);
        return new ContactsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final UsersModel contactsModel = this.mContactsModels.get(position);
        try {
            String username;

                String name = UtilsPhone.getContactName(contactsModel.getPhone());
                if (name != null) {
                    username = name;
                } else {
                    username = contactsModel.getPhone();
                }

            contactsViewHolder.setUsername(username);
            if (contactsModel.getStatus() != null) {
                contactsViewHolder.setStatus(contactsModel.getStatus().getBody());
            }

            contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), username);

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }

        holder.itemView.setActivated(selectedItems.get(position, false));
        if (holder.itemView.isActivated()) {

            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_enter);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    contactsViewHolder.selectIcon.setVisibility(View.VISIBLE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            contactsViewHolder.selectIcon.startAnimation(animation);
        } else {

            final Animation animation = AnimationUtils.loadAnimation(mActivity, R.anim.scale_for_button_animtion_exit);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    contactsViewHolder.selectIcon.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });
            contactsViewHolder.selectIcon.startAnimation(animation);

        }

    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    public int getItemPosition(UsersModel contactsModel) {
        int position = 0;
        if (mContactsModels.contains(contactsModel))
            position = mContactsModels.indexOf(contactsModel);
        return position;
    }

    @Override
    public int getItemCount() {
        if (mContactsModels != null) {
            return mContactsModels.size();
        } else {
            return 0;
        }
    }

    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_REMOVE_CREATE_MEMBER, mContactsModels.get(pos)));
        } else {
            selectedItems.put(pos, true);
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_ADD_CREATE_MEMBER, mContactsModels.get(pos)));
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public int getSelectedItemCount() {
        return selectedItems.size();
    }

    public List<Integer> getSelectedItems() {
        List<Integer> items = new ArrayList<>(selectedItems.size());
        int arraySize = selectedItems.size();
        for (int i = 0; i < arraySize; i++) {
            items.add(selectedItems.keyAt(i));
        }
        return items;
    }


    class ContactsViewHolder extends RecyclerView.ViewHolder {
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
                        .centerCrop().apply(RequestOptions.circleCropTransform())
                        .placeholder(drawable)
                        .error(drawable)
                        .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                        .into(target);
            } else {
                userImage.setImageDrawable(drawable);
            }
        }

        void setUsername(String phone) {
            username.setText(phone);
        }

        void setStatus(String Status) {
            String finalStatus = UtilsString.unescapeJava(Status);
            status.setText(finalStatus);
        }
    }


}

