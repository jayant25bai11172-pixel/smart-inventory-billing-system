package com.retail.inventory.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a complete billing invoice for a customer purchase.
 */
public class Invoice implements Serializable {
    private static final long serialVersionUID = 1L;
    public static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String invoiceId;
    private final LocalDateTime createdAt;
    private final String customerName;
    private final String customerPhone;
    private final List<CartItem> items;
    private final double subtotal;
    private final double discountPercentage;
    private final double discountAmount;
    private final double taxRatePercentage;
    private final double taxAmount;
    private final double finalTotal;
    private final PaymentDetails paymentDetails;

    public Invoice(String invoiceId, LocalDateTime createdAt, String customerName, String customerPhone,
                   List<CartItem> items, double subtotal, double discountPercentage, double discountAmount,
                   double taxRatePercentage, double taxAmount, double finalTotal, PaymentDetails paymentDetails) {
        this.invoiceId = invoiceId;
        this.createdAt = (createdAt != null) ? createdAt : LocalDateTime.now();
        this.customerName = (customerName != null && !customerName.trim().isEmpty()) ? customerName.trim() : "Walk-in Customer";
        this.customerPhone = (customerPhone != null && !customerPhone.trim().isEmpty()) ? customerPhone.trim() : "N/A";
        this.items = new ArrayList<>(items);
        this.subtotal = subtotal;
        this.discountPercentage = discountPercentage;
        this.discountAmount = discountAmount;
        this.taxRatePercentage = taxRatePercentage;
        this.taxAmount = taxAmount;
        this.finalTotal = finalTotal;
        this.paymentDetails = paymentDetails;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getCustomerPhone() {
        return customerPhone;
    }

    public List<CartItem> getItems() {
        return new ArrayList<>(items);
    }

    public double getSubtotal() {
        return subtotal;
    }

    public double getDiscountPercentage() {
        return discountPercentage;
    }

    public double getDiscountAmount() {
        return discountAmount;
    }

    public double getTaxRatePercentage() {
        return taxRatePercentage;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public double getFinalTotal() {
        return finalTotal;
    }

    public PaymentDetails getPaymentDetails() {
        return paymentDetails;
    }

    public int getTotalQuantity() {
        return items.stream().mapToInt(CartItem::getQuantity).sum();
    }

    /**
     * Serializes invoice summary to CSV line for historical reporting.
     * Format: invoiceId,createdAt,customerName,customerPhone,totalItems,subtotal,discount,tax,finalTotal,paymentData,itemsSerialized
     */
    public String toCsvLine() {
        StringBuilder itemsSerialized = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            CartItem it = items.get(i);
            itemsSerialized.append(it.getProduct().getId())
                    .append("#").append(escapeCsv(it.getProduct().getName()))
                    .append("#").append(escapeCsv(it.getProduct().getCategory()))
                    .append("#").append(it.getProduct().getPrice())
                    .append("#").append(it.getQuantity());
            if (i < items.size() - 1) {
                itemsSerialized.append("|");
            }
        }

        return String.format("%s,%s,%s,%s,%d,%.2f,%.2f,%.2f,%.2f,%s,%s",
                escapeCsv(invoiceId),
                createdAt.format(DATE_FORMAT),
                escapeCsv(customerName),
                escapeCsv(customerPhone),
                getTotalQuantity(),
                subtotal,
                discountAmount,
                taxAmount,
                finalTotal,
                paymentDetails.toCsvFormat(),
                itemsSerialized.toString()
        );
    }

    public static Invoice fromCsvLine(String line) {
        String[] parts = line.split(",", -1);
        if (parts.length < 11) {
            throw new IllegalArgumentException("Invalid invoice CSV row: " + line);
        }

        String invId = parts[0].trim();
        LocalDateTime dt = LocalDateTime.parse(parts[1].trim(), DATE_FORMAT);
        String custName = parts[2].trim();
        String custPhone = parts[3].trim();
        double subtotal = Double.parseDouble(parts[5].trim());
        double discountAmt = Double.parseDouble(parts[6].trim());
        double taxAmt = Double.parseDouble(parts[7].trim());
        double total = Double.parseDouble(parts[8].trim());
        PaymentDetails payDetails = PaymentDetails.fromCsvFormat(parts[9].trim());

        List<CartItem> items = new ArrayList<>();
        String rawItems = parts[10].trim();
        if (!rawItems.isEmpty()) {
            String[] itemTokens = rawItems.split("\\|", -1);
            for (String tok : itemTokens) {
                String[] p = tok.split("#", -1);
                if (p.length >= 5) {
                    String pId = p[0];
                    String pName = p[1];
                    String pCat = p[2];
                    double pPrice = Double.parseDouble(p[3]);
                    int pQty = Integer.parseInt(p[4]);
                    Product prod = new Product(pId, pName, pCat, pPrice, 100, 5);
                    items.add(new CartItem(prod, pQty));
                }
            }
        }

        double discPct = subtotal > 0 ? (discountAmt / subtotal) * 100.0 : 0.0;
        double taxRate = (subtotal - discountAmt) > 0 ? (taxAmt / (subtotal - discountAmt)) * 100.0 : 0.0;

        return new Invoice(invId, dt, custName, custPhone, items, subtotal, discPct, discountAmt, taxRate, taxAmt, total, payDetails);
    }

    private static String escapeCsv(String val) {
        if (val == null) return "";
        return val.replace(",", " ").replace("|", " ").replace("#", " ").trim();
    }
}
