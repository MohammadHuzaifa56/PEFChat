package com.pefgloble.pefchate.stories;

public class StorySeenListModel {

    String seenDate,seenId,seenImage;

    public StorySeenListModel(String seenDate, String seenId, String seenImage) {
        this.seenDate = seenDate;
        this.seenId = seenId;
        this.seenImage = seenImage;
    }

    public StorySeenListModel() {
    }

    public String getSeenDate() {
        return seenDate;
    }

    public void setSeenDate(String seenDate) {
        this.seenDate = seenDate;
    }

    public String getSeenId() {
        return seenId;
    }

    public void setSeenId(String seenId) {
        this.seenId = seenId;
    }

    public String getSeenImage() {
        return seenImage;
    }

    public void setSeenImage(String seenImage) {
        this.seenImage = seenImage;
    }
}
