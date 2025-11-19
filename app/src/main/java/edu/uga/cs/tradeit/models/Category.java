package edu.uga.cs.tradeit.models;

public class Category {
    public String id;
    public String name;
    public String createdBy;
    public long createdAt;

    // Default constructor required for Firebase
    public Category() {
    }

    public Category(String id, String name, String createdBy, long createdAt) {
        this.id = id;
        this.name = name;
        this.createdBy = createdBy;
        this.createdAt = createdAt;
    }
}
