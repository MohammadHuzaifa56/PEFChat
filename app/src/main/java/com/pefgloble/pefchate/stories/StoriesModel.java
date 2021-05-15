package com.pefgloble.pefchate.stories;



import com.pefgloble.pefchate.JsonClasses.otherClasses.UniqueId;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Abderrahim El imame on 7/11/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class StoriesModel extends RealmObject {


    @PrimaryKey
    private long id = UniqueId.generateUniqueId();

    private String _id;
    private String username;
    private String userImage;
    private boolean downloaded;
    private String preview;
    private RealmList<StoryModel> stories;

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getPreview() {
        return preview;
    }

    public void setPreview(String preview) {
        this.preview = preview;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded = downloaded;
    }

    public RealmList<StoryModel> getStories() {
        return stories;
    }

    public void setStories(RealmList<StoryModel> stories) {
        this.stories = stories;
    }
}
