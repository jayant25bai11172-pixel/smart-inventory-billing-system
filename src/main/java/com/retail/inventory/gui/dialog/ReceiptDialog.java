package com.retail.inventory.gui.dialog;

import com.retail.inventory.gui.util.CurrencyFormatter;
import com.retail.inventory.model.CartItem;
import com.retail.inventory.model.Invoice;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.time.format.DateTimeFormatter;

/**
 * Thermal-style printable receipt modal dialog.
 */
public class ReceiptDialog {

    public static void show(Window owner, Invoice invoice) {
        Stage stage = new Stage();
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("Customer Tax Invoice - " + invoice.getInvoiceId());

        VBox root = new VBox(16);
        root.setPadding(new Insets(20));
        root.setStyle("-fx-background-color: #F8FAFC;");

        // Receipt Card
        VBox receiptBox = new VBox(10);
        receiptBox.getStyleClass().add("card");
        receiptBox.setStyle("-fx-background-color: #FFFFFF; -fx-padding: 24; -fx-background-radius: 8;");
        receiptBox.setPrefWidth(420);

        // Header
        Label storeName = new Label("SMART RETAIL SUPERMARKET");
        storeName.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #0F172A;");
        storeName.setAlignment(Pos.CENTER);

        Label storeSub = new Label("GSTIN: 33AAAAA0000A1Z5 | Retail Point-of-Sale");
        storeSub.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748B;");
        storeSub.setAlignment(Pos.CENTER);

        Separator sep1 = new Separator();

        // Invoice Meta
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm:ss");
        GridPane metaGrid = new GridPane();
        metaGrid.setHgap(10);
        metaGrid.setVgap(4);

        metaGrid.add(createLabel("Invoice ID:", true), 0, 0);
        metaGrid.add(createLabel(invoice.getInvoiceId(), false), 1, 0);

        metaGrid.add(createLabel("Date & Time:", true), 0, 1);
        metaGrid.add(createLabel(invoice.getCreatedAt().format(dtf), false), 1, 1);

        metaGrid.add(createLabel("Customer:", true), 0, 2);
        metaGrid.add(createLabel(invoice.getCustomerName() + " (" + invoice.getCustomerPhone() + ")", false), 1, 2);

        metaGrid.add(createLabel("Payment Mode:", true), 0, 3);
        metaGrid.add(createLabel(invoice.getPaymentDetails().getPaymentMode(), false), 1, 3);

        Separator sep2 = new Separator();

        // Items Table
        VBox itemsBox = new VBox(6);
        HBox headerRow = new HBox();
        Label hName = createLabel("Item Description", true);
        hName.setPrefWidth(180);
        Label hQty = createLabel("Qty", true);
        hQty.setPrefWidth(50);
        hQty.setAlignment(Pos.CENTER_RIGHT);
        Label hRate = createLabel("Rate", true);
        hRate.setPrefWidth(80);
        hRate.setAlignment(Pos.CENTER_RIGHT);
        Label hTotal = createLabel("Total", true);
        hTotal.setPrefWidth(80);
        hTotal.setAlignment(Pos.CENTER_RIGHT);
        headerRow.getChildren().addAll(hName, hQty, hRate, hTotal);
        itemsBox.getChildren().add(headerRow);
        itemsBox.getChildren().add(new Separator());

        for (CartItem item : invoice.getItems()) {
            HBox row = new HBox();
            Label iName = createLabel(item.getProduct().getName(), false);
            iName.setPrefWidth(180);
            Label iQty = createLabel(String.valueOf(item.getQuantity()), false);
            iQty.setPrefWidth(50);
            iQty.setAlignment(Pos.CENTER_RIGHT);
            Label iRate = createLabel(CurrencyFormatter.format(item.getProduct().getPrice()), false);
            iRate.setPrefWidth(80);
            iRate.setAlignment(Pos.CENTER_RIGHT);
            Label iTotal = createLabel(CurrencyFormatter.format(item.getSubtotal()), false);
            iTotal.setPrefWidth(80);
            iTotal.setAlignment(Pos.CENTER_RIGHT);
            row.getChildren().addAll(iName, iQty, iRate, iTotal);
            itemsBox.getChildren().add(row);
        }

        Separator sep3 = new Separator();

        // Financial Totals
        GridPane totalGrid = new GridPane();
        totalGrid.setHgap(10);
        totalGrid.setVgap(4);
        totalGrid.setAlignment(Pos.CENTER_RIGHT);

        totalGrid.add(createLabel("Subtotal:", false), 0, 0);
        totalGrid.add(createLabel(CurrencyFormatter.format(invoice.getSubtotal()), true), 1, 0);

        if (invoice.getDiscountPercentage() > 0) {
            totalGrid.add(createLabel(String.format("Discount (%.1f%%):", invoice.getDiscountPercentage()), false), 0, 1);
            totalGrid.add(createLabel("-" + CurrencyFormatter.format(invoice.getDiscountAmount()), true), 1, 1);
        }

        totalGrid.add(createLabel(String.format("GST (%.1f%%):", invoice.getTaxRatePercentage()), false), 0, 2);
        totalGrid.add(createLabel(CurrencyFormatter.format(invoice.getTaxAmount()), true), 1, 2);

        Label grandLabel = createLabel("Grand Total:", true);
        grandLabel.setStyle("-fx-font-size: 15px; -fx-font-weight: bold;");
        Label grandAmount = createLabel(CurrencyFormatter.format(invoice.getFinalTotal()), true);
        grandAmount.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: #2563EB;");

        totalGrid.add(grandLabel, 0, 3);
        totalGrid.add(grandAmount, 1, 3);

        Separator sep4 = new Separator();

        Label footer = new Label("Thank you for shopping with SmartRetail!\nPlease visit again.");
        footer.setStyle("-fx-font-size: 11px; -fx-text-fill: #94A3B8; -fx-alignment: center;");
        footer.setMaxWidth(Double.MAX_VALUE);
        footer.setAlignment(Pos.CENTER);

        receiptBox.getChildren().addAll(
                storeName, storeSub, sep1,
                metaGrid, sep2,
                itemsBox, sep3,
                totalGrid, sep4,
                footer
        );

        // Buttons
        HBox buttonBar = new HBox(12);
        buttonBar.setAlignment(Pos.CENTER_RIGHT);

        Button copyBtn = new Button("📋 Copy Receipt Text");
        copyBtn.getStyleClass().add("btn-secondary");
        copyBtn.setOnAction(e -> {
            Clipboard clipboard = Clipboard.getSystemClipboard();
            ClipboardContent content = new ClipboardContent();
            content.putString(generatePlainTextReceipt(invoice));
            clipboard.setContent(content);
            copyBtn.setText("✓ Copied!");
        });

        Button closeBtn = new Button("Close");
        closeBtn.getStyleClass().add("btn-primary");
        closeBtn.setOnAction(e -> stage.close());

        buttonBar.getChildren().addAll(copyBtn, closeBtn);

        root.getChildren().addAll(receiptBox, buttonBar);

        Scene scene = new Scene(root);
        try {
            String cssPath = ReceiptDialog.class.getResource("/com/retail/inventory/gui/css/styles.css").toExternalForm();
            scene.getStylesheets().add(cssPath);
        } catch (Exception ignored) {
        }

        stage.setScene(scene);
        stage.setResizable(false);
        stage.showAndWait();
    }

    private static Label createLabel(String text, boolean bold) {
        Label l = new Label(text);
        if (bold) {
            l.setStyle("-fx-font-weight: bold; -fx-text-fill: #0F172A;");
        } else {
            l.setStyle("-fx-text-fill: #475569;");
        }
        return l;
    }

    private static String generatePlainTextReceipt(Invoice invoice) {
        StringBuilder sb = new StringBuilder();
        sb.append("=========================================\n");
        sb.append("        SMART RETAIL SUPERMARKET         \n");
        sb.append("  GSTIN: 33AAAAA0000A1Z5 | Retail POS    \n");
        sb.append("=========================================\n");
        sb.append("Invoice: ").append(invoice.getInvoiceId()).append("\n");
        sb.append("Date:    ").append(invoice.getCreatedAt().toString()).append("\n");
        sb.append("Customer:").append(invoice.getCustomerName()).append(" (").append(invoice.getCustomerPhone()).append(")\n");
        sb.append("Payment: ").append(invoice.getPaymentDetails().getPaymentMode()).append("\n");
        sb.append("-----------------------------------------\n");
        sb.append(String.format("%-18s %4s %8s %8s\n", "Item", "Qty", "Rate", "Total"));
        sb.append("-----------------------------------------\n");
        for (CartItem item : invoice.getItems()) {
            sb.append(String.format("%-18s %4d %8.2f %8.2f\n",
                    truncate(item.getProduct().getName(), 18),
                    item.getQuantity(),
                    item.getProduct().getPrice(),
                    item.getSubtotal()));
        }
        sb.append("-----------------------------------------\n");
        sb.append(String.format("Subtotal:               %16.2f\n", invoice.getSubtotal()));
        if (invoice.getDiscountPercentage() > 0) {
            sb.append(String.format("Discount (%.1f%%):        %16.2f\n", invoice.getDiscountPercentage(), -invoice.getDiscountAmount()));
        }
        sb.append(String.format("GST (%.1f%%):             %16.2f\n", invoice.getTaxRatePercentage(), invoice.getTaxAmount()));
        sb.append("-----------------------------------------\n");
        sb.append(String.format("GRAND TOTAL:            %16.2f\n", invoice.getFinalTotal()));
        sb.append("=========================================\n");
        sb.append("       Thank you for shopping with us!   \n");
        sb.append("=========================================\n");
        return sb.toString();
    }

    private static String truncate(String text, int max) {
        if (text == null) return "";
        if (text.length() <= max) return text;
        return text.substring(0, max - 2) + "..";
    }
}
