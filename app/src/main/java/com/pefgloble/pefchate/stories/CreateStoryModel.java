package com.pefgloble.pefchate.stories;


import java.util.Date;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by Abderrahim El imame on 12/11/18.
 *
 * @Email : abderrahim.elimame@gmail.com
 * @Author : https://twitter.com/Ben__Cherif
 * @Skype : ben-_-cherif
 */
public class CreateStoryModel  {

    private String storyId;
    private String file;
    private String body;
    private String duration;
    private String type;
    private String created;
    private List<String> ids;

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }
  /*  private String userId;
    private String storyId;
    private String file;
    private String body;
    private Long duration;
    private String type;
    private String url;
    private String created;

    private List<String> ids;

    public CreateStoryModel() {
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public CreateStoryModel(String storyId, String file, String body, Long duration, String type, String created, String url, List<String> ids,String userId) {
        this.storyId = storyId;
        this.file = file;
        this.body = body;
        this.duration = duration;
        this.type = type;
        this.created = created;
        this.ids = ids;
        this.url=url;
        this.userId=userId;
    }

    public String getStoryId() {
        return storyId;
    }

    public void setStoryId(String storyId) {
        this.storyId = storyId;
    }

    public String getFile() {
        return file;
    }

    public void setFile(String file) {
        this.file = file;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public Long getDuration() {
        return duration;
    }

    public void setDuration(Long duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated (String created) {
        this.created = created;
    }

    public List<String> getIds() {
        return ids;
    }

    public void setIds(List<String> ids) {
        this.ids = ids;
    }*/
}
