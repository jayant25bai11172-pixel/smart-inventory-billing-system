package com.retail.inventory.gui.controller;

import com.retail.inventory.gui.dialog.ReceiptDialog;
import com.retail.inventory.gui.util.AlertHelper;
import com.retail.inventory.gui.util.CurrencyFormatter;
import com.retail.inventory.model.CartItem;
import com.retail.inventory.model.Invoice;
import com.retail.inventory.model.Product;
import com.retail.inventory.payment.CardPayment;
import com.retail.inventory.payment.CashPayment;
import com.retail.inventory.payment.PaymentMethod;
import com.retail.inventory.payment.UpiPayment;
import com.retail.inventory.service.BillingService;
import com.retail.inventory.service.InventoryService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Controller managing Point of Sale (POS) operations, cart management, and checkout.
 */
public class PosController {

    @FXML private TextField txtSearchProduct;
    @FXML private HBox boxCategoryChips;

    @FXML private TableView<Product> tableCatalog;
    @FXML private TableColumn<Product, String> colCatalogId;
    @FXML private TableColumn<Product, String> colCatalogName;
    @FXML private TableColumn<Product, String> colCatalogCategory;
    @FXML private TableColumn<Product, String> colCatalogPrice;
    @FXML private TableColumn<Product, String> colCatalogStock;
    @FXML private TableColumn<Product, Product> colCatalogAction;

    @FXML private Label lblCartItemCount;
    @FXML private TableView<CartItem> tableCart;
    @FXML private TableColumn<CartItem, String> colCartItemName;
    @FXML private TableColumn<CartItem, String> colCartItemPrice;
    @FXML private TableColumn<CartItem, CartItem> colCartItemQty;
    @FXML private TableColumn<CartItem, String> colCartItemSubtotal;
    @FXML private TableColumn<CartItem, CartItem> colCartItemRemove;

    @FXML private TextField txtCustomerName;
    @FXML private TextField txtCustomerPhone;

    @FXML private Label lblSubtotal;
    @FXML private TextField txtDiscountPct;
    @FXML private Label lblDiscountAmount;
    @FXML private Label lblTaxAmount;
    @FXML private Label lblGrandTotal;

    @FXML private RadioButton rbCash;
    @FXML private RadioButton rbCard;
    @FXML private RadioButton rbUpi;
    @FXML private ToggleGroup paymentToggleGroup;

    @FXML private StackPane panePaymentInputs;
    @FXML private HBox boxCashInput;
    @FXML private TextField txtCashTendered;
    @FXML private Label lblChangeDue;

    @FXML private HBox boxCardInput;
    @FXML private TextField txtCardNumber;

    @FXML private HBox boxUpiInput;
    @FXML private TextField txtUpiId;

    @FXML private Button btnCheckout;

    private MainController mainController;
    private InventoryService inventoryService;
    private BillingService billingService;

    private String selectedCategory = "All";
    private final ObservableList<CartItem> cartItemsObservable = FXCollections.observableArrayList();

    public void init(MainController mainController, InventoryService inventoryService, BillingService billingService) {
        this.mainController = mainController;
        this.inventoryService = inventoryService;
        this.billingService = billingService;

        setupCatalogTable();
        setupCartTable();
        setupPaymentToggles();
        setupDiscountListener();
        setupTenderedListener();
        setupSearchListener();

        buildCategoryChips();
        refreshCatalog();
        syncCartView();
    }

    private void setupCatalogTable() {
        colCatalogId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getId()));
        colCatalogName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getName()));
        colCatalogCategory.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getCategory()));
        colCatalogPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getPrice())));
        colCatalogStock.setCellValueFactory(c -> {
            int stock = c.getValue().getStockQuantity();
            return new SimpleStringProperty(stock > 0 ? stock + " units" : "Out of Stock");
        });

        colCatalogAction.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue()));
        colCatalogAction.setCellFactory(param -> new TableCell<>() {
            private final Button addBtn = new Button("+ Add");
            {
                addBtn.getStyleClass().add("btn-primary");
                addBtn.setStyle("-fx-font-size: 11px; -fx-padding: 3 8 3 8;");
            }

            @Override
            protected void updateItem(Product product, boolean empty) {
                super.updateItem(product, empty);
                if (empty || product == null) {
                    setGraphic(null);
                } else {
                    addBtn.setDisable(product.getStockQuantity() <= 0);
                    addBtn.setOnAction(event -> addToCart(product, 1));
                    setGraphic(addBtn);
                }
            }
        });

        tableCatalog.setRowFactory(tv -> {
            TableRow<Product> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    addToCart(row.getItem(), 1);
                }
            });
            return row;
        });
    }

    private void setupCartTable() {
        tableCart.setItems(cartItemsObservable);

        colCartItemName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getProduct().getName()));
        colCartItemPrice.setCellValueFactory(c -> new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getProduct().getPrice())));
        colCartItemSubtotal.setCellValueFactory(c -> new SimpleStringProperty(CurrencyFormatter.format(c.getValue().getSubtotal())));

        // Quantity Stepper Cell
        colCartItemQty.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue()));
        colCartItemQty.setCellFactory(param -> new TableCell<>() {
            private final Button minusBtn = new Button("-");
            private final Label qtyLbl = new Label();
            private final Button plusBtn = new Button("+");
            private final HBox box = new HBox(4, minusBtn, qtyLbl, plusBtn);

            {
                box.setAlignment(Pos.CENTER);
                minusBtn.getStyleClass().add("btn-secondary");
                minusBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                plusBtn.getStyleClass().add("btn-secondary");
                plusBtn.setStyle("-fx-font-size: 10px; -fx-padding: 2 6 2 6;");
                qtyLbl.setStyle("-fx-font-weight: bold; -fx-min-width: 20px; -fx-alignment: center;");
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    qtyLbl.setText(String.valueOf(item.getQuantity()));

                    minusBtn.setOnAction(e -> {
                        if (item.getQuantity() > 1) {
                            try {
                                billingService.updateCartItemQuantity(item.getProduct().getId(), item.getQuantity() - 1);
                                syncCartView();
                            } catch (Exception ex) {
                                AlertHelper.showError(tableCart.getScene().getWindow(), "Error", "Quantity Update Failed", ex.getMessage());
                            }
                        } else {
                            billingService.removeItemFromCart(item.getProduct().getId());
                            syncCartView();
                        }
                    });

                    plusBtn.setOnAction(e -> {
                        try {
                            billingService.updateCartItemQuantity(item.getProduct().getId(), item.getQuantity() + 1);
                            syncCartView();
                        } catch (Exception ex) {
                            AlertHelper.showWarning(tableCart.getScene().getWindow(), "Stock Limit", "Cannot Add More", ex.getMessage());
                        }
                    });

                    setGraphic(box);
                }
            }
        });

        // Remove item cell
        colCartItemRemove.setCellValueFactory(c -> new javafx.beans.property.ReadOnlyObjectWrapper<>(c.getValue()));
        colCartItemRemove.setCellFactory(param -> new TableCell<>() {
            private final Button remBtn = new Button("✕");
            {
                remBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #EF4444; -fx-font-weight: bold; -fx-cursor: hand;");
            }

            @Override
            protected void updateItem(CartItem item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    remBtn.setOnAction(e -> {
                        billingService.removeItemFromCart(item.getProduct().getId());
                        syncCartView();
                    });
                    setGraphic(remBtn);
                }
            }
        });
    }

    private void setupPaymentToggles() {
        paymentToggleGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            boxCashInput.setVisible(rbCash.isSelected());
            boxCashInput.setManaged(rbCash.isSelected());

            boxCardInput.setVisible(rbCard.isSelected());
            boxCardInput.setManaged(rbCard.isSelected());

            boxUpiInput.setVisible(rbUpi.isSelected());
            boxUpiInput.setManaged(rbUpi.isSelected());
        });
    }

    private void setupDiscountListener() {
        txtDiscountPct.textProperty().addListener((obs, oldVal, newVal) -> {
            try {
                if (newVal == null || newVal.trim().isEmpty()) {
                    billingService.applyDiscount(0.0);
                } else {
                    double disc = Double.parseDouble(newVal.trim());
                    if (disc < 0 || disc > 100) throw new NumberFormatException();
                    billingService.applyDiscount(disc);
                }
            } catch (NumberFormatException e) {
                billingService.applyDiscount(0.0);
            }
            updateTotals();
        });
    }

    private void setupTenderedListener() {
        txtCashTendered.textProperty().addListener((obs, oldVal, newVal) -> {
            updateChangeDue();
        });
    }

    private void setupSearchListener() {
        txtSearchProduct.textProperty().addListener((obs, oldVal, newVal) -> refreshCatalog());
    }

    private void buildCategoryChips() {
        boxCategoryChips.getChildren().clear();
        List<String> categories = new ArrayList<>();
        categories.add("All");
        inventoryService.getAllProducts().stream()
                .map(Product::getCategory)
                .distinct()
                .sorted()
                .forEach(categories::add);

        for (String cat : categories) {
            Button chip = new Button(cat);
            if (cat.equalsIgnoreCase(selectedCategory)) {
                chip.getStyleClass().add("filter-chip-selected");
            } else {
                chip.getStyleClass().add("filter-chip");
            }

            chip.setOnAction(e -> {
                selectedCategory = cat;
                buildCategoryChips();
                refreshCatalog();
            });

            boxCategoryChips.getChildren().add(chip);
        }
    }

    public void refreshCatalog() {
        if (inventoryService == null) return;

        String query = txtSearchProduct.getText() != null ? txtSearchProduct.getText().trim().toLowerCase() : "";
        List<Product> filtered = inventoryService.getAllProducts().stream()
                .filter(p -> selectedCategory.equalsIgnoreCase("All") || p.getCategory().equalsIgnoreCase(selectedCategory))
                .filter(p -> query.isEmpty() || p.getName().toLowerCase().contains(query) || p.getId().toLowerCase().contains(query))
                .collect(Collectors.toList());

        tableCatalog.setItems(FXCollections.observableArrayList(filtered));
    }

    private void addToCart(Product product, int quantity) {
        try {
            billingService.addItemToCart(product.getId(), quantity);
            syncCartView();
        } catch (Exception ex) {
            AlertHelper.showWarning(tableCatalog.getScene().getWindow(), "Cart Notice", "Could Not Add Item", ex.getMessage());
        }
    }

    private void syncCartView() {
        cartItemsObservable.setAll(billingService.getCurrentCart().getItems());
        lblCartItemCount.setText(billingService.getCurrentCart().getItems().size() + " item(s) in cart");
        updateTotals();
    }

    private void updateTotals() {
        double subtotal = billingService.getCurrentCart().getSubtotal();
        double discountAmt = billingService.getCurrentCart().getDiscountAmount();
        double taxable = billingService.getCurrentCart().getTaxableAmount();
        double taxAmt = billingService.calculateTax(taxable);
        double grandTotal = billingService.calculateFinalTotal();

        lblSubtotal.setText(CurrencyFormatter.format(subtotal));
        lblDiscountAmount.setText("-" + CurrencyFormatter.format(discountAmt));
        lblTaxAmount.setText(CurrencyFormatter.format(taxAmt));
        lblGrandTotal.setText(CurrencyFormatter.format(grandTotal));

        updateChangeDue();
    }

    private void updateChangeDue() {
        double finalTotal = billingService.calculateFinalTotal();
        String tenderStr = txtCashTendered.getText();
        if (tenderStr != null && !tenderStr.trim().isEmpty()) {
            try {
                double tendered = Double.parseDouble(tenderStr.trim());
                double change = tendered - finalTotal;
                if (change >= 0) {
                    lblChangeDue.setText(CurrencyFormatter.format(change));
                    lblChangeDue.setStyle("-fx-font-weight: bold; -fx-text-fill: #059669;");
                } else {
                    lblChangeDue.setText("Short: " + CurrencyFormatter.format(Math.abs(change)));
                    lblChangeDue.setStyle("-fx-font-weight: bold; -fx-text-fill: #EF4444;");
                }
            } catch (NumberFormatException ignored) {
                lblChangeDue.setText("₹0.00");
            }
        } else {
            lblChangeDue.setText("₹0.00");
        }
    }

    @FXML
    public void handleClearSearch() {
        txtSearchProduct.clear();
        selectedCategory = "All";
        buildCategoryChips();
        refreshCatalog();
    }

    @FXML
    public void handleClearCart() {
        if (!billingService.getCurrentCart().isEmpty()) {
            billingService.clearCart();
            syncCartView();
        }
    }

    @FXML
    public void handleCheckout() {
        if (billingService.getCurrentCart().isEmpty()) {
            AlertHelper.showWarning(tableCart.getScene().getWindow(), "Cart Empty", "No Items in Cart", "Please add at least one product to the cart before checking out.");
            return;
        }

        String customer = txtCustomerName.getText() != null && !txtCustomerName.getText().trim().isEmpty()
                ? txtCustomerName.getText().trim() : "Walk-in Customer";
        String phone = txtCustomerPhone.getText() != null && !txtCustomerPhone.getText().trim().isEmpty()
                ? txtCustomerPhone.getText().trim() : "9999999999";

        PaymentMethod paymentMethod;
        String paymentRef = "";

        if (rbCash.isSelected()) {
            paymentMethod = new CashPayment();
            String tenderStr = txtCashTendered.getText();
            if (tenderStr == null || tenderStr.trim().isEmpty()) {
                AlertHelper.showWarning(tableCart.getScene().getWindow(), "Cash Payment", "Tender Amount Missing", "Please enter cash tendered by customer.");
                return;
            }
            paymentRef = tenderStr.trim();
        } else if (rbCard.isSelected()) {
            paymentMethod = new CardPayment();
            String cardNum = txtCardNumber.getText();
            if (cardNum == null || cardNum.trim().isEmpty()) {
                AlertHelper.showWarning(tableCart.getScene().getWindow(), "Card Payment", "Card Number Missing", "Please enter 16-digit card number.");
                return;
            }
            paymentRef = cardNum.trim();
        } else {
            paymentMethod = new UpiPayment();
            String upi = txtUpiId.getText();
            if (upi == null || upi.trim().isEmpty()) {
                AlertHelper.showWarning(tableCart.getScene().getWindow(), "UPI Payment", "UPI ID Missing", "Please enter customer UPI Virtual Payment Address.");
                return;
            }
            paymentRef = upi.trim();
        }

        try {
            Invoice invoice = billingService.checkout(customer, phone, paymentMethod, paymentRef);

            // Notify main controller to refresh badges and other views
            mainController.onDataChanged();
            syncCartView();
            txtCashTendered.clear();
            txtCardNumber.clear();
            txtUpiId.clear();

            // Display printable receipt modal
            ReceiptDialog.show(tableCart.getScene().getWindow(), invoice);

        } catch (Exception ex) {
            AlertHelper.showError(tableCart.getScene().getWindow(), "Checkout Failed", "Transaction Incomplete", ex.getMessage());
        }
    }
}
