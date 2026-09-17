# Smart Inventory & Billing Manager for Retail

![Java](https://img.shields.io/badge/Language-Java%2021%20LTS-orange.svg)
![Build](https://img.shields.io/badge/Build-Passing-brightgreen.svg)
![Tests](https://img.shields.io/badge/Tests-9%2F9%20Passed%20(100%25)-success.svg)
![License](https://img.shields.io/badge/Academic-VITyarthi%20Project-blue.svg)

An enterprise-grade, standalone Java retail management system designed for small to medium retail businesses. It provides real-time stock tracking, atomic Point-of-Sale (POS) customer billing with multi-channel payment options, automated low-stock alerting powered by background multithreading, and business intelligence reporting leveraging Java 8+ Streams.

---

## Table of Contents
1. [Overview](#overview)
2. [Key Features](#key-features)
3. [Core Java Concepts Highlighted](#core-java-concepts-highlighted)
4. [Architecture & Project Structure](#architecture--project-structure)
5. [Prerequisites & Installation](#prerequisites--installation)
6. [Compilation & Execution](#compilation--execution)
7. [Testing Instructions](#testing-instructions)
8. [Sample CLI Outputs & Demo Walkthrough](#sample-cli-outputs--demo-walkthrough)
9. [Author & Academic Details](#author--academic-details)

---

## 1. Overview
Managing a retail storefront requires synchronization between store shelves, customer billing registers, cash/digital accounts, and inventory supplier orders. Traditional paper registers or disconnected spreadsheet trackers lead to stock discrepancies, cashier overbilling, and stockouts.

The **Smart Inventory & Billing Manager** resolves these operational challenges by offering an all-in-one console application structured across three primary functional modules:
1. **Product Catalog & Inventory Tracking**: Centralized catalog maintaining pricing, inventory counts, safety alert thresholds, and category classifications.
2. **Point of Sale (POS) & Billing**: Dynamic cart management supporting discounts, GST tax computation, stock verification, and polymorphic payments (Cash, Card, UPI).
3. **Sales Analytics & Reporting**: Real-time business reporting including gross revenue, top-selling items, category share, and low-stock reorder notices.

---

## 2. Key Features

- **Real-Time Stock Depletion & Restoration**: Stock adjustments occur atomically upon checkout or supplier replenishment with thread-safe synchronization.
- **Over-Purchase Prevention**: Real-time stock reservation prevents cashiers from selling items beyond available inventory via custom checked exceptions.
- **Multi-Method Payment Settlement**:
  - **Cash**: Computes cash tendered, change due, and validates tender sufficiency.
  - **Card**: Validates 16-digit card numbers, masks numbers (`****-****-****-1234`), and generates approval authorization codes.
  - **UPI**: Validates Virtual Payment Addresses (e.g. `retail@okhdfcbank`) and logs unique digital transaction references.
- **Background Multithreaded Low-Stock Monitor**: An autonomous daemon thread runs periodic stock health checks every few seconds and triggers non-blocking safety alerts.
- **Rich Business Analytics**: Powered by Java Stream and Collectors API for calculating Average Order Value (AOV), best-sellers, and category sales shares.
- **Zero-Dependency Persistence**: Data is safely stored in CSV flat files (`data/products.csv` and `data/invoices.csv`) with automatic directory initialization.

---

## 3. Core Java Concepts Highlighted

| Concept | Implementation Details |
| :--- | :--- |
| **Interfaces & Polymorphism** | `PaymentMethod` interface with concrete implementations: `CashPayment`, `CardPayment`, and `UpiPayment`. The checkout engine delegates payment execution polymorphically. |
| **Custom Exception Handling** | Hierarchical checked exception structure rooted at `SmartInventoryException`: `InsufficientStockException`, `ProductNotFoundException`, `InvalidInputException`, and `PaymentFailedException`. |
| **Multithreading & Concurrency** | `LowStockMonitorService` utilizes a `ScheduledExecutorService` with daemon worker threads (`Thread.setDaemon(true)`), running scheduled background tasks and synchronizing against `Product` stock monitors. |
| **Collections Framework** | `Map<String, Product>`, `CopyOnWriteArrayList`, `ConcurrentHashMap`, `LinkedHashMap`, `Set<String>`, and `List<CartItem>`. |
| **Java 8+ Streams & Lambdas** | Used in `AnalyticsService` for aggregating total revenue, sorting top products by units/revenue, and computing category breakdowns with `Collectors.groupingBy()`. |
| **Thread Synchronization & Locks** | `ReentrantReadWriteLock` in repositories ensuring thread-safe reads and atomic writes during concurrent operations. |
| **File I/O & Serialization** | Robust CSV parsing and serialization preserving business state across restarts. |

---

## 4. Architecture & Project Structure

```
smart-inventory-billing-manager/
├── bin/                                       # Compiled bytecode (.class files)
├── data/                                      # Persistent CSV data storage
│   ├── products.csv                           # Product catalog and stock data
│   └── invoices.csv                           # Historical customer billing audit trail
├── src/
│   ├── main/java/com/retail/inventory/
│   │   ├── Main.java                          # Application entry point & thread lifecycle
│   │   ├── model/
│   │   │   ├── Product.java                   # Product entity (ID, name, price, stock, threshold)
│   │   │   ├── CartItem.java                  # Cart line item snapshot
│   │   │   ├── Cart.java                      # Cart operations, subtotal, and discount logic
│   │   │   ├── Invoice.java                   # Complete billing record & CSV serializer
│   │   │   └── PaymentDetails.java            # Transaction payment metadata
│   │   ├── payment/
│   │   │   ├── PaymentMethod.java             # Interface for payment processing
│   │   │   ├── CashPayment.java               # Cash tender and change calculation
│   │   │   ├── CardPayment.java               # Card validation and masking
│   │   │   └── UpiPayment.java                # UPI VPA validation and transaction generation
│   │   ├── exception/
│   │   │   ├── SmartInventoryException.java   # Root checked exception
│   │   │   ├── InsufficientStockException.java# Triggered on overselling
│   │   │   ├── ProductNotFoundException.java  # Triggered on missing SKU
│   │   │   ├── InvalidInputException.java     # User validation errors
│   │   │   └── PaymentFailedException.java    # Payment failure errors
│   │   ├── repository/
│   │   │   ├── ProductRepository.java         # Thread-safe CSV catalog persistence
│   │   │   └── InvoiceRepository.java         # Append-only CSV invoice persistence
│   │   ├── service/
│   │   │   ├── InventoryService.java          # Catalog operations and stock adjustments
│   │   │   ├── BillingService.java            # POS checkout and tax computation
│   │   │   ├── AnalyticsService.java          # Stream analytics & BI calculations
│   │   │   └── LowStockMonitorService.java    # Background multithreaded daemon monitor
│   │   ├── util/
│   │   │   ├── ConsoleUtils.java              # ANSI colors, formatted banners, and input validators
│   │   │   └── TableFormatter.java            # Structured ASCII table renderer
│   │   └── ui/
│   │       └── ConsoleUI.java                 # Interactive text-based menu interface
│   └── test/java/com/retail/inventory/
│       └── SmartInventoryTestSuite.java       # Standalone automated unit & integration test runner
├── build.bat                                  # Windows compilation script
├── run.bat                                    # Windows application launch script
├── test.bat                                   # Windows automated test runner script
├── statement.md                               # Project scope, problem statement, and features
├── PROJECT_REPORT.md                          # Comprehensive academic report (15 sections + diagrams)
└── README.md                                  # Repository documentation
```

---

## 5. Prerequisites & Installation

### Requirements
- **Operating System**: Microsoft Windows 10 / Windows 11 (64-bit).
- **Java Development Kit (JDK)**: JDK 17 or JDK 21+ (configured in `PATH` or `JAVA_HOME`).

### Setup
Navigate to the project directory in Command Prompt or PowerShell:
```cmd
cd C:\Users\jayan\.gemini\antigravity\scratch\smart-inventory-billing-manager
```

---

## 6. Compilation & Execution (Windows)

### Compile the Codebase
Run the Windows batch build script:
```cmd
.\build.bat
```

### Run the Interactive Application
Launch the retail management system:
```cmd
.\run.bat
```

---

## 7. Testing Instructions

The project comes with a built-in automated test suite covering all functional modules and technical concepts:
- Product Model, Encapsulation & Stock adjustments
- Cart Calculations, Discounts & Subtotals
- `InsufficientStockException` edge cases
- `CashPayment`, `CardPayment`, and `UpiPayment` validations
- Atomic Billing Checkout and Inventory Stock Deductions
- Sales Analytics & Stream Aggregations
- Multithreaded Low-Stock Background Daemon Alert verification

To execute the test suite:
```cmd
test.bat
```
*(Or manually: `java -cp bin com.retail.inventory.SmartInventoryTestSuite`)*

### Test Results Output
```
================================================================================
   SMART INVENTORY & BILLING MANAGER - AUTOMATED TEST SUITE EXECUTION
================================================================================
[RUNNING] testProductModelAndStockLogic                 ... PASSED [OK]
[RUNNING] testCartCalculationsAndDiscounts              ... PASSED [OK]
[RUNNING] testInsufficientStockExceptionHandling        ... PASSED [OK]
[RUNNING] testCashPaymentMethod                         ... PASSED [OK]
[RUNNING] testCardPaymentMethod                         ... PASSED [OK]
[RUNNING] testUpiPaymentMethod                          ... PASSED [OK]
[RUNNING] testAtomicBillingCheckoutAndPersistence       ... PASSED [OK]
[RUNNING] testSalesAnalyticsAggregations                ... PASSED [OK]
[RUNNING] testMultithreadedLowStockMonitor              ... PASSED [OK]

--------------------------------------------------------------------------------
Test Execution Summary: 9 PASSED, 0 FAILED (Total: 9)
--------------------------------------------------------------------------------
ALL UNIT AND INTEGRATION TESTS PASSED SUCCESSFULLY! (100% PASS RATE)
```

---

## 8. Sample CLI Outputs & Demo Walkthrough

### 1. Main Navigation Menu
```
================================================================================
  SMART INVENTORY & BILLING MANAGER FOR RETAIL
================================================================================
  [!] NOTICE: 3 product(s) are currently at or below low-stock threshold! Check Module 4.
  1. Product Catalog & Inventory Tracking (Module 1)
  2. Point of Sale & Billing / Cart (Module 2)
  3. Sales Analytics & Business Reports (Module 3)
  4. Live Stock Health & Background Daemon Alerts
  5. Exit System

Enter your selection [1-5]:
```

### 2. Formatted Product Catalog Table
```
+---------+------------------------------+---------------+------------+-------+-----------+-----------+
| ID      | Product Name                 | Category      | Price      | Stock | Threshold | Status    |
+---------+------------------------------+---------------+------------+-------+-----------+-----------+
| PRD-101 | Basmati Rice 5kg             | Groceries     | Rs. 450.00 | 25    | 5         | OPTIMAL   |
| PRD-105 | Farm Fresh Butter 200g       | Dairy         | Rs. 115.00 | 4     | 5         | LOW STOCK |
| PRD-108 | Instant Arabica Coffee 100g  | Beverages     | Rs. 320.00 | 3     | 5         | LOW STOCK |
| PRD-110 | Dark Chocolate Cookie 150g   | Snacks        | Rs. 75.00  | 35    | 10        | OPTIMAL   |
+---------+------------------------------+---------------+------------+-------+-----------+-----------+
```

### 3. Generated Tax Invoice & Receipt
```
========================================================
               RETAIL STORE TAX INVOICE                 
========================================================
 Invoice ID : INV-20260917-0001
 Date & Time: 2026-09-17 17:15:20
 Customer   : Rahul Sharma | Phone: 9876543210
--------------------------------------------------------
+------------------------------+-----+------------+------------+
| Item                         | Qty | Rate       | Amount     |
+------------------------------+-----+------------+------------+
| Basmati Rice 5kg             | 1   | Rs. 450.00 | Rs. 450.00 |
| Instant Arabica Coffee 100g  | 2   | Rs. 320.00 | Rs. 640.00 |
+------------------------------+-----+------------+------------+
 Gross Subtotal :                   Rs. 1090.00
 Discount (10.0%): -                 Rs. 109.00
 GST Tax (5.0%)  : +                  Rs. 49.05
--------------------------------------------------------
 NET TOTAL PAID :                   Rs. 1030.05
 Payment Status : Mode: UPI | Paid: Rs.1030.05 | Ref: VPA: rahul@okhdfcbank / Txn: UPI-849204A91802 | Status: SUCCESS
========================================================
```

---

## 9. Author & Academic Details
- **Course**: Programming in Java
- **Evaluation Type**: Flipped Course Project Evaluation (VITyarthi)
- **Project Title**: Smart Inventory & Billing Manager for Retail
