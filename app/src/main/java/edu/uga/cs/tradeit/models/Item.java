package edu.uga.cs.tradeit.models;

public class Item {
    public String id;
    public String name;
    public String categoryId;
    public String postedBy;
    public long postedAt;
    public double price;
    public boolean free;

    public Item() { }

    public Item(String id, String name, String categoryId, String postedBy,
                long postedAt, double price, boolean free) {
        this.id = id;
        this.name = name;
        this.categoryId = categoryId;
        this.postedBy = postedBy;
        this.postedAt = postedAt;
        this.price = price;
        this.free = free;
    }
}
