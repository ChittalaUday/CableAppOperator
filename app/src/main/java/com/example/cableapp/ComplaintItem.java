package com.example.cableapp;

public class ComplaintItem {
    public static final int TYPE_HEADER = 0;
    public static final int TYPE_COMPLAINT = 1;

    private int type;
    private String header;
    private Complaint complaint;

    public ComplaintItem(int type, String header) {
        this.type = type;
        this.header = header;
    }

    public ComplaintItem(int type, Complaint complaint) {
        this.type = type;
        this.complaint = complaint;
    }

    public int getType() {
        return type;
    }

    public String getHeader() {
        return header;
    }

    public Complaint getComplaint() {
        return complaint;
    }
}
