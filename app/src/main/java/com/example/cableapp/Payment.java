package com.example.cableapp;

public class Payment {
    private String paymentId;
    private String serialNo;
    private String paidTo;
    private double paidAmount;
    private double newDue,packageAmount;
    private String status;
    private long timestamp;


    public Payment() {
        // Default constructor required for calls to DataSnapshot.getValue(Payment.class)
    }

    public Payment(String paymentId, String serialNo, String paidTo, double paidAmount, double newDue, double packageAmount, String status, long timestamp) {
        this.paymentId = paymentId;
        this.serialNo = serialNo;
        this.paidTo = paidTo;
        this.paidAmount = paidAmount;
        this.newDue = newDue;
        this.packageAmount = packageAmount;
        this.status = status;
        this.timestamp = timestamp;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getPaidTo() {
        return paidTo;
    }

    public void setPaidTo(String paidTo) {
        this.paidTo = paidTo;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getNewDue() {
        return newDue;
    }

    public void setNewDue(double newDue) {
        this.newDue = newDue;
    }

    public double getPackageAmount() {
        return packageAmount;
    }

    public void setPackageAmount(double packageAmount) {
        this.packageAmount = packageAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
