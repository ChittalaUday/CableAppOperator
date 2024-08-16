package com.example.cableapp;

public class Due {
    private double amount;
    private String remark;
    private boolean paid;
    private Object timestamp;
    private Object paidOnTimeStamp;

    // Default constructor
    public Due() {
        // Default constructor required by Firebase
    }

    public Due(double amount, String remark, boolean paid, Object timestamp, Object paidOnTimeStamp) {
        this.amount = amount;
        this.remark = remark;
        this.paid = paid;
        this.timestamp = timestamp;
        this.paidOnTimeStamp = paidOnTimeStamp;
    }

    public Object getPaidOnTimeStamp() {
        return paidOnTimeStamp;
    }

    public void setPaidOnTimeStamp(Object paidOnTimeStamp) {
        this.paidOnTimeStamp = paidOnTimeStamp;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public boolean isPaid() {
        return paid;
    }

    public void setPaid(boolean paid) {
        this.paid = paid;
    }

    public Object getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Object timestamp) {
        this.timestamp = timestamp;
    }
}
