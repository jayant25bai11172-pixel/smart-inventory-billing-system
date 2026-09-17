package com.retail.inventory;

import com.retail.inventory.repository.InvoiceRepository;
import com.retail.inventory.repository.ProductRepository;
import com.retail.inventory.service.AnalyticsService;
import com.retail.inventory.service.BillingService;
import com.retail.inventory.service.InventoryService;
import com.retail.inventory.service.LowStockMonitorService;
import com.retail.inventory.ui.ConsoleUI;

import java.io.File;

/**
 * Main Application Entry Point for the Smart Inventory & Billing Manager.
 * Initializes repositories, services, background threads, and the console UI.
 */
public class Main {
    public static void main(String[] args) {
        // Resolve data storage folder
        String baseDir = System.getProperty("user.dir");
        File dataDir = new File(baseDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Initialize Data Repositories
        ProductRepository productRepository = new ProductRepository(dataDir.getAbsolutePath());
        InvoiceRepository invoiceRepository = new InvoiceRepository(dataDir.getAbsolutePath());

        // Initialize Business Services
        InventoryService inventoryService = new InventoryService(productRepository);
        BillingService billingService = new BillingService(inventoryService, invoiceRepository);
        AnalyticsService analyticsService = new AnalyticsService(invoiceRepository);

        // Initialize Background Multithreaded Low-Stock Daemon Monitor (checks every 5 seconds)
        LowStockMonitorService monitorService = new LowStockMonitorService(inventoryService, 5);
        monitorService.start();

        // Register shutdown hook for graceful cleanup
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            monitorService.stop();
        }, "Shutdown-Cleanup-Thread"));

        // Launch Console User Interface
        ConsoleUI consoleUI = new ConsoleUI(inventoryService, billingService, analyticsService, monitorService);
        consoleUI.start();

        // Graceful stop after exit
        monitorService.stop();
    }
}
