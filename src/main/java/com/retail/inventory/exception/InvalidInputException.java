package com.retail.inventory.exception;

/**
 * Thrown when user input validation fails.
 */
public class InvalidInputException extends SmartInventoryException {
    private final String fieldName;

    public InvalidInputException(String fieldName, String message) {
        super(String.format("Invalid value for field '%s': %s", fieldName, message));
        this.fieldName = fieldName;
    }

    public String getFieldName() {
        return fieldName;
    }
}
