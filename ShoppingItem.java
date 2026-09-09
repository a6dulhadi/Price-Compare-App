package com.pricecompare.app.models;

public class ShoppingItem {
    private String id;
    private String name;
    private String quantity;
    private boolean isBought;

    public ShoppingItem() {
        
    }

    public ShoppingItem(String name, String quantity) {
        this.name = name;
        this.quantity = quantity;
        this.isBought = false;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getQuantity() { return quantity; }
    public void setQuantity(String quantity) { this.quantity = quantity; }

    public boolean isBought() { return isBought; }
    public void setBought(boolean bought) { isBought = bought; }
}
