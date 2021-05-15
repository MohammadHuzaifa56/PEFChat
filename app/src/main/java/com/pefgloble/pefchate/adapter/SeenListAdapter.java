package com.pefgloble.pefchate.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.stories.StorySeenListModel;
import com.vanniktech.emoji.EmojiTextView;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Abderrahim El imame on 12/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class SeenListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Activity mActivity;
    private List<UsersModel> usersModels;
    private LayoutInflater mInflater;


    public SeenListAdapter(Activity mActivity) {
        this.mActivity = mActivity;
        this.usersModels = new ArrayList<>();
        mInflater = LayoutInflater.from(mActivity);

    }


    public void setistUsersList(List<UsersModel> usersModels) {
        this.usersModels = usersModels;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.row_seen_list, parent, false);
        return new UsersViewHolder(view);

    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        final UsersViewHolder usersViewHolder = (UsersViewHolder) holder;
        final UsersModel contactsModel = this.usersModels.get(position);
        try {
            String username;
            String name = UtilsPhone.getContactName(contactsModel.getPhone());
            if (name != null) {
                username = name;
            } else {
                username = contactsModel.getPhone();
            }


            usersViewHolder.setUsername(username);
            if (contactsModel.getStatus() != null) {
                usersViewHolder.setStatus(contactsModel.getStatus().getBody());
            }

            usersViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id());

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e.getMessage());
        }


    }

    @Override
    public long getItemId(int position) {
        return position;
    }


    @Override
    public int getItemCount() {
        if (usersModels != null) {
            return usersModels.size();
        } else {
            return 0;
        }
    }


    class UsersViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.user_image)
        ImageView userImage;

        @BindView(R.id.username)
        TextView username;

        @BindView(R.id.status)
        TextView status;

        UsersViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            status.setSelected(true);

        }



        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId) {
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