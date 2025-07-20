package com.signup.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(hidden = true)
public class AuthResponse {
    private boolean success;
    private String message;
    private String token;
    private String refreshToken;
    private UserInfo user;

    public AuthResponse() {}

    public AuthResponse(boolean success, String message, String token, UserInfo user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.user = user;
    }

    // Constructor with refresh token
    public AuthResponse(boolean success, String message, String token, String refreshToken, UserInfo user) {
        this.success = success;
        this.message = message;
        this.token = token;
        this.refreshToken = refreshToken;
        this.user = user;
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

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }

    public UserInfo getUser() {
        return user;
    }

    public void setUser(UserInfo user) {
        this.user = user;
    }

    public static class UserInfo {
        private String userEmail;
        private String userName;

        public UserInfo() {}

        public UserInfo(String userEmail, String userName) {
            this.userEmail = userEmail;
            this.userName = userName;
        }

        public String getUserEmail() {
            return userEmail;
        }

        public void setUserEmail(String userEmail) {
            this.userEmail = userEmail;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }
    }
} 