package com.example.cableapp;

public class Worker {
    private String id;
    private String name;
    private String photoUrl;
    private String mobile;
    private String role;

    public Worker() { }

    public Worker(String id, String name, String photoUrl, String mobile, String role) {
        this.id = id;
        this.name = name;
        this.photoUrl = photoUrl;
        this.mobile = mobile;
        this.role = role;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
