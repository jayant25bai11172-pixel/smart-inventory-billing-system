package com.retail.inventory.payment;

import com.retail.inventory.exception.PaymentFailedException;
import com.retail.inventory.model.PaymentDetails;

/**
 * Interface defining the contract for handling customer payments.
 * Highlights the OOP concept of Abstraction and Polymorphism.
 */
public interface PaymentMethod {
    /**
     * Identifies the payment method mode (e.g. CASH, CARD, UPI).
     */
    String getMethodName();

    /**
     * Executes the payment processing for the billed total.
     *
     * @param amount The bill amount to pay
     * @param inputReference Specific input like cash tendered, card number, or UPI ID
     * @return PaymentDetails containing transaction metadata upon success
     * @throws PaymentFailedException if payment validation fails or amount is insufficient
     */
    PaymentDetails processPayment(double amount, String inputReference) throws PaymentFailedException;
}
