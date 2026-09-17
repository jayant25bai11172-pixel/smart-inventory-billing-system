package com.retail.inventory.exception;

/**
 * Thrown when an inventory operation or checkout request exceeds available stock.
 */
public class InsufficientStockException extends SmartInventoryException {
    private final String productId;
    private final String productName;
    private final int availableStock;
    private final int requestedQuantity;

    public InsufficientStockException(String productId, String productName, int availableStock, int requestedQuantity) {
        super(String.format("Insufficient stock for product '%s' (%s). Available: %d, Requested: %d",
                productName, productId, availableStock, requestedQuantity));
        this.productId = productId;
        this.productName = productName;
        this.availableStock = availableStock;
        this.requestedQuantity = requestedQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getAvailableStock() {
        return availableStock;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }
}
