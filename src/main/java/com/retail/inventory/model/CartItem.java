package com.retail.inventory.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents an item inside a customer's shopping cart.
 */
public class CartItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Product product;
    private int quantity;

    public CartItem(Product product, int quantity) {
        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null in CartItem.");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.product = product;
        this.quantity = quantity;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero.");
        }
        this.quantity = quantity;
    }

    public void incrementQuantity(int amount) {
        if (amount > 0) {
            this.quantity += amount;
        }
    }

    public double getSubtotal() {
        return product.getPrice() * quantity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CartItem cartItem = (CartItem) o;
        return Objects.equals(product.getId(), cartItem.product.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(product.getId());
    }

    @Override
    public String toString() {
        return String.format("%-15s | %-20s | Qty: %-3d | Unit: Rs.%-7.2f | Total: Rs.%.2f",
                product.getId(), product.getName(), quantity, product.getPrice(), getSubtotal());
    }
}
