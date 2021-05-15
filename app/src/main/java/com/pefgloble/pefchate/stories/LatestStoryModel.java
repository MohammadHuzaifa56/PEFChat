package com.pefgloble.pefchate.stories;

public class LatestStoryModel {
    String id;
    String date;
    String lastUrl;

    public LatestStoryModel() {
    }

    public LatestStoryModel(String id, String date, String lastUrl) {
        this.id = id;
        this.date = date;
        this.lastUrl = lastUrl;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getLastUrl() {
        return lastUrl;
    }

    public void setLastUrl(String lastUrl) {
        this.lastUrl = lastUrl;
    }
}
