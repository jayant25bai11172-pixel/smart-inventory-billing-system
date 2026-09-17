package com.retail.inventory.service;

import com.retail.inventory.exception.InsufficientStockException;
import com.retail.inventory.exception.InvalidInputException;
import com.retail.inventory.exception.PaymentFailedException;
import com.retail.inventory.exception.ProductNotFoundException;
import com.retail.inventory.model.*;
import com.retail.inventory.payment.PaymentMethod;
import com.retail.inventory.repository.InvoiceRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service managing Point-of-Sale (POS) billing, cart operations,
 * tax/discount computations, and checkout workflows.
 */
public class BillingService {
    private final InventoryService inventoryService;
    private final InvoiceRepository invoiceRepository;
    private final Cart currentCart;
    private double taxRatePercentage;

    public BillingService(InventoryService inventoryService, InvoiceRepository invoiceRepository) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "InventoryService cannot be null.");
        this.invoiceRepository = Objects.requireNonNull(invoiceRepository, "InvoiceRepository cannot be null.");
        this.currentCart = new Cart();
        this.taxRatePercentage = 5.0; // Default 5% retail GST
    }

    public Cart getCurrentCart() {
        return currentCart;
    }

    public double getTaxRatePercentage() {
        return taxRatePercentage;
    }

    public void setTaxRatePercentage(double taxRatePercentage) {
        if (taxRatePercentage < 0 || taxRatePercentage > 100) {
            throw new IllegalArgumentException("Tax rate must be between 0% and 100%.");
        }
        this.taxRatePercentage = taxRatePercentage;
    }

    public void addItemToCart(String productId, int quantity)
            throws ProductNotFoundException, InsufficientStockException, InvalidInputException {
        if (quantity <= 0) {
            throw new InvalidInputException("quantity", "Quantity must be greater than zero.");
        }
        Product product = inventoryService.getProductById(productId);
        currentCart.addItem(product, quantity);
    }

    public void updateCartItemQuantity(String productId, int newQuantity)
            throws ProductNotFoundException, InsufficientStockException {
        Product product = inventoryService.getProductById(productId);
        currentCart.updateQuantity(product, newQuantity);
    }

    public void removeItemFromCart(String productId) {
        currentCart.removeItem(productId);
    }

    public void clearCart() {
        currentCart.clear();
    }

    public void applyDiscount(double discountPercentage) {
        currentCart.setDiscountPercentage(discountPercentage);
    }

    public double calculateTax(double taxableAmount) {
        return (taxableAmount * taxRatePercentage) / 100.0;
    }

    public double calculateFinalTotal() {
        double taxable = currentCart.getTaxableAmount();
        double tax = calculateTax(taxable);
        return taxable + tax;
    }

    /**
     * Executes atomic checkout:
     * 1. Re-verifies inventory availability
     * 2. Delegates payment to the provided PaymentMethod implementation (Polymorphism)
     * 3. Atomically deducts inventory stock
     * 4. Persists the invoice record
     * 5. Clears active cart upon success
     */
    public synchronized Invoice checkout(String customerName, String customerPhone,
                                         PaymentMethod paymentMethod, String paymentReference)
            throws InsufficientStockException, PaymentFailedException, InvalidInputException, ProductNotFoundException {
        if (currentCart.isEmpty()) {
            throw new InvalidInputException("cart", "Cannot checkout with an empty cart.");
        }
        if (paymentMethod == null) {
            throw new InvalidInputException("paymentMethod", "A payment method must be selected.");
        }

        // 1. Re-verify all item stocks in real-time
        for (CartItem item : currentCart.getItems()) {
            Product currentProduct = inventoryService.getProductById(item.getProduct().getId());
            if (currentProduct.getStockQuantity() < item.getQuantity()) {
                throw new InsufficientStockException(currentProduct.getId(), currentProduct.getName(),
                        currentProduct.getStockQuantity(), item.getQuantity());
            }
        }

        double subtotal = currentCart.getSubtotal();
        double discountPct = currentCart.getDiscountPercentage();
        double discountAmt = currentCart.getDiscountAmount();
        double taxable = currentCart.getTaxableAmount();
        double taxAmt = calculateTax(taxable);
        double finalTotal = taxable + taxAmt;

        // 2. Process payment via polymorphic interface
        PaymentDetails paymentDetails = paymentMethod.processPayment(finalTotal, paymentReference);

        // 3. Atomically deduct stock from inventory
        for (CartItem item : currentCart.getItems()) {
            inventoryService.deductStock(item.getProduct().getId(), item.getQuantity());
        }

        // 4. Generate unique invoice
        int seq = invoiceRepository.getNextInvoiceSequence();
        String datePrefix = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String invoiceId = String.format("INV-%s-%04d", datePrefix, seq);

        Invoice invoice = new Invoice(
                invoiceId,
                LocalDateTime.now(),
                customerName,
                customerPhone,
                currentCart.getItems(),
                subtotal,
                discountPct,
                discountAmt,
                taxRatePercentage,
                taxAmt,
                finalTotal,
                paymentDetails
        );

        // 5. Persist invoice
        invoiceRepository.save(invoice);

        // 6. Reset cart
        currentCart.clear();

        return invoice;
    }

    public List<Invoice> getAllInvoices() {
        return invoiceRepository.findAll();
    }

    public Optional<Invoice> getInvoiceById(String id) {
        return invoiceRepository.findById(id);
    }
}
