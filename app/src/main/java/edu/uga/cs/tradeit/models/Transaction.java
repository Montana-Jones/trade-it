package edu.uga.cs.tradeit.models;

public class Transaction {
    public String id;
    public String itemId;
    public String itemName;
    public String buyerId;
    public String sellerId;
    public long timestamp;
    public boolean completed;
    public long completedAt;

    public Transaction() { }

    public Transaction(String id, String itemId, String itemName,
                       String buyerId, String sellerId,
                       long timestamp, boolean completed, long completedAt) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.timestamp = timestamp;
        this.completed = completed;
        this.completedAt = completedAt;
    }
}
