package com.retail.inventory.exception;

/**
 * Thrown when a product is requested with an ID that does not exist in the catalog.
 */
public class ProductNotFoundException extends SmartInventoryException {
    private final String productId;

    public ProductNotFoundException(String productId) {
        super(String.format("Product with ID '%s' was not found in the inventory catalog.", productId));
        this.productId = productId;
    }

    public String getProductId() {
        return productId;
    }
}
