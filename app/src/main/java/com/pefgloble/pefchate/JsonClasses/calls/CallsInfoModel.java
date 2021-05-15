package com.pefgloble.pefchate.JsonClasses.calls;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class CallsInfoModel extends RealmObject {
    @PrimaryKey
    private long id = UniqueId.generateUniqueId();

    private String _id;
    private boolean received;
    private int counter;
    private int status;
    private String type;
    private String phone;
    private String date;
    private String duration;
    private String from;
    private String callId;
    private String to;
    private UsersModel contactsModel;

    public void setId(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }


    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
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

    public String get_id() {
        return _id;
    }

    public void set_id(String id) {
        this._id = id;
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
