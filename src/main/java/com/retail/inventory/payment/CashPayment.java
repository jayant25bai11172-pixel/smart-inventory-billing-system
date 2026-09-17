package com.retail.inventory.payment;

import com.retail.inventory.exception.PaymentFailedException;
import com.retail.inventory.model.PaymentDetails;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Concrete payment strategy for Cash transactions.
 */
public class CashPayment implements PaymentMethod {

    @Override
    public String getMethodName() {
        return "CASH";
    }

    @Override
    public PaymentDetails processPayment(double amount, String inputReference) throws PaymentFailedException {
        if (inputReference == null || inputReference.trim().isEmpty()) {
            throw new PaymentFailedException(getMethodName(), "Cash tendered amount cannot be empty.");
        }

        double cashTendered;
        try {
            cashTendered = Double.parseDouble(inputReference.trim());
        } catch (NumberFormatException e) {
            throw new PaymentFailedException(getMethodName(), "Invalid cash amount format: " + inputReference);
        }

        if (cashTendered < amount) {
            double shortfall = amount - cashTendered;
            throw new PaymentFailedException(getMethodName(),
                    String.format("Insufficient cash tendered. Bill is Rs.%.2f, tendered Rs.%.2f (Shortfall: Rs.%.2f)",
                            amount, cashTendered, shortfall));
        }

        double changeDue = cashTendered - amount;
        String refId = "CASH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase()
                + " (Tendered: Rs." + String.format("%.2f", cashTendered)
                + ", Change: Rs." + String.format("%.2f", changeDue) + ")";

        return new PaymentDetails(getMethodName(), amount, refId, LocalDateTime.now(), "SUCCESS");
    }
}
