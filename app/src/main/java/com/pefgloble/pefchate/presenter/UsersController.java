package com.pefgloble.pefchate.presenter;

import android.annotation.SuppressLint;


import com.pefgloble.pefchate.AgoraVideo.openvcall.AGApplication;
import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;
import com.pefgloble.pefchate.app.WhatsCloneApplication;
import com.pefgloble.pefchate.helpers.AppHelper;
import com.pefgloble.pefchate.helpers.files.UsersPrivacyModel;

import io.realm.Realm;
import io.realm.RealmQuery;



/**
 * Created by Abderrahim El imame on 7/31/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */

@SuppressLint("CheckResult")
public class UsersController {

    private static volatile UsersController Instance = null;


    public UsersController() {
    }

    public static UsersController getInstance() {

        UsersController localInstance = Instance;
        if (localInstance == null) {
            synchronized (UsersController.class) {
                localInstance = Instance;
                if (localInstance == null) {
                    Instance = localInstance = new UsersController();
                }
            }
        }
        return localInstance;

    }


    public UsersModel getUserById(String userId) {

        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        try {

            return realm.where(UsersModel.class).equalTo("_id", userId).findFirst();
        } catch (Exception e) {
            return null;
        } finally {
            if (!realm.isClosed())
                realm.close();
        }
    }

    public boolean checkIfPrivacyUserExist(String userId) {
        Realm realm = WhatsCloneApplication.getRealmDatabaseInstance(AGApplication.getInstance());
        long size;
        try {
            RealmQuery<UsersPrivacyModel> query = realm.where(UsersPrivacyModel.class).equalTo("_id", userId);
            size = query.count();
        } finally {
            if (!realm.isClosed()) realm.close();
        }
        AppHelper.LogCat("size " + size);
        return size != 0;
    }

}