package com.retail.inventory.gui;

import com.retail.inventory.gui.controller.MainController;
import com.retail.inventory.repository.InvoiceRepository;
import com.retail.inventory.repository.ProductRepository;
import com.retail.inventory.service.AnalyticsService;
import com.retail.inventory.service.BillingService;
import com.retail.inventory.service.InventoryService;
import com.retail.inventory.service.LowStockMonitorService;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;

/**
 * Main JavaFX Application class for the Smart Inventory & Billing Manager.
 */
public class InventoryApp extends Application {

    private ProductRepository productRepository;
    private InvoiceRepository invoiceRepository;
    private InventoryService inventoryService;
    private BillingService billingService;
    private AnalyticsService analyticsService;
    private LowStockMonitorService monitorService;

    @Override
    public void init() throws Exception {
        super.init();

        // Resolve data storage folder
        String baseDir = System.getProperty("user.dir");
        File dataDir = new File(baseDir, "data");
        if (!dataDir.exists()) {
            dataDir.mkdirs();
        }

        // Initialize Data Repositories
        productRepository = new ProductRepository(dataDir.getAbsolutePath());
        invoiceRepository = new InvoiceRepository(dataDir.getAbsolutePath());

        // Initialize Services
        inventoryService = new InventoryService(productRepository);
        billingService = new BillingService(inventoryService, invoiceRepository);
        analyticsService = new AnalyticsService(invoiceRepository);

        // Start background multithreaded daemon monitor
        monitorService = new LowStockMonitorService(inventoryService, 5);
        monitorService.start();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/retail/inventory/gui/views/main_layout.fxml"));
        Parent root = loader.load();

        MainController mainController = loader.getController();
        mainController.initServices(inventoryService, billingService, analyticsService, monitorService);

        Scene scene = new Scene(root, 1220, 780);
        String cssPath = getClass().getResource("/com/retail/inventory/gui/css/styles.css").toExternalForm();
        scene.getStylesheets().add(cssPath);

        primaryStage.setTitle("Smart Retail - Inventory & POS Billing Suite");
        primaryStage.setMinWidth(1080);
        primaryStage.setMinHeight(680);
        primaryStage.setScene(scene);

        // Graceful window close
        primaryStage.setOnCloseRequest(event -> {
            if (monitorService != null) {
                monitorService.stop();
            }
            Platform.exit();
            System.exit(0);
        });

        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        if (monitorService != null) {
            monitorService.stop();
        }
        super.stop();
    }
}
