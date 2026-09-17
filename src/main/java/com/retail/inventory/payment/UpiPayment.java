package com.retail.inventory.payment;

import com.retail.inventory.exception.PaymentFailedException;
import com.retail.inventory.model.PaymentDetails;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Concrete payment strategy for UPI (Unified Payments Interface) transactions.
 */
public class UpiPayment implements PaymentMethod {

    @Override
    public String getMethodName() {
        return "UPI";
    }

    @Override
    public PaymentDetails processPayment(double amount, String inputReference) throws PaymentFailedException {
        if (inputReference == null || inputReference.trim().isEmpty()) {
            throw new PaymentFailedException(getMethodName(), "UPI ID / VPA cannot be empty.");
        }

        String upiId = inputReference.trim();
        if (!upiId.matches("^[a-zA-Z0-9.\\-_]{2,256}@[a-zA-Z]{2,64}$")) {
            throw new PaymentFailedException(getMethodName(),
                    "Invalid UPI ID format. Expected 'handle@bank' (e.g., 'retailer@okhdfcbank' or 'customer@okaxis').");
        }

        String upiTxnId = "UPI-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
        String refId = String.format("VPA: %s / Txn: %s", upiId, upiTxnId);

        return new PaymentDetails(getMethodName(), amount, refId, LocalDateTime.now(), "SUCCESS");
    }
}
