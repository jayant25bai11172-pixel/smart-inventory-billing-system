package com.retail.inventory.gui.controller;

import com.retail.inventory.gui.dialog.ReceiptDialog;
import com.retail.inventory.gui.util.AlertHelper;
import com.retail.inventory.gui.util.CurrencyFormatter;
import com.retail.inventory.model.Invoice;
import com.retail.inventory.service.AnalyticsService;
import com.retail.inventory.service.BillingService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controller for business intelligence, visual charts, and invoice audit history.
 */
public class AnalyticsController {

    @FXML private Label lblGrossRevenue;
    @FXML private Label lblInvoicesIssued;
    @FXML private Label lblUnitsSold;
    @FXML private Label lblAvgOrderValue;

    @FXML private BarChart<String, Number> chartTopProducts;
    @FXML private PieChart chartCategoryBreakdown;

    @FXML private TableView<Invoice> tableInvoices;
    @FXML private TableColumn<Invoice, String> colInvId;
    @FXML private TableColumn<Invoice, String> colInvDate;
    @FXML private TableColumn<Invoice, String> colInvCustomer;
    @FXML private TableColumn<Invoice, String> colInvPhone;
    @FXML private TableColumn<Invoice, String> colInvQty;
    @FXML private TableColumn<Invoice, String> colInvTotal;
    @FXML private TableColumn<Invoice, String> colInvPayment;
    @FXML private TableColumn<Invoice, Void> colInvAction;

    private MainController mainController;
    private AnalyticsService analyticsService;
    private BillingService billingService;

    public void init(MainController mainController, AnalyticsService analyticsService, BillingService billingService) {
        this.mainController = mainController;
        this.analyticsService = analyticsService;
        this.billingService = billingService;

        setupTable();
        refreshAnalytics();
    }

    private void setupTable() {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");

        colInvId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getInvoiceId()));
        colInvDate.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCreatedAt().format(dtf)));
        colInvCustomer.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerName()));
        colInvPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCustomerPhone()));
        colInvQty.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTotalQuantity() + " units"));
        colInvTotal.setCellValueFactory(c -> new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getFinalTotal())));
        colInvPayment.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPaymentDetails().getPaymentMode()));

        colInvAction.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("View");
            {
                btn.getStyleClass().add("btn-secondary");
                btn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
                btn.setOnAction(e -> {
                    Invoice inv = getTableView().getItems().get(getIndex());
                    ReceiptDialog.show(tableInvoices.getScene().getWindow(), inv);
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btn);
                }
            }
        });
    }

    public void refreshAnalytics() {
        if (analyticsService == null || billingService == null) return;

        // KPI values
        double totalRev = analyticsService.getTotalRevenue();
        int invCount = analyticsService.getTotalInvoicesCount();
        int unitsSold = analyticsService.getTotalUnitsSold();
        double aov = analyticsService.getAverageOrderValue();

        lblGrossRevenue.setText(CurrencyFormatter.format(totalRev));
        lblInvoicesIssued.setText(String.valueOf(invCount));
        lblUnitsSold.setText(String.valueOf(unitsSold));
        lblAvgOrderValue.setText(CurrencyFormatter.format(aov));

        // Charts: Top Products BarChart
        chartTopProducts.getData().clear();
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Revenue");
        List<Map.Entry<String, Double>> topProducts = analyticsService.getTopSellingProductsByRevenue(5);
        for (Map.Entry<String, Double> entry : topProducts) {
            String label = entry.getKey();
            if (label.contains("(")) {
                label = label.substring(0, label.indexOf("(")).trim();
            }
            series.getData().add(new XYChart.Data<>(label, entry.getValue()));
        }
        chartTopProducts.getData().add(series);

        // Charts: Category PieChart
        chartCategoryBreakdown.getData().clear();
        Map<String, Double> catRevenue = analyticsService.getCategoryRevenueBreakdown();
        ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();
        for (Map.Entry<String, Double> entry : catRevenue.entrySet()) {
            if (entry.getValue() > 0) {
                pieData.add(new PieChart.Data(entry.getKey() + " (" + CurrencyFormatter.format(entry.getValue()) + ")", entry.getValue()));
            }
        }
        chartCategoryBreakdown.setData(pieData);

        // Invoices Table
        List<Invoice> allInvoices = billingService.getAllInvoices().stream()
                .sorted(Comparator.comparing(Invoice::getCreatedAt).reversed())
                .collect(Collectors.toList());
        tableInvoices.setItems(FXCollections.observableArrayList(allInvoices));
    }

    @FXML
    public void handleViewSelectedReceipt() {
        Invoice selected = tableInvoices.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertHelper.showWarning(tableInvoices.getScene().getWindow(), "Selection Required", "No Invoice Selected", "Please select an invoice from the ledger to view.");
            return;
        }
        ReceiptDialog.show(tableInvoices.getScene().getWindow(), selected);
    }
}
