package com.signup.dto;

public class ProfileResponse {
    private boolean success;
    private String message;
    private UserProfile user;

    public ProfileResponse() {}

    public ProfileResponse(boolean success, String message, UserProfile user) {
        this.success = success;
        this.message = message;
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

    public UserProfile getUser() {
        return user;
    }

    public void setUser(UserProfile user) {
        this.user = user;
    }

    public static class UserProfile {
        private Long id;
        private String name;
        private String contactNumber;
        private String email;

        public UserProfile() {}

        public UserProfile(Long id, String name, String contactNumber, String email) {
            this.id = id;
            this.name = name;
            this.contactNumber = contactNumber;
            this.email = email;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getContactNumber() {
            return contactNumber;
        }

        public void setContactNumber(String contactNumber) {
            this.contactNumber = contactNumber;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }
} 