package com.pefgloble.pefchate.presenter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.ContactsContract;
import android.util.Log;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.AgoraVideo.openvcall.model.User;
import com.pefgloble.pefchate.JsonClasses.Pusher;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersBlockModel;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.JsonClasses.groups.MembersModel;
import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;
import com.pefgloble.pefchate.MessageBlock.MessagesActivity;
import com.pefgloble.pefchate.R;
import com.pefgloble.pefchate.RestAPI.APIGroups;
import com.pefgloble.pefchate.RestAPI.apiServices.BuildConfig;
import com.pefgloble.pefchate.api.APIHelper;
import com.pefgloble.pefchate.app.AppConstants;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.PreferenceManager;
import com.pefgloble.pefchate.helpers.UtilsTime;
import com.pefgloble.pefchate.helpers.files.RealmBackupRestore;
import com.pefgloble.pefchate.helpers.notifications.NotificationsManager;
import com.pefgloble.pefchate.jobs.SocketConnectionManager;
import com.pefgloble.pefchate.jobs.WorkJobsManager;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.acl.Group;
import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmList;
import io.realm.RealmQuery;
import io.socket.client.Socket;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_COUNTER;
import static com.pefgloble.pefchate.app.AppConstants.EVENT_BUS_MESSAGE_IS_READ;


/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class MessagesController {

    private static volatile MessagesController Instance = null;
    RealmList<MembersModel> groupModelsList=new RealmList<>();
    private ArrayList<MessageModel> messageModels = new ArrayList<>();


    public MessagesController() {
    }

    public static MessagesController getInstance() {

       MessagesController localInstance = Instance;
        if (localInstance == null) {
            synchronized (MessagesController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new MessagesController();
                }
            }
        }
        return localInstance;

    }


    public void updateConversationStatus(String conversationId, String recipientId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            realm.executeTransactionAsync(realm1 -> {
                ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationId).equalTo("owner._id", recipientId).findFirst();

                if (conversationsModel1 != null) {
                    conversationsModel1.setUnread_message_counter(0);
                    realm1.copyToRealmOrUpdate(conversationsModel1);

                    List<MessageModel> messagesModel = realm1.where(MessageModel.class)
                            .equalTo("status", AppConstants.IS_DELIVERED)
                            .equalTo("conversationId", conversationId)
                            .equalTo("sender._id", recipientId)
                            .findAll();
                    for (MessageModel messagesModel1 : messagesModel) {
                        if (messagesModel1.getStatus() == AppConstants.IS_DELIVERED) {
                            messagesModel1.setStatus(AppConstants.IS_SEEN);
                            realm1.copyToRealmOrUpdate(messagesModel1);
                        }
                    }
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));

                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_READ, conversationId));
                    NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
                }
            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no conversation unRead MessagesPresenter ");
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }

    public void updateGroupConversationStatus(String conversationId, String groupId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            realm.executeTransactionAsync(realm1 -> {
                ConversationModel conversationsModel1 = realm1.where(ConversationModel.class).equalTo("_id", conversationId)/*.equalTo("group._id", groupId)*/.findFirst();

                if (conversationsModel1 != null) {
                    conversationsModel1.setUnread_message_counter(0);
                    realm1.copyToRealmOrUpdate(conversationsModel1);

                    List<MessageModel> messagesModel = realm1.where(MessageModel.class)
                            .equalTo("status", AppConstants.IS_DELIVERED)
                            .equalTo("conversationId", conversationId)
                            .notEqualTo("sender._id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()))
                            .equalTo("group._id", groupId)
                            .findAll();

                    for (MessageModel messagesModel1 : messagesModel) {
                        if (messagesModel1.getStatus() == AppConstants.IS_DELIVERED) {
                            messagesModel1.setStatus(AppConstants.IS_SEEN);
                            realm1.copyToRealmOrUpdate(messagesModel1);
                        }
                    }
                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));

                    EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_IS_READ, conversationId));
                    NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
                } else {

                    AppHelper.LogCat("conversationsModel1 is nulll ");
                }
            });
        } catch (Exception e) {
            AppHelper.LogCat("There is no conversation unRead MessagesPresenter ");
        } finally {
            if (!realm.isClosed()) realm.close();
        }
    }


    public boolean checkIfUserIsLeft(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<MembersModel> query = realm.where(MembersModel.class).equalTo("owner._id", userId).equalTo("left", true);
            count = query.count();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
        return count != 0;
    }


    public boolean checkIfUserBlockedExist(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<UsersBlockModel> query = realm.where(UsersBlockModel.class).equalTo("usersModel._id", userId);
            count = query.count();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
        return count != 0;
    }

    public boolean checkIfConversationExist(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<ConversationModel> query = realm.where(ConversationModel.class).equalTo("owner._id", userId);
            count = query.count();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
        return count != 0;
    }

    public boolean checkIfGroupConversationExist(String groupId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<ConversationModel> query = realm.where(ConversationModel.class).equalTo("group._id", groupId);
            count = query.count();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
        return count != 0;
    }


    /**
     * method to update status as seen by sender (if recipient have been seen the message)  in realm database
     */
    public void updateSeenStatus(String messageId, String senderId, String recipientId, boolean is_group) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            if (checkIfMessageExist(messageId)) {
                AppHelper.LogCat("Seen messageId " + messageId);
                realm.executeTransaction(realm1 -> {
                    MessageModel messagesModel = realm1.where(MessageModel.class).equalTo("_id", messageId)
                            //.equalTo("status", AppConstants.IS_DELIVERED)
                            .findFirst();
                    if (messagesModel != null) {
                        messagesModel.setStatus(AppConstants.IS_SEEN);
                        realm1.copyToRealmOrUpdate(messagesModel);
                        AppHelper.LogCat("Seen successfully");


                        JSONObject updateMessage = new JSONObject();
                        try {
                            updateMessage.put("messageId", messageId);
                            if (is_group) {
                                updateMessage.put("is_group", true);
                            } else {
                                updateMessage.put("is_group", false);
                                updateMessage.put("recipientId", recipientId);
                            }
                        } catch (JSONException e) {
                            // e.printStackTrace();
                        }
                        Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                        if (mSocket != null) {
                            mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                        }

                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_MESSAGES, messageId));
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SEEN_FOR_CONVERSATIONS, messagesModel.getConversationId()));
                    } else {
                        AppHelper.LogCat("Seen failed ");
                    }
                });
            } else {

                JSONObject updateMessage = new JSONObject();
                try {
                    updateMessage.put("messageId", messageId);
                } catch (JSONException e) {
                    // e.printStackTrace();
                }


                Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                if (mSocket != null) {
                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
                }

            }
        } finally {

            if (!realm.isClosed())
                realm.close();
        }
    }

    /**
     * method to make message as sent
     */
    public void makeMessageAsSent(String SenderID, String messageId, String newMessageId, String oldConversationId, String newConversationId) {
        Log.d("SocTag","Make Sent Msg Called");
        if (!SenderID.equals(PreferenceManager.getInstance().getID(AGApplication.getInstance())))
            return;
        updateStatusAsSentBySender(messageId, newMessageId, oldConversationId, newConversationId);
    }
    /**
     * method to update status for the send message by sender  (as sent message ) in realm  database
     *  @param messageId         this is the first parameter for updateStatusAsSentBySender method
     * @param oldConversationId
     * @param newConversationId
     */
    private void updateStatusAsSentBySender(String messageId, String newMessageId, String oldConversationId, String newConversationId) {


        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            Log.d("SocTag","Update Message As Sent Caled");
            realm.executeTransaction(realm1 -> {
                if (!oldConversationId.equals(newConversationId)) {
                    ConversationModel conversationModel = realm1.where(ConversationModel.class).equalTo("_id", oldConversationId).findFirst();
                    //   if (conversationModel == null) return;
                    if (conversationModel != null) {
                        conversationModel.set_id(newConversationId);
                        realm1.copyToRealmOrUpdate(conversationModel);
                    }
                }

                MessageModel messagesModel = realm1.where(MessageModel.class)
                        .equalTo("_id", messageId)
                        .equalTo("status", AppConstants.IS_WAITING)
                        .findFirst();


                // if (messagesModel == null) return;
                messagesModel.setStatus(AppConstants.IS_SENT);
                messagesModel.set_id(newMessageId);
                messagesModel.setConversationId(newConversationId);
                // MessageModel messageModel = realm1.copyFromRealm(messagesModel);

                realm1.copyToRealmOrUpdate(messagesModel);

                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_SENT_FOR_MESSAGES, newMessageId));
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_IS_SENT_FOR_CONVERSATIONS, newConversationId));

                JSONObject updateMessage = new JSONObject();

                JSONArray ids = new JSONArray();
                try {
                    //  updateMessage.put("message", messageModel);
                    if (messagesModel.isIs_group()) {
                        int arraySize = messagesModel.getGroup().getMembers().size();
                        Log.d("SocTag","Socket ArraySize "+arraySize);
                        if (arraySize == 0) return;
                        for (int x = 0; x <= arraySize - 1; x++) {
                            if (!messagesModel.getGroup().getMembers().get(x).getOwner().get_id().equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                                    ids.put(messagesModel.getGroup().getMembers().get(x).getOwner().get_id());
                                AppHelper.LogCat("member id "+messagesModel.getGroup().getMembers().get(x).getOwner().get_id());
                            }

                        updateMessage.put("ids", ids);
                        updateMessage.put("grop_owner",messagesModel.getGroup().getOwner().get_id());

                    } else {
                        updateMessage.put("ownerId", messagesModel.getRecipient().get_id());
                    }
                    updateMessage.put("is_group", messagesModel.isIs_group());

                } catch (JSONException e) {
                    Log.d("SocTag","Socket Json Exception");
                    e.printStackTrace();
                }
                //emit by socket to other user
                AppHelper.runOnUIThread(() -> {

                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();
                    if (mSocket != null) {
                    Log.d("SocTag","Socket Send Message Caled");
                        mSocket.emit(AppConstants.SocketConstants.SOCKET_NEW_USER_MESSAGE_TO_SERVER, updateMessage);
                    }
                });
            });
        } catch (Exception e) {
            Log.d("SocTag","Send Message Error "+e.getMessage());
            AppHelper.LogCat(" Is sent messages Realm Error" + e.getMessage());
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    /**
     * method to update status for a specific  message (as delivered by sender) in realm database
     *
     * @param messageId this is parameter for  updateDeliveredStatus
     * @param senderId  this is parameter for  updateDeliveredStatus
     */
    public void updateDeliveredStatus(String messageId, String senderId) {
         /*  if (senderId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
           return;*/


        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        if (checkIfMessageExist(messageId)) {
            AppHelper.LogCat("Delivered messageId " + messageId);
            realm.executeTransaction(realm1 -> {
                MessageModel messagesModel = realm1.where(MessageModel.class).equalTo("_id", messageId)
                        .equalTo("status", AppConstants.IS_SENT)
                        .findFirst();
                if (messagesModel != null) {
                    messagesModel.setStatus(AppConstants.IS_DELIVERED);
                    realm1.copyToRealmOrUpdate(messagesModel);
                    AppHelper.LogCat("Delivered successfully");
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_MESSAGES, messageId));
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_MESSAGE_IS_DELIVERED_FOR_CONVERSATIONS, messagesModel.getConversationId()));
                } else {
                    AppHelper.LogCat("Delivered failed ");
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {
                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
                    }
                }
            });
        } else {
            JSONObject updateMessage = new JSONObject();
            try {
                updateMessage.put("messageId", messageId);
            } catch (JSONException e) {
                // e.printStackTrace();
            }


            Socket mSocket = SocketConnectionManager.getInstance().getSocket();

            if (mSocket != null) {
                mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_EXIST_AS_FINISHED, updateMessage);
            }
        }
        if (!realm.isClosed())
            realm.close();

    }

    /*for update message status*/

    public boolean checkIfGroupExist(String groupId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long count;
        try {
            RealmQuery<GroupModel> query = realm.where(GroupModel.class).equalTo("_id", groupId);
            count = query.count();
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
        return count != 0;
    }

    /**
     * method when a user change the image profile
     *
     * @param ownerId
     * @param isGroup
     */
    public void getNotifyForImageProfileChanged(String ownerId, boolean isGroup,String desig,String imageUrl) {

        if (isGroup) {
            if (!checkIfGroupExist(ownerId)) return;
            EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_GROUP_UPDATED, ownerId));

        } else {
            if (ownerId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (checkIfContactExist(WhatsCloneApplication.getInstance(), ownerId)){
                Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
                try {
                    realm.executeTransaction(new Realm.Transaction() {
                        @Override
                        public void execute(Realm realm) {
                            UsersModel usersModel = realm.where(UsersModel.class).equalTo("_id", ownerId).findFirst();
                            usersModel.setDesignation(desig);
                            usersModel.setImage(imageUrl);
                            realm.copyToRealmOrUpdate(usersModel);
                        }
                    });
                    EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_IMAGE_PROFILE_UPDATED, ownerId));
                }
                finally {
                    if (!realm.isClosed()){
                        realm.close();
                    }
                }
            }

        }

    }


    /**
     * method to check if user contact exist
     *
     * @param userId this is the second parameter for checkIfContactExist  method
     * @return return value
     */
    public static boolean checkIfContactExist(Context mContext, String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {
            UsersModel usersModel = realm.where(UsersModel.class).equalTo("_id", userId).findFirst();
            // CONTENT_FILTER_URI allow to search contact by phone number
            Uri lookupUri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(usersModel.getPhone()));
            // This query will return NAME and ID of contact, associated with phone //number.
            Cursor mcursor = mContext.getApplicationContext().getContentResolver().query(lookupUri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME, ContactsContract.PhoneLookup._ID}, null, null, null);
            //Now retrieve _ID from query result
            String name = null;
            try {
                if (mcursor != null) {
                    if (mcursor.moveToFirst()) {
                        name = mcursor.getString(mcursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
                    }
                }
            } finally {
                mcursor.close();
            }

            return name != null;
        } catch (Exception e) {
            AppHelper.LogCat(e);
            return false;
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public static MessageModel getMessageById(String messageId) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            return realm.where(MessageModel.class).equalTo("_id", messageId).findFirst();
        } catch (Exception e) {
            return null;
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }


    /**
     * method to save the incoming message and mark him as waiting
     *
     * @param data this is the parameter for saveNewMessage method
     */
    public void saveNewUserMessage(JSONObject data, Context context) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        try {
            //recipient object
            String recipientId = data.getJSONObject("recipient").getString("_id");
            String recipient_phone = data.getJSONObject("recipient").getString("phone");
            String recipient_name = data.getJSONObject("recipient").getString("username");
            String recipient_image = data.getJSONObject("recipient").getString("image");
            boolean recipient_activated = data.getJSONObject("recipient").getBoolean("activated");
            boolean recipient_linked = data.getJSONObject("recipient").getBoolean("linked");
            //sender object
            String senderId = data.getJSONObject("sender").getString("_id");
            String sender_phone = data.getJSONObject("sender").getString("phone");
            String sender_name = data.getJSONObject("sender").getString("username");
            String sender_image = data.getJSONObject("sender").getString("image");
            boolean sender_activated = data.getJSONObject("sender").getBoolean("activated");
            boolean sender_linked = data.getJSONObject("sender").getBoolean("linked");
            //message object
            String messageId = data.getString("_id");
            String messageBody = data.getString("message");
            String created = UtilsTime.getCorrectDate(data.getString("created")).toString();
            String conversationId = data.getString("conversationId");

            String latitude = data.getString("latitude");
            String longitude = data.getString("longitude");
            String file = data.getString("file");
            String file_type = data.getString("file_type");
            String duration = data.getString("duration_file");

            String fileSize = data.getString("file_size");
            String state = data.getString("state");
            int status = data.getInt("status");

            boolean reply_message = data.getBoolean("reply_message");
            String reply_id = data.getString("reply_id");
            String document_type = data.getString("document_type");
            String document_name = data.getString("document_name");

            if (senderId.equals(PreferenceManager.getInstance().getID(AGApplication.getInstance())))
                return;

            if (!MessagesController.getInstance().checkIfConversationExist(senderId)) {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages
                    realm.executeTransaction(realm1 -> {

                        UsersModel usersModelSender = realm1.where(UsersModel.class).equalTo("_id", senderId).findFirst();
                        UsersModel usersModelRecipient = realm1.where(UsersModel.class).equalTo("_id", recipientId).findFirst();

                        UsersModel usersModelSenderFinal;
                        if (usersModelSender == null) {
                            UsersModel usersModelSenderNew = realm1.createObject(UsersModel.class, senderId);
                            usersModelSenderNew.setPhone(sender_phone);
                            if (!sender_name.equals("null"))
                                usersModelSenderNew.setUsername(sender_name);
                            usersModelSenderNew.setImage(sender_image);
                            usersModelSenderNew.setActivate(sender_activated);
                            usersModelSenderNew.setLinked(sender_linked);
                            usersModelSenderFinal = usersModelSenderNew;
                        } else {
                            usersModelSenderFinal = usersModelSender;
                        }
                        UsersModel usersModelRecipientFinal;
                        if (usersModelRecipient == null) {
                            UsersModel usersModelRecipientNew = realm1.createObject(UsersModel.class, recipientId);
                            usersModelRecipientNew.setPhone(recipient_phone);
                            if (!recipient_name.equals("null"))
                                usersModelRecipientNew.setUsername(recipient_name);
                            usersModelRecipientNew.setImage(recipient_image);
                            usersModelRecipientNew.setActivate(recipient_activated);
                            usersModelRecipientNew.setLinked(recipient_linked);
                            usersModelRecipientFinal = usersModelRecipientNew;
                        } else {
                            usersModelRecipientFinal = usersModelRecipient;
                        }
                        int unreadMessageCounter = 0;
                        unreadMessageCounter++;
                        //   String lastConversationID = RealmBackupRestore.getConversationLastId();
                        //  String lastID = RealmBackupRestore.getMessageLastId();
                        MessageModel messagesModel = realm1.createObject(MessageModel.class, UniqueId.generateUniqueId());
                        messagesModel.set_id(messageId);

                        messagesModel.setSender(usersModelSenderFinal);
                        messagesModel.setRecipient(usersModelRecipientFinal);
                        messagesModel.setCreated(created);
                        messagesModel.setStatus(status);
                        messagesModel.setIs_group(false);
                        messagesModel.setConversationId(conversationId);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setLongitude(longitude);
                        messagesModel.setLatitude(latitude);
                        messagesModel.setState(state);
                        messagesModel.setFile(file);
                        messagesModel.setFile_type(file_type);
                        messagesModel.setFile_size(fileSize);
                        messagesModel.setDuration_file(duration);

                        messagesModel.setReply_id(reply_id);
                        messagesModel.setReply_message(reply_message);
                        messagesModel.setDocument_name(document_name);
                        messagesModel.setDocument_type(document_type);

                        messagesModel.setFile_upload(true);
                        if (!file.equals("null")) {
                            if (longitude != null || !longitude.equals("null")) {
                                messagesModel.setFile_downLoad(true);
                            } else {
                                messagesModel.setFile_downLoad(false);
                            }
                        } else {
                            messagesModel.setFile_downLoad(true);
                        }
                        ConversationModel conversationsModel1 = realm1.createObject(ConversationModel.class, UniqueId.generateUniqueId());
                        conversationsModel1.set_id(conversationId);
                        conversationsModel1.setOwner(usersModelSenderFinal);
                        conversationsModel1.setLatestMessage(messagesModel);
                        conversationsModel1.setCreated(created);
                        conversationsModel1.setIs_group(false);
                        conversationsModel1.setUnread_message_counter(unreadMessageCounter);
                        realm1.copyToRealmOrUpdate(conversationsModel1);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW, messageId, senderId, recipientId));
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, conversationId));


                        String FileType = null;
                        if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                            FileType = "Image";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                            FileType = "Gif";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                            FileType = "Video";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                            FileType = "Audio";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                            FileType = "Document";
                        }

                        Intent mIntent = new Intent("new_user_notification_whatsclone");
                        mIntent.putExtra("actionType", "new_user_message_notification");
                        mIntent.putExtra("conversationID", conversationId);
                        mIntent.putExtra("recipientID", recipientId);
                        mIntent.putExtra("senderId", senderId);
                        mIntent.putExtra("userImage", sender_image);
                        mIntent.putExtra("username", sender_name);
                        mIntent.putExtra("file", FileType);
                        mIntent.putExtra("phone", sender_phone);
                        mIntent.putExtra("messageId", messageId);
                        mIntent.putExtra("message", messageBody);
                        mIntent.putExtra("app", WhatsCloneApplication.getInstance().getPackageName());
                        context.sendBroadcast(mIntent);
                    });
                } /*else {

                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", recipientId);

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data1 = (JSONObject) args[0];
                            try {
                                if (data1.getBoolean("success")) {
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");
                                } else {
                                    AppHelper.LogCat(" duplicate message   seen ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });

                    }
                }*/
            } else {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages
                    realm.executeTransaction(realm1 -> {

                        int unreadMessageCounter = 0;
                        // String lastID = RealmBackupRestore.getMessageLastId();

                        ConversationModel conversationsModel;
                        RealmQuery<ConversationModel> conversationsModelRealmQuery = realm1.where(ConversationModel.class).equalTo("_id", conversationId);
                        conversationsModel = conversationsModelRealmQuery.findAll().first();

                        unreadMessageCounter = conversationsModel.getUnread_message_counter();
                        unreadMessageCounter++;

                        UsersModel usersModelSender = realm1.where(UsersModel.class).equalTo("_id", senderId).findFirst();
                        UsersModel usersModelRecipient = realm1.where(UsersModel.class).equalTo("_id", recipientId).findFirst();
                        UsersModel usersModelSenderFinal;
                        if (usersModelSender == null) {
                            UsersModel usersModelSenderNew = realm1.createObject(UsersModel.class, senderId);
                            usersModelSenderNew.setPhone(sender_phone);
                            if (!sender_name.equals("null"))
                                usersModelSenderNew.setUsername(sender_name);
                            usersModelSenderNew.setImage(sender_image);
                            usersModelSenderNew.setActivate(sender_activated);
                            usersModelSenderNew.setLinked(sender_linked);
                            usersModelSenderFinal = usersModelSenderNew;
                        } else {
                            usersModelSenderFinal = usersModelSender;
                        }

                        UsersModel usersModelRecipientFinal;
                        if (usersModelRecipient == null) {
                            UsersModel usersModelRecipientNew = realm1.createObject(UsersModel.class, recipientId);
                            usersModelRecipientNew.setPhone(recipient_phone);
                            if (!recipient_name.equals("null"))
                                usersModelRecipientNew.setUsername(recipient_name);
                            usersModelRecipientNew.setImage(recipient_image);
                            usersModelRecipientNew.setActivate(recipient_activated);
                            usersModelRecipientNew.setLinked(recipient_linked);
                            usersModelRecipientFinal = usersModelRecipientNew;
                        } else {
                            usersModelRecipientFinal = usersModelRecipient;
                        }

                        MessageModel messagesModel = realm1.createObject(MessageModel.class, UniqueId.generateUniqueId());
                        messagesModel.set_id(messageId);
                        messagesModel.setSender(usersModelSenderFinal);
                        messagesModel.setRecipient(usersModelRecipientFinal);
                        messagesModel.setCreated(created);
                        messagesModel.setStatus(status);
                        messagesModel.setIs_group(false);
                        messagesModel.setConversationId(conversationId);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setState(state);
                        messagesModel.setLongitude(longitude);
                        messagesModel.setLatitude(latitude);
                        messagesModel.setFile(file);
                        messagesModel.setFile_type(file_type);
                        messagesModel.setFile_size(fileSize);
                        messagesModel.setDuration_file(duration);

                        messagesModel.setReply_id(reply_id);
                        messagesModel.setReply_message(reply_message);
                        messagesModel.setDocument_name(document_name);
                        messagesModel.setDocument_type(document_type);

                        messagesModel.setFile_upload(true);
                        if (!file.equals("null")) {
                            Log.d("ImageTag","longitude "+ longitude);
                            Log.d("ImageTag","latitude "+ latitude);
                            messagesModel.setFile_downLoad(false);
                           /* if (latitude == null) {
                                messagesModel.setFile_downLoad(false);
                            } else {
                                messagesModel.setFile_downLoad(true);
                            }*/
                        } else {
                            messagesModel.setFile_downLoad(false);
                        }
                        conversationsModel.setLatestMessage(messagesModel);
                        conversationsModel.setCreated(created);
                        conversationsModel.setUnread_message_counter(unreadMessageCounter);
                        realm1.copyToRealmOrUpdate(conversationsModel);

                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_MESSAGES_NEW_ROW, messageId, senderId, recipientId));
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationId));

                        String FileType = null;
                        if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                            FileType = "Image";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                            FileType = "Gif";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                            FileType = "Video";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                            FileType = "Audio";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                            FileType = "Document";
                        }

                        Intent mIntent = new Intent("new_user_notification_whatsclone");
                        mIntent.putExtra("actionType", "new_user_message_notification");
                        mIntent.putExtra("conversationID", conversationId);
                        mIntent.putExtra("recipientID", recipientId);
                        mIntent.putExtra("senderId", senderId);
                        mIntent.putExtra("userImage", sender_image);
                        mIntent.putExtra("username", sender_name);
                        mIntent.putExtra("file", FileType);
                        mIntent.putExtra("phone", sender_phone);
                        mIntent.putExtra("messageId", messageId);
                        mIntent.putExtra("message", messageBody);
                        mIntent.putExtra("app", WhatsCloneApplication.getInstance().getPackageName());
                        context.sendBroadcast(mIntent);

                    });
                } /*else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", recipientId);

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data1 = (JSONObject) args[0];
                            try {
                                if (data1.getBoolean("success")) {
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");
                                } else {
                                    AppHelper.LogCat(" duplicate message   seen ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });

                    }
                }*/

            }

            AppHelper.runOnUIThread(() -> new Handler().postDelayed(() -> {
                WorkJobsManager.getInstance().sendDeliveredStatusToServer();
            }, 1200));


            AppHelper.runOnUIThread(() -> new Handler().postDelayed(() -> {
                if (AppHelper.isActivityRunning(AGApplication.getInstance(), "MessageBlock.MessagesActivity")) {
                    AppHelper.LogCat("MessagesActivity running");
                    WorkJobsManager.getInstance().sendSeenStatusToServer(senderId, conversationId,context);
                }
                else {
                    Log.d("SeenTag","msg act not running");
                }
            }, 2000));


        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception MainService" + e.getMessage());
        }
        if (!realm.isClosed())
            realm.close();
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
    }
    /**
     * method to save the incoming group message and mark him as waiting
     *
     * @param data this is the parameter for saveNewMessage method
     */
    public void saveNewMessageGroup(JSONObject data, Context context) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());

        try {
            //recipient object
            String groupId = data.getJSONObject("group").getString("_id");
            String group_name = data.getJSONObject("group").getString("name");
            String group_image = data.getJSONObject("group").getString("image");
            String group_owner = data.getJSONObject("group").getString("owner");
            //sender object
            String senderId = data.getJSONObject("sender").getString("_id");
            String sender_phone = data.getJSONObject("sender").getString("phone");
            String sender_name = data.getJSONObject("sender").getString("username");
            String sender_image = data.getJSONObject("sender").getString("image");
            boolean sender_activated = data.getJSONObject("sender").getBoolean("activated");
            boolean sender_linked = data.getJSONObject("sender").getBoolean("linked");
            //message object
            String messageId = data.getString("_id");
            String messageBody = data.getString("message");
            String created = UtilsTime.getCorrectDate(data.getString("created")).toString();
            String conversationId = data.getString("conversationId");

            String latitude = data.getString("latitude");
            String longitude = data.getString("longitude");
            String file = data.getString("file");
            String file_type = data.getString("file_type");
            String duration = data.getString("duration_file");

            String fileSize = data.getString("file_size");
            String state = data.getString("state");
            int status = data.getInt("status");

            boolean reply_message = data.getBoolean("reply_message");
            String reply_id = data.getString("reply_id");
            String document_type = data.getString("document_type");
            String document_name = data.getString("document_name");

            if (senderId.equals(PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())))
                return;

            if (!MessagesController.getInstance().checkIfGroupConversationExist(groupId)) {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages
                    realm.executeTransaction(realm1 -> {

                        UsersModel usersModelSender = realm1.where(UsersModel.class).equalTo("_id", senderId).findFirst();
                        GroupModel groupModel = realm1.where(GroupModel.class).equalTo("_id", groupId).findFirst();
                        UsersModel usersModelSenderFinal;
                        if (usersModelSender == null) {
                            UsersModel usersModelSenderNew = realm1.createObject(UsersModel.class, senderId);
                            usersModelSenderNew.setPhone(sender_phone);
                            if (!sender_name.equals("null"))
                                usersModelSenderNew.setUsername(sender_name);
                            usersModelSenderNew.setImage(sender_image);
                            usersModelSenderNew.setActivate(sender_activated);
                            usersModelSenderNew.setLinked(sender_linked);
                            usersModelSenderFinal = usersModelSenderNew;
                        } else {
                            usersModelSenderFinal = usersModelSender;
                        }

                        GroupModel groupModelFinal;
                        if (groupModel == null) {
                            GroupModel groupModelNew = realm1.createObject(GroupModel.class, groupId);
                            if (!group_name.equals("null"))
                                groupModelNew.setName(group_name);
                            groupModelNew.setImage(group_image);
                            //  groupModelNew.setOwner(recipient_activated);
                            groupModelFinal = groupModelNew;
                        } else {
                            groupModelFinal = groupModel;
                        }
                        int unreadMessageCounter = 0;
                        unreadMessageCounter++;
                        //   String lastConversationID = RealmBackupRestore.getConversationLastId();
                        //  String lastID = RealmBackupRestore.getMessageLastId();
                        MessageModel messagesModel = realm1.createObject(MessageModel.class, UniqueId.generateUniqueId());
                        messagesModel.set_id(messageId);

                        messagesModel.setSender(usersModelSenderFinal);
                        messagesModel.setGroup(groupModelFinal);
                        messagesModel.setCreated(created);
                        messagesModel.setStatus(status);
                        messagesModel.setIs_group(true);
                        messagesModel.setConversationId(conversationId);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setLongitude(longitude);
                        messagesModel.setLatitude(latitude);
                        messagesModel.setState(state);
                        messagesModel.setFile(file);
                        messagesModel.setFile_type(file_type);
                        messagesModel.setFile_size(fileSize);
                        messagesModel.setDuration_file(duration);

                        messagesModel.setReply_id(reply_id);
                        messagesModel.setReply_message(reply_message);
                        messagesModel.setDocument_name(document_name);
                        messagesModel.setDocument_type(document_type);

                        messagesModel.setFile_upload(true);
                        if (!file.equals("null")) {
                            if (longitude != null || !longitude.equals("null")) {
                                messagesModel.setFile_downLoad(true);
                            } else {
                                messagesModel.setFile_downLoad(false);
                            }

                        } else {
                            messagesModel.setFile_downLoad(true);
                        }
                        ConversationModel conversationsModel1 = realm1.createObject(ConversationModel.class, UniqueId.generateUniqueId());
                        conversationsModel1.set_id(conversationId);
                        conversationsModel1.setGroup(groupModelFinal);
                        conversationsModel1.setLatestMessage(messagesModel);
                        conversationsModel1.setCreated(created);
                        conversationsModel1.setIs_group(true);

                        conversationsModel1.setUnread_message_counter(unreadMessageCounter);
                        realm1.copyToRealmOrUpdate(conversationsModel1);
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW, messagesModel));
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_NEW_ROW, conversationId));


                        String FileType = null;
                        if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                            FileType = "Image";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                            FileType = "Gif";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                            FileType = "Video";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                            FileType = "Audio";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                            FileType = "Document";
                        }

                        Intent mIntent = new Intent("new_user_notification_whatsclone");
                        mIntent.putExtra("actionType", "new_group_message_notification");
                        mIntent.putExtra("conversationID", conversationId);
                        mIntent.putExtra("recipientID", senderId);
                        mIntent.putExtra("groupID", groupId);
                        mIntent.putExtra("groupImage", group_image);
                        mIntent.putExtra("username", sender_name);
                        mIntent.putExtra("file", FileType);
                        mIntent.putExtra("senderPhone", sender_phone);
                        mIntent.putExtra("groupName", group_name);
                        mIntent.putExtra("message", messageBody);
                        mIntent.putExtra("state", state);
                        mIntent.putExtra("app", WhatsCloneApplication.getInstance().getPackageName());
                        context.sendBroadcast(mIntent);
                    });
                } /*else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {


                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data1 = (JSONObject) args[0];
                            try {
                                if (data1.getBoolean("success")) {
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");
                                } else {
                                    AppHelper.LogCat(" duplicate message   seen ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });

                    }
                }*/
            } else {

                if (!checkIfMessageExist(messageId)) {//avoid duplicate messages
                    realm.executeTransaction(realm1 -> {

                        int unreadMessageCounter = 0;
                        // String lastID = RealmBackupRestore.getMessageLastId();

                        ConversationModel conversationsModel;
                        RealmQuery<ConversationModel> conversationsModelRealmQuery = realm1.where(ConversationModel.class).equalTo("_id", conversationId);
                        conversationsModel = conversationsModelRealmQuery.findAll().first();

                        unreadMessageCounter = conversationsModel.getUnread_message_counter();
                        unreadMessageCounter++;

                        UsersModel usersModelSender = realm1.where(UsersModel.class).equalTo("_id", senderId).findFirst();
                        GroupModel groupModel = realm1.where(GroupModel.class).equalTo("_id", groupId).findFirst();
                        UsersModel usersModelSenderFinal;
                        if (usersModelSender == null) {
                            UsersModel usersModelSenderNew = realm1.createObject(UsersModel.class, senderId);
                            usersModelSenderNew.setPhone(sender_phone);
                            if (!sender_name.equals("null"))
                                usersModelSenderNew.setUsername(sender_name);
                            usersModelSenderNew.setImage(sender_image);
                            usersModelSenderNew.setActivate(sender_activated);
                            usersModelSenderNew.setLinked(sender_linked);
                            usersModelSenderFinal = usersModelSenderNew;
                        } else {
                            usersModelSenderFinal = usersModelSender;
                        }

                        GroupModel groupModelFinal;
                        if (groupModel == null) {
                            GroupModel groupModelNew = realm1.createObject(GroupModel.class, groupId);

                            if (!group_name.equals("null"))
                                groupModelNew.setName(group_name);
                            groupModelNew.setImage(group_image);
                            //   groupModelNew.setOwner(group_owner);
                            groupModelFinal = groupModelNew;
                        } else {
                            groupModelFinal = groupModel;
                        }

                        MessageModel messagesModel = realm1.createObject(MessageModel.class, UniqueId.generateUniqueId());
                        messagesModel.set_id(messageId);
                        messagesModel.setSender(usersModelSenderFinal);
                        messagesModel.setGroup(groupModelFinal);
                        messagesModel.setCreated(created);
                        messagesModel.setStatus(status);
                        messagesModel.setIs_group(true);
                        messagesModel.setConversationId(conversationId);
                        messagesModel.setMessage(messageBody);
                        messagesModel.setState(state);
                        messagesModel.setLongitude(longitude);
                        messagesModel.setLatitude(latitude);
                        messagesModel.setFile(file);
                        messagesModel.setFile_type(file_type);
                        messagesModel.setFile_size(fileSize);
                        messagesModel.setDuration_file(duration);

                        messagesModel.setReply_id(reply_id);
                        messagesModel.setReply_message(reply_message);
                        messagesModel.setDocument_name(document_name);
                        messagesModel.setDocument_type(document_type);

                        messagesModel.setFile_upload(true);
                        if (!file.equals("null")) {
                            if (longitude != null || !longitude.equals("null")) {
                                messagesModel.setFile_downLoad(false);
                            } else {
                                messagesModel.setFile_downLoad(false);
                            }
                        } else {
                            messagesModel.setFile_downLoad(true);
                        }
                        conversationsModel.set_id(conversationId);
                        conversationsModel.setLatestMessage(messagesModel);
                        conversationsModel.setCreated(created);
                        conversationsModel.setGroup(groupModelFinal);
                        conversationsModel.setUnread_message_counter(unreadMessageCounter);
                        realm1.copyToRealmOrUpdate(conversationsModel);

                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_GROUP_MESSAGE_MESSAGES_NEW_ROW, messagesModel));
                        EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationId));

                        String FileType = null;
                        if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_IMAGE)) {
                            FileType = "Image";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_GIF)) {
                            FileType = "Gif";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_VIDEO)) {
                            FileType = "Video";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_AUDIO)) {
                            FileType = "Audio";
                        } else if (!messagesModel.getFile().equals("null") && messagesModel.getFile_type().equals(AppConstants.MESSAGES_DOCUMENT)) {
                            FileType = "Document";
                        }

                        Intent mIntent = new Intent("new_user_notification_whatsclone");
                        mIntent.putExtra("actionType", "new_group_message_notification");
                        mIntent.putExtra("conversationID", conversationsModel.getId());
                        mIntent.putExtra("recipientID", senderId);
                        mIntent.putExtra("groupID", groupId);
                        mIntent.putExtra("groupImage", group_image);
                        mIntent.putExtra("username", sender_name);
                        mIntent.putExtra("file", FileType);
                        mIntent.putExtra("senderPhone", sender_phone);
                        mIntent.putExtra("groupName", group_name);
                        mIntent.putExtra("message", messageBody);
                        mIntent.putExtra("state", state);
                        mIntent.putExtra("app", WhatsCloneApplication.getInstance().getPackageName());
                        context.sendBroadcast(mIntent);

                    });
                } /*else {
                    JSONObject updateMessage = new JSONObject();
                    try {
                        updateMessage.put("messageId", messageId);
                        updateMessage.put("ownerId", senderId);
                        updateMessage.put("recipientId", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance()));

                    } catch (JSONException e) {
                        // e.printStackTrace();
                    }


                    Socket mSocket = SocketConnectionManager.getInstance().getSocket();

                    if (mSocket != null) {

                        mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_SEEN, updateMessage, (Ack) args -> {

                            JSONObject data1 = (JSONObject) args[0];
                            try {
                                if (data1.getBoolean("success")) {
                                    mSocket.emit(AppConstants.SocketConstants.SOCKET_UPDATE_STATUS_OFFLINE_MESSAGES_AS_FINISHED, updateMessage);
                                    AppHelper.LogCat("--> duplicate message Recipient mark message as  seen <--");
                                } else {
                                    AppHelper.LogCat(" duplicate message   seen ");
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }

                        });


                    }
                }*/
            }

            AppHelper.runOnUIThread(() -> new Handler().postDelayed(() -> {
                WorkJobsManager.getInstance().sendDeliveredGroupStatusToServer();
            }, 1200));


            AppHelper.runOnUIThread(() -> new Handler().postDelayed(() -> {
                if (AppHelper.isActivityRunning(WhatsCloneApplication.getInstance(), "MessageBlock.MessagesActivity")) {
                    AppHelper.LogCat("MessagesActivity running");
                    WorkJobsManager.getInstance().sendSeenGroupStatusToServer(groupId, conversationId);
                }
            }, 2000));


        } catch (JSONException e) {
            AppHelper.LogCat("save message Exception MainService" + e.getMessage());
        }
        if (!realm.isClosed())
            realm.close();
        EventBus.getDefault().post(new Pusher(EVENT_BUS_MESSAGE_COUNTER));
        NotificationsManager.getInstance().SetupBadger(WhatsCloneApplication.getInstance());
    }


    public boolean checkIfMessageExist(String messageId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long size;
        try {
            RealmQuery<MessageModel> query = realm.where(MessageModel.class).equalTo("_id", messageId);
            size = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        AppHelper.LogCat("size " + size);
        return size != 0;
    }

    public boolean checkIfMessageIsWaiting(String messageId, Realm realm) {
        long size;

            RealmQuery<MessageModel> query = realm.where(MessageModel.class)
                    .equalTo("_id", messageId)
                    .equalTo("status", AppConstants.IS_WAITING);
            size = query.count();

        AppHelper.LogCat("size " + size);
        return size != 0;
    }

    private String conversationId;

    public void sendMessageGroupActions(String groupID, String created, String state) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        String lastID = RealmBackupRestore.getMessageLastId();
        try {
            realm.executeTransactionAsync(realm1 -> {
                UsersModel usersModelSender = realm1.where(UsersModel.class).equalTo("_id", PreferenceManager.getInstance().getID(WhatsCloneApplication.getInstance())).findFirst();
                GroupModel groupModel = realm1.where(GroupModel.class).equalTo("_id", groupID).findFirst();

                ConversationModel conversationsModel = realm1.where(ConversationModel.class).equalTo("group._id", groupID).findFirst();
                conversationId = conversationsModel.get_id();
                MessageModel messagesModel = realm1.createObject(MessageModel.class, UniqueId.generateUniqueId());
                messagesModel.set_id(lastID);
                messagesModel.setCreated(created);
                messagesModel.setStatus(AppConstants.IS_WAITING);
                messagesModel.setGroup(groupModel);
                messagesModel.setSender(usersModelSender);
                messagesModel.setIs_group(true);
                messagesModel.setMessage("null");
                messagesModel.setLatitude("null");
                messagesModel.setLongitude("null");
                messagesModel.setFile("null");
                messagesModel.setFile_type("null");
                messagesModel.setState(state);
                messagesModel.setFile_size("0");
                messagesModel.setDuration_file("0");
                messagesModel.setReply_id("null");
                messagesModel.setReply_message(true);
                messagesModel.setDocument_name("null");
                messagesModel.setDocument_type("null");
                messagesModel.setFile_upload(true);
                messagesModel.setFile_downLoad(true);
                messagesModel.setConversationId(conversationsModel.get_id());
                conversationsModel.setLatestMessage(messagesModel);
                conversationsModel.setCreated(created);
                conversationsModel.setUnread_message_counter(0);
                realm1.copyToRealmOrUpdate(conversationsModel);


            }, () -> {

                //new Handler().postDelayed(() -> JobsManager.getInstance().sendGroupMessagesToServer(), 500);
                WorkJobsManager.getInstance().sendUserMessagesToServer();
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_NEW_MESSAGE_CONVERSATION_OLD_ROW, conversationId));
                EventBus.getDefault().post(new Pusher(AppConstants.EVENT_BUS_REFRESH_MESSAGEGS));
            }, error -> {
                AppHelper.LogCat("Save group message failed MessagesPopupActivity " + error.getMessage());
            });
        } finally {
            if (!realm.isClosed())
                realm.close();
        }


    }


    public String getConversationId(String recipientId, Realm realm) {
        try {
            ConversationModel conversationsModelNew = realm.where(ConversationModel.class)
                    .equalTo("owner._id", recipientId)
                    .findFirst();
            return conversationsModelNew.get_id();
        } catch (Exception e) {
            AppHelper.LogCat("Get conversation id Exception MessagesPopupActivity " + e.getMessage());
            return null;
        }
    }
}