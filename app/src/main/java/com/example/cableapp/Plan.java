package com.example.cableapp;

// Plan.java
public class Plan {
    private String id;
    private String name;
    private String price;
    private String details;
    private String category;

    // Default constructor for Firebase
    public Plan() {}

    public Plan(String id, String name, String price, String details, String category) {
        this.id = id;
        this.name = name;
        this.price = price;
        this.details = details;
        this.category = category;
    }

    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPrice() { return price; }
    public void setPrice(String price) { this.price = price; }
    public String getDetails() { return details; }
    public void setDetails(String details) { this.details = details; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
}
