package com.retail.inventory.exception;

/**
 * Thrown when a payment attempt fails due to insufficient tender, invalid credentials, or processing error.
 */
public class PaymentFailedException extends SmartInventoryException {
    private final String paymentMode;

    public PaymentFailedException(String paymentMode, String reason) {
        super(String.format("Payment failed via %s: %s", paymentMode, reason));
        this.paymentMode = paymentMode;
    }

    public String getPaymentMode() {
        return paymentMode;
    }
}
