package com.pefgloble.pefchate.helpers;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;

import com.pefgloble.pefchate.jobs.WorkJobsManager;

/**
 * Created by Abderrahim El imame on 1/30/19.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class ContactsChangeObserver extends ContentObserver {

    private static final String TAG = "ContactsChangeObserver";
    Context context;
    /**
     * Creates a content observer.
     *
     * @param handler The handler to run {@link #onChange} on, or null if none.
     */
    public ContactsChangeObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
    }

    @Override
    public boolean deliverSelfNotifications() {
        return true;
    }

    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
      AppHelper.LogCat( "ContentObserver is called for contacts change");
        WorkJobsManager.getInstance().syncingContactsWithServerWorkerInit();
    }

}