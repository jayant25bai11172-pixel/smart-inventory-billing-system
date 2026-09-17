package com.retail.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Stores the audit trail of a completed transaction payment.
 */
public class PaymentDetails implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String paymentMode;
    private final double amountPaid;
    private final String transactionReference;
    private final LocalDateTime timestamp;
    private final String status;

    public PaymentDetails(String paymentMode, double amountPaid, String transactionReference, LocalDateTime timestamp, String status) {
        this.paymentMode = paymentMode;
        this.amountPaid = amountPaid;
        this.transactionReference = transactionReference;
        this.timestamp = timestamp != null ? timestamp : LocalDateTime.now();
        this.status = status;
    }

    public String getPaymentMode() {
        return paymentMode;
    }

    public double getAmountPaid() {
        return amountPaid;
    }

    public String getTransactionReference() {
        return transactionReference;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public String getStatus() {
        return status;
    }

    public String toCsvFormat() {
        return String.format("%s;%.2f;%s;%s;%s",
                paymentMode, amountPaid, transactionReference, timestamp.format(FORMATTER), status);
    }

    public static PaymentDetails fromCsvFormat(String data) {
        String[] parts = data.split(";", -1);
        if (parts.length < 5) {
            return new PaymentDetails("UNKNOWN", 0.0, "N/A", LocalDateTime.now(), "FAILED");
        }
        String mode = parts[0];
        double amt = Double.parseDouble(parts[1]);
        String ref = parts[2];
        LocalDateTime dt = LocalDateTime.parse(parts[3], FORMATTER);
        String status = parts[4];
        return new PaymentDetails(mode, amt, ref, dt, status);
    }

    @Override
    public String toString() {
        return String.format("Mode: %s | Paid: Rs.%.2f | Ref: %s | Time: %s | Status: %s",
                paymentMode, amountPaid, transactionReference, timestamp.format(FORMATTER), status);
    }
}
