package com.pefgloble.pefchate.JsonClasses.otherClasses;


public class UpdateConversationRow {

    private int status ;
    private boolean group;
    private String member_name;

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public boolean isGroup() {
        return group;
    }

    public void setGroup(boolean group) {
        this.group = group;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
