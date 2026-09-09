package com.pricecompare.app.pricecompare;

public class PriceEntry {

    private String id;          
    private String productName;
    private String storeName;
    private double price;
    private String notes;
    private String category;
    private String barcode;
    private long timestamp;
    private String imageUrl;

    public PriceEntry() {
        
    }

    public PriceEntry(String productName, String storeName, double price, String notes, String category, String barcode, String imageUrl) {
        this.productName = productName;
        this.storeName = storeName;
        this.price = price;
        this.notes = notes;
        this.category = category;
        this.barcode = barcode;
        this.imageUrl = imageUrl;
        this.timestamp = System.currentTimeMillis();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
