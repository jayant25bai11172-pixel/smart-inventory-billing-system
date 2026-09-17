package com.retail.inventory.payment;

import com.retail.inventory.exception.PaymentFailedException;
import com.retail.inventory.model.PaymentDetails;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Concrete payment strategy for Credit / Debit Card transactions.
 */
public class CardPayment implements PaymentMethod {

    @Override
    public String getMethodName() {
        return "CARD";
    }

    @Override
    public PaymentDetails processPayment(double amount, String inputReference) throws PaymentFailedException {
        if (inputReference == null || inputReference.trim().isEmpty()) {
            throw new PaymentFailedException(getMethodName(), "Card number cannot be empty.");
        }

        String cleanedCard = inputReference.replaceAll("[\\s-]", "");
        if (!cleanedCard.matches("^\\d{16}$")) {
            throw new PaymentFailedException(getMethodName(),
                    "Invalid card number format. Must be a 16-digit card number.");
        }

        String maskedCard = "****-****-****-" + cleanedCard.substring(12);
        String authCode = "AUTH-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        String refId = String.format("CARD[%s] / %s", maskedCard, authCode);

        return new PaymentDetails(getMethodName(), amount, refId, LocalDateTime.now(), "SUCCESS");
    }
}
