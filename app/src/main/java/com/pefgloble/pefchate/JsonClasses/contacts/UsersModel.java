package com.pefgloble.pefchate.JsonClasses.contacts;

import com.pefgloble.pefchate.JsonClasses.status.StatusModel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UsersModel extends RealmObject {
    @PrimaryKey
    private String _id;

    private int contactId;

    private String username;

    private String phone;

    private String phone_qurey;

    private boolean linked;

    private boolean activate;

    private boolean exist;

    private String image;

    private String designation;

    private StatusModel status;

    public UsersModel() {

    }

    public String getDesignation() {
        return designation;
    }

    public void setDesignation(String designation) {
        this.designation = designation;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public int getContactId() {
        return contactId;
    }

    public void setContactId(int contactId) {
        this.contactId = contactId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPhone_qurey() {
        return phone_qurey;
    }

    public void setPhone_qurey(String phone_qurey) {
        this.phone_qurey = phone_qurey;
    }

    public boolean isLinked() {
        return linked;
    }

    public void setLinked(boolean linked) {
        this.linked = linked;
    }

    public boolean isActivate() {
        return activate;
    }

    public void setActivate(boolean activate) {
        this.activate = activate;
    }

    public boolean isExist() {
        return exist;
    }

    public void setExist(boolean exist) {
        this.exist = exist;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public StatusModel getStatus() {
        return status;
    }

    public void setStatus(StatusModel status) {
        this.status = status;
    }

}
