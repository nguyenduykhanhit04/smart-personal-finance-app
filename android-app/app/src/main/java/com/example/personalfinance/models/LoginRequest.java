package com.example.personalfinance.models;

public class LoginRequest {
    private String firebaseUid;
    private String email;
    private String fullName;
    private String avatarUrl;

    public LoginRequest() {}

    public LoginRequest(String firebaseUid, String email, String fullName, String avatarUrl) {
        this.firebaseUid = firebaseUid;
        this.email = email;
        this.fullName = fullName;
        this.avatarUrl = avatarUrl;
    }

    public String getFirebaseUid() {
        return firebaseUid;
    }

    public void setFirebaseUid(String firebaseUid) {
        this.firebaseUid = firebaseUid;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
