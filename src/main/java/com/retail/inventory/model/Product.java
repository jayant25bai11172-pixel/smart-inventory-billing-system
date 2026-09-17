package com.retail.inventory.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a retail product in the inventory catalog.
 * Demonstrates encapsulation, data validation, and stock management.
 */
public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;
    private String name;
    private String category;
    private double price;
    private int stockQuantity;
    private int lowStockThreshold;

    public Product(String id, String name, String category, double price, int stockQuantity, int lowStockThreshold) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("Product ID cannot be null or blank.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Product Name cannot be null or blank.");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Product Price cannot be negative.");
        }
        if (stockQuantity < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative.");
        }
        if (lowStockThreshold < 0) {
            throw new IllegalArgumentException("Low stock threshold cannot be negative.");
        }

        this.id = id.trim();
        this.name = name.trim();
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.lowStockThreshold = lowStockThreshold;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        if (name != null && !name.trim().isEmpty()) {
            this.name = name.trim();
        }
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = (category == null || category.trim().isEmpty()) ? "General" : category.trim();
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        if (price >= 0) {
            this.price = price;
        }
    }

    public synchronized int getStockQuantity() {
        return stockQuantity;
    }

    public synchronized void setStockQuantity(int stockQuantity) {
        if (stockQuantity >= 0) {
            this.stockQuantity = stockQuantity;
        }
    }

    public int getLowStockThreshold() {
        return lowStockThreshold;
    }

    public void setLowStockThreshold(int lowStockThreshold) {
        if (lowStockThreshold >= 0) {
            this.lowStockThreshold = lowStockThreshold;
        }
    }

    /**
     * Checks whether the current stock is at or below the safety threshold.
     */
    public synchronized boolean isLowStock() {
        return this.stockQuantity <= this.lowStockThreshold;
    }

    /**
     * Replenishes stock safely.
     * @param quantity Amount to add
     */
    public synchronized void addStock(int quantity) {
        if (quantity > 0) {
            this.stockQuantity += quantity;
        }
    }

    /**
     * Reduces stock safely.
     * @param quantity Amount to deduct
     * @return true if stock was deducted, false if insufficient
     */
    public synchronized boolean reduceStock(int quantity) {
        if (quantity > 0 && this.stockQuantity >= quantity) {
            this.stockQuantity -= quantity;
            return true;
        }
        return false;
    }

    /**
     * Convert product into CSV line for persistent storage.
     */
    public String toCsvLine() {
        return String.format("%s,%s,%s,%.2f,%d,%d",
                escapeCsv(id), escapeCsv(name), escapeCsv(category), price, stockQuantity, lowStockThreshold);
    }

    /**
     * Factory method to deserialize a Product from a CSV line.
     */
    public static Product fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 6) {
            throw new IllegalArgumentException("Invalid CSV line format for Product: " + line);
        }
        String id = parts[0].trim();
        String name = parts[1].trim();
        String category = parts[2].trim();
        double price = Double.parseDouble(parts[3].trim());
        int stock = Integer.parseInt(parts[4].trim());
        int threshold = Integer.parseInt(parts[5].trim());
        return new Product(id, name, category, price, stock, threshold);
    }

    private static String escapeCsv(String value) {
        if (value == null) return "";
        return value.replace(",", " ").trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Product[ID='%s', Name='%s', Category='%s', Price=Rs.%.2f, Stock=%d, Threshold=%d]",
                id, name, category, price, stockQuantity, lowStockThreshold);
    }
}
