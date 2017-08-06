package com.bgarlewicz.firebase.chatapp.firechat;

public class User {

    private String userName;
    private String userToken;
    private String userUid;
    private String photoUrl;

    public User() {
    }

    public User(String userName, String userToken, String userUid, String photoUrl) {
        this.userName = userName;
        this.userToken = userToken;
        this.userUid = userUid;
        this.photoUrl = photoUrl;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserUid() {
        return userUid;
    }

    public void setUserUid(String userUid) {
        this.userUid = userUid;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

}
