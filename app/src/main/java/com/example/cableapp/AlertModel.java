package com.example.cableapp;

class AlertModel {
    private String id;
    private String title;
    private String description;
    private String photoUrl;
    private boolean showAlert;
    private long timestamp;
    private boolean live;
    private long liveTimestamp;

    public AlertModel() {
    }

    public long getLiveTimestamp() {
        return liveTimestamp;
    }

    public void setLiveTimestamp(long liveTimestamp) {
        this.liveTimestamp = liveTimestamp;
    }

    public AlertModel(String id, String title, String description, String photoUrl, boolean showAlert, long timestamp, boolean live, long liveTimestamp) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.showAlert = showAlert;
        this.timestamp = timestamp;
        this.live = live;
        this.liveTimestamp = liveTimestamp;
    }

    public AlertModel(String id, String title, String description, String photoUrl, boolean showAlert, long timestamp, boolean live) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.photoUrl = photoUrl;
        this.showAlert = showAlert;
        this.timestamp = timestamp;
        this.live = live;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getPhotoUrl() {
        return photoUrl;
    }

    public void setPhotoUrl(String photoUrl) {
        this.photoUrl = photoUrl;
    }

    public boolean isShowAlert() {
        return showAlert;
    }

    public void setShowAlert(boolean showAlert) {
        this.showAlert = showAlert;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isLive() {
        return live;
    }

    public void setLive(boolean live) {
        this.live = live;
    }
}
