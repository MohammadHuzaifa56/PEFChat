package com.pefgloble.pefchate.JsonClasses.messags;


import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.groups.GroupModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class ConversationModel extends RealmObject {

    @PrimaryKey
    private long id = UniqueId.generateUniqueId();


    private String _id;
    private String created;
    private UsersModel owner;
    private GroupModel group;
    private MessageModel latestMessage;
    private int status;

    private boolean is_group;
    private int unread_message_counter;


    public ConversationModel() {

    }

    public int getUnread_message_counter() {
        return unread_message_counter;
    }

    public void setUnread_message_counter(int unread_message_counter) {
        this.unread_message_counter = unread_message_counter;
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


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public UsersModel getOwner() {
        return owner;
    }

    public void setOwner(UsersModel owner) {
        this.owner = owner;
    }

    public GroupModel getGroup() {
        return group;
    }

    public void setGroup(GroupModel group) {
        this.group = group;
    }

    public MessageModel getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(MessageModel latestMessage) {
        this.latestMessage = latestMessage;
    }
}
