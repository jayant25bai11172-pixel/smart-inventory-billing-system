# Smart Retail - Inventory & Billing Manager

![Release](https://img.shields.io/badge/Release-v1.0.0-blue.svg)
![Language](https://img.shields.io/badge/Language-Java%2021%20LTS-orange.svg)
![Interface](https://img.shields.io/badge/Interface-JavaFX%20Desktop%20GUI-brightgreen.svg)
![Theme](https://img.shields.io/badge/Design-All--White%20Modern%20Light-blue.svg)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)
![Tests](https://img.shields.io/badge/Tests-9%2F9%20Passed%20(100%25)-success.svg)

An enterprise-grade, standalone retail management and Point-of-Sale (POS) application built in Java 21 LTS. Featuring an all-white modern desktop GUI, real-time stock tracking, atomic customer checkout with multi-channel payment settlements (Cash, Card, UPI), automated low-stock alerting powered by background multithreading, and business intelligence reporting leveraging Java 8+ Streams.

---

## 1. Overview
Managing a retail storefront requires real-time synchronization between store inventory, customer checkout counters, payment logs, and supplier replenishment notices. Traditional spreadsheets or paper ledgers introduce stock discrepancies, delays, and out-of-stock scenarios.

**Smart Retail** delivers an all-in-one software suite featuring both a **Modern JavaFX Desktop Interface** and an interactive **Console Terminal Mode**:
1. **Executive Dashboard**: Live store health KPIs, daily sales metrics, and urgent low-stock alerts.
2. **Point of Sale (POS) & Billing**: Searchable catalog, dynamic cart management with quantity steppers, GST (5%) & discount computation, and multi-channel payments (Cash, Card, UPI) with printable thermal receipts.
3. **Product Catalog & Inventory Management**: Centralized inventory tracking with stock status badges (In Stock, Low Stock, Out of Stock), category filters, restock modals, price adjustments, and warehouse asset valuation.
4. **Sales Analytics & Business Intelligence**: Interactive JavaFX bar charts of best-sellers, category revenue share pie charts, and complete transaction audit ledger.

---

## 2. Key Features

- **All-White Clean Design System**: Modern, high-contrast light theme with clean typography, rounded cards, subtle shadows, and responsive layout containers.
- **Point of Sale (POS) Counter**:
  - Instant product search by ID or name with category filter chips.
  - Interactive cart table with inline quantity adjustment (`+` / `-`) and item removal.
  - Automatic computation of subtotal, customizable discount %, GST tax, and grand total.
  - Printable thermal tax receipt popup with a 1-click clipboard copy feature.
- **Polymorphic Payment Processing**:
  - **Cash**: Real-time cash tendered input and change-due calculator.
  - **Card**: 16-digit card number validation, number masking (`****-****-****-1234`), and authorization code generation.
  - **UPI**: Virtual Payment Address (VPA) validation with unique transaction reference IDs.
- **Background Multithreaded Low-Stock Monitor**:
  - Autonomous daemon thread (`LowStockMonitorService`) continuously scanning inventory levels every 5 seconds.
  - Non-blocking event dispatching via `Platform.runLater()` updating sidebar notification badges and alert banners in real time.
- **Warehouse Asset Valuation & Restocking**:
  - Filterable by text query, Category, and Stock Status.
  - 1-click modal replenishment to easily add stock for low-inventory items.
  - Live total valuation (`Sum(Price * Stock)`) for inventory audits.
- **Business Intelligence & Stream Analytics**:
  - Java 8+ Streams and Collectors aggregating gross revenue, average order value (AOV), top products, and category breakdowns.
- **Zero-Dependency Flat-File Persistence**:
  - Catalog and transaction history are automatically persisted to CSV flat files (`data/products.csv` and `data/invoices.csv`).

---

## 3. Core Java Architecture & Concepts Highlighted

| Concept | Implementation Details |
| :--- | :--- |
| **JavaFX Desktop GUI & MVC** | Decoupled architecture using FXML view templates, CSS styling, and controllers (`MainController`, `DashboardController`, `PosController`, `InventoryController`, `AnalyticsController`). |
| **Interfaces & Polymorphism** | `PaymentMethod` interface with concrete implementations: `CashPayment`, `CardPayment`, and `UpiPayment`. The checkout service processes payments polymorphically. |
| **Custom Exception Handling** | Hierarchical checked exception architecture rooted at `SmartInventoryException`: `InsufficientStockException`, `ProductNotFoundException`, `InvalidInputException`, and `PaymentFailedException`. |
| **Multithreading & Daemon Threads** | `LowStockMonitorService` utilizes a `ScheduledExecutorService` with daemon threads (`Thread.setDaemon(true)`), running periodic checks and triggering listener callbacks on the JavaFX thread. |
| **Java 8+ Streams & Lambdas** | `AnalyticsService` computes total revenue, sorts top products by revenue/volume, and calculates category distributions using `Collectors.groupingBy()`. |
| **Thread Synchronization & Locks** | `ReentrantReadWriteLock` in repositories ensuring thread-safe reads and atomic writes during concurrent operations. |
| **File I/O & CSV Serialization** | Robust CSV parsing and serialization preserving business state across application restarts with zero external database dependencies. |

---

## 4. Project Structure

```
smart-inventory-billing-manager/
├── bin/                                       # Compiled bytecode (.class files)
├── data/                                      # Persistent CSV storage
│   ├── products.csv                           # Product catalog and stock data
│   └── invoices.csv                           # Customer invoices & transaction audit trail
├── lib/
│   └── javafx/                                # OpenJFX 21 runtime libraries
├── src/
│   ├── main/
│   │   ├── java/com/retail/inventory/
│   │   │   ├── Main.java                      # Console application entry point
│   │   │   ├── gui/
│   │   │   │   ├── GuiLauncher.java           # GUI application launcher
│   │   │   │   ├── InventoryApp.java          # JavaFX Application lifecycle
│   │   │   │   ├── controller/
│   │   │   │   │   ├── MainController.java        # Shell navigation & header coordinator
│   │   │   │   │   ├── DashboardController.java   # KPI cards & quick restock alerts
│   │   │   │   │   ├── PosController.java         # Point of Sale billing & cart engine
│   │   │   │   │   ├── InventoryController.java   # SKU management, filters, and modals
│   │   │   │   │   └── AnalyticsController.java   # BI charts & invoice audit ledger
│   │   │   │   ├── dialog/
│   │   │   │   │   ├── ProductDialog.java         # Add/Edit product modal
│   │   │   │   │   ├── ReceiptDialog.java         # Thermal receipt modal & copy utility
│   │   │   │   │   └── RestockDialog.java         # Stock replenishment dialog
│   │   │   │   └── util/
│   │   │   │       ├── AlertHelper.java           # Standardized dialog helper
│   │   │   │       └── CurrencyFormatter.java     # Indian Rupee (₹) number formatting
│   │   │   ├── model/
│   │   │   │   ├── Product.java               # Product entity (ID, name, price, stock, threshold)
│   │   │   │   ├── CartItem.java              # Cart line item snapshot
│   │   │   │   ├── Cart.java                  # Cart operations, subtotal, and discount logic
│   │   │   │   ├── Invoice.java               # Complete billing record & CSV serializer
│   │   │   │   └── PaymentDetails.java        # Transaction payment metadata
│   │   │   ├── payment/
│   │   │   │   ├── PaymentMethod.java         # Payment processor interface
│   │   │   │   ├── CashPayment.java           # Cash tender & change calculator
│   │   │   │   ├── CardPayment.java           # 16-digit card validation & masking
│   │   │   │   └── UpiPayment.java            # UPI VPA validation & transaction generation
│   │   │   ├── exception/
│   │   │   │   ├── SmartInventoryException.java
│   │   │   │   ├── InsufficientStockException.java
│   │   │   │   ├── ProductNotFoundException.java
│   │   │   │   ├── InvalidInputException.java
│   │   │   │   └── PaymentFailedException.java
│   │   │   ├── repository/
│   │   │   │   ├── ProductRepository.java     # Thread-safe product catalog persistence
│   │   │   │   └── InvoiceRepository.java     # Append-only invoice persistence
│   │   │   ├── service/
│   │   │   │   ├── InventoryService.java      # Catalog operations & stock adjustments
│   │   │   │   ├── BillingService.java        # POS checkout & tax computations
│   │   │   │   ├── AnalyticsService.java      # Stream analytics & BI calculations
│   │   │   │   └── LowStockMonitorService.java# Background multithreaded daemon monitor
│   │   │   ├── util/
│   │   │   │   ├── ConsoleUtils.java          # CLI formatting & input helpers
│   │   │   │   └── TableFormatter.java        # Structured ASCII table renderer
│   │   │   └── ui/
│   │   │       └── ConsoleUI.java             # Terminal menu interface
│   │   └── resources/com/retail/inventory/gui/
│   │       ├── css/
│   │       │   └── styles.css                 # All-white modern design system
│   │       └── views/
│   │           ├── main_layout.fxml           # Master shell layout & sidebar
│   │           ├── dashboard_view.fxml        # Dashboard view template
│   │           ├── pos_view.fxml              # POS Billing view template
│   │           ├── inventory_view.fxml        # Inventory catalog view template
│   │           └── analytics_view.fxml        # Analytics & charts view template
│   └── test/java/com/retail/inventory/
│       └── SmartInventoryTestSuite.java       # Standalone automated test suite (9 tests)
├── build.bat                                  # Windows compilation script (auto-fetches OpenJFX)
├── run.bat                                    # Interactive launcher (defaults to GUI)
├── run-gui.bat                                # Direct GUI launcher
├── run-console.bat                            # Direct CLI console launcher
├── test.bat                                   # Automated test suite runner
├── PROJECT_REPORT.md                          # Comprehensive academic report
└── README.md                                  # Repository documentation
```

---

## 5. Getting Started (Windows)

### Prerequisites
- **Operating System**: Windows 10 / Windows 11 (64-bit)
- **Java**: Java 17 LTS or Java 21 LTS installed and available on `PATH`

### 1. Compile the Project
Run the automated build script (will automatically download required OpenJFX 21 binaries if not present):
```cmd
build.bat
```

### 2. Launch the Application
- **Default Launcher (Interactive Menu)**:
  ```cmd
  run.bat
  ```
  *(Launches the Modern JavaFX Desktop GUI automatically after 3 seconds, or press `2` for Terminal Console Mode).*

- **Direct Modern Desktop GUI Launch**:
  ```cmd
  run-gui.bat
  ```

- **Direct Classic Console Launch**:
  ```cmd
  run-console.bat
  ```

### 3. Run Automated Tests
Execute the comprehensive 9-part unit and integration test suite:
```cmd
test.bat
```

---

## 6. Author Details
- **Developer**: Jayant Yadav
- **Register Number**: 25BAI11172
- **Institution**: Vellore Institute of Technology (VIT), Bhopal
- **Degree & Branch**: B.Tech (Computer Science & Engineering - AI & ML)
- **Course**: CSE1007 - Programming in Java
