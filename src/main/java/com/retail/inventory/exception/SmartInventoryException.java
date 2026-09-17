package com.retail.inventory.exception;

/**
 * Base checked exception for the Smart Inventory and Billing system.
 */
public class SmartInventoryException extends Exception {
    public SmartInventoryException(String message) {
        super(message);
    }

    public SmartInventoryException(String message, Throwable cause) {
        super(message, cause);
    }
}
