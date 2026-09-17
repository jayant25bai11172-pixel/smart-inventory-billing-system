package com.retail.inventory.ui;

import com.retail.inventory.exception.InsufficientStockException;
import com.retail.inventory.exception.InvalidInputException;
import com.retail.inventory.exception.PaymentFailedException;
import com.retail.inventory.exception.ProductNotFoundException;
import com.retail.inventory.model.Cart;
import com.retail.inventory.model.CartItem;
import com.retail.inventory.model.Invoice;
import com.retail.inventory.model.Product;
import com.retail.inventory.payment.CardPayment;
import com.retail.inventory.payment.CashPayment;
import com.retail.inventory.payment.PaymentMethod;
import com.retail.inventory.payment.UpiPayment;
import com.retail.inventory.service.AnalyticsService;
import com.retail.inventory.service.BillingService;
import com.retail.inventory.service.InventoryService;
import com.retail.inventory.service.LowStockMonitorService;
import com.retail.inventory.util.ConsoleUtils;
import com.retail.inventory.util.TableFormatter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

/**
 * Interactive Console User Interface providing full workflow navigation.
 */
public class ConsoleUI {
    private final InventoryService inventoryService;
    private final BillingService billingService;
    private final AnalyticsService analyticsService;
    private final LowStockMonitorService monitorService;
    private final Scanner scanner;

    public ConsoleUI(InventoryService inventoryService, BillingService billingService,
                     AnalyticsService analyticsService, LowStockMonitorService monitorService) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.analyticsService = analyticsService;
        this.monitorService = monitorService;
        this.scanner = new Scanner(System.in);
    }

    public void start() {
        boolean running = true;
        while (running) {
            ConsoleUtils.printHeader("Smart Inventory & Billing Manager for Retail");
            int lowStockCount = monitorService.getActiveLowStockCount();
            if (lowStockCount > 0) {
                System.out.println(ConsoleUtils.YELLOW + "  [!] NOTICE: " + lowStockCount
                        + " product(s) are currently at or below low-stock threshold! Check Module 4." + ConsoleUtils.RESET);
            }

            System.out.println("  1. Product Catalog & Inventory Tracking (Module 1)");
            System.out.println("  2. Point of Sale & Billing / Cart (Module 2)");
            System.out.println("  3. Sales Analytics & Business Reports (Module 3)");
            System.out.println("  4. Live Stock Health & Background Daemon Alerts");
            System.out.println("  5. Exit System");

            int choice = ConsoleUtils.readInt(scanner, "\nEnter your selection [1-5]: ", 1, 5);
            switch (choice) {
                case 1:
                    handleInventoryMenu();
                    break;
                case 2:
                    handleBillingMenu();
                    break;
                case 3:
                    handleAnalyticsMenu();
                    break;
                case 4:
                    handleAlertsMenu();
                    break;
                case 5:
                    running = false;
                    ConsoleUtils.printInfo("Exiting Smart Inventory & Billing Manager. Goodbye!");
                    break;
            }
        }
    }

    // ==========================================
    // MODULE 1: INVENTORY MANAGEMENT
    // ==========================================
    private void handleInventoryMenu() {
        boolean inMenu = true;
        while (inMenu) {
            ConsoleUtils.printHeader("Module 1: Product Catalog & Inventory Tracking");
            System.out.println("  1. List All Products");
            System.out.println("  2. Add New Product");
            System.out.println("  3. Restock Product Inventory");
            System.out.println("  4. Update Product Price / Safety Threshold");
            System.out.println("  5. Filter Products by Category");
            System.out.println("  6. Delete Product");
            System.out.println("  7. Return to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "\nSelect option [1-7]: ", 1, 7);
            switch (choice) {
                case 1:
                    displayProductTable(inventoryService.getAllProducts());
                    break;
                case 2:
                    addNewProduct();
                    break;
                case 3:
                    restockProduct();
                    break;
                case 4:
                    updateProductDetails();
                    break;
                case 5:
                    filterProductsByCategory();
                    break;
                case 6:
                    deleteProduct();
                    break;
                case 7:
                    inMenu = false;
                    break;
            }
        }
    }

    private void displayProductTable(List<Product> products) {
        if (products.isEmpty()) {
            ConsoleUtils.printWarning("No products found in catalog.");
            return;
        }
        TableFormatter table = new TableFormatter("ID", "Product Name", "Category", "Price", "Stock", "Threshold", "Status");
        for (Product p : products) {
            String status = p.isLowStock() ? ConsoleUtils.RED + "LOW STOCK" + ConsoleUtils.RESET : ConsoleUtils.GREEN + "OPTIMAL" + ConsoleUtils.RESET;
            table.addRow(
                    p.getId(),
                    p.getName(),
                    p.getCategory(),
                    ConsoleUtils.formatCurrency(p.getPrice()),
                    p.getStockQuantity(),
                    p.getLowStockThreshold(),
                    status
            );
        }
        System.out.println(table.render());
    }

    private void addNewProduct() {
        ConsoleUtils.printHeader("Add New Product");
        String id = ConsoleUtils.readString(scanner, "Enter unique Product ID (e.g. PRD-115): ");
        String name = ConsoleUtils.readString(scanner, "Enter Product Name: ");
        String category = ConsoleUtils.readString(scanner, "Enter Category (Groceries, Dairy, Beverages, Snacks, etc.): ");
        double price = ConsoleUtils.readDouble(scanner, "Enter Unit Price (Rs.): ", 0.01);
        int initialStock = ConsoleUtils.readInt(scanner, "Enter Initial Stock Quantity: ", 0, 100000);
        int threshold = ConsoleUtils.readInt(scanner, "Enter Low Stock Alert Threshold: ", 0, 10000);

        try {
            Product p = inventoryService.addProduct(id, name, category, price, initialStock, threshold);
            ConsoleUtils.printSuccess("Product added successfully: " + p.getName() + " (" + p.getId() + ")");
        } catch (InvalidInputException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void restockProduct() {
        ConsoleUtils.printHeader("Restock Product Inventory");
        String id = ConsoleUtils.readString(scanner, "Enter Product ID to restock: ");
        try {
            Product p = inventoryService.getProductById(id);
            System.out.println("Current Stock for " + p.getName() + ": " + p.getStockQuantity());
            int addQty = ConsoleUtils.readInt(scanner, "Enter quantity to add: ", 1, 50000);
            inventoryService.replenishStock(id, addQty);
            ConsoleUtils.printSuccess(String.format("Successfully restocked! New stock for '%s': %d",
                    p.getName(), p.getStockQuantity()));
        } catch (ProductNotFoundException | InvalidInputException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void updateProductDetails() {
        ConsoleUtils.printHeader("Update Product Price / Threshold");
        String id = ConsoleUtils.readString(scanner, "Enter Product ID: ");
        try {
            Product p = inventoryService.getProductById(id);
            System.out.println("Selected: " + p.getName() + " | Price: Rs." + p.getPrice() + " | Threshold: " + p.getLowStockThreshold());
            System.out.println("  1. Update Price");
            System.out.println("  2. Update Low-Stock Threshold");
            int sel = ConsoleUtils.readInt(scanner, "Choice [1-2]: ", 1, 2);
            if (sel == 1) {
                double newPrice = ConsoleUtils.readDouble(scanner, "Enter new price (Rs.): ", 0.01);
                inventoryService.updateProductPrice(id, newPrice);
                ConsoleUtils.printSuccess("Price updated successfully.");
            } else {
                int newThreshold = ConsoleUtils.readInt(scanner, "Enter new threshold: ", 0, 10000);
                inventoryService.updateProductThreshold(id, newThreshold);
                ConsoleUtils.printSuccess("Threshold updated successfully.");
            }
        } catch (ProductNotFoundException | InvalidInputException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void filterProductsByCategory() {
        String cat = ConsoleUtils.readString(scanner, "Enter Category Name to filter: ");
        List<Product> list = inventoryService.getProductsByCategory(cat);
        displayProductTable(list);
    }

    private void deleteProduct() {
        String id = ConsoleUtils.readString(scanner, "Enter Product ID to remove: ");
        try {
            boolean ok = inventoryService.deleteProduct(id);
            if (ok) {
                ConsoleUtils.printSuccess("Product " + id + " deleted from catalog.");
            } else {
                ConsoleUtils.printWarning("Product could not be deleted.");
            }
        } catch (ProductNotFoundException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    // ==========================================
    // MODULE 2: POINT OF SALE & BILLING
    // ==========================================
    private void handleBillingMenu() {
        boolean inMenu = true;
        while (inMenu) {
            ConsoleUtils.printHeader("Module 2: Point of Sale & Billing (Active Cart)");
            displayCartSummary();

            System.out.println("  1. Add Product to Cart");
            System.out.println("  2. Update Item Quantity in Cart");
            System.out.println("  3. Remove Item from Cart");
            System.out.println("  4. Apply Discount Percentage");
            System.out.println("  5. Checkout & Generate Bill (Payment)");
            System.out.println("  6. Clear Cart");
            System.out.println("  7. Return to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "\nSelect option [1-7]: ", 1, 7);
            switch (choice) {
                case 1:
                    addItemToCart();
                    break;
                case 2:
                    updateCartItem();
                    break;
                case 3:
                    removeItemFromCart();
                    break;
                case 4:
                    applyCartDiscount();
                    break;
                case 5:
                    processCheckout();
                    break;
                case 6:
                    billingService.clearCart();
                    ConsoleUtils.printInfo("Cart cleared.");
                    break;
                case 7:
                    inMenu = false;
                    break;
            }
        }
    }

    private void displayCartSummary() {
        Cart cart = billingService.getCurrentCart();
        if (cart.isEmpty()) {
            System.out.println("  Cart Status: [EMPTY]");
            return;
        }

        TableFormatter table = new TableFormatter("Product ID", "Name", "Unit Price", "Qty", "Subtotal");
        for (CartItem item : cart.getItems()) {
            table.addRow(
                    item.getProduct().getId(),
                    item.getProduct().getName(),
                    ConsoleUtils.formatCurrency(item.getProduct().getPrice()),
                    item.getQuantity(),
                    ConsoleUtils.formatCurrency(item.getSubtotal())
            );
        }
        System.out.println(table.render());

        double subtotal = cart.getSubtotal();
        double discountAmt = cart.getDiscountAmount();
        double taxable = cart.getTaxableAmount();
        double tax = billingService.calculateTax(taxable);
        double total = billingService.calculateFinalTotal();

        System.out.println(String.format("  Items: %-4d | Subtotal: %s | Disc (%.1f%%): -%s | GST (%.1f%%): +%s | Total Payable: %s%s%s",
                cart.getTotalItemCount(),
                ConsoleUtils.formatCurrency(subtotal),
                cart.getDiscountPercentage(),
                ConsoleUtils.formatCurrency(discountAmt),
                billingService.getTaxRatePercentage(),
                ConsoleUtils.formatCurrency(tax),
                ConsoleUtils.BOLD + ConsoleUtils.GREEN,
                ConsoleUtils.formatCurrency(total),
                ConsoleUtils.RESET
        ));
    }

    private void addItemToCart() {
        String id = ConsoleUtils.readString(scanner, "Enter Product ID to add: ");
        int qty = ConsoleUtils.readInt(scanner, "Enter quantity: ", 1, 1000);
        try {
            billingService.addItemToCart(id, qty);
            ConsoleUtils.printSuccess("Added to cart successfully.");
        } catch (ProductNotFoundException | InsufficientStockException | InvalidInputException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void updateCartItem() {
        String id = ConsoleUtils.readString(scanner, "Enter Product ID to update: ");
        int qty = ConsoleUtils.readInt(scanner, "Enter new quantity (0 to remove): ", 0, 1000);
        try {
            billingService.updateCartItemQuantity(id, qty);
            ConsoleUtils.printSuccess("Cart updated.");
        } catch (ProductNotFoundException | InsufficientStockException e) {
            ConsoleUtils.printError(e.getMessage());
        }
    }

    private void removeItemFromCart() {
        String id = ConsoleUtils.readString(scanner, "Enter Product ID to remove: ");
        billingService.removeItemFromCart(id);
        ConsoleUtils.printSuccess("Item removed from cart.");
    }

    private void applyCartDiscount() {
        double disc = ConsoleUtils.readDouble(scanner, "Enter discount percentage (0 to 50%): ", 0.0);
        if (disc > 50) {
            ConsoleUtils.printWarning("Maximum allowed cashier discount is 50%. Setting to 50%.");
            disc = 50.0;
        }
        billingService.applyDiscount(disc);
        ConsoleUtils.printSuccess(String.format("%.1f%% discount applied.", disc));
    }

    private void processCheckout() {
        Cart cart = billingService.getCurrentCart();
        if (cart.isEmpty()) {
            ConsoleUtils.printWarning("Cart is empty! Add products before checking out.");
            return;
        }

        ConsoleUtils.printHeader("Customer Checkout & Payment Settlement");
        String name = ConsoleUtils.readString(scanner, "Enter Customer Name: ");
        String phone = ConsoleUtils.readString(scanner, "Enter Customer Phone: ");

        double totalPayable = billingService.calculateFinalTotal();
        System.out.println("\nTotal Amount Due: " + ConsoleUtils.BOLD + ConsoleUtils.formatCurrency(totalPayable) + ConsoleUtils.RESET);

        System.out.println("\nSelect Payment Method (Interface Polymorphism):");
        System.out.println("  1. Cash Payment");
        System.out.println("  2. Credit / Debit Card");
        System.out.println("  3. UPI (Unified Payments Interface)");

        int payChoice = ConsoleUtils.readInt(scanner, "Choose payment mode [1-3]: ", 1, 3);
        PaymentMethod method;
        String reference;

        switch (payChoice) {
            case 1:
                method = new CashPayment();
                reference = ConsoleUtils.readString(scanner, "Enter cash tendered by customer (Rs.): ");
                break;
            case 2:
                method = new CardPayment();
                reference = ConsoleUtils.readString(scanner, "Enter 16-digit Card Number: ");
                break;
            case 3:
                method = new UpiPayment();
                reference = ConsoleUtils.readString(scanner, "Enter Customer UPI ID (e.g. user@bank): ");
                break;
            default:
                return;
        }

        try {
            Invoice invoice = billingService.checkout(name, phone, method, reference);
            ConsoleUtils.printSuccess("Transaction Completed Successfully! Bill Generated.");
            printReceipt(invoice);
        } catch (InsufficientStockException | PaymentFailedException | InvalidInputException | ProductNotFoundException e) {
            ConsoleUtils.printError("Checkout Failed: " + e.getMessage());
        }
    }

    private void printReceipt(Invoice inv) {
        System.out.println(ConsoleUtils.PURPLE + "\n========================================================");
        System.out.println("               RETAIL STORE TAX INVOICE                 ");
        System.out.println("========================================================" + ConsoleUtils.RESET);
        System.out.println(" Invoice ID : " + inv.getInvoiceId());
        System.out.println(" Date & Time: " + inv.getCreatedAt().format(Invoice.DATE_FORMAT));
        System.out.println(" Customer   : " + inv.getCustomerName() + " | Phone: " + inv.getCustomerPhone());
        System.out.println("--------------------------------------------------------");

        TableFormatter table = new TableFormatter("Item", "Qty", "Rate", "Amount");
        for (CartItem it : inv.getItems()) {
            table.addRow(it.getProduct().getName(), it.getQuantity(),
                    ConsoleUtils.formatCurrency(it.getProduct().getPrice()),
                    ConsoleUtils.formatCurrency(it.getSubtotal()));
        }
        System.out.println(table.render());

        System.out.printf(" Gross Subtotal : %25s\n", ConsoleUtils.formatCurrency(inv.getSubtotal()));
        System.out.printf(" Discount (%.1f%%): -%24s\n", inv.getDiscountPercentage(), ConsoleUtils.formatCurrency(inv.getDiscountAmount()));
        System.out.printf(" GST Tax (%.1f%%) : +%24s\n", inv.getTaxRatePercentage(), ConsoleUtils.formatCurrency(inv.getTaxAmount()));
        System.out.println("--------------------------------------------------------");
        System.out.printf(ConsoleUtils.BOLD + " NET TOTAL PAID : %25s\n" + ConsoleUtils.RESET, ConsoleUtils.formatCurrency(inv.getFinalTotal()));
        System.out.println(" Payment Status : " + inv.getPaymentDetails().toString());
        System.out.println(ConsoleUtils.PURPLE + "========================================================\n" + ConsoleUtils.RESET);
    }

    // ==========================================
    // MODULE 3: SALES ANALYTICS & REPORTS
    // ==========================================
    private void handleAnalyticsMenu() {
        boolean inMenu = true;
        while (inMenu) {
            ConsoleUtils.printHeader("Module 3: Sales Analytics & Business Intelligence");
            System.out.println("  1. Executive Revenue & Order Overview");
            System.out.println("  2. Top 5 Best-Selling Products (By Units Sold)");
            System.out.println("  3. Top 5 Best-Selling Products (By Revenue)");
            System.out.println("  4. Category-Wise Revenue Breakdown");
            System.out.println("  5. Payment Method Distribution Analysis");
            System.out.println("  6. Search Invoice Audit Trail by ID");
            System.out.println("  7. Return to Main Menu");

            int choice = ConsoleUtils.readInt(scanner, "\nSelect report [1-7]: ", 1, 7);
            switch (choice) {
                case 1:
                    displayExecutiveSummary();
                    break;
                case 2:
                    displayTopProductsUnits();
                    break;
                case 3:
                    displayTopProductsRevenue();
                    break;
                case 4:
                    displayCategoryRevenue();
                    break;
                case 5:
                    displayPaymentModeAnalytics();
                    break;
                case 6:
                    searchInvoice();
                    break;
                case 7:
                    inMenu = false;
                    break;
            }
        }
    }

    private void displayExecutiveSummary() {
        ConsoleUtils.printHeader("Executive Sales Summary");
        double revenue = analyticsService.getTotalRevenue();
        int orders = analyticsService.getTotalInvoicesCount();
        int units = analyticsService.getTotalUnitsSold();
        double aov = analyticsService.getAverageOrderValue();

        TableFormatter table = new TableFormatter("Metric", "Value");
        table.addRow("Total Cumulative Revenue", ConsoleUtils.formatCurrency(revenue));
        table.addRow("Total Completed Orders", orders);
        table.addRow("Total Units of Stock Sold", units);
        table.addRow("Average Order Value (AOV)", ConsoleUtils.formatCurrency(aov));
        System.out.println(table.render());
    }

    private void displayTopProductsUnits() {
        ConsoleUtils.printHeader("Top 5 Best-Selling Products (By Units)");
        List<Map.Entry<String, Integer>> top = analyticsService.getTopSellingProductsByUnits(5);
        if (top.isEmpty()) {
            ConsoleUtils.printWarning("No sales recorded yet.");
            return;
        }
        TableFormatter table = new TableFormatter("Rank", "Product", "Units Sold");
        int rank = 1;
        for (Map.Entry<String, Integer> e : top) {
            table.addRow("#" + (rank++), e.getKey(), e.getValue());
        }
        System.out.println(table.render());
    }

    private void displayTopProductsRevenue() {
        ConsoleUtils.printHeader("Top 5 Best-Selling Products (By Revenue)");
        List<Map.Entry<String, Double>> top = analyticsService.getTopSellingProductsByRevenue(5);
        if (top.isEmpty()) {
            ConsoleUtils.printWarning("No sales recorded yet.");
            return;
        }
        TableFormatter table = new TableFormatter("Rank", "Product", "Total Revenue Generated");
        int rank = 1;
        for (Map.Entry<String, Double> e : top) {
            table.addRow("#" + (rank++), e.getKey(), ConsoleUtils.formatCurrency(e.getValue()));
        }
        System.out.println(table.render());
    }

    private void displayCategoryRevenue() {
        ConsoleUtils.printHeader("Category-Wise Sales Breakdown");
        Map<String, Double> catRevenue = analyticsService.getCategoryRevenueBreakdown();
        if (catRevenue.isEmpty()) {
            ConsoleUtils.printWarning("No category sales recorded yet.");
            return;
        }
        TableFormatter table = new TableFormatter("Category", "Total Sales Revenue");
        for (Map.Entry<String, Double> e : catRevenue.entrySet()) {
            table.addRow(e.getKey(), ConsoleUtils.formatCurrency(e.getValue()));
        }
        System.out.println(table.render());
    }

    private void displayPaymentModeAnalytics() {
        ConsoleUtils.printHeader("Customer Payment Method Distribution");
        Map<String, Long> dist = analyticsService.getPaymentModeDistribution();
        if (dist.isEmpty()) {
            ConsoleUtils.printWarning("No transactions recorded yet.");
            return;
        }
        TableFormatter table = new TableFormatter("Payment Method", "Transaction Count");
        for (Map.Entry<String, Long> e : dist.entrySet()) {
            table.addRow(e.getKey(), e.getValue());
        }
        System.out.println(table.render());
    }

    private void searchInvoice() {
        String id = ConsoleUtils.readString(scanner, "Enter Invoice ID (e.g. INV-20260917-0001): ");
        Optional<Invoice> invOpt = billingService.getInvoiceById(id);
        if (invOpt.isPresent()) {
            printReceipt(invOpt.get());
        } else {
            ConsoleUtils.printWarning("No invoice found with ID: " + id);
        }
    }

    // ==========================================
    // MODULE 4: REAL-TIME ALERTS & MULTITHREADING
    // ==========================================
    private void handleAlertsMenu() {
        ConsoleUtils.printHeader("Real-Time Inventory Health & Background Daemon Alerts");
        System.out.println("Daemon Thread Status: "
                + (monitorService.isRunning() ? ConsoleUtils.GREEN + "ACTIVE (Running in Background)" : ConsoleUtils.RED + "STOPPED")
                + ConsoleUtils.RESET);

        List<Product> lowStock = inventoryService.getLowStockProducts();
        System.out.println("\n--- Current Low-Stock Reorder Checklist ---");
        if (lowStock.isEmpty()) {
            ConsoleUtils.printSuccess("All inventory items have optimal stock levels.");
        } else {
            TableFormatter table = new TableFormatter("Product ID", "Name", "Category", "Current Stock", "Threshold", "Units Needed");
            for (Product p : lowStock) {
                int needed = (p.getLowStockThreshold() * 2) - p.getStockQuantity();
                table.addRow(p.getId(), p.getName(), p.getCategory(),
                        ConsoleUtils.RED + p.getStockQuantity() + ConsoleUtils.RESET,
                        p.getLowStockThreshold(), Math.max(1, needed));
            }
            System.out.println(table.render());
        }

        System.out.println("\n--- Background Daemon Event Log ---");
        List<String> logs = monitorService.getAlertLogs();
        if (logs.isEmpty()) {
            System.out.println("  No background alert events recorded yet.");
        } else {
            for (String log : logs) {
                System.out.println("  " + log);
            }
        }

        ConsoleUtils.readString(scanner, "\nPress [ENTER] to return to main menu...");
    }
}
