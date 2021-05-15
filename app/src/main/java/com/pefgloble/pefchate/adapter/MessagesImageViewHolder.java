package com.pefgloble.pefchate.adapter;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PorterDuff;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.bumptech.glide.request.target.DrawableImageViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.bumptech.glide.signature.ObjectKey;
import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.messags.FilesResponse;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.EndPoints.EndPoints;
import com.pefgloble.pefchate.RestAPI.FilesDownloadService;
import com.pefgloble.pefchate.RestAPI.FilesUploadService;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.activities.LocationActivity;
import com.pefgloble.pefchate.activities.settings.PreferenceSettingsManager;
import com.pefgloble.pefchate.activities.stories.StoriesDetailsActivity;
import com.pefgloble.pefchate.animation.AnimationsUtil;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.app.interfaces.UploadCallbacks;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.AspectRatioImageView;
import com.pefgloble.pefchate.helpers.GlideUrlHeaders;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.helpers.files.FilesManager;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.helpers.permissions.permissions.Permissions;
import com.pefgloble.pefchate.jobs.DownloadHelper;
import com.pefgloble.pefchate.jobs.DownloadProgressInterceptor;
import com.pefgloble.pefchate.jobs.DownloadProgressResponseBody;
import com.pefgloble.pefchate.jobs.PendingFilesTask;
import com.pefgloble.pefchate.jobs.ProgressRequestBody;
import com.pefgloble.pefchate.jobs.UploadProgressRequestBody;
import com.pefgloble.pefchate.presenter.MessagesController;
import com.pefgloble.pefchate.presenter.StoriesController;
import com.pefgloble.pefchate.presenter.UsersController;
import com.pefgloble.pefchate.stories.StoryModel;
import com.pefgloble.pefchate.ui.ColorGenerator;

import org.greenrobot.eventbus.EventBus;
import org.joda.time.DateTime;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.realm.Realm;
import io.realm.RealmObject;
import jp.wasabeef.glide.transformations.BlurTransformation;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;



/**
 * Created by Abderrahim El imame on 10/5/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

public class MessagesImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
    @BindView(R.id.image_file)
    public ImageView imageFile;

    @BindView(R.id.progress_bar_upload_image)
    public ProgressBar mProgressUploadImage;

    @BindView(R.id.progress_bar_upload_image_init)
    public ProgressBar mProgressUploadImageInitial;

    @BindView(R.id.cancel_upload_image)
    public AppCompatImageButton cancelUploadImage;

    @BindView(R.id.retry_upload_image)
    public LinearLayout retryUploadImage;

    @BindView(R.id.progress_bar_download_image)
    public ProgressBar mProgressDownloadImage;

    @BindView(R.id.progress_bar_download_image_init)
    public ProgressBar mProgressDownloadImageInitial;

    @BindView(R.id.cancel_download_image)
    public AppCompatImageButton cancelDownloadImage;

    @BindView(R.id.download_image)
    public LinearLayout downloadImage;

    @BindView(R.id.file_size_image)
    public AppCompatTextView fileSizeImage;

//normal message

    @BindView(R.id.message_text)
    public TextView message;

    @BindView(R.id.date_message)
    public AppCompatTextView date;

    @BindView(R.id.sender_name)
    public AppCompatTextView senderName;

    @BindView(R.id.status_messages)
    public AppCompatImageView statusMessages;


    @BindView(R.id.date_general_message)
    public AppCompatTextView date_general_message;

    @BindView(R.id.message_layout)
    public LinearLayout message_layout;
    //repliyed view
    @BindView(R.id.replied_message_view)
    public View replied_message_view;

    @BindView(R.id.color_view)
    public View color_view;

    @BindView(R.id.owner_name)
    public AppCompatTextView owner_name;

    @BindView(R.id.message_type)
    public AppCompatTextView message_type;

    @BindView(R.id.short_message)
    public TextView short_message;


    @BindView(R.id.message_file_thumbnail)
    public AppCompatImageView message_file_thumbnail;

    private Activity mActivity;
    private MessagesAdapter messagesAdapter;


    private boolean isUploadServiceStopped = false;
    private boolean isDownloadServiceStopped = false;


    public MessagesImageViewHolder(MessagesAdapter messagesAdapter, @NonNull View itemView) {
        super(itemView);
        this.messagesAdapter = messagesAdapter;

        ButterKnife.bind(this, itemView);

        senderName.setSelected(true);
        mActivity = (Activity) itemView.getContext();

        //for image upload
        setupProgressBarUploadImage();


        cancelDownloadImage.setOnClickListener(this);
        replied_message_view.setOnClickListener(this);
        downloadImage.setOnClickListener(this);
        cancelUploadImage.setOnClickListener(this);
        retryUploadImage.setOnClickListener(this);
        imageFile.setOnClickListener(this);

        itemView.setOnClickListener(view -> {


        });

    }


    private void setupProgressBarUploadImage() {
        mProgressUploadImageInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mProgressUploadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
    }

    private void setupProgressBarDownloadImage() {
        mProgressDownloadImageInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mProgressDownloadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);

    }


    public void setRepliedMessage(String messageId, boolean isMessage) {
        int maxLength = 50;
        if (isMessage) {
            MessageModel messageModel = MessagesController.getMessageById(messageId);

            if (messageModel == null) return;
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(messageModel.getSender().getPhone());
            color_view.setBackgroundColor(color);
            if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                owner_name.setText(mActivity.getString(R.string.you));
                owner_name.setTextColor(color);
            } else {
                String name = UtilsPhone.getContactName(messageModel.getSender().getPhone());

                if (name != null) {

                    if (name.length() > 8) {

                        owner_name.setText(String.format("%s... ", name.substring(0, 8)));
                    } else {
                        owner_name.setText(name);
                    }
                } else {

                    owner_name.setText(messageModel.getSender().getPhone());
                }
                owner_name.setTextColor(color);
            }

            message_type.setVisibility(View.GONE);

            if (messageModel.getFile() != null && !messageModel.getFile().equals("null")) {
                short_message.setVisibility(View.VISIBLE);
                short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));


                switch (messageModel.getFile_type()) {
                    case AppConstants.MESSAGES_IMAGE:
                        if (messageModel.getLatitude() != null && !messageModel.getLatitude().equals("null")) {
                            short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_location_gray_24dp), null, null, null);

                            if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                                String message = UtilsString.unescapeJava(messageModel.getMessage());
                                if (message.length() > maxLength) {

                                    short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                                } else {
                                    short_message.setText(message);
                                }

                            } else
                                short_message.setText(R.string.conversation_row_location);
                        } else {
                            short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                            short_message.setText(R.string.conversation_row_image);
                        }
                        break;
                    case AppConstants.MESSAGES_GIF:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_gif_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_gif);
                        break;
                    case AppConstants.MESSAGES_VIDEO:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_videocam_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_video);
                        break;
                    case AppConstants.MESSAGES_AUDIO:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_headset_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_audio);
                        break;
                    case AppConstants.MESSAGES_DOCUMENT:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_document_file_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_document);
                        break;

                }
                RequestBuilder<Drawable> thumbnailRequest;
                switch (messageModel.getFile_type()) {
                    case AppConstants.MESSAGES_IMAGE:
                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        thumbnailRequest = messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()));

                        messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()))
                                .signature(new ObjectKey(messageModel.getFile()))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    case AppConstants.MESSAGES_VIDEO:

                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        long interval = 5000 * 1000;
                        RequestOptions options = new RequestOptions().frame(interval);
                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + messageModel.getFile()))
                                .signature(new ObjectKey(messageModel.getFile()))
                                .dontAnimate()
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .apply(options)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    case AppConstants.MESSAGES_GIF:

                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        thumbnailRequest = messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()));

                        messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()))
                                .signature(new ObjectKey(messageModel.getFile()))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    default:
                        message_file_thumbnail.setVisibility(View.GONE);
                        break;
                }
            } else {

                message_file_thumbnail.setVisibility(View.GONE);
                if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                    short_message.setVisibility(View.VISIBLE);
                    String message = UtilsString.unescapeJava(messageModel.getMessage());
                    if (message.length() > maxLength) {

                        short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                    } else {
                        short_message.setText(message);
                    }
                } else {
                    short_message.setVisibility(View.GONE);
                }
            }
        } else {


            StoryModel storyModel = StoriesController.getInstance().getStoryById(messageId);

            if (storyModel == null) return;
            UsersModel usersModel = UsersController.getInstance().getUserById(storyModel.getUserId());
            if (usersModel == null) return;
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(usersModel.getPhone());
            color_view.setBackgroundColor(color);
            if (usersModel.get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                owner_name.setText(mActivity.getString(R.string.you));
                owner_name.setTextColor(color);
            } else {
                String name = UtilsPhone.getContactName(usersModel.getPhone());
                if (name != null) {
                    if (name.length() > 8) {

                        owner_name.setText(String.format("%s... ", name.substring(0, 8)));
                    } else {
                        owner_name.setText(name);
                    }
                } else {
                    owner_name.setText(usersModel.getPhone());
                }
                owner_name.setTextColor(color);
            }

            message_type.setVisibility(View.VISIBLE);
            message_type.setText(mActivity.getString(R.string.status));

            if (storyModel.getFile() != null && !storyModel.getFile().equals("null")) {
                short_message.setVisibility(View.VISIBLE);
                short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));


                switch (storyModel.getType()) {
                    case "image":

                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                        if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                            String message = UtilsString.unescapeJava(storyModel.getBody());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else short_message.setText(R.string.conversation_row_image);

                        break;
                    case "video":
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_videocam_gray_24dp), null, null, null);
                        if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                            String message = UtilsString.unescapeJava(storyModel.getBody());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_video);
                        break;


                }
                RequestBuilder<Drawable> thumbnailRequest;
                switch (storyModel.getType()) {
                    case "image":
                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        thumbnailRequest = messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()));

                        messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()))
                                .signature(new ObjectKey(storyModel.getFile()))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    case "video":

                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        long interval = 5000 * 1000;
                        RequestOptions options = new RequestOptions().frame(interval);
                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + storyModel.getFile()))
                                .signature(new ObjectKey(storyModel.getFile()))
                                .dontAnimate()
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .apply(options)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;

                    default:
                        message_file_thumbnail.setVisibility(View.GONE);
                        break;
                }
            } else {
                short_message.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                message_file_thumbnail.setVisibility(View.GONE);
                if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                    short_message.setVisibility(View.VISIBLE);
                    String message = UtilsString.unescapeJava(storyModel.getBody());
                    if (message.length() > maxLength) {

                        short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                    } else {
                        short_message.setText(message);
                    }
                } else {
                    short_message.setVisibility(View.GONE);
                }
            }
        }
    }


    public void setImageFileOffline(MessageModel messagesModel) {
        String ImageUrl = messagesModel.getFile();
        String messageId = messagesModel.get_id();
        File file = new File(ImageUrl);
        //   thumbnailRequestMap.put(messagesModel.getId(), Drawable.createFromPath(ImageUrl));

        AppHelper.LogCat("ImageUrl " + ImageUrl);
        AppHelper.LogCat("ImageUrl file " + file);
        BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {


            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                imageFile.setImageBitmap(resource);
                AppHelper.LogCat("onResourceReady file " + resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                imageFile.setImageDrawable(errorDrawable);
                AppHelper.LogCat("onLoadFailed file ");
            }


            @Override
            public void onLoadStarted(Drawable placeHolderDrawable) {
                super.onLoadStarted(placeHolderDrawable);
                imageFile.setImageDrawable(placeHolderDrawable);
            }
        };


        //get filename from path
        String filename = ImageUrl.substring(ImageUrl.lastIndexOf("/") + 1);
        //remove extension
        if (filename.indexOf(".") > 0)
            filename = filename.substring(0, filename.lastIndexOf("."));
        messagesAdapter.glideRequests
                .asBitmap()
                .load(file)
                /* .listener(new RequestListener<Bitmap>() {
                     @Override
                     public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                         AppHelper.LogCat("onLoadFailed GlideException " + e.getMessage());
                         return false;
                     }

                     @Override
                     public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                         AppHelper.LogCat("onResourceReady ");
                         imageFile.setImageBitmap(resource);
                         return false;
                     }
                 })*/.signature(new ObjectKey(filename))
                .error(R.drawable.image_holder_full_screen)
                .placeholder(R.drawable.image_holder_full_screen)
                .centerCrop()
                .dontAnimate()
                .into(target);


    }

    public void setGifFileOffline(MessageModel messagesModel) {
        String ImageUrl = messagesModel.getFile();
        String messageId = messagesModel.get_id();
        File file = new File(ImageUrl);
        AppHelper.LogCat("setGifFileOffline " + ImageUrl);
        //  thumbnailRequestMap.put(messagesModel.getId(), Drawable.createFromPath(ImageUrl));
        messagesAdapter.glideRequests
                .asBitmap()
                //.dontAnimate()
                .load(file)
                .signature(new ObjectKey(ImageUrl))
                .apply(new RequestOptions().centerCrop())
                .error(R.drawable.image_holder_full_screen)
                .placeholder(R.drawable.image_holder_full_screen)
                .into(imageFile);


    }

    public void setGifFile(MessageModel messagesModel) {
        String ImageUrl = messagesModel.getFile();
        String senderId = messagesModel.getSender().get_id();
        String messageId = messagesModel.get_id();
        boolean isDownLoad = messagesModel.isFile_downLoad();

        RequestBuilder<Drawable> thumbnailRequest = messagesAdapter.glideRequests
                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE);

        if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {

            if (FilesManager.isFileGifSentExists(mActivity, FilesManager.getGif(ImageUrl))) {

                AppHelper.LogCat("exist " + FilesManager.getFileGifSent(mActivity, ImageUrl));
                /*if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {
                    glideRequests
                            .load(FilesManager.getFileGifSent(mActivity, ImageUrl))
                            .signature(new ObjectKey(FilesManager.getFileGifSent(mActivity, ImageUrl)))
                            .thumbnail(thumbnailRequest)
                            .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                            .error(thumbnailRequestMap.get(messagesModel.getId()))


                            .into(imageFile);
                } else {*/

                messagesAdapter.glideRequests
                        .load(FilesManager.getFileGifSent(mActivity, ImageUrl))
                        .signature(new ObjectKey(FilesManager.getFileGifSent(mActivity, ImageUrl)))
                        .thumbnail(thumbnailRequest)
                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                        .apply(new RequestOptions().centerCrop())
                        .into(imageFile);

                //  }
            } else {
                AppHelper.LogCat("not exist " + FilesManager.getFileGifSent(mActivity, ImageUrl));
                DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        imageFile.setImageDrawable(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageFile.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        imageFile.setImageDrawable(placeHolderDrawable);
                    }
                };
             /*   if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                    glideRequests
                            .load(EndPoints.MESSAGE_IMAGE_URL + ImageUrl)
                            .signature(new ObjectKey(ImageUrl))
                            .thumbnail(thumbnailRequest)
                            .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                            .error(thumbnailRequestMap.get(messagesModel.getId()))
                            .into(target);

                    ;
                } else {*/
                messagesAdapter.glideRequests
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .thumbnail(thumbnailRequest)
                        .apply(new RequestOptions().centerCrop())
                        .error(R.drawable.image_holder_full_screen)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .into(target);

                //  }


                // }
            }

        } else {


            if (isDownLoad) {

                if (FilesManager.isFileGifExists(mActivity, FilesManager.getGif(ImageUrl))) {

                    AppHelper.LogCat("exist isDownLoad " + FilesManager.getFileGif(mActivity, ImageUrl));
                    messagesAdapter.glideRequests
                            .load(FilesManager.getFileGif(mActivity, ImageUrl))
                            .signature(new ObjectKey(FilesManager.getFileGif(mActivity, ImageUrl)))
                            .thumbnail(thumbnailRequest)
                            .apply(new RequestOptions().centerCrop())
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(imageFile);


                } else {
                    AppHelper.LogCat("not exist isDownLoad" +
                            " " + FilesManager.getFileGifSent(mActivity, ImageUrl));
                    DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    messagesAdapter.glideRequests
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .thumbnail(thumbnailRequest)
                            .apply(new RequestOptions().centerCrop().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);


                }


            } else {

                downloadImage.setVisibility(View.VISIBLE);
                getFileSize(messagesModel.getFile_size(), "image");
                DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        imageFile.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageFile.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        imageFile.setImageDrawable(placeHolderDrawable);
                    }
                };
                messagesAdapter.glideRequests
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .thumbnail(thumbnailRequest)
                        .apply(new RequestOptions().centerCrop().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                        .error(R.drawable.image_holder_full_screen)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .into(target);

            }
        }


    }

    public void setImageFile(MessageModel messagesModel) {

        String ImageUrl = messagesModel.getFile();
        String senderId = messagesModel.getSender().get_id();
        String messageId = messagesModel.get_id();
        boolean isDownLoad = messagesModel.isFile_downLoad();


        Log.d("ImageTag","isDownlaod "+String.valueOf(isDownLoad));

        RequestBuilder<Bitmap> thumbnailRequest = messagesAdapter.glideRequests
                .asBitmap()
                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE);

        if (messagesModel.getLatitude() != null && !messagesModel.getLatitude().equals("null")) {


            BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {


                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    imageFile.setImageBitmap(resource);
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    imageFile.setImageDrawable(errorDrawable);

                }


                @Override
                public void onLoadStarted(Drawable placeHolderDrawable) {
                    super.onLoadStarted(placeHolderDrawable);
                    imageFile.setImageDrawable(placeHolderDrawable);

                }
            };
           /* if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                glideRequests
                        .load(EndPoints.MESSAGE_IMAGE_URL + ImageUrl)
                        .signature(new ObjectKey(ImageUrl))
                        .dontAnimate()
                        .thumbnail(thumbnailRequest)
                        .centerCrop()
                        .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                        .error(thumbnailRequestMap.get(messagesModel.getId()))
                        .centerCrop()


                        .into(target);
            } else {*/
            messagesAdapter.glideRequests
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                    .signature(new ObjectKey(ImageUrl))
                    .dontAnimate()
                    .thumbnail(thumbnailRequest)
                    .centerCrop()
                    .error(R.drawable.image_holder_full_screen)
                    .placeholder(R.drawable.image_holder_full_screen)
                    .centerCrop()
                    .into(target);
            //}


        } else {

            if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {


                if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(ImageUrl))) {

                    /*if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {
                        glideRequests
                                .load(FilesManager.getFileImageSent(mActivity, ImageUrl))
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                                .error(thumbnailRequestMap.get(messagesModel.getId()))


                                .centerCrop()

                                .into(imageFile);
                    } else {*/

                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(FilesManager.getFileImageSent(mActivity, ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .centerCrop()
                            .into(imageFile);
                    //  }
                } else {

                    BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {


                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };

                   /* if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                        glideRequests
                                .load(EndPoints.MESSAGE_IMAGE_URL + ImageUrl)
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                                .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                                .error(thumbnailRequestMap.get(messagesModel.getId())).centerCrop()


                                .into(target);
                    } else {*/
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);
                }
            } else {

                if (isDownLoad) {

                    if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(ImageUrl))) {

                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(FilesManager.getFileImage(mActivity, ImageUrl))
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .error(R.drawable.image_holder_full_screen)
                                .placeholder(R.drawable.image_holder_full_screen)
                                .into(imageFile);

                    } else {

                        BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                super.onResourceReady(resource, transition);
                                imageFile.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                imageFile.setImageDrawable(errorDrawable);
                            }


                            @Override
                            public void onLoadStarted(Drawable placeHolderDrawable) {
                                super.onLoadStarted(placeHolderDrawable);
                                imageFile.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .error(R.drawable.image_holder_full_screen)
                                .placeholder(R.drawable.image_holder_full_screen)
                                .into(target);

                    }


                } else {


                    downloadImage.setVisibility(View.VISIBLE);
                    getFileSize(messagesModel.getFile_size(), "image");


                    BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);
                }
            }
        }
    }


    private void getFileSize(String size, String type) {
        try {
            long filesSize = Long.parseLong(size);
            switch (type) {
                case "image":
                    fileSizeImage.setVisibility(View.VISIBLE);
                    fileSizeImage.setText(String.valueOf(FilesManager.getFileSize(filesSize)));
                    break;
            }
        } catch (Exception e) {
            AppHelper.LogCat(" MessagesAdapter " + e.getMessage());
        }
    }

    @Override
    public void onClick(View view) {
        if (!messagesAdapter.isActivated) {
            MessageModel messagesModel = messagesAdapter.getItem(getAdapterPosition());

            String senderId = messagesModel.getSender().get_id();
            String messageId = messagesModel.get_id();
            boolean isDownLoad = messagesModel.isFile_downLoad();
            switch (view.getId()) {


                case R.id.image_file:
                    if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null")) {
                        if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF))
                            return;
                        if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {
                            if (messagesModel.isFile_upload()) {
                                showImage(messagesModel);
                            }
                        } else {
                            if (messagesModel.isFile_downLoad()) {
                                showImage(messagesModel);
                            } else {
                                Log.d("LocTag","else called");
                                if (messagesModel.getLatitude() != null) {
                                    Log.d("LocTag","if called");
                                    showImage(messagesModel);
                                }
                            }
                            }
                        }

                    break;
                case R.id.cancel_download_image:
                    PendingFilesTask.removeFile(messageId, false, true);
                    break;

                case R.id.download_image:
                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initDownloadListener(messageId, messagesAdapter);
                    }
                    break;
                case R.id.cancel_upload_image:
                    PendingFilesTask.removeFile(messageId, false, false);
                    setShowUpload();
                    break;

                case R.id.retry_upload_image:
                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                        PendingFilesTask.initUploadListener(messageId, messagesAdapter);

                    }
                    break;


                case R.id.replied_message_view:

                    if (messagesModel.isReply_message()) {
                        if (!MessagesController.getInstance().checkIfMessageExist(messagesModel.getReply_id()))
                            return;
                        messagesAdapter.scrollToItem(messagesModel.getReply_id());
                    } else {

                        if (!StoriesController.getInstance().checkIfSingleStoryExist(messagesModel.getReply_id()))
                            return;
                        StoryModel storyModel = StoriesController.getInstance().getStoryById(messagesModel.getReply_id());
                        int currentStoryPosition;

                        if (storyModel.getUserId().equals(PreferenceManager.getInstance().getID(mActivity))) {

                            List<StoryModel> storiesModels = StoriesController.getInstance().getStoriesHeaderById(storyModel.getUserId());
                            if (storiesModels.size() == 0) return;
                            AppHelper.LogCat("storiesModels.size() " + storiesModels.size());
                            currentStoryPosition = storiesModels.indexOf(storyModel);
                        } else {

                            List<StoryModel> storiesModels = StoriesController.getInstance().getStoriesById(storyModel.getUserId());
                            if (storiesModels.size() == 0) return;
                            currentStoryPosition = storiesModels.indexOf(storyModel);
                        }

                        AppHelper.LogCat("currentStoryPosition " + currentStoryPosition);
                        Intent a = new Intent(itemView.getContext(), StoriesDetailsActivity.class);
                        a.putExtra("position", 0);
                        a.putExtra("currentStoryPosition", currentStoryPosition);
                        a.putExtra("storyId", storyModel.getUserId());
                        itemView.getContext().startActivity(a);
                    }
                    break;
            }
        }
    }

    public void setBlinkEffect() {
        AnimationsUtil.manageBlinkEffect(message_layout);
    }

    public void setListeners(final MessageModel messageModel) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (!messageModel.isFile_upload()) {
                    if (PendingFilesTask.containsFile(messageModel.get_id())) {
                        PendingFilesTask.updateUploadListener(messagesAdapter);
                    }
                }
            } else {
                if (!messageModel.isFile_downLoad()) {
                    if (PendingFilesTask.containsFile(messageModel.get_id())) {
                        PendingFilesTask.updateDownloadListener(messagesAdapter);
                    }
                }
            }

        } finally {
            if (!realm.isClosed())
                realm.close();
        }

    }


    public void setSenderName(String SendName) {
        senderName.setText(SendName);
    }

    public void setSenderColor(int Sendcolor) {
        senderName.setTextColor(Sendcolor);
    }

    public void hideSenderName() {
        senderName.setVisibility(View.GONE);
    }

    public void showSenderName() {
        senderName.setVisibility(View.VISIBLE);
    }

    public void hideSent() {
        statusMessages.setVisibility(View.GONE);
    }

    public void showSent(int status) {
        statusMessages.setVisibility(View.VISIBLE);
        switch (status) {
            case AppConstants.IS_WAITING:
                statusMessages.setImageResource(R.drawable.ic_access_time_gray_24dp);
                break;
            case AppConstants.IS_SENT:
                if (messagesAdapter.isStatusUpdated) {
                    AppHelper.playSound(mActivity, "audio/message_is_sent.m4a");
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_gray_24dp);

                break;
            case AppConstants.IS_DELIVERED:
                if (messagesAdapter.isStatusUpdated) {
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_all_gray_24dp);
                break;
            case AppConstants.IS_SEEN:
                if (messagesAdapter.isStatusUpdated) {
                    AnimationsUtil.rotationY(statusMessages);
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_all_blue_24dp);
                break;

        }

    }


    public void setHeaderDate(long now_tm, long msg_tm, DateTime time) {
        Date nowDate = new Date();
        nowDate.setTime(now_tm);
        Date msgDate = new Date();
        msgDate.setTime(msg_tm);
        Calendar now_calendar = Calendar.getInstance();
        now_calendar.setTimeInMillis(now_tm);
        Calendar msg_calendar = Calendar.getInstance();
        msg_calendar.setTimeInMillis(msg_tm);

        if (now_tm == msg_tm) {
            date_general_message.setVisibility(View.VISIBLE);
            date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
        } else if (msg_tm == 0) {
            date_general_message.setVisibility(View.VISIBLE);
            date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
        } else {
            if (msgDate.before(nowDate)) {

                boolean sameDay = now_calendar.get(Calendar.YEAR) == msg_calendar.get(Calendar.YEAR) &&
                        now_calendar.get(Calendar.MONTH) == msg_calendar.get(Calendar.MONTH)
                        && now_calendar.get(Calendar.DAY_OF_MONTH) == msg_calendar.get(Calendar.DAY_OF_MONTH);
                if (sameDay) {
                    date_general_message.setVisibility(View.GONE);
                    date_general_message.setText("");
                } else {
                    date_general_message.setVisibility(View.VISIBLE);
                    date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
                }
            } else {
                date_general_message.setVisibility(View.GONE);
                date_general_message.setText("");
            }
        }


    }

    @SuppressLint("CheckResult")
    public void setDate(String Date) {
        date.setText(UtilsTime.convertMessageDateToStringFormat(mActivity, UtilsTime.getCorrectDate(Date)));
    }


    private void showImage(MessageModel messagesModel) {
        String imageUrl = messagesModel.getFile();

        if (messagesModel.getLongitude() != null && !messagesModel.getLongitude().equals("null")) {
            Permissions.with(mActivity)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .ifNecessary()
                    .withPermanentDenialDialog(mActivity.getString(R.string.location_permission_message))
                    .onAllGranted(() -> {
                        if (messagesModel.getLatitude() == null) return;
                        Intent location = new Intent(mActivity, LocationActivity.class);
                        location.putExtra("userId", messagesModel.getSender().get_id());
                        location.putExtra("lat", Double.parseDouble(messagesModel.getLatitude()));
                        location.putExtra("long", Double.parseDouble(messagesModel.getLongitude()));
                        mActivity.startActivity(location);
                    })
                    .execute();
        } else {
            if (imageUrl == null || messagesModel.getFile().equals("null")) return;
            if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (messagesModel.isFile_upload()) {
                    if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(imageUrl))) {
                        AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.SENT_IMAGE, imageUrl, messagesModel.getSender().get_id());
                    } else {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_image_is_not_exist));
                    }
                }
            } else {
                if (messagesModel.isFile_downLoad()) {
                     String newUrl=FilesManager.getDownloadImage(imageUrl);
                     Log.d("ImageTag","imageUrl "+newUrl);

                    if (FilesManager.isFileImagesExists(mActivity, newUrl)) {
                        AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.RECEIVED_IMAGE, imageUrl, messagesModel.getSender().get_id());
                    } else {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_image_is_not_exist));
                    }
                }

            }
        }

    }

    public void setHideUpload() {
        mProgressUploadImage.setVisibility(View.GONE);
        mProgressUploadImageInitial.setVisibility(View.GONE);
        cancelUploadImage.setVisibility(View.GONE);
        retryUploadImage.setVisibility(View.GONE);
    }

    public void setShowUpload() {
        mProgressUploadImage.setVisibility(View.GONE);
        mProgressUploadImageInitial.setVisibility(View.GONE);
        cancelUploadImage.setVisibility(View.GONE);
        retryUploadImage.setVisibility(View.VISIBLE);
    }

    //methods for upload process
    public void onUploadUpdate(int percentage, String type) {

        switch (type) {

            case "gif":
            case "image":
                if (isUploadServiceStopped) return;

                mProgressUploadImage.setVisibility(View.VISIBLE);
                cancelUploadImage.setVisibility(View.VISIBLE);
                mProgressUploadImageInitial.setVisibility(View.GONE);
                retryUploadImage.setVisibility(View.GONE);
                mProgressUploadImage.setIndeterminate(false);
                mProgressUploadImage.setProgress(percentage);
                break;

        }
    }

    public void onUploadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        AppHelper.LogCat("on error " + type);
        switch (type) {
            case "gif":
            case "image":
                isUploadServiceStopped = true;
                mProgressUploadImage.setVisibility(View.GONE);
                mProgressUploadImageInitial.setVisibility(View.GONE);
                cancelUploadImage.setVisibility(View.GONE);
                retryUploadImage.setVisibility(View.VISIBLE);
                break;

        }
    }

    public void onUploadFinish(String type, MessageModel messagesModel) {

        switch (type) {
            case "gif":
            case "image":
                isUploadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, false);
                mProgressUploadImage.setVisibility(View.GONE);
                mProgressUploadImageInitial.setVisibility(View.GONE);
                cancelUploadImage.setVisibility(View.GONE);
                retryUploadImage.setVisibility(View.GONE);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES, messagesModel));
                break;

        }
    }

    public void onUploadStart(String type) {
        switch (type) {

            case "gif":
            case "image":
                isUploadServiceStopped = false;
                retryUploadImage.setVisibility(View.GONE);
                setupProgressBarUploadImage();
                mProgressUploadImageInitial.setVisibility(View.VISIBLE);
                cancelUploadImage.setVisibility(View.VISIBLE);
                mProgressUploadImageInitial.setIndeterminate(true);
                break;

        }
    }

//end methods for upload process
//start methods for download process

    public void onDownloadStart(String type) {
        switch (type) {

            case "gif":
            case "image":
                isDownloadServiceStopped = false;
                setupProgressBarDownloadImage();
                mProgressDownloadImageInitial.setVisibility(View.VISIBLE);
                cancelDownloadImage.setVisibility(View.VISIBLE);
                downloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setIndeterminate(true);

                break;

        }
    }

    public void onDownloadUpdate(int percentage, String type) {
        switch (type) {
            case "gif":
            case "image":
                if (isDownloadServiceStopped) return;
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                mProgressDownloadImage.setVisibility(View.VISIBLE);
                cancelDownloadImage.setVisibility(View.VISIBLE);

                downloadImage.setVisibility(View.GONE);
                mProgressDownloadImage.setIndeterminate(false);
                mProgressDownloadImage.setProgress(percentage);
                break;

        }


    }


    public void onDownloadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        switch (type) {
            case "gif":
            case "image":
                isDownloadServiceStopped = true;
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.VISIBLE);
                break;


        }
    }


    public void onDownloadFinish(String type, MessageModel messagesModel) {
        switch (type) {
            case "image":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.GONE);
                setImageFile(messagesModel);
                break;

            case "gif":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.GONE);
                setGifFile(messagesModel);
                break;
        }
    }


}



//public class MessagesImageViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,ProgressRequestBody.UploadCallbacks, DownloadProgressResponseBody.DownloadProgressListener {


    //var for  images


   /* @BindView(R.id.image_file)
    public ImageView imageFile;

    @BindView(R.id.progress_bar_upload_image)
    public ProgressBar mProgressUploadImage;

    @BindView(R.id.progress_bar_upload_image_init)
    public ProgressBar mProgressUploadImageInitial;

    @BindView(R.id.cancel_upload_image)
    public AppCompatImageButton cancelUploadImage;

    @BindView(R.id.retry_upload_image)
    public LinearLayout retryUploadImage;

    @BindView(R.id.progress_bar_download_image)
    public ProgressBar mProgressDownloadImage;

    @BindView(R.id.progress_bar_download_image_init)
    public ProgressBar mProgressDownloadImageInitial;

    @BindView(R.id.cancel_download_image)
    public AppCompatImageButton cancelDownloadImage;

    @BindView(R.id.download_image)
    public LinearLayout downloadImage;

    @BindView(R.id.file_size_image)
    public AppCompatTextView fileSizeImage;

    private static WeakReference<UploadCallbacks> mWaitingListenerWeakReference;

//normal message

    @BindView(R.id.message_text)
    public TextView message;

    @BindView(R.id.date_message)
    public AppCompatTextView date;

    @BindView(R.id.sender_name)
    public AppCompatTextView senderName;

    @BindView(R.id.status_messages)
    public AppCompatImageView statusMessages;


    @BindView(R.id.date_general_message)
    public AppCompatTextView date_general_message;

    @BindView(R.id.message_layout)
    public LinearLayout message_layout;
    //repliyed view
    @BindView(R.id.replied_message_view)
    public View replied_message_view;

    @BindView(R.id.color_view)
    public View color_view;

    @BindView(R.id.owner_name)
    public AppCompatTextView owner_name;

    @BindView(R.id.message_type)
    public AppCompatTextView message_type;

    @BindView(R.id.short_message)
    public TextView short_message;


    @BindView(R.id.message_file_thumbnail)
    public AppCompatImageView message_file_thumbnail;

    private Activity mActivity;
    private MessagesAdapter messagesAdapter;


    private boolean isUploadServiceStopped = false;
    private boolean isDownloadServiceStopped = false;
    private MessageModel model;


    public MessagesImageViewHolder(MessagesAdapter messagesAdapter, @NonNull View itemView) {
        super(itemView);
        this.messagesAdapter = messagesAdapter;

        ButterKnife.bind(this, itemView);

        senderName.setSelected(true);
        mActivity = (Activity) itemView.getContext();

        //for image upload
        setupProgressBarUploadImage();


        cancelDownloadImage.setOnClickListener(this);
        replied_message_view.setOnClickListener(this);
        downloadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    Gson gson = new GsonBuilder()
                            .setExclusionStrategies(new ExclusionStrategy() {
                                @Override
                                public boolean shouldSkipField(FieldAttributes f) {
                                    return f.getDeclaringClass().equals(RealmObject.class);
                                }

                                @Override
                                public boolean shouldSkipClass(Class<?> clazz) {
                                    return false;
                                }
                            })
                            .create();

                    DownloadProgressInterceptor interceptor = new DownloadProgressInterceptor(MessagesImageViewHolder.this);

                    OkHttpClient.Builder httpClient = new OkHttpClient.Builder();


                    httpClient.addInterceptor(chain -> {
                        Request original = chain.request();

                        // Customize the request
                        Request request = original.newBuilder()
                                .header("Content-Type", "application/json")
                                .header("Authorization", PreferenceManager.getInstance().getToken(getApplicationContext()))
                                .method(original.method(), original.body())
                                .build();
                        // Customize or return the response
                        return chain.proceed(request);
                    });


                    HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
                    if (AppConstants.DEBUGGING_MODE) {
                        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
                    } else {
                        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.NONE);
                    }

                    OkHttpClient client = httpClient
                            // .addInterceptor(loggingInterceptor)
                            .addInterceptor(interceptor)
                            .connectTimeout(5, TimeUnit.MINUTES)
                            .readTimeout(5, TimeUnit.MINUTES)
                            .writeTimeout(5, TimeUnit.MINUTES)
                            .build();
                    Retrofit builder = new Retrofit.Builder()
                            .baseUrl(BuildConfig.BACKEND_BASE_URL)
                            .client(client)
                            .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();

                    FilesDownloadService downloadService=builder.create(FilesDownloadService.class);
                    Call<ResponseBody> exapmpleCall=downloadService.downloadAllFilesSyns(model.getFile());
                    exapmpleCall.enqueue(new Callback<ResponseBody>() {
                        @Override
                        public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                            if (response.isSuccessful()){
                                new AsyncTask<Void,Void,Void>(){

                                    @Override
                                    protected Void doInBackground(Void... voids) {
                                        DownloadHelper.writeResponseBody(getApplicationContext(),response.body(),"image",model.getFile());
                                      //  Toast.makeText(getApplicationContext(),"Download Completed",Toast.LENGTH_SHORT).show();
                                        return null;
                                    }
                                }.execute();
                            }
                        }

                        @Override
                        public void onFailure(Call<ResponseBody> call, Throwable t) {
                            Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                        }
                    });

                }


            }
        });
        cancelUploadImage.setOnClickListener(this);
*//*        retryUploadImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

                retryUploadImage.setVisibility(View.GONE);
                mProgressUploadImage.setVisibility(View.VISIBLE);

                httpClient.addInterceptor(chain -> {
                    Request original = chain.request();

                    // Customize the request
                    Request request = original.newBuilder()
                            .header("Content-Type", "application/json")
                            .header("Authorization", PreferenceManager.getInstance().getToken(getApplicationContext()))
                            .method(original.method(), original.body())
                            .build();
                    // Customize or return the response
                    return chain.proceed(request);
                });

                Gson gson = new GsonBuilder()
                        .setExclusionStrategies(new ExclusionStrategy() {
                            @Override
                            public boolean shouldSkipField(FieldAttributes f) {
                                return f.getDeclaringClass().equals(RealmObject.class);
                            }

                            @Override
                            public boolean shouldSkipClass(Class<?> clazz) {
                                return false;
                            }
                        })
                        .create();

                OkHttpClient client = httpClient
                        //   .addInterceptor(loggingInterceptor)
                        .connectTimeout(5, TimeUnit.MINUTES)
                        .readTimeout(5, TimeUnit.MINUTES)
                        .writeTimeout(5, TimeUnit.MINUTES)
                        .build();
                Retrofit builder = new Retrofit.Builder()
                        .baseUrl(BuildConfig.BACKEND_BASE_URL)
                        .client(client)
                        .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                 FilesUploadService uploadService=builder.create(FilesUploadService.class);

                 File file=new File(model.getFile());

                 RequestBody requestBody=RequestBody.create(MediaType.parse("image/*"),file);

                 MultipartBody.Part imagePart=MultipartBody.Part.createFormData("file",file.getName(),requestBody);

                 Call<FilesResponse> resourceCall=uploadService.uploadImageFile(imagePart);
                 resourceCall.enqueue(new Callback<FilesResponse>() {
                     @Override
                     public void onResponse(Call<FilesResponse> call, Response<FilesResponse> response) {
                         FilesResponse filesResponse=response.body();
                         mProgressUploadImage.setVisibility(View.GONE);
                         if (filesResponse.isSuccess())
                         {
                             Toast.makeText(getApplicationContext(),filesResponse.getMessage(),Toast.LENGTH_SHORT).show();
                         }
                         else {
                             retryUploadImage.setVisibility(View.VISIBLE);
                             Toast.makeText(getApplicationContext(),filesResponse.getMessage(),Toast.LENGTH_SHORT).show();
                         }
                     }

                     @Override
                     public void onFailure(Call<FilesResponse> call, Throwable t) {
                         Toast.makeText(getApplicationContext(),t.getMessage(),Toast.LENGTH_SHORT).show();
                     }
                 });
            }
        });*//*


        imageFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (model.getLatitude()!=null){
                    String uri = "geo:"+model.getLatitude()+","+model.getLongitude();
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                    intent.setPackage("com.google.android.apps.maps");
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    getApplicationContext().startActivity(intent);
                }
            }
        });

        itemView.setOnClickListener(view -> {


        });

    }




    private void setupProgressBarUploadImage() {
        mProgressUploadImageInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mProgressUploadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);
    }

    private void setupProgressBarDownloadImage() {
        mProgressDownloadImageInitial.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorWhite), PorterDuff.Mode.SRC_IN);
        mProgressDownloadImage.getIndeterminateDrawable().setColorFilter(AppHelper.getColor(mActivity, R.color.colorGreenProgressBars), PorterDuff.Mode.SRC_IN);

    }


    public void setRepliedMessage(String messageId, boolean isMessage) {
        int maxLength = 50;
        if (isMessage) {
            MessageModel messageModel = MessagesController.getMessageById(messageId);

            if (messageModel == null) return;
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(messageModel.getSender().getPhone());
            color_view.setBackgroundColor(color);
            if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                owner_name.setText(mActivity.getString(R.string.you));
                owner_name.setTextColor(color);
            } else {
                String name = UtilsPhone.getContactName(messageModel.getSender().getPhone());

                if (name != null) {

                    if (name.length() > 8) {

                        owner_name.setText(String.format("%s... ", name.substring(0, 8)));
                    } else {
                        owner_name.setText(name);
                    }
                } else {

                    owner_name.setText(messageModel.getSender().getPhone());
                }
                owner_name.setTextColor(color);
            }

            message_type.setVisibility(View.GONE);

            if (messageModel.getFile() != null && !messageModel.getFile().equals("null")) {
                short_message.setVisibility(View.VISIBLE);
                short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));


                switch (messageModel.getFile_type()) {
                    case AppConstants.MESSAGES_IMAGE:
                        if (messageModel.getLatitude() != null && !messageModel.getLatitude().equals("null")) {
                            short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_location_gray_24dp), null, null, null);
                            if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                                String message = UtilsString.unescapeJava(messageModel.getMessage());
                                if (message.length() > maxLength) {
                                    short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                                } else {
                                    short_message.setText(message);
                                }
                            } else
                                short_message.setText(R.string.conversation_row_location);
                        } else {
                            short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                            short_message.setText(R.string.conversation_row_image);
                        }
                        break;
                    case AppConstants.MESSAGES_GIF:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_gif_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_gif);
                        break;
                    case AppConstants.MESSAGES_VIDEO:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_videocam_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_video);
                        break;
                    case AppConstants.MESSAGES_AUDIO:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_headset_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_audio);
                        break;
                    case AppConstants.MESSAGES_DOCUMENT:
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_document_file_gray_24dp), null, null, null);
                        if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                            String message = UtilsString.unescapeJava(messageModel.getMessage());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_document);
                        break;

                }
                RequestBuilder<Drawable> thumbnailRequest;
                switch (messageModel.getFile_type()) {
                    case AppConstants.MESSAGES_IMAGE:
                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        thumbnailRequest = messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()));

                        messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + messageModel.getFile()))
                                .signature(new ObjectKey(messageModel.getFile()))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    case AppConstants.MESSAGES_VIDEO:

                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        long interval = 5000 * 1000;
                        RequestOptions options = new RequestOptions().frame(interval);
                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + messageModel.getFile()))
                                .signature(new ObjectKey(messageModel.getFile()))
                                .dontAnimate()
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .apply(options)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    case AppConstants.MESSAGES_GIF:

                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        thumbnailRequest = messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()));

                        messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + messageModel.getFile()))
                                .signature(new ObjectKey(messageModel.getFile()))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    default:
                        message_file_thumbnail.setVisibility(View.GONE);
                        break;
                }
            } else {

                message_file_thumbnail.setVisibility(View.GONE);
                if (messageModel.getMessage() != null && !messageModel.getMessage().equals("null")) {
                    short_message.setVisibility(View.VISIBLE);
                    String message = UtilsString.unescapeJava(messageModel.getMessage());
                    if (message.length() > maxLength) {

                        short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                    } else {
                        short_message.setText(message);
                    }
                } else {
                    short_message.setVisibility(View.GONE);
                }
            }
        } else {


            StoryModel storyModel = StoriesController.getInstance().getStoryById(messageId);

            if (storyModel == null) return;
            UsersModel usersModel = UsersController.getInstance().getUserById(storyModel.getUserId());
            if (usersModel == null) return;
            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
            // generate random color
            int color = generator.getColor(usersModel.getPhone());
            color_view.setBackgroundColor(color);
            if (usersModel.get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                owner_name.setText(mActivity.getString(R.string.you));
                owner_name.setTextColor(color);
            } else {
                String name = UtilsPhone.getContactName(usersModel.getPhone());
                if (name != null) {
                    if (name.length() > 8) {

                        owner_name.setText(String.format("%s... ", name.substring(0, 8)));
                    } else {
                        owner_name.setText(name);
                    }
                } else {
                    owner_name.setText(usersModel.getPhone());
                }
                owner_name.setTextColor(color);
            }

            message_type.setVisibility(View.VISIBLE);
            message_type.setText(mActivity.getString(R.string.status));

            if (storyModel.getFile() != null && !storyModel.getFile().equals("null")) {
                short_message.setVisibility(View.VISIBLE);
                short_message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));


                switch (storyModel.getType()) {
                    case "image":

                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_photo_camera_gra_24dp), null, null, null);
                        if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                            String message = UtilsString.unescapeJava(storyModel.getBody());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else short_message.setText(R.string.conversation_row_image);

                        break;
                    case "video":
                        short_message.setCompoundDrawablesWithIntrinsicBounds(AppHelper.getDrawable(mActivity, R.drawable.ic_videocam_gray_24dp), null, null, null);
                        if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                            String message = UtilsString.unescapeJava(storyModel.getBody());
                            if (message.length() > maxLength) {

                                short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                            } else {
                                short_message.setText(message);
                            }

                        } else
                            short_message.setText(R.string.conversation_row_video);
                        break;


                }
                RequestBuilder<Drawable> thumbnailRequest;
                switch (storyModel.getType()) {
                    case "image":
                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        thumbnailRequest = messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()));

                        messagesAdapter.glideRequests
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + storyModel.getFile()))
                                .signature(new ObjectKey(storyModel.getFile()))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;
                    case "video":

                        message_file_thumbnail.setVisibility(View.VISIBLE);

                        long interval = 5000 * 1000;
                        RequestOptions options = new RequestOptions().frame(interval);
                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_VIDEO_URL + storyModel.getFile()))
                                .signature(new ObjectKey(storyModel.getFile()))
                                .dontAnimate()
                                .centerCrop()
                                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE)
                                .apply(options)
                                .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                                .into(message_file_thumbnail);
                        break;

                    default:
                        message_file_thumbnail.setVisibility(View.GONE);
                        break;
                }
            } else {
                short_message.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
                message_file_thumbnail.setVisibility(View.GONE);
                if (storyModel.getBody() != null && !storyModel.getBody().equals("null")) {
                    short_message.setVisibility(View.VISIBLE);
                    String message = UtilsString.unescapeJava(storyModel.getBody());
                    if (message.length() > maxLength) {

                        short_message.setText(String.format("%s... ", message.substring(0, maxLength)));
                    } else {
                        short_message.setText(message);
                    }
                } else {
                    short_message.setVisibility(View.GONE);
                }
            }
        }


    }


    public void setImageFileOffline(MessageModel messagesModel) {
        this.model=messagesModel;
        String ImageUrl = messagesModel.getFile();
        String messageId = messagesModel.get_id();
        File file = new File(ImageUrl);
        Log.d("ImageTag","Offline Called");

        //   thumbnailRequestMap.put(messagesModel.getId(), Drawable.createFromPath(ImageUrl));

        AppHelper.LogCat("ImageUrl " + ImageUrl);
        AppHelper.LogCat("ImageUrl file " + file);
        BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {


            @Override
            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                super.onResourceReady(resource, transition);
                imageFile.setImageBitmap(resource);
                AppHelper.LogCat("onResourceReady file " + resource);
            }

            @Override
            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                super.onLoadFailed(errorDrawable);
                imageFile.setImageDrawable(errorDrawable);
                AppHelper.LogCat("onLoadFailed file ");
            }


            @Override
            public void onLoadStarted(Drawable placeHolderDrawable) {
                super.onLoadStarted(placeHolderDrawable);
                imageFile.setImageDrawable(placeHolderDrawable);
            }
        };


        //get filename from path
        String filename = ImageUrl.substring(ImageUrl.lastIndexOf("/") + 1);
        //remove extension
        if (filename.indexOf(".") > 0)
            filename = filename.substring(0, filename.lastIndexOf("."));
        messagesAdapter.glideRequests
                .asBitmap()
                .load(file)
                *//* .listener(new RequestListener<Bitmap>() {
                     @Override
                     public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Bitmap> target, boolean isFirstResource) {
                         AppHelper.LogCat("onLoadFailed GlideException " + e.getMessage());
                         return false;
                     }

                     @Override
                     public boolean onResourceReady(Bitmap resource, Object model, Target<Bitmap> target, DataSource dataSource, boolean isFirstResource) {
                         AppHelper.LogCat("onResourceReady ");
                         imageFile.setImageBitmap(resource);
                         return false;
                     }
                 })*//*.signature(new ObjectKey(filename))
                .error(R.drawable.image_holder_full_screen)
                .placeholder(R.drawable.image_holder_full_screen)
                .centerCrop()
                .dontAnimate()
                .into(target);


    }

    public void setGifFileOffline(MessageModel messagesModel) {
        String ImageUrl = messagesModel.getFile();
        String messageId = messagesModel.get_id();
        File file = new File(ImageUrl);
        AppHelper.LogCat("setGifFileOffline " + ImageUrl);
        //  thumbnailRequestMap.put(messagesModel.getId(), Drawable.createFromPath(ImageUrl));
        messagesAdapter.glideRequests
                .asBitmap()
                //.dontAnimate()
                .load(file)
                .signature(new ObjectKey(ImageUrl))
                .apply(new RequestOptions().centerCrop())
                .error(R.drawable.image_holder_full_screen)
                .placeholder(R.drawable.image_holder_full_screen)
                .into(imageFile);


    }

    public void setGifFile(MessageModel messagesModel) {
        String ImageUrl = messagesModel.getFile();
        String senderId = messagesModel.getSender().get_id();
        String messageId = messagesModel.get_id();
        boolean isDownLoad = messagesModel.isFile_downLoad();

        RequestBuilder<Drawable> thumbnailRequest = messagesAdapter.glideRequests
                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE);

        if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {

            if (FilesManager.isFileGifSentExists(mActivity, FilesManager.getGif(ImageUrl))) {

                AppHelper.LogCat("exist " + FilesManager.getFileGifSent(mActivity, ImageUrl));
                *//*if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {
                    glideRequests
                            .load(FilesManager.getFileGifSent(mActivity, ImageUrl))
                            .signature(new ObjectKey(FilesManager.getFileGifSent(mActivity, ImageUrl)))
                            .thumbnail(thumbnailRequest)
                            .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                            .error(thumbnailRequestMap.get(messagesModel.getId()))


                            .into(imageFile);
                } else {*//*

                messagesAdapter.glideRequests
                        .load(FilesManager.getFileGifSent(mActivity, ImageUrl))
                        .signature(new ObjectKey(FilesManager.getFileGifSent(mActivity, ImageUrl)))
                        .thumbnail(thumbnailRequest)
                        .placeholder(new ColorDrawable(AppHelper.getColor(mActivity, R.color.colorHolder)))
                        .apply(new RequestOptions().centerCrop())
                        .into(imageFile);

                //  }
            } else {
                AppHelper.LogCat("not exist " + FilesManager.getFileGifSent(mActivity, ImageUrl));
                DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {

                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        imageFile.setImageDrawable(resource);
                    }


                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageFile.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        imageFile.setImageDrawable(placeHolderDrawable);
                    }
                };
             *//*   if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                    glideRequests
                            .load(EndPoints.MESSAGE_IMAGE_URL + ImageUrl)
                            .signature(new ObjectKey(ImageUrl))
                            .thumbnail(thumbnailRequest)
                            .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                            .error(thumbnailRequestMap.get(messagesModel.getId()))
                            .into(target);

                    ;
                } else {*//*
                messagesAdapter.glideRequests
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .thumbnail(thumbnailRequest)
                        .apply(new RequestOptions().centerCrop())
                        .error(R.drawable.image_holder_full_screen)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .into(target);

                //  }


                // }
            }

        } else {


            if (isDownLoad) {

                if (FilesManager.isFileGifExists(mActivity, FilesManager.getGif(ImageUrl))) {

                    AppHelper.LogCat("exist isDownLoad " + FilesManager.getFileGif(mActivity, ImageUrl));
                    messagesAdapter.glideRequests
                            .load(FilesManager.getFileGif(mActivity, ImageUrl))
                            .signature(new ObjectKey(FilesManager.getFileGif(mActivity, ImageUrl)))
                            .thumbnail(thumbnailRequest)
                            .apply(new RequestOptions().centerCrop())
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(imageFile);


                } else {
                    AppHelper.LogCat("not exist isDownLoad" +
                            " " + FilesManager.getFileGifSent(mActivity, ImageUrl));
                    DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {
                        @Override
                        public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageDrawable(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    messagesAdapter.glideRequests
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .thumbnail(thumbnailRequest)
                            .apply(new RequestOptions().centerCrop().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);


                }


            } else {

                downloadImage.setVisibility(View.VISIBLE);
                getFileSize(messagesModel.getFile_size(), "image");
                DrawableImageViewTarget target = new DrawableImageViewTarget(imageFile) {
                    @Override
                    public void onResourceReady(@NonNull Drawable resource, @Nullable Transition<? super Drawable> transition) {
                        super.onResourceReady(resource, transition);
                        imageFile.setImageDrawable(resource);
                    }

                    @Override
                    public void onLoadFailed(@Nullable Drawable errorDrawable) {
                        super.onLoadFailed(errorDrawable);
                        imageFile.setImageDrawable(errorDrawable);
                    }


                    @Override
                    public void onLoadStarted(Drawable placeHolderDrawable) {
                        super.onLoadStarted(placeHolderDrawable);
                        imageFile.setImageDrawable(placeHolderDrawable);
                    }
                };
                messagesAdapter.glideRequests
                        .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_GIF_URL + ImageUrl))
                        .signature(new ObjectKey(ImageUrl))
                        .thumbnail(thumbnailRequest)
                        .apply(new RequestOptions().centerCrop().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                        .error(R.drawable.image_holder_full_screen)
                        .placeholder(R.drawable.image_holder_full_screen)
                        .into(target);

            }
        }


    }

    public void setImageFile(MessageModel messagesModel) {

        this.model=messagesModel;
         String ImageUrl = messagesModel.getFile();
        String senderId = messagesModel.getSender().get_id();
        String messageId = messagesModel.get_id();
        boolean isDownLoad = messagesModel.isFile_downLoad();

        RequestBuilder<Bitmap> thumbnailRequest = messagesAdapter.glideRequests
                .asBitmap()
                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl,getApplicationContext()))
                .override(AppConstants.ROWS_IMAGE_SIZE, AppConstants.ROWS_IMAGE_SIZE);

        if (messagesModel.getLatitude() != null && !messagesModel.getLatitude().equals("null")) {


            BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {


                @Override
                public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                    super.onResourceReady(resource, transition);
                    imageFile.setImageBitmap(resource);
                    AppHelper.LogCat("Resource Ready");
                }

                @Override
                public void onLoadFailed(@Nullable Drawable errorDrawable) {
                    super.onLoadFailed(errorDrawable);
                    AppHelper.LogCat("Error Occured");
                    imageFile.setImageDrawable(errorDrawable);

                }


                @Override
                public void onLoadStarted(Drawable placeHolderDrawable) {
                    super.onLoadStarted(placeHolderDrawable);
                    imageFile.setImageDrawable(placeHolderDrawable);

                }
            };
           *//* if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                glideRequests
                        .load(EndPoints.MESSAGE_IMAGE_URL + ImageUrl)
                        .signature(new ObjectKey(ImageUrl))
                        .dontAnimate()
                        .thumbnail(thumbnailRequest)
                        .centerCrop()
                        .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                        .error(thumbnailRequestMap.get(messagesModel.getId()))
                        .centerCrop()


                        .into(target);
            } else {*//*
            messagesAdapter.glideRequests
                    .asBitmap()
                    .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl,getApplicationContext()))
                    .signature(new ObjectKey(ImageUrl))
                    .dontAnimate()
                    .thumbnail(thumbnailRequest)
                    .centerCrop()
                    .error(R.drawable.image_holder_full_screen)
                    .placeholder(R.drawable.image_holder_full_screen)
                    .centerCrop()
                    .into(target);
            //}


        } else {

            if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {


                if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(ImageUrl))) {

                    *//*if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {
                        glideRequests
                                .load(FilesManager.getFileImageSent(mActivity, ImageUrl))
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                                .error(thumbnailRequestMap.get(messagesModel.getId()))


                                .centerCrop()

                                .into(imageFile);
                    } else {*//*

                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(FilesManager.getFileImageSent(mActivity, ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .centerCrop()
                            .into(imageFile);
                    //  }
                } else {

                    BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {


                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };

                   *//* if (thumbnailRequestMap.size() != 0 && thumbnailRequestMap.get(messagesModel.getId()) != null) {

                        glideRequests
                                .load(EndPoints.MESSAGE_IMAGE_URL + ImageUrl)
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                                .placeholder(thumbnailRequestMap.get(messagesModel.getId()))
                                .error(thumbnailRequestMap.get(messagesModel.getId())).centerCrop()


                                .into(target);
                    } else {*//*
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);
                }


            } else {

                if (isDownLoad) {

                    if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(ImageUrl))) {

                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(FilesManager.getFileImage(mActivity, ImageUrl))
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .error(R.drawable.image_holder_full_screen)
                                .placeholder(R.drawable.image_holder_full_screen)
                                .into(imageFile);

                    } else {

                        BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                                super.onResourceReady(resource, transition);
                                imageFile.setImageBitmap(resource);
                            }

                            @Override
                            public void onLoadFailed(@Nullable Drawable errorDrawable) {
                                super.onLoadFailed(errorDrawable);
                                imageFile.setImageDrawable(errorDrawable);
                            }


                            @Override
                            public void onLoadStarted(Drawable placeHolderDrawable) {
                                super.onLoadStarted(placeHolderDrawable);
                                imageFile.setImageDrawable(placeHolderDrawable);
                            }
                        };
                        messagesAdapter.glideRequests
                                .asBitmap()
                                .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl))
                                .signature(new ObjectKey(ImageUrl))
                                .dontAnimate()
                                .thumbnail(thumbnailRequest)
                                .centerCrop()
                                .error(R.drawable.image_holder_full_screen)
                                .placeholder(R.drawable.image_holder_full_screen)
                                .into(target);

                    }


                } else {


                    downloadImage.setVisibility(View.VISIBLE);
                    getFileSize(messagesModel.getFile_size(), "image");


                    BitmapImageViewTarget target = new BitmapImageViewTarget(imageFile) {
                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {
                            super.onResourceReady(resource, transition);
                            imageFile.setImageBitmap(resource);
                        }

                        @Override
                        public void onLoadFailed(@Nullable Drawable errorDrawable) {
                            super.onLoadFailed(errorDrawable);
                            imageFile.setImageDrawable(errorDrawable);
                        }


                        @Override
                        public void onLoadStarted(Drawable placeHolderDrawable) {
                            super.onLoadStarted(placeHolderDrawable);
                            imageFile.setImageDrawable(placeHolderDrawable);
                        }
                    };
                    messagesAdapter.glideRequests
                            .asBitmap()
                            .load(GlideUrlHeaders.getUrlWithHeaders(EndPoints.MESSAGE_IMAGE_URL + ImageUrl,getApplicationContext()))
                            .signature(new ObjectKey(ImageUrl))
                            .dontAnimate()
                            .thumbnail(thumbnailRequest)
                            .centerCrop()
                            .apply(new RequestOptions().transform(new BlurTransformation(AppConstants.BLUR_RADIUS)))
                            .error(R.drawable.image_holder_full_screen)
                            .placeholder(R.drawable.image_holder_full_screen)
                            .into(target);
                }
            }
        }
    }


    private void getFileSize(String size, String type) {
        try {
            long filesSize = Long.parseLong(size);
            switch (type) {
                case "image":
                    fileSizeImage.setVisibility(View.VISIBLE);
                    fileSizeImage.setText(String.valueOf(FilesManager.getFileSize(filesSize)));
                    break;
            }
        } catch (Exception e) {
            AppHelper.LogCat(" MessagesAdapter " + e.getMessage());
        }


    }

    @Override
    public void onClick(View view) {
        if (!messagesAdapter.isActivated) {
            MessageModel messagesModel = messagesAdapter.getItem(getAdapterPosition());

            String senderId = messagesModel.getSender().get_id();
            String messageId = messagesModel.getSender().get_id();
           // boolean isDownLoad = messagesModel.isFile_downLoad();
            switch (view.getId()) {


                case R.id.image_file:
                    if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null")) {
                        if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF))
                            return;
                        if (senderId.equals(PreferenceManager.getInstance().getID(mActivity))) {
                            if (messagesModel.isFile_upload()) {
                                showImage(messagesModel);
                            }
                        } else {
                                if (messagesModel.isFile_downLoad()) {
                                showImage(messagesModel);
                            }
                        }
                    }

                    break;
                case R.id.cancel_download_image:
                    PendingFilesTask.removeFile(messageId, false, true);
                    break;

                case R.id.download_image:
                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run
                       // PendingFilesTask.initDownloadListener(messageId, messagesAdapter);
                    }
                    break;
                case R.id.cancel_upload_image:
                    break;

                case R.id.retry_upload_image:
                    if (Permissions.hasAny(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                        // Create the task, set the listener, add to the task controller, and run

                    //    APIHelper.initializeUploadFiles().uploadImageFile(createMultipartBody(messagesModel.getFile(), "image/*", "image"));

                        PendingFilesTask.initUploadListener(messageId, messagesAdapter);

                    }
                    break;


                case R.id.replied_message_view:

                    if (messagesModel.isReply_message()) {
                        if (!MessagesController.getInstance().checkIfMessageExist(messagesModel.getReply_id()))
                            return;
                        messagesAdapter.scrollToItem(messagesModel.getReply_id());
                    } else {
                        if (!StoriesController.getInstance().checkIfSingleStoryExist(messagesModel.getReply_id()))
                            return;
                        StoryModel storyModel = StoriesController.getInstance().getStoryById(messagesModel.getReply_id());
                        int currentStoryPosition;

                        if (storyModel.getUserId().equals(PreferenceManager.getInstance().getID(mActivity))) {

                            List<StoryModel> storiesModels = StoriesController.getInstance().getStoriesHeaderById(storyModel.getUserId());
                            if (storiesModels.size() == 0) return;
                            AppHelper.LogCat("storiesModels.size() " + storiesModels.size());
                            currentStoryPosition = storiesModels.indexOf(storyModel);
                        } else {

                            List<StoryModel> storiesModels = StoriesController.getInstance().getStoriesById(storyModel.getUserId());
                            if (storiesModels.size() == 0) return;
                            currentStoryPosition = storiesModels.indexOf(storyModel);
                        }

                        AppHelper.LogCat("currentStoryPosition " + currentStoryPosition);
                        Intent a = new Intent(itemView.getContext(), StoriesDetailsActivity.class);
                        a.putExtra("position", 0);
                        a.putExtra("currentStoryPosition", currentStoryPosition);
                        a.putExtra("storyId", storyModel.getUserId());
                        itemView.getContext().startActivity(a);
                    }
                    break;
            }
        }
    }

    public void setBlinkEffect() {
        AnimationsUtil.manageBlinkEffect(message_layout);
    }

    public void setListeners(final MessageModel messageModel) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);
        try {
            if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (!messageModel.isFile_upload()) {
                    if (PendingFilesTask.containsFile(messageModel.get_id())) {
                        PendingFilesTask.updateUploadListener(messagesAdapter);
                    }
                }
            } else {
                if (!messageModel.isFile_downLoad()) {
                    if (PendingFilesTask.containsFile(messageModel.get_id())) {
                        PendingFilesTask.updateDownloadListener(messagesAdapter);
                    }
                }
            }

        } finally {
            if (!realm.isClosed())
                realm.close();
        }

    }


    public void setSenderName(String SendName) {
        senderName.setText(SendName);
    }

    public void setSenderColor(int Sendcolor) {
        senderName.setTextColor(Sendcolor);
    }

    public void hideSenderName() {
        senderName.setVisibility(View.GONE);
    }

    public void showSenderName() {
        senderName.setVisibility(View.VISIBLE);
    }

    public void hideSent() {
        statusMessages.setVisibility(View.GONE);
    }

    public void showSent(int status) {
        statusMessages.setVisibility(View.VISIBLE);
        switch (status) {
            case AppConstants.IS_WAITING:
                statusMessages.setImageResource(R.drawable.ic_access_time_gray_24dp);
                break;
            case AppConstants.IS_SENT:
                if (messagesAdapter.isStatusUpdated) {
                    AppHelper.playSound(mActivity, "audio/message_is_sent.m4a");
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_gray_24dp);

                break;
            case AppConstants.IS_DELIVERED:
                if (messagesAdapter.isStatusUpdated) {
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_all_gray_24dp);
                break;
            case AppConstants.IS_SEEN:
                if (messagesAdapter.isStatusUpdated) {
                    AnimationsUtil.rotationY(statusMessages);
                    messagesAdapter.isStatusUpdated = false;
                }
                statusMessages.setImageResource(R.drawable.ic_done_all_blue_24dp);
                break;

        }

    }



    public void setHeaderDate(long now_tm, long msg_tm, DateTime time) {
        Date nowDate = new Date();
        nowDate.setTime(now_tm);
        Date msgDate = new Date();
        msgDate.setTime(msg_tm);
        Calendar now_calendar = Calendar.getInstance();
        now_calendar.setTimeInMillis(now_tm);
        Calendar msg_calendar = Calendar.getInstance();
        msg_calendar.setTimeInMillis(msg_tm);

        if (now_tm == msg_tm) {
            date_general_message.setVisibility(View.VISIBLE);
            date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
        } else if (msg_tm == 0) {
            date_general_message.setVisibility(View.VISIBLE);
            date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
        } else {
            if (msgDate.before(nowDate)) {

                boolean sameDay = now_calendar.get(Calendar.YEAR) == msg_calendar.get(Calendar.YEAR) &&
                        now_calendar.get(Calendar.MONTH) == msg_calendar.get(Calendar.MONTH)
                        && now_calendar.get(Calendar.DAY_OF_MONTH) == msg_calendar.get(Calendar.DAY_OF_MONTH);
                if (sameDay) {
                    date_general_message.setVisibility(View.GONE);
                    date_general_message.setText("");
                } else {
                    date_general_message.setVisibility(View.VISIBLE);
                    date_general_message.setText(UtilsTime.convertDateToStringHeader(mActivity, time));
                }
            } else {
                date_general_message.setVisibility(View.GONE);
                date_general_message.setText("");
            }
        }


    }

    @SuppressLint("CheckResult")
    public void setDate(String Date) {
        date.setText(UtilsTime.convertMessageDateToStringFormat(mActivity, UtilsTime.getCorrectDate(Date)));
    }


    private void showImage(MessageModel messagesModel) {
        String imageUrl = messagesModel.getFile();

        if (messagesModel.getLongitude() != null && !messagesModel.getLongitude().equals("null")) {
            Permissions.with(mActivity)
                    .request(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)
                    .ifNecessary()
                    .withPermanentDenialDialog(mActivity.getString(R.string.location_permission_message))
                    .onAllGranted(() -> {
                          if (messagesModel.getLatitude() == null) return;
                      *//*  Intent location = new Intent(mActivity, LocationActivity.class);
                        location.putExtra("userId", messagesModel.getSender().get_id());
                        location.putExtra("lat", Double.parseDouble(messagesModel.getLatitude()));
                        location.putExtra("long", Double.parseDouble(messagesModel.getLongitude()));
                        mActivity.startActivity(location);*//*
                    })
                    .execute();
        } else {
              if (imageUrl == null || messagesModel.getFile().equals("null")) return;

            if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (messagesModel.isFile_upload()) {
                    if (FilesManager.isFileImagesSentExists(mActivity, FilesManager.getImage(imageUrl))) {
                        AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.SENT_IMAGE, imageUrl, messagesModel.getSender().get_id());

                    } else {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_image_is_not_exist));
                    }

                }

            } else {

                if (messagesModel.isFile_downLoad()) {

                    if (FilesManager.isFileImagesExists(mActivity, FilesManager.getImage(imageUrl))) {
                        AppHelper.LaunchImagePreviewActivity(mActivity, AppConstants.RECEIVED_IMAGE, imageUrl, messagesModel.getSender().get_id());

                    } else {
                        AppHelper.CustomToast(mActivity, mActivity.getString(R.string.this_image_is_not_exist));
                    }
                }

            }
        }

    }

    public void setHideUpload() {
        mProgressUploadImage.setVisibility(View.GONE);
        mProgressUploadImageInitial.setVisibility(View.GONE);
        cancelUploadImage.setVisibility(View.GONE);
        retryUploadImage.setVisibility(View.GONE);
    }

    public void setShowUpload(MessageModel messageModel) {
        mProgressUploadImage.setVisibility(View.GONE);
        mProgressUploadImageInitial.setVisibility(View.GONE);
        cancelUploadImage.setVisibility(View.GONE);
        if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(getApplicationContext()))){
        retryUploadImage.setVisibility(View.VISIBLE);
        }
        else {
            downloadImage.setVisibility(View.VISIBLE);
        }
        if (messageModel.getLongitude()!=null){
            retryUploadImage.setVisibility(View.GONE);
            downloadImage.setVisibility(View.GONE);
        }
    }

    //methods for upload process
    public void onUploadUpdate(int percentage, String type) {

        switch (type) {

            case "gif":
            case "image":
                if (isUploadServiceStopped) return;

                mProgressUploadImage.setVisibility(View.VISIBLE);
                cancelUploadImage.setVisibility(View.VISIBLE);
                mProgressUploadImageInitial.setVisibility(View.GONE);
                retryUploadImage.setVisibility(View.GONE);
                mProgressUploadImage.setIndeterminate(false);
                mProgressUploadImage.setProgress(percentage);
                break;
        }
    }

    public void onUploadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        AppHelper.LogCat("on error " + type);
        switch (type) {
            case "gif":
            case "image":
                isUploadServiceStopped = true;
                mProgressUploadImage.setVisibility(View.GONE);
                mProgressUploadImageInitial.setVisibility(View.GONE);
                cancelUploadImage.setVisibility(View.GONE);
                retryUploadImage.setVisibility(View.VISIBLE);
                break;
        }
    }

    public void onUploadFinish(String type, MessageModel messagesModel) {

        switch (type) {
            case "gif":
            case "image":
                isUploadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, false);
                mProgressUploadImage.setVisibility(View.GONE);
                mProgressUploadImageInitial.setVisibility(View.GONE);
                cancelUploadImage.setVisibility(View.GONE);
                retryUploadImage.setVisibility(View.GONE);
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_UPLOAD_MESSAGE_FILES, messagesModel));
                break;

        }
    }

    public void onUploadStart(String type) {
        switch (type) {

            case "gif":
            case "image":
                isUploadServiceStopped = false;
                retryUploadImage.setVisibility(View.GONE);
                setupProgressBarUploadImage();
                mProgressUploadImageInitial.setVisibility(View.VISIBLE);
                cancelUploadImage.setVisibility(View.VISIBLE);
                mProgressUploadImageInitial.setIndeterminate(true);
                break;

        }
    }

    //end methods for upload process
    //start methods for download process

    public void onDownloadStart(String type) {
        switch (type) {

            case "gif":
            case "image":
                isDownloadServiceStopped = false;
                setupProgressBarDownloadImage();
                mProgressDownloadImageInitial.setVisibility(View.VISIBLE);
                cancelDownloadImage.setVisibility(View.VISIBLE);
                downloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setIndeterminate(true);

                break;

        }
    }

    public void onDownloadUpdate(int percentage, String type) {
        switch (type) {
            case "gif":
            case "image":
                if (isDownloadServiceStopped) return;
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                mProgressDownloadImage.setVisibility(View.VISIBLE);
                cancelDownloadImage.setVisibility(View.VISIBLE);

                downloadImage.setVisibility(View.GONE);
                mProgressDownloadImage.setIndeterminate(false);
                mProgressDownloadImage.setProgress(percentage);
                break;

        }


    }


    public void onDownloadError(String type) {

        if (AppHelper.isActivityRunning(mActivity, "activities.messages.MessagesActivity"))
            AppHelper.CustomToast(mActivity, mActivity.getString(R.string.oops_something));
        switch (type) {
            case "gif":
            case "image":
                isDownloadServiceStopped = true;
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.VISIBLE);
                break;


        }
    }


    public void onDownloadFinish(String type, MessageModel messagesModel) {
        switch (type) {
            case "image":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.GONE);
                setImageFile(messagesModel);
                break;

            case "gif":
                isDownloadServiceStopped = true;
                PendingFilesTask.removeFile(messagesModel.get_id(), true, true);
                mProgressDownloadImage.setVisibility(View.GONE);
                mProgressDownloadImageInitial.setVisibility(View.GONE);
                cancelDownloadImage.setVisibility(View.GONE);
                downloadImage.setVisibility(View.GONE);
                setGifFile(messagesModel);
                break;
        }
    }





    private MultipartBody.Part createMultipartBody(String filePath, String mimeType, String mType) {
        File file = new File(filePath);
        return MultipartBody.Part.createFormData("file", file.getName(), createCountingRequestBody(file, mimeType, mType));
    }
    private RequestBody createRequestBody(File file, String mimeType) {
        return RequestBody.create(MediaType.parse(mimeType), file);
    }
    private RequestBody createCountingRequestBody(File file, String mimeType, String mType) {
        RequestBody requestBody = createRequestBody(file, mimeType);
        //  private CountDownLatch latch;
        return new UploadProgressRequestBody(requestBody, (bytesWritten, contentLength) -> {
            double progress = (100 * bytesWritten) / contentLength;

            if (mWaitingListenerWeakReference != null) {
                UploadCallbacks listener = mWaitingListenerWeakReference.get();
                if (listener != null) {
                    listener.onUpdate((int) progress, mType, model.get_id());
                    NotificationsManager.getInstance().updateUpDownNotification(getApplicationContext(), model.get_id(), (int) progress);
                }
            }
        }, model.get_id());
    }

    @Override
    public void onProgressUpdate(int percentage) {
     mProgressUploadImage.setVisibility(View.VISIBLE);
    }

    @Override
    public void onError() {

    }

    @Override
    public void onFinish() {
     mProgressUploadImage.setVisibility(View.GONE);
    }

    @Override
    public void update(long bytesRead, long contentLength, boolean done) {

    }*/
