package com.pefgloble.pefchate.JsonClasses.otherClasses;


import com.pefgloble.pefchate.JsonClasses.messags.MessageModel;

public class MessageUploadInfo {

    private int percentage;
    private String type;
    private String status;
    private MessageModel messageModel;

    public MessageModel getMessageModel() {
        return messageModel;
    }

    public void setMessageModel(MessageModel messageModel) {
        this.messageModel = messageModel;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getPercentage() {
        return percentage;
    }

    public void setPercentage(int percentage) {
        this.percentage = percentage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
