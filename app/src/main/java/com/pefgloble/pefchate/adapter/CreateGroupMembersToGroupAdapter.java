package com.pefgloble.pefchate.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
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
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.activities.group.CreateGroupActivity;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.UtilsPhone;


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
public class CreateGroupMembersToGroupAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<UsersModel> mContactsModels;
    private LayoutInflater mInflater;

    public CreateGroupMembersToGroupAdapter(Activity mActivity) {
        this.mActivity = mActivity;
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
        View view = mInflater.inflate(R.layout.row_create_group_members, parent, false);
        return new ContactsViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
        final UsersModel contactsModel = this.mContactsModels.get(position);
        try {

            String username;
         /*   if (contactsModel.getUsername() != null) {
                username = contactsModel.getUsername();
            } else {*/
            String name = UtilsPhone.getContactName(contactsModel.getPhone());
            if (name != null) {
                username = name;
            } else {
                username = contactsModel.getPhone();
            }


            contactsViewHolder.setUsername(username);

            contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), username);

        } catch (Exception e) {
            AppHelper.LogCat("Create group members Exception" + e.getMessage());
        }


    }

    @Override
    public long getItemId(int position) {
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


    class ContactsViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        ImageView userImage;

        @BindView(R.id.username)
        TextView username;


        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            itemView.setOnClickListener(view -> {

            });

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

        void setUsername(String phone) {
            username.setText(phone);

        }

    }


}

