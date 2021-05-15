package com.pefgloble.pefchate.JsonClasses.groups;

import io.realm.RealmList;

public class GroupResponse {
    private boolean success;
    private String message;
    private String groupId;
    private String groupImage;
    private RealmList<MembersModelJson> membersModels;

    public GroupResponse() {

    }

    public RealmList<MembersModelJson> getMembersModels() {
        return membersModels;
    }

    public void setMembersModels(RealmList<MembersModelJson> membersModels) {
        this.membersModels = membersModels;
    }

    public String getGroupImage() {
        return groupImage;
    }

    public void setGroupImage(String groupImage) {
        this.groupImage = groupImage;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }


    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
