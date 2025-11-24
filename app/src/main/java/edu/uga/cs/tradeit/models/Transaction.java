package edu.uga.cs.tradeit.models;

public class Transaction {
    public String id;
    public String itemId;
    public String itemName;
    public String buyerId;
    public String sellerId;
    public long timestamp;
    public boolean buyerConfirmed;
    public boolean sellerConfirmed;
    public boolean completed;
    public long completedAt;

    public Transaction() {}

    public Transaction(String id, String itemId, String itemName, String buyerId,
                       String sellerId, long timestamp) {
        this.id = id;
        this.itemId = itemId;
        this.itemName = itemName;
        this.buyerId = buyerId;
        this.sellerId = sellerId;
        this.timestamp = timestamp;
        this.buyerConfirmed = false;
        this.sellerConfirmed = false;
        this.completed = false;
        this.completedAt = 0;
    }
}
