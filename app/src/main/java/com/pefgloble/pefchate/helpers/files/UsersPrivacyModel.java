package com.pefgloble.pefchate.helpers.files;



import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 12/12/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */public class UsersPrivacyModel extends RealmObject {
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
