package com.retail.inventory.gui.controller;

import com.retail.inventory.gui.dialog.ProductDialog;
import com.retail.inventory.gui.dialog.RestockDialog;
import com.retail.inventory.gui.util.AlertHelper;
import com.retail.inventory.gui.util.CurrencyFormatter;
import com.retail.inventory.model.Invoice;
import com.retail.inventory.model.Product;
import com.retail.inventory.service.AnalyticsService;
import com.retail.inventory.service.BillingService;
import com.retail.inventory.service.InventoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the Dashboard KPI and stock alert overview.
 */
public class DashboardController {

    @FXML private Label lblTotalRevenue;
    @FXML private Label lblTotalInvoices;
    @FXML private Label lblTotalProducts;
    @FXML private Label lblLowStockBreaches;

    @FXML private HBox boxAlertBanner;
    @FXML private Label lblAlertMessage;

    @FXML private TableView<Product> tableLowStock;
    @FXML private TableColumn<Product, String> colLowId;
    @FXML private TableColumn<Product, String> colLowName;
    @FXML private TableColumn<Product, String> colLowCategory;
    @FXML private TableColumn<Product, String> colLowStock;
    @FXML private TableColumn<Product, String> colLowThreshold;
    @FXML private TableColumn<Product, Void> colLowAction;

    @FXML private TableView<Invoice> tableRecentInvoices;
    @FXML private TableColumn<Invoice, String> colRecentInvId;
    @FXML private TableColumn<Invoice, String> colRecentCustomer;
    @FXML private TableColumn<Invoice, String> colRecentTotal;
    @FXML private TableColumn<Invoice, String> colRecentMode;

    private MainController mainController;
    private InventoryService inventoryService;
    private BillingService billingService;
    private AnalyticsService analyticsService;

    public void init(MainController mainController,
                     InventoryService inventoryService,
                     BillingService billingService,
                     AnalyticsService analyticsService) {
        this.mainController = mainController;
        this.inventoryService = inventoryService;
        this.billingService = billingService;
        this.analyticsService = analyticsService;

        setupTables();
        refreshDashboard();
    }

    private void setupTables() {
        // Low Stock Table
        colLowId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getId()));
        colLowName.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getName()));
        colLowCategory.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCategory()));
        colLowStock.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getStockQuantity() + " units"));
        colLowThreshold.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getLowStockThreshold() + " units"));

        colLowAction.setCellFactory(param -> new TableCell<>() {
            private final Button restockBtn = new Button("Restock");
            {
                restockBtn.getStyleClass().add("btn-secondary");
                restockBtn.setStyle("-fx-font-size: 11px; -fx-padding: 4 8 4 8;");
                restockBtn.setOnAction(event -> {
                    Product product = getTableView().getItems().get(getIndex());
                    promptRestock(product);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(restockBtn);
                }
            }
        });

        // Recent Invoices Table
        colRecentInvId.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getInvoiceId()));
        colRecentCustomer.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getCustomerName()));
        colRecentTotal.setCellValueFactory(cellData -> new SimpleStringProperty(CurrencyFormatter.format(cellData.getValue().getFinalTotal())));
        colRecentMode.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getPaymentDetails().getPaymentMode()));
    }

    public void refreshDashboard() {
        if (inventoryService == null || analyticsService == null) return;

        // KPI metrics
        double revenue = analyticsService.getTotalRevenue();
        int invoicesCount = analyticsService.getTotalInvoicesCount();
        int productsCount = inventoryService.getAllProducts().size();
        List<Product> lowStockList = inventoryService.getLowStockProducts();

        lblTotalRevenue.setText(CurrencyFormatter.format(revenue));
        lblTotalInvoices.setText(String.valueOf(invoicesCount));
        lblTotalProducts.setText(String.valueOf(productsCount));
        lblLowStockBreaches.setText(String.valueOf(lowStockList.size()));

        // Alert Banner
        if (!lowStockList.isEmpty()) {
            boxAlertBanner.setVisible(true);
            boxAlertBanner.setManaged(true);
            lblAlertMessage.setText(String.format("Critical Notice: %d product(s) have breached safety thresholds and need restocking!", lowStockList.size()));
        } else {
            boxAlertBanner.setVisible(false);
            boxAlertBanner.setManaged(false);
        }

        // Tables
        tableLowStock.setItems(FXCollections.observableArrayList(lowStockList));

        List<Invoice> recent = billingService.getAllInvoices().stream()
                .sorted(Comparator.comparing(Invoice::getCreatedAt).reversed())
                .limit(10)
                .collect(Collectors.toList());
        tableRecentInvoices.setItems(FXCollections.observableArrayList(recent));
    }

    private void promptRestock(Product product) {
        Integer addedQty = RestockDialog.show(tableLowStock.getScene().getWindow(), product);
        if (addedQty != null && addedQty > 0) {
            try {
                inventoryService.replenishStock(product.getId(), addedQty);
                mainController.onDataChanged();
                AlertHelper.showInfo(tableLowStock.getScene().getWindow(), "Stock Replenished",
                        "Inventory Updated",
                        String.format("Successfully added %d unit(s) to '%s'. New stock: %d",
                                addedQty, product.getName(), product.getStockQuantity()));
            } catch (Exception ex) {
                AlertHelper.showError(tableLowStock.getScene().getWindow(), "Error", "Replenishment Failed", ex.getMessage());
            }
        }
    }

    @FXML
    public void handleQuickRestockFromBanner() {
        List<Product> low = inventoryService.getLowStockProducts();
        if (!low.isEmpty()) {
            promptRestock(low.get(0));
        }
    }

    @FXML
    public void handleOpenPos() {
        if (mainController != null) {
            mainController.loadPosView();
        }
    }

    @FXML
    public void handleAddProduct() {
        ProductDialog.ProductResult res = ProductDialog.show(tableLowStock.getScene().getWindow(), null);
        if (res != null) {
            try {
                inventoryService.addProduct(res.id, res.name, res.category, res.price, res.stock, res.threshold);
                mainController.onDataChanged();
                AlertHelper.showInfo(tableLowStock.getScene().getWindow(), "Success", "Product Added",
                        "Created product " + res.name + " (" + res.id + ")");
            } catch (Exception ex) {
                AlertHelper.showError(tableLowStock.getScene().getWindow(), "Error", "Could not add product", ex.getMessage());
            }
        }
    }
}
