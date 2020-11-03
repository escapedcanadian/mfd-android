package com.couchbase.mobile.mfd.data.model;

/**
 * Data class that captures user information for users retrieved from LoginRepository
 */
public class User {

    public static final String ATR_USERNAME = "username";
    public static final String ATR_PASSWORD = "password";
    public static final String ATR_DISPLAY_NAME = "displayName";
    public static final String ATR_LAST_LOGIN = "lastLogin";
    private String userId;
    private String displayName;

    public User(String userId, String displayName) {
        this.userId = userId;
        this.displayName = displayName;
    }

    public String getUserId() {
        return userId;
    }

    public String getDisplayName() {
        return displayName;
    }
}