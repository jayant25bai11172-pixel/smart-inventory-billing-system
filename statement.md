# Project Statement: Smart Inventory & Billing Manager for Retail

## 1. Problem Statement
Small and medium retail establishments (such as neighborhood grocery stores, mini-marts, and boutique shops) frequently face operational bottlenecks due to manual inventory registers, disjointed checkout calculators, and lack of real-time visibility into stock depletion. Crucial problems include:
- **Stockouts and Revenue Loss**: Fast-moving consumer goods often go out of stock without prior notification, causing lost sales and dissatisfied customers.
- **Inaccurate and Slow Billing**: Manual calculation of prices, discounts, and taxes is prone to human error, delays checkout queues, and complicates cash flow reconciliation.
- **Payment Inflexibility**: Modern consumers utilize multiple payment channels (Cash, Debit/Credit Cards, and UPI apps). Fragmented record-keeping complicates end-of-day auditing.
- **Lack of Actionable Sales Intelligence**: Small retailers rarely know their top-performing stock keeping units (SKUs) or average order value, hampering informed reordering.

The **Smart Inventory & Billing Manager** resolves these challenges by delivering an integrated, lightweight, real-time Java solution combining inventory tracking, point-of-sale billing with multiple payment options, background automated low-stock alerting, and analytical business intelligence.

---

## 2. Scope of the Project
The application serves as a standalone Point of Sale (POS) and inventory tracking platform designed for desktop/workstation environments in retail stores.

### In-Scope:
- **Product Catalog Management**: Full CRUD operations for catalog items with unique SKU/IDs, unit pricing, real-time stock balances, and customizable safety alert thresholds.
- **Dynamic Cart & Checkout Engine**: Live cart management with stock reservation checks, discount application, multi-tier GST/tax calculation, and atomic stock deductions upon payment.
- **Pluggable Multi-Channel Payment System**: Polymorphic support for Cash (with change calculation), Card (with card number validation and masking), and UPI (with Virtual Payment Address validation and transaction IDs).
- **Background Multithreaded Low-Stock Monitor**: An autonomous daemon thread running scheduled health checks on inventory levels and publishing real-time safety alerts.
- **Analytical Reporting**: Stream-based sales performance metrics including gross revenue, average order value, top 5 bestsellers by unit and revenue, and category-wise contributions.
- **Data Persistence**: File-based CSV persistence ensuring all inventory and invoice data persists across system restarts.

### Out-of-Scope (Future Enhancements):
- Direct cloud-synced database clusters (e.g., PostgreSQL/Oracle over WAN).
- Hardware-integrated thermal receipt printers or barcode laser guns (simulated via formatted terminal display and keyboard input).
- Multi-store distributed inventory synchronization.

---

## 3. Target Users
1. **Store Cashiers / Front-Desk Operators**:
   - Fast lookup of items, intuitive cart updates, quick calculation of discounts and taxes, and instantaneous receipt generation.
2. **Retail Store Managers & Inventory Controllers**:
   - Monitoring live stock levels, receiving automated reorder alerts, updating item prices, and replenishing depleted inventory.
3. **Small Business Owners & Sole Proprietors**:
   - Reviewing end-of-day sales revenue, inspecting bestseller reports, and analyzing category contributions for smarter purchasing decisions.

---

## 4. High-Level Features

| Feature ID | Feature Name | Description |
| :--- | :--- | :--- |
| **FEAT-01** | **Real-Time Catalog & Stock Tracking** | Add, edit, restock, and delete products with automatic category classification and safety threshold settings. |
| **FEAT-02** | **Point-of-Sale (POS) Cart Management** | Interactive shopping cart allowing item addition, quantity modification, and automatic subtotal and discount computations. |
| **FEAT-03** | **Insufficient Stock Protection** | Real-time stock verification preventing cashier overselling with custom checked exception handling. |
| **FEAT-04** | **Polymorphic Multi-Payment Processing** | Extensible payment strategy interface supporting Cash, Card, and UPI with validation and audit trail logging. |
| **FEAT-05** | **Tax Invoice Generation** | Automatic generation of structured, itemized tax invoices with unique sequential IDs and persistent logging. |
| **FEAT-06** | **Multithreaded Stock Alert Daemon** | Background daemon thread periodically scanning SKU stock levels and triggering live warnings when thresholds are breached. |
| **FEAT-07** | **Executive Sales Analytics** | Calculation of Gross Revenue, Average Order Value, Top 5 Bestselling Products, and Category Revenue shares using Java Streams. |
| **FEAT-08** | **Persistent CSV Data Storage** | Thread-safe CSV read/write repositories maintaining data durability across application lifecycles without external database dependencies. |
