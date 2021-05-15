package com.pefgloble.pefchate.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.bumptech.glide.RequestManager;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.util.Util;
import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.JsonClasses.messags.UpdateItem;
import com.pefgloble.pefchate.JsonClasses.messags.UpdateMessageModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.MessageDownloadInfo;
import com.pefgloble.pefchate.JsonClasses.otherClasses.MessageUploadInfo;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.activities.settings.PreferenceSettingsManager;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.app.interfaces.UploadCallbacks;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsPhone;
import com.pefgloble.pefchate.helpers.UtilsString;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.interfaces.DownloadCallbacks;
import com.pefgloble.pefchate.jobs.PendingFilesTask;
import com.pefgloble.pefchate.presenter.MessagesController;
import com.pefgloble.pefchate.ui.ColorGenerator;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import io.realm.Realm;
import io.realm.RealmList;


/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */

@SuppressLint("StaticFieldLeak")
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements Handler.Callback, UploadCallbacks, DownloadCallbacks {

    public RealmList<MessageModel> mMessagesModel;
    //for outgoing messages
    private static final int OUTGOING_MESSAGES = 1;
    private static final int OUTGOING_MESSAGES_IMAGE = 2;
    private static final int OUTGOING_MESSAGES_DOCUMENT = 3;
    private static final int OUTGOING_MESSAGES_AUDIO = 4;
    private static final int OUTGOING_MESSAGES_VIDEO = 5;
    //for incoming messages
    private static final int INCOMING_MESSAGES = 6;
    private static final int INCOMING_MESSAGES_IMAGE = 7;
    private static final int INCOMING_MESSAGES_DOCUMENT = 8;
    private static final int INCOMING_MESSAGES_AUDIO = 9;
    private static final int INCOMING_MESSAGES_VIDEO = 10;

    private static final int GROUP_STATES_MESSAGES = 11;
    public MessagesAudioViewHolder mMessagesAudioViewHolder;
    public RecyclerView.ViewHolder generalHolder;
    //for player audio

    public boolean isPlaying = false;
    private Player.DefaultEventListener defaultEventListener;
    private SimpleExoPlayer player;
    private Activity mActivity;
    private ExtractorsFactory extractorsFactory;
    private TrackSelector trackSelector;
    private DataSource.Factory dataSourceFactory;


    private static final int MSG_UPDATE_SEEK_BAR = 1845;
    private Handler uiUpdateHandler;
    public int playingPosition;
    //for player audio

    private String SearchQuery;
    private SparseBooleanArray selectedItems;
    public boolean isStatusUpdated = false;
    public boolean isActivated = false;
    /// private SparseArray<Drawable> thumbnailRequestMap = new SparseArray<>();
    public RequestManager glideRequests;
    public RecyclerView messagesList;

    public MessagesAdapter(@NonNull RequestManager glideRequests, Activity mActivity, RecyclerView messagesList) {
        this.mMessagesModel = new RealmList<>();
        this.mActivity = mActivity;
        this.selectedItems = new SparseBooleanArray();
        this.glideRequests = glideRequests;
        this.messagesList = messagesList;
        //for audio
        this.playingPosition = -1;
        uiUpdateHandler = new Handler(this);
        BandwidthMeter bandwidthMeter = new DefaultBandwidthMeter();
        extractorsFactory = new DefaultExtractorsFactory();
        AdaptiveTrackSelection.Factory trackSelectionFactory = new AdaptiveTrackSelection.Factory();
        trackSelector = new DefaultTrackSelector(trackSelectionFactory);
        DefaultBandwidthMeter defaultBandwidthMeter = new DefaultBandwidthMeter();
        dataSourceFactory = new DefaultDataSourceFactory(mActivity, Util.getUserAgent(mActivity, mActivity.getString(R.string.app_name)), defaultBandwidthMeter);
        //for audio

    }

    public void setPlayer(String AudioDataSource) {

        MediaSource mediaSource = new ExtractorMediaSource.Factory(dataSourceFactory)
                .setExtractorsFactory(extractorsFactory)
                .createMediaSource(Uri.parse(AudioDataSource));
        player = ExoPlayerFactory.newSimpleInstance(mActivity, trackSelector);
        player.prepare(mediaSource);

    }

    public SimpleExoPlayer getPlayer() {
        return player;
    }


    public void setMessages(RealmList<MessageModel> messagesModelList) {
        this.mMessagesModel = messagesModelList;
        notifyDataSetChanged();
    }

    public RealmList<MessageModel> getMessages() {
        return this.mMessagesModel;

    }
/*
    @Override
    public void onViewAttachedToWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        AppHelper.LogCat("holder " + holder.getAdapterPosition());
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull RecyclerView.ViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
    }*/

    public void addMessage(MessageModel messageModel) {
        this.mMessagesModel.add(messageModel);
         notifyItemInserted(mMessagesModel.size() - 1);
              if (messageModel.getFile() != null && !messageModel.getFile().equals("null")) {
                  if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                      if (!messageModel.isFile_upload()) {
                          PendingFilesTask.initUploadListener(messageModel.get_id(), MessagesAdapter.this);
                      }
                  }
              }
       /*      if (messageModel.getFile() != null && !messageModel.getFile().equals("null")) {
            if (messageModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (!messageModel.isFile_upload()) {
                    PendingFilesTask.initUploadListener(messageModel.get_id(), MessagesAdapter.this);
                }
            } else {
                if (!messageModel.isFile_downLoad()) {
                    PendingFilesTask.initDownloadListener(messageModel.get_id(), MessagesAdapter.this);
                }
            }
        }*/
    }



    //Methods for search start
    public void setString(String SearchQuery) {
        this.SearchQuery = SearchQuery;
        notifyDataSetChanged();
    }

    public void animateTo(RealmList<MessageModel> models) {
        applyAndAnimateRemovals(models);
        applyAndAnimateAdditions(models);
        applyAndAnimateMovedItems(models);
    }

    private void applyAndAnimateRemovals(RealmList<MessageModel> newModels) {
        int arraySize = mMessagesModel.size();
        for (int i = arraySize - 1; i >= 0; i--) {
            final MessageModel model = getItem(i);
            if (!newModels.contains(model)) {
                removeItem(i);
            }
        }
    }

    private void applyAndAnimateAdditions(RealmList<MessageModel> newModels) {
        int arraySize = newModels.size();
        for (int i = 0; i < arraySize; i++) {
            final MessageModel model = newModels.get(i);
            if (!mMessagesModel.contains(model)) {
                addItem(i, model);
            }
        }
    }

    private void applyAndAnimateMovedItems(RealmList<MessageModel> newModels) {
        int arraySize = newModels.size();
        for (int toPosition = arraySize - 1; toPosition >= 0; toPosition--) {
            final MessageModel model = newModels.get(toPosition);
            final int fromPosition = mMessagesModel.indexOf(model);
            if (fromPosition >= 0 && fromPosition != toPosition) {
                moveItem(fromPosition, toPosition);
            }
        }
    }

    private MessageModel removeItem(int position) {
        final MessageModel model = mMessagesModel.remove(position);
        notifyItemRemoved(position);
        return model;
    }

    private void addItem(int position, MessageModel model) {
        mMessagesModel.add(position, model);
        notifyItemInserted(position);
    }

    private void moveItem(int fromPosition, int toPosition) {
        final MessageModel model = mMessagesModel.remove(fromPosition);
        mMessagesModel.add(toPosition, model);
        notifyItemMoved(fromPosition, toPosition);
    }
    //Methods for search end

    @Override
    public int getItemViewType(int position) {
        try {
            MessageModel messagesModel = getItem(position);

            Log.d("MyTag","Get Item VIew Type Called");

            if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                if (messagesModel.getState().equals(AppConstants.CREATE_STATE)
                        || messagesModel.getState().equals(AppConstants.LEFT_STATE)
                        || messagesModel.getState().equals(AppConstants.ADD_STATE)
                        || messagesModel.getState().equals(AppConstants.REMOVE_STATE)
                        || messagesModel.getState().equals(AppConstants.ADMIN_STATE)
                        || messagesModel.getState().equals(AppConstants.MEMBER_STATE)
                        || messagesModel.getState().equals(AppConstants.EDITED_STATE)
                        ) {
                    return GROUP_STATES_MESSAGES;
                } else {
                    if (messagesModel.getFile_type() != null) {

                        switch (messagesModel.getFile_type()) {
                            case AppConstants.MESSAGES_IMAGE:
                                return OUTGOING_MESSAGES_IMAGE;
                            case AppConstants.MESSAGES_GIF:
                                return OUTGOING_MESSAGES_IMAGE;
                            case AppConstants.MESSAGES_AUDIO:
                                return OUTGOING_MESSAGES_AUDIO;
                            case AppConstants.MESSAGES_DOCUMENT:
                                return OUTGOING_MESSAGES_DOCUMENT;
                            case AppConstants.MESSAGES_VIDEO:
                                return OUTGOING_MESSAGES_VIDEO;
                            default:
                                return OUTGOING_MESSAGES;
                        }
                    } else {
                        return OUTGOING_MESSAGES;
                    }
                }
            } else {
                if (messagesModel.getState().equals(AppConstants.CREATE_STATE)
                        || messagesModel.getState().equals(AppConstants.LEFT_STATE)
                        || messagesModel.getState().equals(AppConstants.ADD_STATE)
                        || messagesModel.getState().equals(AppConstants.REMOVE_STATE)
                        || messagesModel.getState().equals(AppConstants.ADMIN_STATE)
                        || messagesModel.getState().equals(AppConstants.MEMBER_STATE)
                        || messagesModel.getState().equals(AppConstants.EDITED_STATE)
                        ) {
                    return GROUP_STATES_MESSAGES;
                } else {
                    if (messagesModel.getFile_type() != null && !messagesModel.getFile_type().equals("null")) {
                        switch (messagesModel.getFile_type()) {
                            case AppConstants.MESSAGES_IMAGE:
                                return INCOMING_MESSAGES_IMAGE;
                            case AppConstants.MESSAGES_GIF:
                                return INCOMING_MESSAGES_IMAGE;
                            case AppConstants.MESSAGES_AUDIO:
                                return INCOMING_MESSAGES_AUDIO;
                            case AppConstants.MESSAGES_DOCUMENT:
                                return INCOMING_MESSAGES_DOCUMENT;
                            case AppConstants.MESSAGES_VIDEO:
                                return INCOMING_MESSAGES_VIDEO;
                            default:
                                return INCOMING_MESSAGES;
                        }

                    } else {
                        return INCOMING_MESSAGES;
                    }
                }

            }

        } catch (Exception e) {
            AppHelper.LogCat("kdoub rminin Exception" + e.getMessage());
            return 0;
        }


    }


    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context mActivity = parent.getContext();
        View view;
        if (viewType == INCOMING_MESSAGES) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_left, parent, false);
            return new MessagesViewHolder(this, view);
        } else if (viewType == INCOMING_MESSAGES_IMAGE) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_left_message_image_layout, parent, false);
            return new MessagesImageViewHolder(this, view);
        } else if (viewType == INCOMING_MESSAGES_VIDEO) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_left_message_video_layout, parent, false);
            return new MessagesVideoViewHolder(this, view);
        } else if (viewType == INCOMING_MESSAGES_AUDIO) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_left_message_audio_layout, parent, false);
            return new MessagesAudioViewHolder(this, view);

        } else if (viewType == INCOMING_MESSAGES_DOCUMENT) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_left_message_document_layout, parent, false);
            return new MessagesDocumentViewHolder(this, view);
        } else if (viewType == OUTGOING_MESSAGES) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right, parent, false);
            return new MessagesViewHolder(this, view);
        } else if (viewType == OUTGOING_MESSAGES_IMAGE) {
          view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right_message_image_layout, parent, false);
          return new MessagesImageViewHolder(this,view);
        } else if (viewType == OUTGOING_MESSAGES_VIDEO) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right_message_video_layout, parent, false);
            return new MessagesVideoViewHolder(this, view);
        } else if (viewType == OUTGOING_MESSAGES_AUDIO) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right_message_audio_layout, parent, false);
            return new MessagesAudioViewHolder(this, view);

        } else if (viewType == OUTGOING_MESSAGES_DOCUMENT) {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right_message_document_layout, parent, false);
            return new MessagesDocumentViewHolder(this, view);
        } else {
            view = LayoutInflater.from(mActivity).inflate(R.layout.bubble_right, parent, false);
            return new MessagesViewHolder(this, view);
        }
    }


    private UsersModel getUserInfo(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);
        UsersModel userInfo;
        try {
            userInfo = realm.where(UsersModel.class).equalTo("_id", userId).findFirst();
        } finally {
            if (!realm.isClosed()) realm.close();

        }

        return userInfo;

    }

    // Update only part of ViewHolder that you are interested in
    // Invoked before onBindViewHolder(ViewHolder holder, int position)
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position, @NonNull List<Object> payloads) {
        if (!payloads.isEmpty()) {
            AppHelper.LogCat("position " + position);
            if (payloads.get(0) instanceof MessageUploadInfo && ((MessageUploadInfo) payloads.get(0)).getType().equals("document") && holder instanceof MessagesDocumentViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageUploadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageUploadInfo) payloads.get(0)).getPercentage());
                switch (((MessageUploadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesDocumentViewHolder) holder).onUploadStart(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "update":

                        ((MessagesDocumentViewHolder) holder).onUploadUpdate(((MessageUploadInfo) payloads.get(0)).getPercentage(), ((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesDocumentViewHolder) holder).onUploadError(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesDocumentViewHolder) holder).onUploadFinish(((MessageUploadInfo) payloads.get(0)).getType(), ((MessageUploadInfo) payloads.get(0)).getMessageModel());
                        break;
                }

            } else if (payloads.get(0) instanceof MessageUploadInfo && ((MessageUploadInfo) payloads.get(0)).getType().equals("image") && holder instanceof MessagesImageViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageUploadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageUploadInfo) payloads.get(0)).getPercentage());
                switch (((MessageUploadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesImageViewHolder) holder).onUploadStart(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesImageViewHolder) holder).onUploadUpdate(((MessageUploadInfo) payloads.get(0)).getPercentage(), ((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesImageViewHolder) holder).onUploadError(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesImageViewHolder) holder).onUploadFinish(((MessageUploadInfo) payloads.get(0)).getType(), ((MessageUploadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageUploadInfo && ((MessageUploadInfo) payloads.get(0)).getType().equals("gif") && holder instanceof MessagesImageViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageUploadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageUploadInfo) payloads.get(0)).getPercentage());
                switch (((MessageUploadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesImageViewHolder) holder).onUploadStart(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesImageViewHolder) holder).onUploadUpdate(((MessageUploadInfo) payloads.get(0)).getPercentage(), ((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesImageViewHolder) holder).onUploadError(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesImageViewHolder) holder).onUploadFinish(((MessageUploadInfo) payloads.get(0)).getType(), ((MessageUploadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageUploadInfo && ((MessageUploadInfo) payloads.get(0)).getType().equals("audio") && holder instanceof MessagesAudioViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageUploadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageUploadInfo) payloads.get(0)).getPercentage());
                switch (((MessageUploadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesAudioViewHolder) holder).onUploadStart(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesAudioViewHolder) holder).onUploadUpdate(((MessageUploadInfo) payloads.get(0)).getPercentage(), ((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesAudioViewHolder) holder).onUploadError(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesAudioViewHolder) holder).onUploadFinish(((MessageUploadInfo) payloads.get(0)).getType(), ((MessageUploadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageUploadInfo && ((MessageUploadInfo) payloads.get(0)).getType().equals("video") && holder instanceof MessagesVideoViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageUploadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageUploadInfo) payloads.get(0)).getPercentage());
                switch (((MessageUploadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesVideoViewHolder) holder).onUploadStart(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesVideoViewHolder) holder).onUploadUpdate(((MessageUploadInfo) payloads.get(0)).getPercentage(), ((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesVideoViewHolder) holder).onUploadError(((MessageUploadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesVideoViewHolder) holder).onUploadFinish(((MessageUploadInfo) payloads.get(0)).getType(), ((MessageUploadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageDownloadInfo && ((MessageDownloadInfo) payloads.get(0)).getType().equals("document") && holder instanceof MessagesDocumentViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageDownloadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageDownloadInfo) payloads.get(0)).getPercentage());
                switch (((MessageDownloadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesDocumentViewHolder) holder).onDownloadStart(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "update":

                        ((MessagesDocumentViewHolder) holder).onDownloadUpdate(((MessageDownloadInfo) payloads.get(0)).getPercentage(), ((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesDocumentViewHolder) holder).onDownloadError(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesDocumentViewHolder) holder).onDownloadFinish(((MessageDownloadInfo) payloads.get(0)).getType(), ((MessageDownloadInfo) payloads.get(0)).getMessageModel());
                        break;
                }

            } else if (payloads.get(0) instanceof MessageDownloadInfo && ((MessageDownloadInfo) payloads.get(0)).getType().equals("image") && holder instanceof MessagesImageViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageDownloadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageDownloadInfo) payloads.get(0)).getPercentage());
                switch (((MessageDownloadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesImageViewHolder) holder).onDownloadStart(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesImageViewHolder) holder).onDownloadUpdate(((MessageDownloadInfo) payloads.get(0)).getPercentage(), ((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesImageViewHolder) holder).onDownloadError(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesImageViewHolder) holder).onDownloadFinish(((MessageDownloadInfo) payloads.get(0)).getType(), ((MessageDownloadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageDownloadInfo && ((MessageDownloadInfo) payloads.get(0)).getType().equals("gif") && holder instanceof MessagesImageViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageDownloadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageDownloadInfo) payloads.get(0)).getPercentage());
                switch (((MessageDownloadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesImageViewHolder) holder).onDownloadStart(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesImageViewHolder) holder).onDownloadUpdate(((MessageDownloadInfo) payloads.get(0)).getPercentage(), ((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesImageViewHolder) holder).onDownloadError(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesImageViewHolder) holder).onDownloadFinish(((MessageDownloadInfo) payloads.get(0)).getType(), ((MessageDownloadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageDownloadInfo && ((MessageDownloadInfo) payloads.get(0)).getType().equals("audio") && holder instanceof MessagesAudioViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageDownloadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageDownloadInfo) payloads.get(0)).getPercentage());
                switch (((MessageDownloadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesAudioViewHolder) holder).onDownloadStart(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesAudioViewHolder) holder).onDownloadUpdate(((MessageDownloadInfo) payloads.get(0)).getPercentage(), ((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesAudioViewHolder) holder).onDownloadError(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesAudioViewHolder) holder).onDownloadFinish(((MessageDownloadInfo) payloads.get(0)).getType(), ((MessageDownloadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof MessageDownloadInfo && ((MessageDownloadInfo) payloads.get(0)).getType().equals("video") && holder instanceof MessagesVideoViewHolder) {
                AppHelper.LogCat("position " + position + " " + ((MessageDownloadInfo) payloads.get(0)).getStatus() + " perc " + ((MessageDownloadInfo) payloads.get(0)).getPercentage());
                switch (((MessageDownloadInfo) payloads.get(0)).getStatus()) {
                    case "start":
                        ((MessagesVideoViewHolder) holder).onDownloadStart(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "update":
                        ((MessagesVideoViewHolder) holder).onDownloadUpdate(((MessageDownloadInfo) payloads.get(0)).getPercentage(), ((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "error":
                        ((MessagesVideoViewHolder) holder).onDownloadError(((MessageDownloadInfo) payloads.get(0)).getType());
                        break;
                    case "finish":
                        ((MessagesVideoViewHolder) holder).onDownloadFinish(((MessageDownloadInfo) payloads.get(0)).getType(), ((MessageDownloadInfo) payloads.get(0)).getMessageModel());
                        break;
                }
            } else if (payloads.get(0) instanceof UpdateItem
                    && ((UpdateItem) payloads.get(0)).getAction().equals("scrollToMessage")
                    && ((UpdateItem) payloads.get(0)).getType().equals("message")
                    && holder instanceof MessagesViewHolder) {

                ((MessagesViewHolder) holder).setBlinkEffect();

            } else if (payloads.get(0) instanceof UpdateItem
                    && ((UpdateItem) payloads.get(0)).getAction().equals("scrollToMessage")
                    && ((UpdateItem) payloads.get(0)).getType().equals(AppConstants.MESSAGES_VIDEO)
                    && holder instanceof MessagesVideoViewHolder) {

                ((MessagesVideoViewHolder) holder).setBlinkEffect();

            } else if (payloads.get(0) instanceof UpdateItem
                    && ((UpdateItem) payloads.get(0)).getAction().equals("scrollToMessage")
                    && ((UpdateItem) payloads.get(0)).getType().equals(AppConstants.MESSAGES_AUDIO)
                    && holder instanceof MessagesAudioViewHolder) {

                ((MessagesAudioViewHolder) holder).setBlinkEffect();

            } else if (payloads.get(0) instanceof UpdateItem
                    && ((UpdateItem) payloads.get(0)).getAction().equals("scrollToMessage")
                    && ((UpdateItem) payloads.get(0)).getType().equals(AppConstants.MESSAGES_DOCUMENT)
                    && holder instanceof MessagesDocumentViewHolder) {

                ((MessagesDocumentViewHolder) holder).setBlinkEffect();

            } else if (payloads.get(0) instanceof UpdateItem
                    && ((UpdateItem) payloads.get(0)).getAction().equals("scrollToMessage")
                    && ((UpdateItem) payloads.get(0)).getType().equals(AppConstants.MESSAGES_IMAGE)
                    && holder instanceof MessagesImageViewHolder) {

                ((MessagesImageViewHolder) holder).setBlinkEffect();
            }
        } else {
            super.onBindViewHolder(holder, position, payloads);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        MessageModel messagesModel = getItem(position);
        if (holder instanceof MessagesViewHolder) {
            MessagesViewHolder mMessagesViewHolder = (MessagesViewHolder) holder;
            if (!messagesModel.isIs_group()) {
                mMessagesViewHolder.hideSenderName();
                mMessagesViewHolder.message.setVisibility(View.VISIBLE);

                String message = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }
                PreferenceManager preferenceManager = new PreferenceManager();
                mMessagesViewHolder.setDate(messagesModel.getCreated());
                if (messagesModel.getSender().get_id().equals(preferenceManager.getID(mActivity))) {
                    mMessagesViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesViewHolder.hideSent();
                }
            }else {
                PreferenceManager preferenceManager = new PreferenceManager();
                mMessagesViewHolder.setDate(messagesModel.getCreated());
                if (messagesModel.getSender().get_id().equals(preferenceManager.getID(mActivity))) {
                    mMessagesViewHolder.showSent(messagesModel.getStatus());
                    mMessagesViewHolder.hideSenderName();
                } else {
                    mMessagesViewHolder.hideSent();
                    mMessagesViewHolder.showSenderName();

                    ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                    // generate random color
                    int color = generator.getColor(messagesModel.getSender().getPhone());
                    mMessagesViewHolder.showSenderName();
                    mMessagesViewHolder.setSenderName(messagesModel.getSender().getPhone());
                    mMessagesViewHolder.setSenderColor(color);
                }
                mMessagesViewHolder.message.setVisibility(View.VISIBLE);

                String message = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }
            }
            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesViewHolder.setrepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesViewHolder.replied_message_view.setVisibility(View.GONE);
            }
                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    MessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);

        }


        else if (holder instanceof MessagesImageViewHolder) {
            MessagesImageViewHolder mMessagesImageViewHolder = (MessagesImageViewHolder) holder;
            generalHolder = mMessagesImageViewHolder;
            Context mActivity = holder.itemView.getContext();
            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null"))
                mMessagesImageViewHolder.setListeners(messagesModel);
            if (messagesModel.isFile_upload()) {
                mMessagesImageViewHolder.setHideUpload();
                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesImageViewHolder.setImageFile(messagesModel);
                    } else {

                        mMessagesImageViewHolder.setImageFile(messagesModel);
                    }

                } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {

                        mMessagesImageViewHolder.setGifFile(messagesModel);
                    } else {

                        mMessagesImageViewHolder.setGifFile(messagesModel);
                    }

                }

            } else {
                mMessagesImageViewHolder.setShowUpload();
                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesImageViewHolder.setImageFileOffline(messagesModel);
                    } else {
                        mMessagesImageViewHolder.setImageFileOffline(messagesModel);

                    }
                } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesImageViewHolder.setGifFileOffline(messagesModel);
                    } else {

                        mMessagesImageViewHolder.setGifFileOffline(messagesModel);

                    }
                }
            }


            try {


                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    MessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesImageViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);

                if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    mMessagesImageViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesImageViewHolder.hideSent();
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }

            if (messagesModel.isIs_group()) {
                try {
                    if (!messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {


                        String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesImageViewHolder.showSenderName();
                            mMessagesImageViewHolder.setSenderName(name);
                            mMessagesImageViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getSender().getPhone());
                            mMessagesImageViewHolder.showSenderName();
                            mMessagesImageViewHolder.setSenderName(messagesModel.getSender().getPhone());
                            mMessagesImageViewHolder.setSenderColor(color);
                        }
                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Group username is null" + e.getMessage());
                }

                if (messagesModel.getState().equals(AppConstants.CREATE_STATE))
                    mMessagesImageViewHolder.date.setVisibility(View.GONE);
                else
                    mMessagesImageViewHolder.setDate(messagesModel.getCreated());

            } else {
                mMessagesImageViewHolder.setDate(messagesModel.getCreated());
                mMessagesImageViewHolder.hideSenderName();

            }

            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesImageViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesImageViewHolder.setRepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesImageViewHolder.replied_message_view.setVisibility(View.GONE);
            }

            if (messagesModel.getMessage() != null && !messagesModel.getMessage().equals("null")) {
                String message = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesImageViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesImageViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesImageViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesImageViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }
            }
      /*      MessagesImageViewHolder mMessagesImageViewHolder = (MessagesImageViewHolder) holder;
            generalHolder = mMessagesImageViewHolder;
            Context mActivity = holder.itemView.getContext();
            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null"))
             //   mMessagesImageViewHolder.setListeners(messagesModel);
            if (messagesModel.isFile_upload()) {
                mMessagesImageViewHolder.setHideUpload();
                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesImageViewHolder.setImageFile(messagesModel);
                    } else {
                        mMessagesImageViewHolder.setImageFile(messagesModel);
                    }

                } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {

                        mMessagesImageViewHolder.setGifFile(messagesModel);
                    } else {

                        mMessagesImageViewHolder.setGifFile(messagesModel);
                    }

                }

            } else {
                mMessagesImageViewHolder.setShowUpload(messagesModel);
                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesImageViewHolder.setImageFileOffline(messagesModel);
                    } else {
                        mMessagesImageViewHolder.setImageFileOffline(messagesModel);

                    }
                } else if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                   if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesImageViewHolder.setGifFileOffline(messagesModel);
                    } else {

                        mMessagesImageViewHolder.setGifFileOffline(messagesModel);

                    }
                }

            }


            try {


                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    MessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesImageViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);

                if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    mMessagesImageViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesImageViewHolder.hideSent();
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }

            if (messagesModel.isIs_group()) {
                try {
                    if (!messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {


                        String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesImageViewHolder.showSenderName();
                            mMessagesImageViewHolder.setSenderName(name);
                            mMessagesImageViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getSender().getPhone());
                            mMessagesImageViewHolder.showSenderName();
                            mMessagesImageViewHolder.setSenderName(messagesModel.getSender().getPhone());
                            mMessagesImageViewHolder.setSenderColor(color);
                        }

                        //  }


                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Group username is null" + e.getMessage());
                }

                if (messagesModel.getState().equals(AppConstants.CREATE_STATE))
                    mMessagesImageViewHolder.date.setVisibility(View.GONE);
                else
                    mMessagesImageViewHolder.setDate(messagesModel.getCreated());

            } else {
                mMessagesImageViewHolder.setDate(messagesModel.getCreated());
                mMessagesImageViewHolder.hideSenderName();
            }

            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesImageViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesImageViewHolder.setRepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesImageViewHolder.replied_message_view.setVisibility(View.GONE);
            }

            if (messagesModel.getMessage() != null && !messagesModel.getMessage().equals("null")) {
                String message1 = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message1 = SpannableString.valueOf(message1);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message1.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message1.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message1.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesImageViewHolder.message.setText(Message1, TextView.BufferType.SPANNABLE);
                    mMessagesImageViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesImageViewHolder.message.setText(message1, TextView.BufferType.NORMAL);
                    mMessagesImageViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }
            }


            mMessagesImageViewHolder.itemView.setActivated(selectedItems.get(position, false));*/
        }

        else if (holder instanceof MessagesVideoViewHolder) {

            MessagesVideoViewHolder mMessagesVideoViewHolder = (MessagesVideoViewHolder) holder;
            generalHolder = mMessagesVideoViewHolder;
            Context mActivity = holder.itemView.getContext();

            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null"))
                mMessagesVideoViewHolder.setListeners(messagesModel);
            if (messagesModel.isFile_upload()) {

                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                    mMessagesVideoViewHolder.setHideUpload();
                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesVideoViewHolder.setVideoThumbnailFile(messagesModel);
                        mMessagesVideoViewHolder.setVideoTotalDuration(messagesModel);
                    } else {
                        mMessagesVideoViewHolder.setVideoThumbnailFile(messagesModel);
                        mMessagesVideoViewHolder.setVideoTotalDuration(messagesModel);
                    }

                }

            } else {

                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                    mMessagesVideoViewHolder.setShowUpload();

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {

                        mMessagesVideoViewHolder.setVideoThumbnailFileOffline(messagesModel);
                        mMessagesVideoViewHolder.setVideoTotalDuration(messagesModel);
                    } else {
                        mMessagesVideoViewHolder.setVideoThumbnailFileOffline(messagesModel);

                    }
                }
            }


            try {


                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    MessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesVideoViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);

                if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    mMessagesVideoViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesVideoViewHolder.hideSent();
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }

            if (messagesModel.isIs_group()) {
                try {
                    if (!messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {


                        String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesVideoViewHolder.showSenderName();
                            mMessagesVideoViewHolder.setSenderName(name);
                            mMessagesVideoViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getSender().getPhone());
                            mMessagesVideoViewHolder.showSenderName();
                            mMessagesVideoViewHolder.setSenderName(messagesModel.getSender().getPhone());
                            mMessagesVideoViewHolder.setSenderColor(color);
                        }

                        //  }


                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Group username is null" + e.getMessage());
                }

                if (messagesModel.getState().equals(AppConstants.CREATE_STATE))
                    mMessagesVideoViewHolder.date.setVisibility(View.GONE);
                else
                    mMessagesVideoViewHolder.setDate(messagesModel.getCreated());
            } else {
                mMessagesVideoViewHolder.setDate(messagesModel.getCreated());
                mMessagesVideoViewHolder.hideSenderName();

            }

            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesVideoViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesVideoViewHolder.setrepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesVideoViewHolder.replied_message_view.setVisibility(View.GONE);
            }


            if (messagesModel.getMessage() != null && !messagesModel.getMessage().equals("null")) {
                String message = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesVideoViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesVideoViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesVideoViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesVideoViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }


            }
            mMessagesVideoViewHolder.itemView.setActivated(selectedItems.get(position, false));
        }
        else if (holder instanceof MessagesDocumentViewHolder) {

            MessagesDocumentViewHolder mMessagesDocumentViewHolder = (MessagesDocumentViewHolder) holder;
            generalHolder = mMessagesDocumentViewHolder;
            Context mActivity = holder.itemView.getContext();

            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null"))
                mMessagesDocumentViewHolder.setListeners(messagesModel);
            if (messagesModel.isFile_upload()) {
                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesDocumentViewHolder.setDocumentTitle(messagesModel);
                        mMessagesDocumentViewHolder.mProgressUploadDocumentInitial.setVisibility(View.GONE);
                        mMessagesDocumentViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                        mMessagesDocumentViewHolder.cancelUploadDocument.setVisibility(View.GONE);
                        mMessagesDocumentViewHolder.retryUploadDocumentButton.setVisibility(View.GONE);
                        mMessagesDocumentViewHolder.documentImage.setVisibility(View.VISIBLE);
                    } else {
                        mMessagesDocumentViewHolder.setDocumentTitle(messagesModel);
                        if (messagesModel.isFile_downLoad()) {
                            mMessagesDocumentViewHolder.retryDownloadDocumentButton.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.cancelDownloadDocument.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.documentImage.setVisibility(View.VISIBLE);
                        } else {
                            mMessagesDocumentViewHolder.retryDownloadDocumentButton.setVisibility(View.VISIBLE);
                            mMessagesDocumentViewHolder.mProgressDownloadDocument.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.mProgressDownloadDocumentInitial.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.cancelDownloadDocument.setVisibility(View.GONE);
                            mMessagesDocumentViewHolder.documentImage.setVisibility(View.GONE);
                        }
                    }

                }

            } else {

                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {

                        mMessagesDocumentViewHolder.setDocumentTitle(messagesModel);
                        mMessagesDocumentViewHolder.retryUploadDocumentButton.setVisibility(View.VISIBLE);
                        mMessagesDocumentViewHolder.documentImage.setVisibility(View.GONE);
                    } else {
                        mMessagesDocumentViewHolder.setDocumentTitle(messagesModel);
                    }
                }
            }


            try {



                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    MessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesDocumentViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);

                if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    mMessagesDocumentViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesDocumentViewHolder.hideSent();
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }

            if (messagesModel.isIs_group()) {
                try {
                    if (!messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {


                        String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesDocumentViewHolder.showSenderName();
                            mMessagesDocumentViewHolder.setSenderName(name);
                            mMessagesDocumentViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getSender().getPhone());
                            mMessagesDocumentViewHolder.showSenderName();
                            mMessagesDocumentViewHolder.setSenderName(messagesModel.getSender().getPhone());
                            mMessagesDocumentViewHolder.setSenderColor(color);
                        }

                        //  }


                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Group username is null" + e.getMessage());
                }

                if (messagesModel.getState().equals(AppConstants.CREATE_STATE))
                    mMessagesDocumentViewHolder.date.setVisibility(View.GONE);
                else
                    mMessagesDocumentViewHolder.setDate(messagesModel.getCreated());
            } else {
                mMessagesDocumentViewHolder.setDate(messagesModel.getCreated());
                mMessagesDocumentViewHolder.hideSenderName();

            }

            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesDocumentViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesDocumentViewHolder.setrepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesDocumentViewHolder.replied_message_view.setVisibility(View.GONE);
            }


            if (messagesModel.getMessage() != null && !messagesModel.getMessage().equals("null")) {
                String message = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesDocumentViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesDocumentViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesDocumentViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesDocumentViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }

            }

            mMessagesDocumentViewHolder.itemView.setActivated(selectedItems.get(position, false));

        }  else if (holder instanceof MessagesAudioViewHolder) {

            mMessagesAudioViewHolder = (MessagesAudioViewHolder) holder;
            generalHolder = mMessagesAudioViewHolder;
            if (position == playingPosition) {
                //  mMessagesAudioViewHolder = (MessagesAudioViewHolder) holder;
                // this view holder corresponds to the currently playing audio cell
                // update its view to show playing progress
                updatePlayingView();
            } else {
                // and this one corresponds to non playing
                updateNonPlayingView((MessagesAudioViewHolder) holder);
            }
            Context mActivity = holder.itemView.getContext();
            if (messagesModel.getFile() != null && !messagesModel.getFile().equals("null"))
                mMessagesAudioViewHolder.setListeners(messagesModel);
            if (messagesModel.isFile_upload()) {
                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {

                        mMessagesAudioViewHolder.setUserAudioImage(messagesModel.getSender());
                        mMessagesAudioViewHolder.setAudioTotalDurationAudio(messagesModel);
                        mMessagesAudioViewHolder.mProgressUploadAudioInitial.setVisibility(View.GONE);
                        mMessagesAudioViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                        mMessagesAudioViewHolder.cancelUploadAudio.setVisibility(View.GONE);
                        mMessagesAudioViewHolder.retryUploadAudioButton.setVisibility(View.GONE);
                        mMessagesAudioViewHolder.playBtnAudio.setVisibility(View.VISIBLE);
                        mMessagesAudioViewHolder.audioSeekBar.setEnabled(true);

                    } else {


                        mMessagesAudioViewHolder.setUserAudioImage(messagesModel.getSender());
                        mMessagesAudioViewHolder.setAudioTotalDurationAudio(messagesModel);
                        if (messagesModel.isFile_downLoad()) {
                            mMessagesAudioViewHolder.retryDownloadAudioButton.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.mProgressDownloadAudioInitial.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.cancelDownloadAudio.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.playBtnAudio.setVisibility(View.VISIBLE);
                            mMessagesAudioViewHolder.audioSeekBar.setEnabled(true);
                        } else {
                            mMessagesAudioViewHolder.retryDownloadAudioButton.setVisibility(View.VISIBLE);
                            mMessagesAudioViewHolder.mProgressDownloadAudio.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.mProgressDownloadAudioInitial.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.cancelDownloadAudio.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.playBtnAudio.setVisibility(View.GONE);
                            mMessagesAudioViewHolder.audioSeekBar.setEnabled(false);
                        }
                    }

                }

            } else {

                if (messagesModel.getFile_type() != null && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {

                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesAudioViewHolder.setUserAudioImage(messagesModel.getSender());
                        mMessagesAudioViewHolder.setAudioTotalDurationAudio(messagesModel);
                        mMessagesAudioViewHolder.retryUploadAudioButton.setVisibility(View.VISIBLE);
                        mMessagesAudioViewHolder.audioSeekBar.setEnabled(false);
                    } else {
                        mMessagesAudioViewHolder.setUserAudioImage(messagesModel.getRecipient());
                    }
                }
            }


            try {


                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    MessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesAudioViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);

                if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                    mMessagesAudioViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesAudioViewHolder.hideSent();
                }
            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }

            if (messagesModel.isIs_group()) {
                try {
                    if (!messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {


                        String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesAudioViewHolder.showSenderName();
                            mMessagesAudioViewHolder.setSenderName(name);
                            mMessagesAudioViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getSender().getPhone());
                            mMessagesAudioViewHolder.showSenderName();
                            mMessagesAudioViewHolder.setSenderName(messagesModel.getSender().getPhone());
                            mMessagesAudioViewHolder.setSenderColor(color);
                        }

                        //  }


                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Group username is null" + e.getMessage());
                }


                if (messagesModel.getState().equals(AppConstants.CREATE_STATE))
                    mMessagesAudioViewHolder.date.setVisibility(View.GONE);
                else
                    mMessagesAudioViewHolder.setDate(messagesModel.getCreated());
            } else {
                mMessagesAudioViewHolder.setDate(messagesModel.getCreated());
                mMessagesAudioViewHolder.hideSenderName();

            }

            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesAudioViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesAudioViewHolder.setrepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesAudioViewHolder.replied_message_view.setVisibility(View.GONE);
            }

            if (messagesModel.getMessage() != null && !messagesModel.getMessage().equals("null")) {
                String message = UtilsString.unescapeJava(messagesModel.getMessage());
                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesAudioViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesAudioViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesAudioViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesAudioViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }
            }

            mMessagesAudioViewHolder.itemView.setActivated(selectedItems.get(position, false));
        } /*else if (holder instanceof MessagesViewHolder) {
            MessagesViewHolder mMessagesViewHolder = (MessagesViewHolder) holder;
            Context mActivity = holder.itemView.getContext();
            UpdateMessageModel messagesModel = getItem(position);

            if (messagesModel.isIs_group()) {
                try {
                    if (!messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {


                        String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                        if (name != null) {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(name);
                            mMessagesViewHolder.showSenderName();
                            mMessagesViewHolder.setSenderName(name);
                            mMessagesViewHolder.setSenderColor(color);
                        } else {
                            ColorGenerator generator = ColorGenerator.MATERIAL; // or use DEFAULT
                            // generate random color
                            int color = generator.getColor(messagesModel.getSender().getPhone());
                            mMessagesViewHolder.showSenderName();
                            mMessagesViewHolder.setSenderName(messagesModel.getSender().getPhone());
                            mMessagesViewHolder.setSenderColor(color);
                        }

                        //  }


                    }
                } catch (Exception e) {
                    AppHelper.LogCat("Group username is null" + e.getMessage());
                }
                mMessagesViewHolder.message.setVisibility(View.VISIBLE);

                switch (messagesModel.getState()) {
                    case AppConstants.CREATE_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_created_this_group), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_created_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_created_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }

                        }

                        break;
                    case AppConstants.LEFT_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_left), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_left), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_left), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }


                        }


                        break;

                    case AppConstants.ADD_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_added_this_group), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_added_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_added_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }


                        }


                        break;

                    case AppConstants.REMOVE_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_removed_this_group), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_removed_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_removed_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }


                        }


                        break;

                    case AppConstants.MEMBER_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_make_member_this_group), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_make_member_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_make_member_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }


                        }


                        break;

                    case AppConstants.ADMIN_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_make_admin_this_group), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_make_admin_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_make_admin_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }


                        }


                        break;

                    case AppConstants.EDITED_STATE:

                        mMessagesViewHolder.hideSenderName();
                        if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                            mMessagesViewHolder.message.setText(mActivity.getString(R.string.you_edited_this_group), TextView.BufferType.NORMAL);
                            mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                        } else {
                            String name = UtilsPhone.getContactName(messagesModel.getSender().getPhone());
                            if (name != null) {
                                mMessagesViewHolder.message.setText("" + name + " " + mActivity.getString(R.string.he_edited_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText("" + messagesModel.getSender().getPhone() + " " + mActivity.getString(R.string.he_edited_this_group), TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }


                        }


                        break;
                        default:
                            String message = UtilsString.unescapeJava(messagesModel.getMessage());

                            SpannableString Message = SpannableString.valueOf(message);
                            if (SearchQuery != null) {
                                int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                                if (index >= 0) {
                                    Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                    Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                                }
                                mMessagesViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            } else {
                                mMessagesViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                                mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                            }
                            break;
                }
                if (messagesModel.getState().equals(AppConstants.CREATE_STATE))
                    mMessagesViewHolder.date.setVisibility(View.GONE);
                else
                    mMessagesViewHolder.setDate(messagesModel.getCreated());
                if (messagesModel.getState().equals(AppConstants.NORMAL_STATE)) {
                    if (messagesModel.getSender().get_id().equals(PreferenceManager.getInstance().getID(mActivity))) {
                        mMessagesViewHolder.showSent(messagesModel.getStatus());
                    } else {
                        mMessagesViewHolder.hideSent();
                    }
                }
            } else {
                mMessagesViewHolder.hideSenderName();
                mMessagesViewHolder.message.setVisibility(View.VISIBLE);
                String message = UtilsString.unescapeJava(messagesModel.getMessage());

                SpannableString Message = SpannableString.valueOf(message);
                if (SearchQuery != null) {
                    int index = TextUtils.indexOf(message.toLowerCase(), SearchQuery.toLowerCase());
                    if (index >= 0) {
                        Message.setSpan(new ForegroundColorSpan(AppHelper.getColor(mActivity, R.color.colorSpanSearch)), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                        Message.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), index, index + SearchQuery.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
                    }
                    mMessagesViewHolder.message.setText(Message, TextView.BufferType.SPANNABLE);
                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                } else {
                    mMessagesViewHolder.message.setText(message, TextView.BufferType.NORMAL);
                    mMessagesViewHolder.message.setTextSize(PreferenceSettingsManager.getMessage_font_size(mActivity));
                }
                mMessagesViewHolder.setDate(messagesModel.getCreated());
                if ("6041b7d102ca2d000410939e".equals("6041b7d102ca2d000410939e")) {
                    mMessagesViewHolder.showSent(messagesModel.getStatus());
                } else {
                    mMessagesViewHolder.hideSent();
                }
            }

            if (messagesModel.getReply_id() != null && !messagesModel.getReply_id().equals("null")) {
                mMessagesViewHolder.replied_message_view.setVisibility(View.VISIBLE);
                mMessagesViewHolder.setrepliedMessage(messagesModel.getReply_id(), messagesModel.isReply_message());
            } else {
                mMessagesViewHolder.replied_message_view.setVisibility(View.GONE);
            }



            try {


                DateTime previousTs = UtilsTime.getCorrectDate(messagesModel.getCreated());
                DateTime currentDate = UtilsTime.getCorrectDate(messagesModel.getCreated());
                if (position > 0) {
                    UpdateMessageModel pm = getItem(position - 1);
                    previousTs = UtilsTime.getCorrectDate(pm.getCreated());

                }
                mMessagesViewHolder.setHeaderDate(currentDate.getMillis(), previousTs.getMillis(), currentDate);


            } catch (Exception e) {
                AppHelper.LogCat("Exception time " + e.getMessage());
            }
            mMessagesViewHolder.itemView.setActivated(selectedItems.get(position, false));
        }*/
        }



    @Override
    public int getItemCount() {
        return mMessagesModel != null ? mMessagesModel.size() : 0;
    }


    public void toggleSelection(int pos) {
        if (selectedItems.get(pos, false)) {
            selectedItems.delete(pos);
        } else {
            selectedItems.put(pos, true);
            if (!isActivated)
                isActivated = true;
        }
        notifyItemChanged(pos);
    }

    public void clearSelections() {
        selectedItems.clear();
        if (isActivated)
            isActivated = false;
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
    public long getItemId(int position) {
        MessageModel messagesModel = getItem(position);
        return messagesModel.getId(); ///to avoid blink recyclerview item when notify the adapter
    }

    public MessageModel getItem(int position) {
        return mMessagesModel.get(position);
    }


    public void removeMessageItem(int position) {
        if (position != 0) {
            try {
                mMessagesModel.remove(position);
                notifyItemRemoved(position);
            } catch (Exception e) {
                AppHelper.LogCat(e);
            }
        }
    }

    public void updateStatusMessageItem(String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        try {

            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            changeItemAtPosition(position, messagesModel);
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }


    public void changeItemAtPosition(int position, MessageModel messagesModel) {
        mMessagesModel.set(position, messagesModel);
        Log.d("SeenTag","Change Seen Called");
        notifyItemChanged(position);
        notifyItemRangeChanged(position, mMessagesModel.size());
        isStatusUpdated = true;
    }


    @Override
    public void onViewRecycled(@NonNull RecyclerView.ViewHolder holder) {

//        AppHelper.LogCat("onViewRecycled ");
        if (holder instanceof MessagesAudioViewHolder) {
/*
            PendingFilesTask task = mWaitingTaskSparseArray.get(((MessagesAudioViewHolder) holder).getId());
            if (task != null) {
                task.updateUploadListener(null);
            }*/
            if (playingPosition == holder.getAdapterPosition()) {
                //     AppHelper.LogCat("onViewRecycled 1");
                // view holder displaying playing audio cell is being recycled
                // change its state to non-playing
                //   updateNonPlayingView(mMessagesAudioViewHolder);
                stopPlayer();
                mMessagesAudioViewHolder = null;
            }
        }
        super.onViewRecycled(holder);
    }

    /**
     * Changes the view to non playing state
     * - icon is changed to play arrow
     * - seek bar disabled
     * - remove seek bar updater, if needed
     *
     * @param holder ViewHolder whose state is to be changed to non playing
     */
    @SuppressLint("SetTextI18n")
    public void updateNonPlayingView(MessagesAudioViewHolder holder) {

        if (holder == mMessagesAudioViewHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }

        holder.audioCurrentDurationAudio.setText("00:00");
        holder.setAnimation("0");
        holder.playBtnAudio.setVisibility(View.VISIBLE);
        holder.pauseBtnAudio.setVisibility(View.GONE);
        holder.audioSeekBar.setEnabled(false);
        holder.audioSeekBar.setProgress(0);

    }

    /**
     * Changes the view to playing state
     * - icon is changed to pause
     * - seek bar enabled
     * - start seek bar updater, if needed
     */
    public void updatePlayingView() {

        long totalDuration = player.getDuration();
        long currentDuration = player.getCurrentPosition();
        mMessagesAudioViewHolder.audioCurrentDurationAudio.setText(UtilsTime.getFileTime(currentDuration));
        int progress = (int) UtilsTime.getProgressPercentage(currentDuration, totalDuration);
        mMessagesAudioViewHolder.audioSeekBar.setProgress(progress);
        mMessagesAudioViewHolder.audioSeekBar.setEnabled(true);
        if (isPlaying) {
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
            mMessagesAudioViewHolder.playBtnAudio.setVisibility(View.GONE);
            mMessagesAudioViewHolder.pauseBtnAudio.setVisibility(View.VISIBLE);
        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
            mMessagesAudioViewHolder.playBtnAudio.setVisibility(View.VISIBLE);
            mMessagesAudioViewHolder.pauseBtnAudio.setVisibility(View.GONE);
        }
    }

    public void stopPlayer() {

        if (null != player) {
            releaseMediaPlayer();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SEEK_BAR: {
                long totalDuration = player.getDuration();
                long currentDuration = player.getCurrentPosition();
                mMessagesAudioViewHolder.audioCurrentDurationAudio.setText(UtilsTime.getFileTime(currentDuration));
                int progress = (int) UtilsTime.getProgressPercentage(currentDuration, totalDuration);
                mMessagesAudioViewHolder.audioSeekBar.setProgress(progress);
                uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
                return true;
            }
        }
        return false;
    }


    public void startMediaPlayer(String AudioDataSource) {

        setPlayer(AudioDataSource);
        defaultEventListener = new Player.DefaultEventListener() {

            @Override
            public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
                if (playbackState == Player.STATE_BUFFERING && playWhenReady) {
                    AppHelper.LogCat("ExoPlayer State is: BUFFERING");
                } else if (playbackState == Player.STATE_READY) {
                    AppHelper.LogCat("ExoPlayer State is: READY");
                    isPlaying = true;
                    updatePlayingView();
                } else if (playbackState == Player.STATE_ENDED) {
                    AppHelper.LogCat("ExoPlayer State is: ENDED");
                    isPlaying = false;
                    releaseMediaPlayer();
                } else if (playbackState == Player.STATE_IDLE) {
                    AppHelper.LogCat("ExoPlayer State is: IDLE");

                }
            }


        };
        player.addListener(defaultEventListener);
        player.setPlayWhenReady(true);
    }

    private void releaseMediaPlayer() {

        if (null != mMessagesAudioViewHolder) {
            updateNonPlayingView((mMessagesAudioViewHolder));
        }

        player.removeListener(defaultEventListener);
        player.release();
        player = null;
        playingPosition = -1;
        isPlaying = false;

    }


    @Override
    public void onStart(String type, String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);

        try {
            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            AppHelper.runOnUIThread(() -> {
                MessageUploadInfo messageUploadInfo = new MessageUploadInfo();
                messageUploadInfo.setType(type);
                messageUploadInfo.setStatus("start");
                notifyItemChanged(position, messageUploadInfo);

            });
        } finally {
            if (!realm.isClosed())
                realm.close();
        }

    }


    @Override
    public void onUpdate(int percentage, String type, String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);

        try {
            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            AppHelper.runOnUIThread(() -> {
                MessageUploadInfo messageUploadInfo = new MessageUploadInfo();
                messageUploadInfo.setPercentage(percentage);
                messageUploadInfo.setType(type);
                messageUploadInfo.setStatus("update");
                notifyItemChanged(position, messageUploadInfo);

            });

        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    @Override
    public void onError(String type, String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);
        AppHelper.LogCat("onError " + type);
        AppHelper.LogCat("onError messageId " + messageId);
        try {

            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            AppHelper.runOnUIThread(() -> {
                AppHelper.LogCat("onError position" + position);
                MessageUploadInfo messageUploadInfo = new MessageUploadInfo();
                messageUploadInfo.setType(type);
                messageUploadInfo.setStatus("error");
                notifyItemChanged(position, messageUploadInfo);

            });

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e);
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    @Override
    public void onFinish(String type, MessageModel messagesModel) {
        int position = mMessagesModel.indexOf(messagesModel);
        if (position == -1) return;
        AppHelper.runOnUIThread(() -> {
            MessageUploadInfo messageUploadInfo = new MessageUploadInfo();
            messageUploadInfo.setType(type);
            messageUploadInfo.setMessageModel(messagesModel);
            messageUploadInfo.setStatus("finish");
            notifyItemChanged(position, messageUploadInfo);
        });
    }

    @Override
    public void onStartDownload(String type, String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);

        try {
            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            AppHelper.runOnUIThread(() -> {
                MessageDownloadInfo messageDownloadInfo = new MessageDownloadInfo();
                messageDownloadInfo.setType(type);
                messageDownloadInfo.setStatus("start");
                notifyItemChanged(position, messageDownloadInfo);
            });
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    @Override
    public void onUpdateDownload(int percentage, String type, String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);

        try {
            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            AppHelper.runOnUIThread(() -> {
                MessageDownloadInfo messageDownloadInfo = new MessageDownloadInfo();
                messageDownloadInfo.setPercentage(percentage);
                messageDownloadInfo.setType(type);
                messageDownloadInfo.setStatus("update");
                notifyItemChanged(position, messageDownloadInfo);

            });

        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    @Override
    public void onErrorDownload(String type, String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(mActivity);
        AppHelper.LogCat("onError " + type);
        AppHelper.LogCat("onError messageId " + messageId);
        try {

            MessageModel messagesModel = realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
            int position = mMessagesModel.indexOf(messagesModel);
            if (position == -1) return;
            AppHelper.runOnUIThread(() -> {
                AppHelper.LogCat("onError position" + position);
                MessageDownloadInfo messageDownloadInfo = new MessageDownloadInfo();
                messageDownloadInfo.setType(type);
                messageDownloadInfo.setStatus("error");
                notifyItemChanged(position, messageDownloadInfo);

            });

        } catch (Exception e) {
            AppHelper.LogCat("Exception " + e);
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    @Override
    public void onFinishDownload(String type, MessageModel messagesModel) {
        int position = mMessagesModel.indexOf(messagesModel);
        if (position == -1) return;
        AppHelper.runOnUIThread(() -> {
            MessageDownloadInfo messageDownloadInfo = new MessageDownloadInfo();
            messageDownloadInfo.setType(type);
            messageDownloadInfo.setMessageModel(messagesModel);
            messageDownloadInfo.setStatus("finish");
            notifyItemChanged(position, messageDownloadInfo);
        });
    }


    public void scrollToItem(String messageId) {
        MessageModel messageModel = MessagesController.getMessageById(messageId);
        if (messageModel == null) return;
        UpdateItem updateItem = new UpdateItem();
        updateItem.setAction("scrollToMessage");
        if (messageModel.getFile_type() != null && !messageModel.getFile_type().equals("null")) {
            updateItem.setType(messageModel.getFile_type());
        } else {
            updateItem.setType("message");
        }
        updateItem.setMessageModel(messageModel);
        int position = mMessagesModel.indexOf(messageModel);
        messagesList.smoothScrollToPosition(position);
        notifyItemChanged(position, updateItem);

    }

}
