package com.pefgloble.pefchate.JsonClasses.messags;


import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class MessageModel extends RealmObject {

    @PrimaryKey
    private long id = UniqueId.generateUniqueId();


    private String _id;

    private String created;

    private String conversationId;

    private UsersModel sender;

    private UsersModel recipient;

    private String state;

    private GroupModel group;


    private String reply_id;

    private boolean reply_message;


    private boolean is_group;

    private String duration_file;

    private String file_size;

    private String message;

    private int status;

    private String file;

    private String file_type;

    private boolean file_upload;

    private boolean file_downLoad;

    private String longitude;

    private String latitude;

    private String document_name;

    private String document_type;


    public MessageModel() {

    }

    public String getDocument_name() {
        return document_name;
    }

    public void setDocument_name(String document_name) {
        this.document_name = document_name;
    }

    public String getDocument_type() {
        return document_type;
    }

    public void setDocument_type(String document_type) {
        this.document_type = document_type;
    }

    public boolean isReply_message() {
        return reply_message;
    }

    public void setReply_message(boolean reply_message) {
        this.reply_message = reply_message;
    }

    public String getReply_id() {
        return reply_id;
    }

    public void setReply_id(String reply_id) {
        this.reply_id = reply_id;
    }

    public boolean isIs_group() {
        return is_group;
    }

    public void setIs_group(boolean is_group) {
        this.is_group = is_group;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getConversationId() {
        return conversationId;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }


    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }


    public String getDuration_file() {
        return duration_file;
    }

    public void setDuration_file(String duration_file) {
        this.duration_file = duration_file;
    }

    public String getFile_size() {
        return file_size;
    }

    public void setFile_size(String file_size) {
        this.file_size = file_size;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
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

    public boolean isFile_upload() {
        return file_upload;
    }

    public void setFile_upload(boolean file_upload) {
        this.file_upload = file_upload;
    }

    public boolean isFile_downLoad() {
        return file_downLoad;
    }

    public void setFile_downLoad(boolean file_downLoad) {
        this.file_downLoad = file_downLoad;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public UsersModel getSender() {
        return sender;
    }

    public void setSender(UsersModel sender) {
        this.sender = sender;
    }

    public UsersModel getRecipient() {
        return recipient;
    }

    public void setRecipient(UsersModel recipient) {
        this.recipient = recipient;
    }

    public GroupModel getGroup() {
        return group;
    }

    public void setGroup(GroupModel group) {
        this.group = group;
    }
}
