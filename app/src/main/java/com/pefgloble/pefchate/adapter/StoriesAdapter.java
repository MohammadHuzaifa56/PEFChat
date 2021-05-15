package com.pefgloble.pefchate.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.activities.stories.StoriesDetailsActivity;
import com.pefgloble.pefchate.activities.stories.StoriesListActivity;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.stories.StoriesHeaderModel;
import com.pefgloble.pefchate.stories.StoriesModel;
import com.pefgloble.pefchate.stories.StoryModel;
import com.pefgloble.pefchate.ui.RelativeTimeTextView;
import com.pefgloble.pefchate.ui.StoryView;
import com.vanniktech.emoji.EmojiTextView;

import org.joda.time.DateTime;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.realm.RealmResults;

/**
 * Created by Abderrahim El imame on 6/29/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public static final int VIEW_TYPE_HEADER = 0;
    public static final int VIEW_TYPE_ITEM = 1;

    private RealmList<StoriesModel> storiesModelList;
    private RecyclerView storiesList;
    private AppCompatActivity mActivity;

    private StoriesHeaderModel storiesHeaderModel;

    public StoriesAdapter(AppCompatActivity mActivity, RecyclerView storiesList) {
        this.mActivity = mActivity;
        this.storiesList = storiesList;

    }

    public void setStoriesModelList(RealmList<StoriesModel> storiesModelList) {
        this.storiesModelList = storiesModelList;
        notifyDataSetChanged();
    }

    public void setStoriesHeaderModel(StoriesHeaderModel storiesHeaderModel) {
        this.storiesHeaderModel = storiesHeaderModel;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_HEADER) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.stories_header, parent, false);
            return new HeaderStoryViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_stories, parent, false);
            return new StoryViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {

        if (holder instanceof StoryViewHolder) {
            StoriesModel item = getItem(position);
            StoryViewHolder storyViewHolder = (StoryViewHolder) holder;
            storyViewHolder.username.setText(item.getUsername());

            RealmResults<StoryModel> storyModels = item.getStories().where().equalTo("deleted", false).findAll();
            RealmList<StoryModel> storiesList = new RealmList<>();
            storiesList.addAll(storyModels);
            storyViewHolder.setImageStory(storiesList);
            DateTime previousTs = UtilsTime.getCorrectDate(item.getStories().last().getDate());
            storyViewHolder.story_date.setReferenceTime(previousTs.getMillis());

        } else if (holder instanceof HeaderStoryViewHolder) {
            HeaderStoryViewHolder headerStoryViewHolder = (HeaderStoryViewHolder) holder;
            headerStoryViewHolder.username.setText(R.string.my_story);
            if (storiesHeaderModel != null && storiesHeaderModel.isValid()) {
                RealmResults<StoryModel> storyModels = storiesHeaderModel.getStories().where().equalTo("deleted", false).findAll();
                RealmList<StoryModel> storiesList = new RealmList<>();
                storiesList.addAll(storyModels);
                headerStoryViewHolder.setImageStory(storiesList);

                if (!storiesList.last().isUploaded()) {
                    headerStoryViewHolder.story_date.setVisibility(View.GONE);
                    headerStoryViewHolder.story_status.setVisibility(View.VISIBLE);
                } else {
                    headerStoryViewHolder.story_date.setVisibility(View.VISIBLE);
                    headerStoryViewHolder.story_status.setVisibility(View.GONE);
                    if (storiesList.size() != 0) {

                        DateTime previousTs = UtilsTime.getCorrectDate(storiesList.last().getDate());
                        headerStoryViewHolder.story_date.setReferenceTime(previousTs.getMillis());
                    }
                }
            } else {
                if (getItemCount() != 0)
                    headerStoryViewHolder.setImageStory();
            }
        }
    }

    @Override
    public int getItemViewType(int position) {
        super.getItemViewType(position);
        return position == 0 ? VIEW_TYPE_HEADER : VIEW_TYPE_ITEM;
    }


    public StoriesModel getItem(int position) {
        return storiesModelList.get(position - 1);
    }

    @Override
    public long getItemId(int position) {
        try {
            StoriesModel storiesModel = getItem(position);
            return storiesModel.getId(); ///to avoid blink recyclerview item when notify the adapter
        } catch (Exception e) {
            return position;
        }

    }

    @Override
    public int getItemCount() {
        if (storiesModelList != null)
            return storiesModelList.size() + 1;
        else
            return 1;
    }


    public void updateStoryItem(String storyId) {
        try {
            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
            int arraySize = storiesModelList.size();
            for (int i = 0; i < arraySize; i++) {
                StoriesModel model = storiesModelList.get(i);
                if (storyId.equals(model.getId())) {
                    StoriesModel storiesModel = realm.where(StoriesModel.class).equalTo("_id", storyId).findFirst();
                    changeItemAtPosition(i, storiesModel);
                    if (i != 0)
                        MoveItemToPosition(i, 1);
                    break;
                }

            }
            realm.close();
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }

    private void changeItemAtPosition(int position, StoriesModel storiesModel) {
        storiesModelList.set(position, storiesModel);
        notifyItemChanged(position);
    }

    private void MoveItemToPosition(int fromPosition, int toPosition) {
        StoriesModel model = storiesModelList.remove(fromPosition);
        storiesModelList.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
        storiesList.scrollToPosition(fromPosition);
    }


    public void updateStoryMineItem(String storyId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            StoriesHeaderModel storiesHeaderModel = realm.where(StoriesHeaderModel.class).equalTo("_id", storyId).findFirst();
            setStoriesHeaderModel(storiesHeaderModel);
        } catch (Exception e) {
            AppHelper.LogCat("updateStoryMineItem  Exception" + e);
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    public void addStoryItem(String storyId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            StoriesModel storiesModel = realm.where(StoriesModel.class).equalTo("_id", storyId).findFirst();
            if (!isStoryExistInList(storiesModel.get_id())) {
                addStoryItem(1, storiesModel);
            } else {
                return;
            }

        } catch (Exception e) {
            AppHelper.LogCat("addStoryItem  Exception" + e);
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    private void addStoryItem(int position, StoriesModel storiesModel) {
        try {
            this.storiesModelList.add(position, storiesModel);
            notifyItemInserted(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);

        }
    }

    private boolean isStoryExistInList(String storyId) {
        int arraySize = storiesModelList.size();
        boolean exist = false;
        for (int i = 0; i < arraySize; i++) {
            StoriesModel model = storiesModelList.get(i);
            if (storyId.equals(model.getId())) {
                exist = true;
                break;
            }
        }
        return exist;
    }

    public void deleteStoryMineItem() {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            removeStoryItem(0);

        } catch (Exception e) {
            AppHelper.LogCat(e);
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }

    public void removeStoryItem(int position) {
        try {
            storiesModelList.remove(position);
            notifyItemRemoved(position);
        } catch (Exception e) {
            AppHelper.LogCat(e);
        }
    }


    public class StoryViewHolder extends RecyclerView.ViewHolder {


        @BindView(R.id.user_image)
        StoryView storyView;


        @BindView(R.id.username)
        TextView username;


        @BindView(R.id.story_date)
        RelativeTimeTextView story_date;


        StoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(v -> {

                StoriesModel storiesModel = storiesModelList.get(getAdapterPosition() - 1);
                Intent a = new Intent(itemView.getContext(), StoriesDetailsActivity.class);
                a.putExtra("position", getAdapterPosition() - 1);
                a.putExtra("storyId", storiesModel.get_id());
                itemView.getContext().startActivity(a);

            });
        }


        public void setImageStory(RealmList<StoryModel> storyModels) {
            storyView.setStoriesModels(storyModels);
        }

    }

    public class HeaderStoryViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.user_image)
        StoryView storyView;


        @BindView(R.id.empty_story_layout)
        FrameLayout empty_story_layout;

        @BindView(R.id.user_image_profile)
        AppCompatImageView user_image_profile;


        @BindView(R.id.username)
        TextView username;


        @BindView(R.id.story_date)
        RelativeTimeTextView story_date;

        @BindView(R.id.story_status)
        AppCompatImageView story_status;

        @BindView(R.id.my_stories)
        AppCompatImageView my_stories;

        Context context;

        public HeaderStoryViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            context = itemView.getContext();
            itemView.setOnClickListener(v -> {

                if (storiesHeaderModel == null) {
                    FilesManager.capturePhoto(mActivity, AppConstants.PICK_CAMERA_GALLERY_STORY, true);
                } else {

                  //  if (checkIfStoryUploadExist()) return;
                  //  if (checkIfStoryWatingExist()) return;
                    Intent a = new Intent(mActivity, StoriesDetailsActivity.class);
                    a.putExtra("position", getAdapterPosition());
                    a.putExtra("storyId", storiesHeaderModel.get_id());
                    itemView.getContext().startActivity(a);
                }

            });


            my_stories.setOnClickListener(v -> {
                if (storiesHeaderModel == null) {
                    FilesManager.capturePhoto(mActivity, AppConstants.PICK_CAMERA_GALLERY_STORY, true);
                } else {
                    Intent a = new Intent(itemView.getContext(), StoriesListActivity.class);
                    itemView.getContext().startActivity(a);
                }

            });
        }


        private boolean checkIfStoryWatingExist() {

            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
            try {
                RealmQuery<StoryModel> query = realm.where(StoryModel.class)
                        .equalTo("userId", PreferenceManager.getInstance().getID(mActivity))
                        .equalTo("status", AppConstants.IS_WAITING);
                return query.count() != 0;
            } finally {
                if (!realm.isClosed()) realm.close();
            }

        }

        private boolean checkIfStoryUploadExist() {

            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
            try {
                RealmQuery<StoryModel> query = realm.where(StoryModel.class)
                        .equalTo("userId", PreferenceManager.getInstance().getID(mActivity))
                        .equalTo("uploaded", false);
                return query.count() != 0;
            } finally {
                if (!realm.isClosed()) realm.close();
            }

        }

        public void setImageStory() {
            storyView.setVisibility(View.GONE);
            empty_story_layout.setVisibility(View.VISIBLE);
            Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
            try {
                UsersModel usersModel = realm.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(context)).findFirst();

                if (usersModel.getImage() != null) {
                    DrawableImageViewTarget target = new DrawableImageViewTarget(user_image_profile) {

                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            super.onResourceReady(resource, transition);
                            user_image_profile.setImageDrawable(resource);
                        }


                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            user_image_profile.setImageDrawable(errorDrawable);
                        }

                        @Override
                        public void onLoadStarted(@Nullable Drawable placeholder) {
                            super.onLoadStarted(placeholder);
                            user_image_profile.setImageDrawable(placeholder);
                        }
                    };

                    Glide.with(mActivity.getApplicationContext())
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + usersModel.get_id() + "/" + usersModel.getImage()))
                            .signature(new ObjectKey(usersModel.getImage()))
                            .centerCrop()
                            .apply(RequestOptions.circleCropTransform())
                            .placeholder(R.drawable.image_holder_ur_circle)
                            .error(R.drawable.image_holder_ur_circle)
                            .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                            .into(target);
                } else {
                    user_image_profile.setBackgroundDrawable(AppHelper.getDrawable(mActivity, R.drawable.image_holder_ur_circle));
                }

            } catch (Exception e) {
                AppHelper.LogCat("Exception " + e.getMessage());
            } finally {
                if (!realm.isClosed()) realm.close();
            }
        }

        public void setImageStory(RealmList<StoryModel> storyModels) {
            if (storyModels.size() == 0) {
                storyView.setVisibility(View.GONE);
                empty_story_layout.setVisibility(View.VISIBLE);
                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                try {
                    UsersModel usersModel = realm.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(context)).findFirst();

                    if (usersModel.getImage() != null) {
                        DrawableImageViewTarget target = new DrawableImageViewTarget(user_image_profile) {

                            @Override
                            public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                                super.onResourceReady(resource, transition);
                                user_image_profile.setImageDrawable(resource);
                            }


                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                user_image_profile.setImageDrawable(errorDrawable);
                            }

                            @Override
                            public void onLoadStarted(@Nullable Drawable placeholder) {
                                super.onLoadStarted(placeholder);
                                user_image_profile.setImageDrawable(placeholder);
                            }
                        };

                        Glide.with(mActivity.getApplicationContext())
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.ROWS_IMAGE_URL + usersModel.get_id() + "/" + usersModel.getImage()))
                                .signature(new ObjectKey(usersModel.getImage()))
                                .centerCrop()
                                .apply(RequestOptions.circleCropTransform())
                                .placeholder(R.drawable.image_holder_ur_circle)
                                .error(R.drawable.image_holder_ur_circle)
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .into(target);
                    } else {
                        user_image_profile.setBackgroundDrawable(AppHelper.getDrawable(mActivity, R.drawable.image_holder_ur_circle));
                    }

                } catch (Exception e) {
                    AppHelper.LogCat("Exception " + e.getMessage());
                } finally {
                    if (!realm.isClosed()) realm.close();
                }
            } else {

                storyView.setVisibility(View.VISIBLE);
                empty_story_layout.setVisibility(View.GONE);
                storyView.setStoriesModels(storyModels);
            }

        }
    }


}