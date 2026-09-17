package com.retail.inventory.service;

import com.retail.inventory.model.CartItem;
import com.retail.inventory.model.Invoice;
import com.retail.inventory.repository.InvoiceRepository;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Functional Module 3: Sales Analytics and Business Reporting.
 * Leverages Java 8+ Streams, Lambdas, and Collectors to compute business intelligence metrics.
 */
public class AnalyticsService {
    private final InvoiceRepository invoiceRepository;

    public AnalyticsService(InvoiceRepository invoiceRepository) {
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository, "InvoiceRepository cannot be null.");
    }

    public double getTotalRevenue() {
        return invoiceRepository.findAll().stream()
                .mapToDouble(Invoice::getFinalTotal)
                .sum();
    }

    public int getTotalInvoicesCount() {
        return invoiceRepository.findAll().size();
    }

    public int getTotalUnitsSold() {
        return invoiceRepository.findAll().stream()
                .mapToInt(Invoice::getTotalQuantity)
                .sum();
    }

    public double getAverageOrderValue() {
        List<Invoice> invoices = invoiceRepository.findAll();
        if (invoices.isEmpty()) return 0.0;
        return getTotalRevenue() / invoices.size();
    }

    /**
     * Aggregates total units sold per product across all historical invoices.
     * Returns a sorted map (highest units sold first).
     */
    public List<Map.Entry<String, Integer>> getTopSellingProductsByUnits(int limit) {
        Map<String, Integer> productUnits = new HashMap<>();

        for (Invoice inv : invoiceRepository.findAll()) {
            for (CartItem item : inv.getItems()) {
                String key = item.getProduct().getName() + " (" + item.getProduct().getId() + ")";
                productUnits.put(key, productUnits.getOrDefault(key, 0) + item.getQuantity());
            }
        }

        return productUnits.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Aggregates total revenue per product across all historical invoices.
     */
    public List<Map.Entry<String, Double>> getTopSellingProductsByRevenue(int limit) {
        Map<String, Double> productRevenue = new HashMap<>();

        for (Invoice inv : invoiceRepository.findAll()) {
            for (CartItem item : inv.getItems()) {
                String key = item.getProduct().getName() + " (" + item.getProduct().getId() + ")";
                productRevenue.put(key, productRevenue.getOrDefault(key, 0.0) + item.getSubtotal());
            }
        }

        return productRevenue.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /**
     * Computes total revenue grouped by product category.
     */
    public Map<String, Double> getCategoryRevenueBreakdown() {
        Map<String, Double> categoryRevenue = new HashMap<>();

        for (Invoice inv : invoiceRepository.findAll()) {
            for (CartItem item : inv.getItems()) {
                String cat = item.getProduct().getCategory();
                categoryRevenue.put(cat, categoryRevenue.getOrDefault(cat, 0.0) + item.getSubtotal());
            }
        }

        return categoryRevenue;
    }

    /**
     * Computes the distribution of payment methods used by customers.
     */
    public Map<String, Long> getPaymentModeDistribution() {
        return invoiceRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getPaymentDetails().getPaymentMode(),
                        Collectors.counting()
                ));
    }

    /**
     * Computes daily revenue.
     */
    public Map<LocalDate, Double> getDailyRevenue() {
        return invoiceRepository.findAll().stream()
                .collect(Collectors.groupingBy(
                        inv -> inv.getCreatedAt().toLocalDate(),
                        TreeMap::new,
                        Collectors.summingDouble(Invoice::getFinalTotal)
                ));
    }
}
