package com.retail.inventory.gui.controller;

import com.retail.inventory.gui.dialog.ProductDialog;
import com.retail.inventory.gui.dialog.RestockDialog;
import com.retail.inventory.gui.util.AlertHelper;
import com.retail.inventory.gui.util.CurrencyFormatter;
import com.retail.inventory.model.Product;
import com.retail.inventory.service.InventoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for managing the product inventory catalog, stock levels, and pricing.
 */
public class InventoryController {

    @FXML private TextField txtSearch;
    @FXML private ComboBox<String> cbCategoryFilter;
    @FXML private ComboBox<String> cbStatusFilter;

    @FXML private TableView<Product> tableInventory;
    @FXML private TableColumn<Product, String> colId;
    @FXML private TableColumn<Product, String> colName;
    @FXML private TableColumn<Product, String> colCategory;
    @FXML private TableColumn<Product, String> colPrice;
    @FXML private TableColumn<Product, String> colStock;
    @FXML private TableColumn<Product, String> colThreshold;
    @FXML private TableColumn<Product, Void> colStatus;
    @FXML private TableColumn<Product, String> colTotalVal;

    @FXML private Label lblInventoryCount;
    @FXML private Label lblTotalInventoryValue;
    @FXML private Label lblLowStockNotice;

    private MainController mainController;
    private InventoryService inventoryService;

    public void init(MainController mainController, InventoryService inventoryService) {
        this.mainController = mainController;
        this.inventoryService = inventoryService;

        setupFilters();
        setupTable();
        refreshTable();
    }

    private void setupFilters() {
        cbStatusFilter.getItems().addAll("All Statuses", "In Stock", "Low Stock", "Out of Stock");
        cbStatusFilter.setValue("All Statuses");

        cbStatusFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        cbCategoryFilter.valueProperty().addListener((obs, oldVal, newVal) -> refreshTable());
        txtSearch.textProperty().addListener((obs, oldVal, newVal) -> refreshTable());
    }

    private void setupTable() {
        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getPrice())));
        colStock.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getStockQuantity() + " units"));
        colThreshold.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLowStockThreshold() + " units"));
        colTotalVal.setCellValueFactory(c -> {
            double total = c.getValue().getPrice() * c.getValue().getStockQuantity();
            return new SimpleStringProperty(CurrencyFormatter.format(total));
        });

        colStatus.setCellFactory(param -> new TableCell<>() {
            private final Label badge = new Label();
            {
                badge.setStyle("-fx-font-size: 11px; -fx-font-weight: bold;");
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Product product = getTableView().getItems().get(getIndex());
                    int stock = product.getStockQuantity();
                    int threshold = product.getLowStockThreshold();

                    badge.getStyleClass().removeAll("badge-in-stock", "badge-low-stock", "badge-out-of-stock");
                    if (stock == 0) {
                        badge.setText("Out of Stock");
                        badge.getStyleClass().add("badge-out-of-stock");
                    } else if (stock <= threshold) {
                        badge.setText("Low Stock (" + stock + ")");
                        badge.getStyleClass().add("badge-low-stock");
                    } else {
                        badge.setText("In Stock (" + stock + ")");
                        badge.getStyleClass().add("badge-in-stock");
                    }
                    setGraphic(badge);
                }
            }
        });
    }

    public void refreshTable() {
        if (inventoryService == null) return;

        // Populate category dropdown dynamically
        List<String> categories = new ArrayList<>();
        categories.add("All Categories");
        inventoryService.getAllProducts().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .forEach(categories::add);

        String currentCat = cbCategoryFilter.getValue();
        cbCategoryFilter.getItems().setAll(categories);
        if (categories.contains(currentCat)) {
            cbCategoryFilter.setValue(currentCat);
        } else {
            cbCategoryFilter.setValue("All Categories");
        }

        // Filtering
        String search = txtSearch.getText() != null ? txtSearch.getText().trim().toLowerCase() : "";
        String cat = cbCategoryFilter.getValue() != null ? cbCategoryFilter.getValue() : "All Categories";
        String status = cbStatusFilter.getValue() != null ? cbStatusFilter.getValue() : "All Statuses";

        List<Product> filtered = inventoryService.getAllProducts().stream()
                .filter(p -> {
                    if (search.isEmpty()) return true;
                    return p.getId().toLowerCase().contains(search) || p.getName().toLowerCase().contains(search);
                })
                .filter(p -> {
                    if (cat.equalsIgnoreCase("All Categories")) return true;
                    return p.getCategory().equalsIgnoreCase(cat);
                })
                .filter(p -> {
                    if (status.equalsIgnoreCase("All Statuses")) return true;
                    if (status.equalsIgnoreCase("Out of Stock")) return p.getStockQuantity() == 0;
                    if (status.equalsIgnoreCase("Low Stock")) return p.isLowStock();
                    if (status.equalsIgnoreCase("In Stock")) return p.getStockQuantity() > p.getLowStockThreshold();
                    return true;
                })
                .collect(Collectors.toList());

        tableInventory.setItems(FXCollections.observableArrayList(filtered));

        // Update footer counters
        int totalProducts = inventoryService.getAllProducts().size();
        double totalValuation = inventoryService.getAllProducts().stream()
                .mapToDouble(p -> p.getPrice() * p.getStockQuantity())
                .sum();
        int lowStockCount = inventoryService.getLowStockProducts().size();

        lblInventoryCount.setText("Total Products: " + totalProducts);
        lblTotalInventoryValue.setText("Total Valuation: " + CurrencyFormatter.format(totalValuation));
        lblLowStockNotice.setText("Low Stock Items: " + lowStockCount);
    }

    @FXML
    public void handleAddProduct() {
        ProductDialog.ProductResult res = ProductDialog.show(tableInventory.getScene().getWindow(), null);
        if (res != null) {
            try {
                inventoryService.addProduct(res.id, res.name, res.category, res.price, res.stock, res.threshold);
                mainController.onDataChanged();
                AlertHelper.showInfo(tableInventory.getScene().getWindow(), "Success", "Product Added",
                        "Product '" + res.name + "' (" + res.id + ") was registered.");
            } catch (Exception ex) {
                AlertHelper.showError(tableInventory.getScene().getWindow(), "Creation Error", "Failed to Add Product", ex.getMessage());
            }
        }
    }

    @FXML
    public void handleRestockSelected() {
        Product selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning(tableInventory.getScene().getWindow(), "Selection Required", "No Product Selected", "Please select a product from the table to restock.");
            return;
        }

        Integer qty = RestockDialog.show(tableInventory.getScene().getWindow(), selected);
        if (qty != null && qty > 0) {
            try {
                inventoryService.replenishStock(selected.getId(), qty);
                mainController.onDataChanged();
                AlertHelper.showInfo(tableInventory.getScene().getWindow(), "Replenished", "Stock Updated",
                        String.format("Added %d units to '%s'. New Stock: %d", qty, selected.getName(), selected.getStockQuantity()));
            } catch (Exception ex) {
                AlertHelper.showError(tableInventory.getScene().getWindow(), "Error", "Restock Failed", ex.getMessage());
            }
        }
    }

    @FXML
    public void handleEditSelected() {
        Product selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning(tableInventory.getScene().getWindow(), "Selection Required", "No Product Selected", "Please select a product to edit.");
            return;
        }

        ProductDialog.ProductResult res = ProductDialog.show(tableInventory.getScene().getWindow(), selected);
        if (res != null) {
            try {
                if (res.price != selected.getPrice()) {
                    inventoryService.updateProductPrice(selected.getId(), res.price);
                }
                if (res.threshold != selected.getLowStockThreshold()) {
                    inventoryService.updateProductThreshold(selected.getId(), res.threshold);
                }
                mainController.onDataChanged();
                AlertHelper.showInfo(tableInventory.getScene().getWindow(), "Updated", "Product Updated",
                        "Successfully updated product details for " + selected.getName());
            } catch (Exception ex) {
                AlertHelper.showError(tableInventory.getScene().getWindow(), "Error", "Update Failed", ex.getMessage());
            }
        }
    }

    @FXML
    public void handleDeleteSelected() {
        Product selected = tableInventory.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning(tableInventory.getScene().getWindow(), "Selection Required", "No Product Selected", "Please select a product to delete.");
            return;
        }

        boolean confirmed = AlertHelper.showConfirmation(tableInventory.getScene().getWindow(),
                "Confirm Deletion",
                "Delete " + selected.getName() + " (" + selected.getId() + ")?",
                "Are you sure you want to permanently remove this product from the inventory catalog?");

        if (confirmed) {
            try {
                inventoryService.deleteProduct(selected.getId());
                mainController.onDataChanged();
                AlertHelper.showInfo(tableInventory.getScene().getWindow(), "Deleted", "Product Removed",
                        "Successfully deleted product from catalog.");
            } catch (Exception ex) {
                AlertHelper.showError(tableInventory.getScene().getWindow(), "Error", "Deletion Failed", ex.getMessage());
            }
        }
    }
}
