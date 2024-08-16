package com.example.cableapp;

import java.io.Serializable;

public class Customer implements Serializable {
    private String serialNo;
    private String customerId;
    private String customerName;
    private String apartmentNO;
    private String doorNo;
    private String address;
    private String gmail;
    private String mobileNo;
    private String stbSerialNo;
    private String activationDate;
    private String expiryDate;
    private String packageId;
    private String status;
    private int due;
    private String createdNo;
    private String boxTYpe;

    public Customer() {
    }

    public Customer(String serialNo, String customerId, String customerName, String apartmentNO, String doorNo, String address, String gmail, String mobileNo, String stbSerialNo, String activationDate, String expiryDate, String packageId, String status, int due, String createdNo, String boxTYpe) {
        this.serialNo = serialNo;
        this.customerId = customerId;
        this.customerName = customerName;
        this.apartmentNO = apartmentNO;
        this.doorNo = doorNo;
        this.address = address;
        this.gmail = gmail;
        this.mobileNo = mobileNo;
        this.stbSerialNo = stbSerialNo;
        this.activationDate = activationDate;
        this.expiryDate = expiryDate;
        this.packageId = packageId;
        this.status = status;
        this.due = due;
        this.createdNo = createdNo;
        this.boxTYpe = boxTYpe;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getApartmentNo() {
        return apartmentNO;
    }

    public void setApartmentNo(String apartmentNO) {
        this.apartmentNO = apartmentNO;
    }

    public String getDoorNo() {
        return doorNo;
    }

    public void setDoorNo(String doorNo) {
        this.doorNo = doorNo;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getGmail() {
        return gmail;
    }

    public void setGmail(String gmail) {
        this.gmail = gmail;
    }

    public String getMobileNo() {
        return mobileNo;
    }

    public void setMobileNo(String mobileNo) {
        this.mobileNo = mobileNo;
    }

    public String getStbSerialNo() {
        return stbSerialNo;
    }

    public void setStbSerialNo(String stbSerialNo) {
        this.stbSerialNo = stbSerialNo;
    }

    public String getActivationNo() {
        return activationDate;
    }

    public void setActivationNo(String activationDate) {
        this.activationDate = activationDate;
    }

    public String getExpiryNo() {
        return expiryDate;
    }

    public void setExpiryNo(String expiryDate) {
        this.expiryDate = expiryDate;
    }

    public String getPackageId() {
        return packageId;
    }

    public void setPackageId(String packageId) {
        this.packageId = packageId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getDue() {
        return due;
    }

    public void setDue(int due) {
        this.due = due;
    }

    public String getCreatedNo() {
        return createdNo;
    }

    public void setCreatedNo(String createdNo) {
        this.createdNo = createdNo;
    }

    public String getBoxType() {
        return boxTYpe;
    }

    public void setBoxType(String boxTYpe) {
        this.boxTYpe = boxTYpe;
    }
}