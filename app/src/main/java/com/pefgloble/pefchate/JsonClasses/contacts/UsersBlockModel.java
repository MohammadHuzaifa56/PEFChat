package com.pefgloble.pefchate.JsonClasses.contacts;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class UsersBlockModel extends RealmObject {
    @PrimaryKey
    private String _id;
    private UsersModel usersModel;

    public String getId() {
        return _id;
    }

    public void setId(String id) {
        this._id = id;
    }

    public UsersModel getUsersModel() {
        return usersModel;
    }

    public void setUsersModel(UsersModel usersModel) {
        this.usersModel = usersModel;
    }
}
