package com.pricecompare.app.models;

public class UserProfile {
    private String name;
    private int age;
    private String email;
    private String profilePicUrl;

    public UserProfile() {
        
    }

    public UserProfile(String name, int age, String email, String profilePicUrl) {
        this.name = name;
        this.age = age;
        this.email = email;
        this.profilePicUrl = profilePicUrl;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getProfilePicUrl() { return profilePicUrl; }
    public void setProfilePicUrl(String profilePicUrl) { this.profilePicUrl = profilePicUrl; }
}
