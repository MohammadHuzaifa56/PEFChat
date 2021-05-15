package com.pefgloble.pefchate.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.mikhaellopez.circularimageview.CircularImageView;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.UtilsPhone;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.RealmList;

public class RecentUserAdapter extends RecyclerView.Adapter<RecentUserAdapter.RecentViewHolder> {
    RealmList<ConversationModel> mConversations;
    Context context;

    public RecentUserAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override

    public RecentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view=LayoutInflater.from(context).inflate(R.layout.friends_holder,parent,false);
        return new RecentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecentUserAdapter.RecentViewHolder holder, int position) {
     if (mConversations!=null) {
         ConversationModel conversationModel = mConversations.get(position);
         if (conversationModel.getOwner() != null) {
             String imageUrl = conversationModel.getOwner().getImage();
             if (!conversationModel.isIs_group()) {
                 setUserImage(holder, imageUrl,conversationModel.getOwner().get_id());
             } else {
                 setGroupImage(holder, imageUrl);
             }
         }

         holder.imgView.setOnClickListener(new View.OnClickListener() {
             @Override
             public void onClick(View view) {
                 if (conversationModel.isIs_group()) {
                     Intent messagingIntent = new Intent(context, MessagesActivity.class);
                     messagingIntent.putExtra("conversationID", conversationModel.get_id());
                     messagingIntent.putExtra("groupID", conversationModel.getGroup().get_id());
                     messagingIntent.putExtra("recName", conversationModel.getGroup().getName());
                     messagingIntent.putExtra("isGroup", true);
                     context.startActivity(messagingIntent);
                 }
                 else {
                     Intent messagingIntent = new Intent(context, MessagesActivity.class);
                     messagingIntent.putExtra("conversationID", conversationModel.get_id());
                     messagingIntent.putExtra("recipientID", conversationModel.getOwner().get_id());
                     messagingIntent.putExtra("isGroup", false);
                     String name= UtilsPhone.getContactName(conversationModel.getOwner().getPhone());
                     if (name!=null) {
                         messagingIntent.putExtra("recName", name);
                     }
                     else {
                         messagingIntent.putExtra("recName", conversationModel.getOwner().getPhone());
                     }
                     context.startActivity(messagingIntent);
                 }
             }
         });
     }
    }

    private void setGroupImage(RecentViewHolder holder, String imageUrl) {
        Drawable drawable = AppHelper.getDrawable(context, R.drawable.gropic);

        if (imageUrl != null) {
            BitmapImageViewTarget target = new BitmapImageViewTarget(holder.imgView) {


                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    holder.imgView.setImageBitmap(resource);

                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    holder.imgView.setImageDrawable(errorDrawable);
                }


                @Override
                public void onLoadStarted(Drawable placeHolderDrawable) {
                    super.onLoadStarted(placeHolderDrawable);
                    holder.imgView.setImageDrawable(placeHolderDrawable);
                }
            };
            Glide.with(AGApplication.getInstance())
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_GROUP_IMAGE_URL + imageUrl))
                    .signature(new ObjectKey(imageUrl))
                    .dontAnimate()
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                    .into(target);
        } else {
            holder.imgView.setImageDrawable(drawable);
        }
    }

    private void setUserImage(RecentViewHolder holder, String imageUrl,String recipientId) {
        Drawable drawable = AppHelper.getDrawable(context, R.drawable.useric);

        if (imageUrl != null) {
            BitmapImageViewTarget target = new BitmapImageViewTarget(holder.imgView) {


                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    holder.imgView.setImageBitmap(resource);

                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    holder.imgView.setImageDrawable(errorDrawable);
                }


                @Override
                public void onLoadStarted(Drawable placeHolderDrawable) {
                    super.onLoadStarted(placeHolderDrawable);
                    holder.imgView.setImageDrawable(placeHolderDrawable);
                }
            };
            Glide.with(AGApplication.getInstance())
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + imageUrl))
                    .signature(new ObjectKey(imageUrl))
                    .dontAnimate()
                    .centerCrop()
                    .apply(RequestOptions.circleCropTransform())
                    .placeholder(drawable)
                    .error(drawable)
                    .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                    .into(target);
        } else {
            holder.imgView.setImageDrawable(drawable);
        }

    }

    public void setConversations(RealmList<ConversationModel> conversationsModelList) {
        this.mConversations = conversationsModelList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return mConversations.size();
    }

    class RecentViewHolder extends RecyclerView.ViewHolder{
        CircularImageView imgView;
        public RecentViewHolder(@NonNull View itemView) {
            super(itemView);
            imgView=itemView.findViewById(R.id.imgfriend);
        }
    }
}
