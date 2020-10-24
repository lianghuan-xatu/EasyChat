package com.xatu.easyChat.entity;

public class MyFriends {
    private String id;

    private String myUserId;

    private String myFriendUserId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMyUserId() {
        return myUserId;
    }

    public void setMyUserId(String myUserId) {
        this.myUserId = myUserId;
    }

    public String getMyFriendUserId() {
        return myFriendUserId;
    }

    public void setMyFriendUserId(String myFriendUserId) {
        this.myFriendUserId = myFriendUserId;
    }
}