package com.example.pantrymanager.model;

/**
 * One ingredient the user has at home. Maps to a row in the pantry_items table.
 */
public class PantryItem {

    private long id;
    private String name;
    private double quantity;
    private String unit;
    /** ISO date (yyyy-MM-dd) or null when the user did not set one. */
    private String expiryDate;

    public PantryItem() {
    }

    public PantryItem(long id, String name, double quantity, String unit, String expiryDate) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.unit = unit;
        this.expiryDate = expiryDate;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) { this.quantity = quantity; }

    public String getUnit() { return unit; }
    public void setUnit(String unit) { this.unit = unit; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public boolean hasExpiryDate() {
        return expiryDate != null && !expiryDate.trim().isEmpty();
    }
}
