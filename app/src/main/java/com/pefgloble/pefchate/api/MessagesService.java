package com.pefgloble.pefchate.api;




import com.pefgloble.pefchate.JsonClasses.messags.ConversationModel;
import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;
import com.pefgloble.pefchate.app.AppConstants;

import java.util.List;

import io.reactivex.Observable;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * Created by Abderrahim El imame on 20/02/2016.
 * Email : abderrahim.elimame@gmail.com
 */
public class MessagesService {

    private Realm realm;

    public MessagesService(Realm realm) {
        this.realm = realm;

    }

    /**
     * method to get all conversation messages
     *
     * @param conversationID this is the first parameter for getConversation method
     * @param recipientID    this is the second parameter for getConversation method
     * @param senderID       this is the thirded parameter for getConversation method
     * @return return value
     */
    public Observable<RealmResults<MessageModel>> getConversation(String conversationID, String recipientID, String senderID) {

        RealmResults<MessageModel> messages;
        if (conversationID == null) {

            messages = realm.where(MessageModel.class)
                    .beginGroup()
                    .beginGroup()
                    .equalTo("sender._id", recipientID)
                    .and()
                    .equalTo("recipient._id", senderID)
                    .endGroup()
                    .or()
                    .beginGroup()
                    .equalTo("sender._id", senderID)
                    .and()
                    .equalTo("recipient._id", recipientID)
                    .endGroup()
                    .endGroup()
                    .equalTo("is_group", false)
                    .sort("created", Sort.ASCENDING).findAllAsync();

        } else {
            messages = realm.where(MessageModel.class)
                    .equalTo("conversationId", conversationID)
                    .equalTo("is_group", false)
                    .sort("created", Sort.ASCENDING).findAllAsync();
        }
        return Observable.just(messages);
    }

    /**
     * method to get messages list from local
     *
     * @param groupId this is parameter for getConversation method
     * @return return value
     */
    public Observable<RealmResults<MessageModel>> getConversation(String groupId) {
        return Observable.just(realm.where(MessageModel.class).equalTo("group._id", groupId).equalTo("is_group", true).sort("created", Sort.ASCENDING).findAllAsync());

    }

    public Observable<List<MessageModel>> getLastMessage(String conversationID) {
        List<MessageModel> messages = realm.where(MessageModel.class).equalTo("conversationId", conversationID).sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }

    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Observable<List<MessageModel>> getUserMedia(String recipientID, String senderID) {
        List<MessageModel> messages;
        ConversationModel conversationsModel = realm.where(ConversationModel.class)
                .beginGroup()
                .equalTo("recipient._id", recipientID)
                .or()
                .equalTo("recipient._id", senderID)
                .endGroup()
                .findAll()
                .first();

        messages = realm.where(MessageModel.class)
                .notEqualTo("file", "null")
                .notEqualTo("file_type", AppConstants.MESSAGES_DOCUMENT)
                .equalTo("file_upload", true)
                .equalTo("file_downLoad", true)
                .equalTo("conversationId", conversationsModel.getId())
                .equalTo("is_group", false)
                .sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }


    /**
     * method to get group media for profile
     *
     * @param groupID this is the first parameter for getGroupMedia method
     * @return return value
     */
    public Observable<List<MessageModel>> getGroupMedia(String groupID) {
        List<MessageModel> messages;

        messages = realm.where(MessageModel.class)
                .notEqualTo("file", "null")
                .notEqualTo("file_type", AppConstants.MESSAGES_DOCUMENT)
                .equalTo("file_upload", true)
                .equalTo("file_downLoad", true)
                .equalTo("group._id", groupID)
                .equalTo("is_group", true)
                .sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }


    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Observable<List<MessageModel>> getUserDocuments(String recipientID, String senderID) {
        List<MessageModel> messages;
        ConversationModel conversationsModel = realm.where(ConversationModel.class)
                .beginGroup()
                .equalTo("recipient._id", recipientID)
                .or()
                .equalTo("recipient._id", senderID)
                .endGroup()
                .findAll()
                .first();

        messages = realm.where(MessageModel.class)
                .notEqualTo("file", "null")
                .equalTo("file_type", AppConstants.MESSAGES_DOCUMENT)
                .equalTo("file_upload", true)
                .equalTo("file_downLoad", true)
                .equalTo("conversationId", conversationsModel.getId())
                .equalTo("is_group", false)
                .sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }

    /**
     * method to get group media for profile
     *
     * @param groupID this is the first parameter for getGroupMedia method
     * @return return value
     */
    public Observable<List<MessageModel>> getGroupDocuments(String groupID) {
        List<MessageModel> messages;

        messages = realm.where(MessageModel.class)
                .notEqualTo("file", "null")
                .equalTo("file_type", AppConstants.MESSAGES_DOCUMENT)
                .equalTo("file_upload", true)
                .equalTo("file_downLoad", true)
                .equalTo("group._id", groupID)
                .equalTo("is_group", true)
                .sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }

    /**
     * method to get user media for profile
     *
     * @param recipientID this is the first parameter for getUserMedia method
     * @param senderID    this is the second parameter for getUserMedia method
     * @return return value
     */
    public Observable<List<MessageModel>> getUserLinks(String recipientID, String senderID) {
        List<MessageModel> messages;
        ConversationModel conversationsModel = realm.where(ConversationModel.class)
                .beginGroup()
                .equalTo("recipient._id", recipientID)
                .or()
                .equalTo("recipient._id", senderID)
                .endGroup()
                .findAll()
                .first();

        messages = realm.where(MessageModel.class)
                /* .beginGroup()
                 .contains("message", "https")
                 .or()
                 .contains("message", "http")
                 .or()
                 .contains("message", "www.")
                 .endGroup()*/

                .contains("message", "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$")
                .equalTo("file_upload", true)
                .equalTo("file_downLoad", true)
                .equalTo("conversationId", conversationsModel.getId())
                .equalTo("is_group", false)
                .sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }

    /**
     * method to get group media for profile
     *
     * @param groupID this is the first parameter for getGroupMedia method
     * @return return value
     */
    public Observable<List<MessageModel>> getGroupLinks(String groupID) {
        List<MessageModel> messages;

        messages = realm.where(MessageModel.class)
                .contains("message", "^((https?|ftp)://|(www|ftp)\\.)?[a-z0-9-]+(\\.[a-z0-9-]+)+([/?].*)?$")
                .equalTo("file_upload", true)
                .equalTo("file_downLoad", true)
                .equalTo("group._id", groupID)
                .equalTo("is_group", true)
                .sort("created", Sort.ASCENDING).findAll();
        return Observable.just(messages);
    }


}
