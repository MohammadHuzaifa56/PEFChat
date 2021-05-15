package com.pefgloble.pefchate.JsonClasses.calls;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CallsModel extends RealmObject {


    @PrimaryKey
    private long id = UniqueId.generateUniqueId();

    private String _id;
    private boolean received;
    private int counter;

    private String type;
    private String phone;
    private String username;
    private String date;
    private String duration;
    private String from;
    private String to;
    private UsersModel contactsModel;
    private RealmList<CallsInfoModel> callsInfoModels;

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

    public UsersModel getContactsModel() {
        return contactsModel;
    }

    public void setContactsModel(UsersModel contactsModel) {
        this.contactsModel = contactsModel;
    }

    public int getCounter() {
        return counter;
    }

    public void setCounter(int counter) {
        this.counter = counter;
    }

    public RealmList<CallsInfoModel> getCallsInfoModels() {
        return callsInfoModels;
    }

    public void setCallsInfoModels(RealmList<CallsInfoModel> callsInfoModels) {
        this.callsInfoModels = callsInfoModels;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }


    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }
}
