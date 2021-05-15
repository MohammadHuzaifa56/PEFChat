package com.pefgloble.pefchate.JsonClasses.groups;

import com.pefgloble.pefchate.JsonClasses.contacts.UsersModel;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;


public class GroupModel extends RealmObject {
    @PrimaryKey
    private String _id;

    private String created;

    private String image;

    private String name;

    private UsersModel owner;

    private RealmList<UsersModel> membersList;


    private RealmList<MembersModel> members;

    public GroupModel() {

    }

    public RealmList<UsersModel> getMembersList() {
        return membersList;
    }

    public void setMembersList(RealmList<UsersModel> membersList) {
        this.membersList = membersList;
    }

    public String get_id() {
        return _id;
    }

    public void set_id(String _id) {
        this._id = _id;
    }

    public String getCreated() {
        return created;
    }

    public void setCreated(String created) {
        this.created = created;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UsersModel getOwner() {
        return owner;
    }

    public void setOwner(UsersModel owner) {
        this.owner = owner;
    }

    public RealmList<MembersModel> getMembers() {
        return members;
    }

    public void setMembers(RealmList<MembersModel> members) {
        this.members = members;
    }
}
