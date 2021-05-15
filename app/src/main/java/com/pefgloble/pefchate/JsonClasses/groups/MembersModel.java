package com.pefgloble.pefchate.JsonClasses.groups;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;



public class MembersModel extends RealmObject {
    @PrimaryKey
    private String _id;

    private String groupId;

    private boolean left;

    private boolean deleted;

    private boolean admin;

    private UsersModel owner;

    public MembersModel() {

    }


    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public boolean isLeft() {
        return left;
    }

    public void setLeft(boolean left) {
        this.left = left;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public UsersModel getOwner() {
        return owner;
    }

    public void setOwner(UsersModel owner) {
        this.owner = owner;
    }
}
