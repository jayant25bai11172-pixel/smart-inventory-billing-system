package com.retail.inventory.model;

import com.retail.inventory.exception.InsufficientStockException;

import java.io.Serializable;
import java.util.*;

/**
 * Manages the collection of items selected by a customer for billing.
 * Contains business logic for subtotal, discount calculation, and validations.
 */
public class Cart implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Map<String, CartItem> items;
    private double discountPercentage;

    public Cart() {
        this.items = new LinkedHashMap<>();
        this.discountPercentage = 0.0;
    }

    /**
     * Adds a product to the cart with the desired quantity.
     * Validates against current available inventory.
     */
    public synchronized void addItem(Product product, int quantity) throws InsufficientStockException {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }

        int currentInCart = items.containsKey(product.getId()) ? items.get(product.getId()).getQuantity() : 0;
        int requestedTotal = currentInCart + quantity;

        if (requestedTotal > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getId(), product.getName(),
                    product.getStockQuantity(), requestedTotal);
        }

        if (items.containsKey(product.getId())) {
            items.get(product.getId()).incrementQuantity(quantity);
        } else {
            items.put(product.getId(), new CartItem(product, quantity));
        }
    }

    /**
     * Updates the exact quantity of a product in the cart.
     */
    public synchronized void updateQuantity(Product product, int newQuantity) throws InsufficientStockException {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null.");
        }
        if (newQuantity <= 0) {
            removeItem(product.getId());
            return;
        }

        if (newQuantity > product.getStockQuantity()) {
            throw new InsufficientStockException(product.getId(), product.getName(),
                    product.getStockQuantity(), newQuantity);
        }

        if (items.containsKey(product.getId())) {
            items.get(product.getId()).setQuantity(newQuantity);
        } else {
            items.put(product.getId(), new CartItem(product, newQuantity));
        }
    }

    public synchronized void removeItem(String productId) {
        items.remove(productId);
    }

    public synchronized void clear() {
        items.clear();
        this.discountPercentage = 0.0;
    }

    public synchronized boolean isEmpty() {
        return items.isEmpty();
    }

    public synchronized List<CartItem> getItems() {
        return new ArrayList<>(items.values());
    }

    public synchronized int getTotalItemCount() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public synchronized double getSubtotal() {
        return items.values().stream().mapToDouble(CartItem::getSubtotal).sum();
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public void setDiscountPercentage(double discountPercentage) {
        if (discountPercentage < 0 || discountPercentage > 100) {
            throw new IllegalArgumentException("Discount percentage must be between 0 and 100.");
        }
        this.discountPercentage = discountPercentage;
    }

    public double getDiscountAmount() {
        return (getSubtotal() * discountPercentage) / 100.0;
    }

    public double getTaxableAmount() {
        return Math.max(0.0, getSubtotal() - getDiscountAmount());
    }
}
