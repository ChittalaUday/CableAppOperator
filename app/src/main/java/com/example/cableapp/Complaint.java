package com.example.cableapp;

import com.google.firebase.database.ServerValue;

public class Complaint {
    private String serialNo;
    private String complaintDetails;
    private long timestamp;
    private String status, solvedOn, solvedBy;
    private String complaintId,complaintType,remarks;

    public Complaint() {
    }

    public Complaint(String serialNo, String complaintDetails, long timestamp, String status, String solvedOn, String solvedBy, String complaintId, String complaintType, String remarks) {
        this.serialNo = serialNo;
        this.complaintDetails = complaintDetails;
        this.timestamp = timestamp;
        this.status = status;
        this.solvedOn = solvedOn;
        this.solvedBy = solvedBy;
        this.complaintId = complaintId;
        this.complaintType = complaintType;
        this.remarks = remarks;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getComplaintDetails() {
        return complaintDetails;
    }

    public void setComplaintDetails(String complaintDetails) {
        this.complaintDetails = complaintDetails;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSolvedOn() {
        return solvedOn;
    }

    public void setSolvedOn(String solvedOn) {
        this.solvedOn = solvedOn;
    }

    public String getSolvedBy() {
        return solvedBy;
    }

    public void setSolvedBy(String solvedBy) {
        this.solvedBy = solvedBy;
    }

    public String getComplaintId() {
        return complaintId;
    }

    public void setComplaintId(String complaintId) {
        this.complaintId = complaintId;
    }

    public String getComplaintType() {
        return complaintType;
    }

    public void setComplaintType(String complaintType) {
        this.complaintType = complaintType;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }
}