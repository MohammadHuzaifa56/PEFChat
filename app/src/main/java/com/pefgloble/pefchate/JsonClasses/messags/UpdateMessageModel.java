package com.pefgloble.pefchate.JsonClasses.messags;

import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UpdateMessageModel {
    public UpdateMessageModel() {
    }
    @PrimaryKey
    private long id = UniqueId.generateUniqueId();

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    private String otherUserId;
    private String message;
    private String created;
    private String file;
    private String file_type;
    private String file_size;
    private String duration_file;
    private String state;
    private String longitude;
    private String latitude;

    private String messageId;
    private String conversationId;
    private String groupId;
    private List<String> members_ids;
    private String senderId;

    private String reply_id;
    private boolean reply_message;
    private String document_type;
    private String document_name;

    public UpdateMessageModel(String otherUserId, String message, String senderId) {
        this.otherUserId = otherUserId;
        this.message = message;
        this.senderId = senderId;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public boolean isReply_message() {
        return reply_message;
    }

    public void setReply_message(boolean reply_message) {
        this.reply_message = reply_message;
    }

    public String getDocument_type() {
        return document_type;
    }

    public void setDocument_type(String document_type) {
        this.document_type = document_type;
    }

    public String getDocument_name() {
        return document_name;
    }

    public void setDocument_name(String document_name) {
        this.document_name = document_name;
    }

    public String getSenderId() {
        return senderId;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public String getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(String otherUserId) {
        this.otherUserId = otherUserId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getFile_type() {
        return file_type;
    }

    public void setFile_type(String file_type) {
        this.file_type = file_type;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getDuration_file() {
        return duration_file;
    }

    public void setDuration_file(String duration_file) {
        this.duration_file = duration_file;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public List<String> getMembers_ids() {
        return members_ids;
    }

    public void setMembers_ids(List<String> members_ids) {
        this.members_ids = members_ids;
    }
}
