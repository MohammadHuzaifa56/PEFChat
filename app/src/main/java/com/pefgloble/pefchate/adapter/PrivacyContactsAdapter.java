package com.pefgloble.pefchate.adapter;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.EndPoints;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.ImageLoader;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.ui.RecyclerViewFastScroller;
import com.vanniktech.emoji.EmojiTextView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.RealmList;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class PrivacyContactsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements RecyclerViewFastScroller.BubbleTextGetter {
    private RealmList<UsersModel> mContactsModel;
    private String SearchQuery;

    private SparseBooleanArray selectedItems;

    public boolean isSelectedAll = false;


    public PrivacyContactsAdapter() {
        this.selectedItems = new SparseBooleanArray();
        this.mContactsModel = new RealmList<>();

    }


    public void setContacts(RealmList<UsersModel> contactsModelList) {
        this.mContactsModel = contactsModelList;
        notifyDataSetChanged();
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
        int arraySize = mContactsModel.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final UsersModel model = mContactsModel.get(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final UsersModel model = newModels.get(i);
            if (!mContactsModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(List<UsersModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final UsersModel model = newModels.get(toPosition);
            final int fromPosition = mContactsModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private UsersModel removeItem(int position) {
        final UsersModel model = mContactsModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, UsersModel model) {
        mContactsModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final UsersModel model = mContactsModel.remove(fromPosition);
        mContactsModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_privacy_contacts, parent, false);
        return new ContactsViewHolder(itemView);

    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {


        if (holder instanceof ContactsViewHolder) {
            ContactsViewHolder contactsViewHolder = (ContactsViewHolder) holder;
            Activity context = (Activity) holder.itemView.getContext();

            UsersModel contactsModel = this.mContactsModel.get(position);
            String username;

                String name = UtilsPhone.getContactName(contactsModel.getPhone());
                if (name != null) {
                    username = name;
                } else {
                    username = contactsModel.getPhone();
                }


            contactsViewHolder.setUsername(username);


            SpannableString recipientUsername = SpannableString.valueOf(username);
            if (SearchQuery == null) {
                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.NORMAL);
            } else {
                int index = TextUtils.indexOf(username.toLowerCase(), SearchQuery.toLowerCase());
                if (index >= 0) {
                    recipientUsername.setSpan(new ForegroundColorSpan(AppHelper.getColor(context, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    recipientUsername.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                }

                contactsViewHolder.username.setText(recipientUsername, TextView.BufferType.SPANNABLE);
            }

            contactsViewHolder.setUserImage(contactsModel.getImage(), contactsModel.get_id(), username);


            if (!isSelectedAll) {
                contactsViewHolder.privacy_check_box.setChecked(selectedItems.get(position, false));
            } else {

                if (contactsViewHolder.privacy_check_box.isChecked()) {
                    contactsViewHolder.privacy_check_box.setChecked(false);
                    isSelectedAll = false;
                } else {
                    toggleSelectionAll(position);
                    contactsViewHolder.privacy_check_box.setChecked(true);
                }

            }


        }


    }

    private void changeItemAtPosition(int position, UsersModel contactsModel) {
        mContactsModel.set(position, contactsModel);
        notifyItemChanged(position);
    }


    public List<UsersModel> getUnSelectedItem() {
        ArrayList<UsersModel> usersModelList = new ArrayList<>();
        for (int x = 0; x < mContactsModel.size(); x++) {
            if (!exist(x)) {
                usersModelList.add(mContactsModel.get(x));
            }

        }
        return usersModelList;
    }

    public boolean exist(int pos) {
        return selectedItems.get(pos, false);
    }

    public void toggleSelection(int pos) {
        if (isSelectedAll)
            isSelectedAll = false;
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);

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

    @Override
    public int getItemCount() {
        return mContactsModel.size() > 0 ? mContactsModel.size() : 0;
    }

    @Override
    public String getTextToShowInBubble(int pos) {
        try {
            if (mContactsModel.size() > pos) {
                if (mContactsModel.get(pos).getUsername() != null) {
                    return Character.toString(mContactsModel.get(pos).getUsername().charAt(0));
                } else {
                    String name = UtilsPhone.getContactName(mContactsModel.get(pos).getPhone());
                    if (name != null) {
                        return Character.toString(name.charAt(0));
                    } else {
                        return Character.toString(mContactsModel.get(pos).getPhone().charAt(0));
                    }
                }
            } else {
                return null;
            }
        } catch (Exception e) {
            AppHelper.LogCat(e.getMessage());
            return e.getMessage();
        }

    }

    public UsersModel getItem(int position) {
        return mContactsModel.get(position);
    }

    public void checkAll() {
        //if (!isSelectedAll) {

        clearSelectionsAll();
        isSelectedAll = true;
        notifyDataSetChanged();
        // }

    }

    public void clearSelectionsAll() {
        selectedItems.clear();
    }

    private void toggleSelectionAll(int pos) {
        if (!selectedItems.get(pos, false)) {
            selectedItems.put(pos, true);
        }


    }

    public class ContactsViewHolder extends RecyclerView.ViewHolder {
        Context context;
        @BindView(R.id.user_image)
        ImageView userImage;


        @BindView(R.id.username)
        EmojiTextView username;

        @BindView(R.id.privacy_check_box)
        AppCompatCheckBox privacy_check_box;


        ContactsViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            context = itemView.getContext();

        }




        @SuppressLint("StaticFieldLeak")
        void setUserImage(String ImageUrl, String recipientId, String name) {

            Drawable drawable = AppHelper.getDrawable(context, R.drawable.holder_user);
            if (ImageUrl != null) {
                BitmapImageViewTarget target = new BitmapImageViewTarget(userImage) {

                    @Override
                    public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                        super.onResourceReady(resource, transition);
                        userImage.setImageBitmap(resource);


                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        Bitmap bitmap = null;
                        try {
                            bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.parse(ImageUrl));
                        } catch (IOException ex) {
                            // AppHelper.LogCat(e.getMessage());
                        }
                        if (bitmap != null) {
                            ImageLoader.SetBitmapImage(bitmap, userImage);
                        } else {
                            userImage.setImageDrawable(errorDrawable);
                        }
                    }

                    @Override
                    public void onLoadStarted(@Nullable Drawable placeholder) {
                        super.onLoadStarted(placeholder);
                        userImage.setImageDrawable(placeholder);
                    }
                };
                if (ImageUrl.startsWith("content:")) {

                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(ImageUrl)
                            .signature(new ObjectKey(ImageUrl))
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                } else {

                    Glide.with(context.getApplicationContext())
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + recipientId + "/" + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(drawable)
                            .error(drawable)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                }
            } else {
                userImage.setImageDrawable(drawable);
            }


        }


        void setUsername(String phone) {
            username.setText(phone);
        }


        void setOnClickListener(View.OnClickListener listener) {
            itemView.setOnClickListener(listener);
            userImage.setOnClickListener(listener);
    /*        privacy_check_box.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                clearSelections();

            });*/
        }

    }


}
