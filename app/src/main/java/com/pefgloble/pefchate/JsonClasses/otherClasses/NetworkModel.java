package com.pefgloble.pefchate.JsonClasses.otherClasses;

public class NetworkModel {


    private boolean success;
    private String message;

    public boolean isConnected() {
        return success;
    }

    public void setConnected(boolean success) {
        this.success = success;
    }

    public String getStatus() {
        return message;
    }

    public void setStatus(String message) {
        this.message = message;
    }
}
