package com.retail.inventory.gui.controller;

import com.retail.inventory.gui.util.AlertHelper;
import com.retail.inventory.model.Product;
import com.retail.inventory.service.AnalyticsService;
import com.retail.inventory.service.BillingService;
import com.retail.inventory.service.InventoryService;
import com.retail.inventory.service.LowStockMonitorService;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Controller orchestrating master shell navigation, header status, and views.
 */
public class MainController {

    @FXML private Button btnNavDashboard;
    @FXML private Button btnNavPos;
    @FXML private Button btnNavInventory;
    @FXML private Button btnNavAnalytics;

    @FXML private Label lblHeaderTitle;
    @FXML private Label lblHeaderSubtitle;
    @FXML private Label lblClock;
    @FXML private Label lblSidebarLowStockCount;
    @FXML private Circle circleMonitorStatus;

    @FXML private StackPane contentArea;

    private InventoryService inventoryService;
    private BillingService billingService;
    private AnalyticsService analyticsService;
    private LowStockMonitorService monitorService;

    // View caches
    private Parent dashboardView;
    private DashboardController dashboardController;

    private Parent posView;
    private PosController posController;

    private Parent inventoryView;
    private InventoryController inventoryController;

    private Parent analyticsView;
    private AnalyticsController analyticsController;

    public void initServices(InventoryService inventoryService,
                             BillingService billingService,
                             AnalyticsService analyticsService,
                             LowStockMonitorService monitorService) {
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.analyticsService = analyticsService;
        this.monitorService = monitorService;

        initClock();
        initDaemonListener();
        updateLowStockBadge();

        // Load initial dashboard view
        loadDashboardView();
    }

    private void initClock() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("hh:mm:ss a");
        Timeline clock = new Timeline(new KeyFrame(Duration.ZERO, e -> {
            lblClock.setText(LocalDateTime.now().format(dtf));
        }), new KeyFrame(Duration.seconds(1)));
        clock.setCycleCount(Animation.INDEFINITE);
        clock.play();
    }

    private void initDaemonListener() {
        if (monitorService != null) {
            monitorService.addListener(product -> {
                Platform.runLater(() -> {
                    updateLowStockBadge();
                    if (dashboardController != null) {
                        dashboardController.refreshDashboard();
                    }
                });
            });
        }
    }

    public void updateLowStockBadge() {
        if (inventoryService != null) {
            int lowCount = inventoryService.getLowStockProducts().size();
            lblSidebarLowStockCount.setText(String.valueOf(lowCount));
            if (lowCount > 0) {
                circleMonitorStatus.setStyle("-fx-fill: #F59E0B;");
            } else {
                circleMonitorStatus.setStyle("-fx-fill: #10B981;");
            }
        }
    }

    @FXML
    public void handleNavDashboard() {
        loadDashboardView();
    }

    @FXML
    public void handleNavPos() {
        loadPosView();
    }

    @FXML
    public void handleNavInventory() {
        loadInventoryView();
    }

    @FXML
    public void handleNavAnalytics() {
        loadAnalyticsView();
    }

    private void setActiveNavButton(Button activeButton) {
        Button[] navButtons = {btnNavDashboard, btnNavPos, btnNavInventory, btnNavAnalytics};
        for (Button btn : navButtons) {
            if (btn == activeButton) {
                btn.getStyleClass().remove("nav-button");
                if (!btn.getStyleClass().contains("nav-button-active")) {
                    btn.getStyleClass().add("nav-button-active");
                }
            } else {
                btn.getStyleClass().remove("nav-button-active");
                if (!btn.getStyleClass().contains("nav-button")) {
                    btn.getStyleClass().add("nav-button");
                }
            }
        }
    }

    public void loadDashboardView() {
        setActiveNavButton(btnNavDashboard);
        lblHeaderTitle.setText("Store Overview & Health");
        lblHeaderSubtitle.setText("Live inventory summary, critical alerts, and retail performance");

        try {
            if (dashboardView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/retail/inventory/gui/views/dashboard_view.fxml"));
                dashboardView = loader.load();
                dashboardController = loader.getController();
                dashboardController.init(this, inventoryService, billingService, analyticsService);
            } else {
                dashboardController.refreshDashboard();
            }
            contentArea.getChildren().setAll(dashboardView);
            updateLowStockBadge();
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError(contentArea.getScene().getWindow(), "Navigation Error", "Could not load Dashboard", e.getMessage());
        }
    }

    public void loadPosView() {
        setActiveNavButton(btnNavPos);
        lblHeaderTitle.setText("Point of Sale (POS) Billing");
        lblHeaderSubtitle.setText("Search products, manage customer cart, and process checkout");

        try {
            if (posView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/retail/inventory/gui/views/pos_view.fxml"));
                posView = loader.load();
                posController = loader.getController();
                posController.init(this, inventoryService, billingService);
            } else {
                posController.refreshCatalog();
            }
            contentArea.getChildren().setAll(posView);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError(contentArea.getScene().getWindow(), "Navigation Error", "Could not load POS View", e.getMessage());
        }
    }

    public void loadInventoryView() {
        setActiveNavButton(btnNavInventory);
        lblHeaderTitle.setText("Inventory Catalog Management");
        lblHeaderSubtitle.setText("Comprehensive catalog of retail SKUs, pricing, stock levels, and safety thresholds");

        try {
            if (inventoryView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/retail/inventory/gui/views/inventory_view.fxml"));
                inventoryView = loader.load();
                inventoryController = loader.getController();
                inventoryController.init(this, inventoryService);
            } else {
                inventoryController.refreshTable();
            }
            contentArea.getChildren().setAll(inventoryView);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError(contentArea.getScene().getWindow(), "Navigation Error", "Could not load Inventory View", e.getMessage());
        }
    }

    public void loadAnalyticsView() {
        setActiveNavButton(btnNavAnalytics);
        lblHeaderTitle.setText("Business Intelligence & Analytics");
        lblHeaderSubtitle.setText("Sales trends, revenue distribution, top performers, and audit history");

        try {
            if (analyticsView == null) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/retail/inventory/gui/views/analytics_view.fxml"));
                analyticsView = loader.load();
                analyticsController = loader.getController();
                analyticsController.init(this, analyticsService, billingService);
            } else {
                analyticsController.refreshAnalytics();
            }
            contentArea.getChildren().setAll(analyticsView);
        } catch (IOException e) {
            e.printStackTrace();
            AlertHelper.showError(contentArea.getScene().getWindow(), "Navigation Error", "Could not load Analytics View", e.getMessage());
        }
    }

    public void onDataChanged() {
        updateLowStockBadge();
        if (dashboardController != null) dashboardController.refreshDashboard();
        if (inventoryController != null) inventoryController.refreshTable();
        if (posController != null) posController.refreshCatalog();
        if (analyticsController != null) analyticsController.refreshAnalytics();
    }
}
