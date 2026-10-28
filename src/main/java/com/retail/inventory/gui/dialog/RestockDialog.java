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
 * Modal dialog for replenishing stock of a product.
 */
public class RestockDialog {

    public static Integer show(Window owner, Product product) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Replenish Inventory Stock");

        VBox root = new VBox(16);
        root.setPadding(new Insets(24));
        root.setPrefWidth(380);
        root.setStyle("-fx-background-color: #FFFFFF;");

        Label titleLabel = new Label("Restock Product");
        titleLabel.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");

        Label productInfo = new Label(product.getName() + " (" + product.getId() + ")");
        productInfo.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");

        Label currentStock = new Label("Current available stock: " + product.getStockQuantity() + " units");
        currentStock.setStyle("-fx-text-fill: #64748B;");

        TextField qtyField = new TextField();
        qtyField.setPromptText("Units to add (e.g. 50)");

        final Integer[] result = new Integer[1];

        HBox btnBox = new HBox(12);
        btnBox.setAlignment(Pos.CENTER_RIGHT);

        Button cancelBtn = new Button("Cancel");
        cancelBtn.getStyleClass().add("btn-secondary");
        cancelBtn.setOnAction(e -> stage.close());

        Button addBtn = new Button("Add Stock");
        addBtn.getStyleClass().add("btn-success");
        addBtn.setOnAction(e -> {
            try {
                int qty = Integer.parseInt(qtyField.getText().trim());
                if (qty <= 0) throw new NumberFormatException();
                result[0] = qty;
                stage.close();
            } catch (NumberFormatException ex) {
                AlertHelper.showWarning(stage, "Invalid Input", "Invalid Quantity", "Please enter a positive whole number.");
            }
        });

        btnBox.getChildren().addAll(cancelBtn, addBtn);

        root.getChildren().addAll(titleLabel, productInfo, currentStock, qtyField, btnBox);

        Scene scene = new Scene(root);
        try {
            String cssPath = RestockDialog.class.getResource("/com/retail/inventory/gui/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception ignored) {
        }

        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();

        return result[0];
    }
}
