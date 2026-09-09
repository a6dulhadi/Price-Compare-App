package com.pricecompare.app.pricecompare;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ProductGroup {

    private String productName;
    private final List<PriceEntry> entries = new ArrayList<>();

    public ProductGroup() {}

    public ProductGroup(String productName) {
        this.productName = productName;
    }

    public String getProductName() { return productName; }

    public List<PriceEntry> getEntries() { return entries; }

    public void addEntry(PriceEntry entry) { entries.add(entry); }

    public String getCategory() {
        PriceEntry latest = null;
        for (PriceEntry e : entries) {
            if (latest == null || e.getTimestamp() > latest.getTimestamp()) latest = e;
        }
        return latest != null ? latest.getCategory() : null;
    }

    public PriceEntry getLowestEntry() {
        PriceEntry lowest = null;
        for (PriceEntry e : entries) {
            if (lowest == null || e.getPrice() < lowest.getPrice()) lowest = e;
        }
        return lowest;
    }

    public String getFirstImagePath() {
        for (PriceEntry e : entries) {
            if (e.getImageUrl() != null && !e.getImageUrl().isEmpty()) {
                return e.getImageUrl();
            }
        }
        return null;
    }

    public int getStoreCount() {
        Set<String> unique = new HashSet<>();
        for (PriceEntry e : entries) {
            if (e.getStoreName() != null) unique.add(e.getStoreName().trim().toLowerCase());
        }
        return unique.size();
    }

    public int getEntryCount() { return entries.size(); }
}
