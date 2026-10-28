package com.retail.inventory.gui.dialog;

import com.retail.inventory.gui.util.AlertHelper;
import com.retail.inventory.model.Product;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * Modal dialog for creating or updating products.
 */
public class ProductDialog {

    public static class ProductResult {
        public String id;
        public String name;
        public String category;
        public double price;
        public int stock;
        public int threshold;

        public ProductResult(String id, String name, String category, double price, int stock, int threshold) {
            this.id = id;
            this.name = name;
            this.category = category;
            this.price = price;
            this.stock = stock;
            this.threshold = threshold;
        }
    }

    public static ProductResult show(Window owner, Product existingProduct) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        boolean isEdit = existingProduct != null;
        stage.setTitle(isEdit ? "Edit Product Details" : "Add New Retail Product");

        VBox root = new VBox(20);
        root.setPadding(new Insets(24));
        root.setPrefWidth(440);
        root.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label(isEdit ? "Update Product Information" : "Create New Inventory Item");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        GridPane grid = new GridPane();
        grid.setHgap(12);
        grid.setVgap(12);

        // Fields
        TextField idField = new TextField();
        idField.setPromptText("e.g. PRD-115");
        if (isEdit) {
            idField.setText(existingProduct.getId());
            idField.setDisable(true);
        }

        TextField nameField = new TextField();
        nameField.setPromptText("Product Name");
        if (isEdit) nameField.setText(existingProduct.getName());

        ComboBox<String> categoryCombo = new ComboBox<>();
        categoryCombo.getItems().addAll("Groceries", "Dairy", "Beverages", "Snacks", "Personal Care", "Household", "Bakery");
        categoryCombo.setEditable(true);
        if (isEdit) {
            categoryCombo.setValue(existingProduct.getCategory());
        } else {
            categoryCombo.setValue("Groceries");
        }

        TextField priceField = new TextField();
        priceField.setPromptText("Unit Price (₹)");
        if (isEdit) priceField.setText(String.valueOf(existingProduct.getPrice()));

        TextField stockField = new TextField();
        stockField.setPromptText("Initial Stock Units");
        if (isEdit) {
            stockField.setText(String.valueOf(existingProduct.getStockQuantity()));
            stockField.setDisable(true); // Inventory replenishment should be done via Restock
        }

        TextField thresholdField = new TextField();
        thresholdField.setPromptText("Alert Minimum Threshold");
        if (isEdit) {
            thresholdField.setText(String.valueOf(existingProduct.getLowStockThreshold()));
        } else {
            thresholdField.setText("5");
        }

        int row = 0;
        grid.add(new Label("Product ID:"), 0, row);
        grid.add(idField, 1, row++);

        grid.add(new Label("Product Name:"), 0, row);
        grid.add(nameField, 1, row++);

        grid.add(new Label("Category:"), 0, row);
        grid.add(categoryCombo, 1, row++);

        grid.add(new Label("Unit Price (₹):"), 0, row);
        grid.add(priceField, 1, row++);

        grid.add(new Label(isEdit ? "Current Stock:" : "Initial Stock:"), 0, row);
        grid.add(stockField, 1, row++);

        grid.add(new Label("Low-Stock Threshold:"), 0, row);
        grid.add(thresholdField, 1, row++);

        final ProductResult[] result = new ProductResult[1];

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> stage.close());

        Button saveBtn = new Button(isEdit ? "Save Changes" : "Create Product");
        saveBtn.getStyleClass().add("btn-primary");
        saveBtn.setOnAction(e -> {
            String id = idField.getText().trim();
            String name = nameField.getText().trim();
            String cat = categoryCombo.getValue() != null ? categoryCombo.getValue().trim() : "General";
            String priceStr = priceField.getText().trim();
            String stockStr = stockField.getText().trim();
            String threshStr = thresholdField.getText().trim();

            if (id.isEmpty() || name.isEmpty() || cat.isEmpty() || priceStr.isEmpty() || threshStr.isEmpty() || (!isEdit && stockStr.isEmpty())) {
                AlertHelper.showWarning(stage, "Validation Warning", "Incomplete Form", "Please fill in all required fields.");
                return;
            }

            double price;
            int stock = isEdit ? existingProduct.getStockQuantity() : 0;
            int threshold;

            try {
                price = Double.parseDouble(priceStr);
                if (price <= 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertHelper.showWarning(stage, "Invalid Input", "Price Error", "Price must be a positive number.");
                return;
            }

            if (!isEdit) {
                try {
                    stock = Integer.parseInt(stockStr);
                    if (stock < 0) throw new NumberFormatException();
                } catch (NumberFormatException ex) {
                    AlertHelper.showWarning(stage, "Invalid Input", "Stock Error", "Stock must be a non-negative integer.");
                    return;
                }
            }

            try {
                threshold = Integer.parseInt(threshStr);
                if (threshold < 0) throw new NumberFormatException();
            } catch (NumberFormatException ex) {
                AlertHelper.showWarning(stage, "Invalid Input", "Threshold Error", "Threshold must be a non-negative integer.");
                return;
            }

            result[0] = new ProductResult(id, name, cat, price, stock, threshold);
            stage.close();
        });

        btnBox.getChildren().addAll(cancelBtn, saveBtn);

        root.getChildren().addAll(titleLabel, grid, btnBox);

        Scene scene = new Scene(root);
        try {
            String cssPath = ProductDialog.class.getResource("/com/retail/inventory/gui/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception ignored) {
        }

        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return result[0];
    }
}
