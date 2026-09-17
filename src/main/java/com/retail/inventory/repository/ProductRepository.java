package com.retail.inventory.repository;

import com.retail.inventory.model.Product;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe repository for Product persistence and querying.
 * Persists data to a CSV file to ensure data survives across application restarts.
 */
public class ProductRepository {
    private final Path filePath;
    private final Map<String, Product> productMap;
    private final ReentrantReadWriteLock lock;

    public ProductRepository(String dataDirectory) {
        this.filePath = Paths.get(dataDirectory, "products.csv");
        this.productMap = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        initStorage();
    }

    private void initStorage() {
        lock.writeLock().lock();
        try {
            if (Files.exists(filePath)) {
                loadFromCsv();
            } else {
                seedInitialData();
                saveToCsv();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void seedInitialData() {
        List<Product> defaultProducts = Arrays.asList(
                new Product("PRD-101", "Basmati Rice 5kg", "Groceries", 450.00, 25, 5),
                new Product("PRD-102", "Whole Wheat Flour 5kg", "Groceries", 280.00, 30, 8),
                new Product("PRD-103", "Sunflower Oil 1L", "Groceries", 145.00, 18, 5),
                new Product("PRD-104", "Full Cream Milk 1L", "Dairy", 68.00, 12, 6),
                new Product("PRD-105", "Farm Fresh Butter 200g", "Dairy", 115.00, 4, 5), // Already low stock for alert testing
                new Product("PRD-106", "Cheddar Cheese 200g", "Dairy", 160.00, 15, 4),
                new Product("PRD-107", "Organic Green Tea 25s", "Beverages", 199.00, 22, 5),
                new Product("PRD-108", "Instant Arabica Coffee 100g", "Beverages", 320.00, 3, 5), // Low stock
                new Product("PRD-109", "Sparkling Lemon Soda 600ml", "Beverages", 45.00, 40, 10),
                new Product("PRD-110", "Dark Chocolate Cookie 150g", "Snacks", 75.00, 35, 10),
                new Product("PRD-111", "Salted Roasted Almonds 200g", "Snacks", 290.00, 8, 5),
                new Product("PRD-112", "Herbal Aloe Shampoo 300ml", "Personal Care", 225.00, 14, 5),
                new Product("PRD-113", "Antibacterial Hand Soap 250ml", "Personal Care", 85.00, 20, 6),
                new Product("PRD-114", "Sparkle Dental Paste 150g", "Personal Care", 95.00, 2, 5) // Low stock
        );

        for (Product p : defaultProducts) {
            productMap.put(p.getId().toUpperCase(), p);
        }
    }

    private void loadFromCsv() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                try {
                    Product p = Product.fromCsvLine(line);
                    productMap.put(p.getId().toUpperCase(), p);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed product record: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading product CSV: " + e.getMessage());
        }
    }

    public void saveToCsv() {
        lock.writeLock().lock();
        try {
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            List<String> lines = new ArrayList<>();
            lines.add("# ID,Name,Category,Price,StockQuantity,LowStockThreshold");
            for (Product p : productMap.values()) {
                lines.add(p.toCsvLine());
            }
            Files.write(filePath, lines, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            System.err.println("Error saving product CSV: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public Optional<Product> findById(String id) {
        if (id == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return Optional.ofNullable(productMap.get(id.trim().toUpperCase()));
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Product> findAll() {
        lock.readLock().lock();
        try {
            return productMap.values().stream()
                    .sorted(Comparator.comparing(Product::getId))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Product> findByCategory(String category) {
        if (category == null) return Collections.emptyList();
        lock.readLock().lock();
        try {
            return productMap.values().stream()
                    .filter(p -> p.getCategory().equalsIgnoreCase(category.trim()))
                    .sorted(Comparator.comparing(Product::getName))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public List<Product> findLowStockProducts() {
        lock.readLock().lock();
        try {
            return productMap.values().stream()
                    .filter(Product::isLowStock)
                    .sorted(Comparator.comparingInt(Product::getStockQuantity))
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }

    public void save(Product product) {
        if (product == null) return;
        lock.writeLock().lock();
        try {
            productMap.put(product.getId().toUpperCase(), product);
            saveToCsv();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean delete(String id) {
        if (id == null) return false;
        lock.writeLock().lock();
        try {
            Product removed = productMap.remove(id.trim().toUpperCase());
            if (removed != null) {
                saveToCsv();
                return true;
            }
            return false;
        } finally {
            lock.writeLock().unlock();
        }
    }

    public boolean existsById(String id) {
        if (id == null) return false;
        return productMap.containsKey(id.trim().toUpperCase());
    }
}
