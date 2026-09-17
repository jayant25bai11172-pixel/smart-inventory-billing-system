package com.retail.inventory;

import com.retail.inventory.exception.*;
import com.retail.inventory.model.*;
import com.retail.inventory.payment.*;
import com.retail.inventory.repository.*;
import com.retail.inventory.service.*;

import java.io.File;
import java.nio.file.Files;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Automated Verification and Unit Test Suite for Smart Inventory & Billing Manager.
 * Exercises all 3 functional modules, OOP concepts, exceptions, and multithreading.
 */
public class SmartInventoryTestSuite {
    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("================================================================================");
        System.out.println("   SMART INVENTORY & BILLING MANAGER - AUTOMATED TEST SUITE EXECUTION");
        System.out.println("================================================================================");

        runTest("testProductModelAndStockLogic", SmartInventoryTestSuite::testProductModelAndStockLogic);
        runTest("testCartCalculationsAndDiscounts", SmartInventoryTestSuite::testCartCalculationsAndDiscounts);
        runTest("testInsufficientStockExceptionHandling", SmartInventoryTestSuite::testInsufficientStockExceptionHandling);
        runTest("testCashPaymentMethod", SmartInventoryTestSuite::testCashPaymentMethod);
        runTest("testCardPaymentMethod", SmartInventoryTestSuite::testCardPaymentMethod);
        runTest("testUpiPaymentMethod", SmartInventoryTestSuite::testUpiPaymentMethod);
        runTest("testAtomicBillingCheckoutAndPersistence", SmartInventoryTestSuite::testAtomicBillingCheckoutAndPersistence);
        runTest("testSalesAnalyticsAggregations", SmartInventoryTestSuite::testSalesAnalyticsAggregations);
        runTest("testMultithreadedLowStockMonitor", SmartInventoryTestSuite::testMultithreadedLowStockMonitor);

        System.out.println("\n--------------------------------------------------------------------------------");
        System.out.printf("Test Execution Summary: %d PASSED, %d FAILED (Total: %d)\n",
                testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("--------------------------------------------------------------------------------");

        if (testsFailed > 0) {
            System.err.println("Some tests failed!");
            System.exit(1);
        } else {
            System.out.println("ALL UNIT AND INTEGRATION TESTS PASSED SUCCESSFULLY! (100% PASS RATE)");
        }
    }

    private static void runTest(String testName, TestCase testCase) {
        System.out.printf("[RUNNING] %-45s ... ", testName);
        try {
            testCase.execute();
            System.out.println("PASSED [OK]");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED [X]");
            System.err.println("  Error: " + t.getMessage());
            t.printStackTrace();
            testsFailed++;
        }
    }

    @FunctionalInterface
    interface TestCase {
        void execute() throws Exception;
    }

    private static void assertTrue(boolean condition, String msg) {
        if (!condition) throw new AssertionError("Assertion failed: " + msg);
    }

    private static void assertEquals(Object expected, Object actual, String msg) {
        if (!java.util.Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("Expected '%s' but found '%s': %s", expected, actual, msg));
        }
    }

    private static void assertEqualsDouble(double expected, double actual, double delta, String msg) {
        if (Math.abs(expected - actual) > delta) {
            throw new AssertionError(String.format("Expected %.2f but found %.2f: %s", expected, actual, msg));
        }
    }

    // -------------------------------------------------------------
    // Test 1: Product Model and Encapsulation
    // -------------------------------------------------------------
    public static void testProductModelAndStockLogic() {
        Product p = new Product("TEST-1", "Test Product", "Groceries", 100.0, 10, 3);
        assertEquals("TEST-1", p.getId(), "Product ID should match");
        assertEquals("Test Product", p.getName(), "Product Name should match");
        assertEquals("Groceries", p.getCategory(), "Category should match");
        assertEqualsDouble(100.0, p.getPrice(), 0.001, "Price should match");
        assertEquals(10, p.getStockQuantity(), "Stock should match");
        assertTrue(!p.isLowStock(), "Stock 10 is above threshold 3");

        p.reduceStock(8);
        assertEquals(2, p.getStockQuantity(), "Stock should be 2 after reducing 8");
        assertTrue(p.isLowStock(), "Stock 2 should trigger low-stock alert (threshold 3)");

        p.addStock(5);
        assertEquals(7, p.getStockQuantity(), "Stock should be 7 after adding 5");
        assertTrue(!p.isLowStock(), "Stock 7 should not be low stock");

        // Test CSV conversion round-trip
        String csv = p.toCsvLine();
        Product reconstructed = Product.fromCsvLine(csv);
        assertEquals(p.getId(), reconstructed.getId(), "Reconstructed ID should match");
        assertEquals(p.getName(), reconstructed.getName(), "Reconstructed Name should match");
        assertEqualsDouble(p.getPrice(), reconstructed.getPrice(), 0.001, "Reconstructed Price should match");
        assertEquals(p.getStockQuantity(), reconstructed.getStockQuantity(), "Reconstructed Stock should match");
    }

    // -------------------------------------------------------------
    // Test 2: Cart Operations, Discounts, Subtotals
    // -------------------------------------------------------------
    public static void testCartCalculationsAndDiscounts() throws Exception {
        Product p1 = new Product("P1", "Item One", "Dairy", 50.0, 20, 5);
        Product p2 = new Product("P2", "Item Two", "Snacks", 20.0, 30, 5);

        Cart cart = new Cart();
        cart.addItem(p1, 2); // 100.00
        cart.addItem(p2, 5); // 100.00

        assertEqualsDouble(200.0, cart.getSubtotal(), 0.01, "Cart subtotal should be 200");
        assertEquals(7, cart.getTotalItemCount(), "Cart total item count should be 7");

        // Test discount
        cart.setDiscountPercentage(10.0); // 10% of 200 = 20
        assertEqualsDouble(20.0, cart.getDiscountAmount(), 0.01, "Discount amount should be 20.0");
        assertEqualsDouble(180.0, cart.getTaxableAmount(), 0.01, "Taxable amount should be 180.0");

        // Update quantity
        cart.updateQuantity(p1, 3); // 3 * 50 = 150 + 100 = 250
        assertEqualsDouble(250.0, cart.getSubtotal(), 0.01, "Updated subtotal should be 250");
    }

    // -------------------------------------------------------------
    // Test 3: InsufficientStockException
    // -------------------------------------------------------------
    public static void testInsufficientStockExceptionHandling() {
        Product p = new Product("P-LIMITED", "Limited Item", "General", 500.0, 3, 1);
        Cart cart = new Cart();

        boolean caught = false;
        try {
            cart.addItem(p, 5); // Requesting 5 when only 3 available
        } catch (InsufficientStockException e) {
            caught = true;
            assertEquals("P-LIMITED", e.getProductId(), "Exception product ID should match");
            assertEquals(3, e.getAvailableStock(), "Available stock in exception should be 3");
            assertEquals(5, e.getRequestedQuantity(), "Requested quantity in exception should be 5");
        }
        assertTrue(caught, "InsufficientStockException must be thrown when cart exceeds available stock");
    }

    // -------------------------------------------------------------
    // Test 4: CashPayment Strategy
    // -------------------------------------------------------------
    public static void testCashPaymentMethod() throws Exception {
        PaymentMethod cash = new CashPayment();
        assertEquals("CASH", cash.getMethodName(), "Payment mode name should be CASH");

        // Successful cash payment with change
        PaymentDetails details = cash.processPayment(450.0, "500");
        assertEquals("SUCCESS", details.getStatus(), "Payment status should be SUCCESS");
        assertEqualsDouble(450.0, details.getAmountPaid(), 0.01, "Amount paid should be 450");
        assertTrue(details.getTransactionReference().contains("Change: Rs.50.00"), "Change should be 50");

        // Insufficient cash tendered
        boolean failed = false;
        try {
            cash.processPayment(500.0, "400");
        } catch (PaymentFailedException e) {
            failed = true;
            assertTrue(e.getMessage().contains("Insufficient cash"), "Message should mention shortfall");
        }
        assertTrue(failed, "PaymentFailedException should be thrown when cash tendered is less than total");
    }

    // -------------------------------------------------------------
    // Test 5: CardPayment Strategy
    // -------------------------------------------------------------
    public static void testCardPaymentMethod() throws Exception {
        PaymentMethod card = new CardPayment();
        assertEquals("CARD", card.getMethodName(), "Payment mode name should be CARD");

        // Valid 16-digit card
        PaymentDetails details = card.processPayment(1200.0, "1234567812345678");
        assertEquals("SUCCESS", details.getStatus(), "Payment should succeed");
        assertTrue(details.getTransactionReference().contains("****-****-****-5678"), "Card number should be masked for security");

        // Invalid short card number
        boolean caught = false;
        try {
            card.processPayment(500.0, "123456");
        } catch (PaymentFailedException e) {
            caught = true;
        }
        assertTrue(caught, "Invalid card number must throw PaymentFailedException");
    }

    // -------------------------------------------------------------
    // Test 6: UpiPayment Strategy
    // -------------------------------------------------------------
    public static void testUpiPaymentMethod() throws Exception {
        PaymentMethod upi = new UpiPayment();
        assertEquals("UPI", upi.getMethodName(), "Payment mode should be UPI");

        // Valid UPI ID
        PaymentDetails details = upi.processPayment(750.0, "retailer@okhdfcbank");
        assertEquals("SUCCESS", details.getStatus(), "UPI payment should succeed");
        assertTrue(details.getTransactionReference().contains("VPA: retailer@okhdfcbank"), "UPI reference should have VPA");

        // Invalid UPI ID without @
        boolean caught = false;
        try {
            upi.processPayment(750.0, "invalidupiid");
        } catch (PaymentFailedException e) {
            caught = true;
        }
        assertTrue(caught, "Invalid UPI ID format must throw PaymentFailedException");
    }

    // -------------------------------------------------------------
    // Test 7: Atomic Billing Checkout & Persistence
    // -------------------------------------------------------------
    public static void testAtomicBillingCheckoutAndPersistence() throws Exception {
        File tempDir = Files.createTempDirectory("smart_retail_test_").toFile();
        tempDir.deleteOnExit();

        ProductRepository pRepo = new ProductRepository(tempDir.getAbsolutePath());
        InvoiceRepository iRepo = new InvoiceRepository(tempDir.getAbsolutePath());

        InventoryService invService = new InventoryService(pRepo);
        BillingService billService = new BillingService(invService, iRepo);
        billService.setTaxRatePercentage(5.0); // 5% GST

        // Add test product
        Product p = invService.addProduct("T-PROD-1", "Test Coffee", "Beverages", 100.0, 10, 2);
        assertEquals(10, p.getStockQuantity(), "Initial stock is 10");

        // Add 3 to cart
        billService.addItemToCart("T-PROD-1", 3);
        billService.applyDiscount(10.0); // 10% discount: 300 - 30 = 270. Tax 5% of 270 = 13.50. Total = 283.50

        PaymentMethod upi = new UpiPayment();
        Invoice invoice = billService.checkout("Alice Smith", "9876543210", upi, "alice@okaxis");

        // Verify stock was atomically deducted: 10 - 3 = 7
        Product updatedP = invService.getProductById("T-PROD-1");
        assertEquals(7, updatedP.getStockQuantity(), "Stock should be decremented to 7");

        // Verify invoice totals
        assertEqualsDouble(300.0, invoice.getSubtotal(), 0.01, "Invoice subtotal should be 300");
        assertEqualsDouble(30.0, invoice.getDiscountAmount(), 0.01, "Discount should be 30");
        assertEqualsDouble(13.50, invoice.getTaxAmount(), 0.01, "Tax should be 13.50");
        assertEqualsDouble(283.50, invoice.getFinalTotal(), 0.01, "Final total should be 283.50");

        // Verify invoice was saved to repository
        assertTrue(iRepo.findById(invoice.getInvoiceId()).isPresent(), "Invoice must be persisted in repository");
    }

    // -------------------------------------------------------------
    // Test 8: Sales Analytics Aggregations
    // -------------------------------------------------------------
    public static void testSalesAnalyticsAggregations() throws Exception {
        File tempDir = Files.createTempDirectory("smart_analytics_test_").toFile();
        tempDir.deleteOnExit();

        InvoiceRepository iRepo = new InvoiceRepository(tempDir.getAbsolutePath());
        AnalyticsService analytics = new AnalyticsService(iRepo);

        Product p1 = new Product("AN-1", "Milk", "Dairy", 50.0, 50, 5);
        Product p2 = new Product("AN-2", "Bread", "Bakery", 40.0, 50, 5);

        List<CartItem> items1 = List.of(new CartItem(p1, 2), new CartItem(p2, 1)); // 100 + 40 = 140
        PaymentDetails pay1 = new PaymentDetails("CASH", 140.0, "CASH-1", null, "SUCCESS");
        Invoice inv1 = new Invoice("INV-1", null, "User A", "111", items1, 140.0, 0, 0, 0, 0, 140.0, pay1);

        List<CartItem> items2 = List.of(new CartItem(p1, 4)); // 200
        PaymentDetails pay2 = new PaymentDetails("UPI", 200.0, "UPI-1", null, "SUCCESS");
        Invoice inv2 = new Invoice("INV-2", null, "User B", "222", items2, 200.0, 0, 0, 0, 0, 200.0, pay2);

        iRepo.save(inv1);
        iRepo.save(inv2);

        assertEqualsDouble(340.0, analytics.getTotalRevenue(), 0.01, "Total revenue should be 340.0");
        assertEquals(2, analytics.getTotalInvoicesCount(), "Invoices count should be 2");
        assertEquals(7, analytics.getTotalUnitsSold(), "Total units sold should be 7 (2+1+4)");
        assertEqualsDouble(170.0, analytics.getAverageOrderValue(), 0.01, "AOV should be 170.0");

        // Top product by units: Milk (6 units) should be #1
        List<Map.Entry<String, Integer>> topUnits = analytics.getTopSellingProductsByUnits(1);
        assertEquals(1, topUnits.size(), "Should have 1 top product");
        assertTrue(topUnits.get(0).getKey().contains("Milk"), "Milk should be top product by units");
        assertEquals(6, topUnits.get(0).getValue().intValue(), "Milk units sold should be 6");

        // Category breakdown: Dairy = 300, Bakery = 40
        Map<String, Double> catRev = analytics.getCategoryRevenueBreakdown();
        assertEqualsDouble(300.0, catRev.get("Dairy"), 0.01, "Dairy revenue should be 300");
        assertEqualsDouble(40.0, catRev.get("Bakery"), 0.01, "Bakery revenue should be 40");
    }

    // -------------------------------------------------------------
    // Test 9: Multithreaded Low-Stock Monitor
    // -------------------------------------------------------------
    public static void testMultithreadedLowStockMonitor() throws Exception {
        File tempDir = Files.createTempDirectory("smart_thread_test_").toFile();
        tempDir.deleteOnExit();

        ProductRepository pRepo = new ProductRepository(tempDir.getAbsolutePath());
        InventoryService invService = new InventoryService(pRepo);

        // Product with stock below threshold
        invService.addProduct("LT-1", "Low Stock Butter", "Dairy", 120.0, 2, 5);

        LowStockMonitorService monitor = new LowStockMonitorService(invService, 1);
        final boolean[] alertReceived = {false};
        monitor.addListener(product -> {
            if ("LT-1".equals(product.getId())) {
                alertReceived[0] = true;
            }
        });

        monitor.start();
        assertTrue(monitor.isRunning(), "Monitor service should be running");

        // Wait up to 3 seconds for the daemon thread to execute
        Thread.sleep(2500);

        monitor.stop();
        assertTrue(alertReceived[0], "Low stock daemon alert listener should have been invoked for LT-1");
        assertTrue(!monitor.getAlertLogs().isEmpty(), "Alert logs should contain recorded low stock notifications");
    }
}
