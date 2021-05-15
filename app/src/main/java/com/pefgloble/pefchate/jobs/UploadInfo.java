package com.pefgloble.pefchate.jobs;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 10/18/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class UploadInfo extends RealmObject {
    @PrimaryKey
    private String uploadId;

    public String getUploadId() {
        return uploadId;
    }

    public void setUploadId(String uploadId) {
        this.uploadId = uploadId;
    }

}
