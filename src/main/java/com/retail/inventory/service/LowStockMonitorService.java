package com.retail.inventory.service;

import com.retail.inventory.model.Product;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.*;

/**
 * Demonstrates Multithreading in Java.
 * Runs as a background daemon thread periodically inspecting inventory stock levels,
 * detecting low-stock breaches in real-time, and recording safety reorder alerts.
 */
public class LowStockMonitorService {
    public interface StockAlertListener {
        void onLowStockDetected(Product product);
    }

    private final InventoryService inventoryService;
    private final ScheduledExecutorService scheduler;
    private final List<String> alertLogs;
    private final Set<String> notifiedProductIds;
    private final List<StockAlertListener> listeners;
    private volatile boolean running;
    private final long intervalSeconds;

    public LowStockMonitorService(InventoryService inventoryService, long intervalSeconds) {
        this.inventoryService = Objects.requireNonNull(inventoryService, "InventoryService cannot be null.");
        this.intervalSeconds = Math.max(2, intervalSeconds);
        this.alertLogs = new CopyOnWriteArrayList<>();
        this.notifiedProductIds = ConcurrentHashMap.newKeySet();
        this.listeners = new CopyOnWriteArrayList<>();
        this.running = false;

        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "LowStock-Daemon-Thread");
            t.setDaemon(true); // Daemon thread so JVM can terminate cleanly
            return t;
        });
    }

    public synchronized void start() {
        if (running) return;
        running = true;
        scheduler.scheduleWithFixedDelay(this::checkInventoryLevels, 1, intervalSeconds, TimeUnit.SECONDS);
    }

    public synchronized void stop() {
        if (!running) return;
        running = false;
        scheduler.shutdownNow();
    }

    public void addListener(StockAlertListener listener) {
        if (listener != null) {
            listeners.add(listener);
        }
    }

    /**
     * Periodic task executed by the background thread.
     */
    public void checkInventoryLevels() {
        try {
            List<Product> lowStockItems = inventoryService.getLowStockProducts();
            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("HH:mm:ss");
            String timeStr = LocalDateTime.now().format(dtf);

            // Clean up cleared items
            Set<String> currentLowIds = new HashSet<>();
            for (Product p : lowStockItems) {
                currentLowIds.add(p.getId());
            }
            notifiedProductIds.removeIf(id -> !currentLowIds.contains(id));

            for (Product p : lowStockItems) {
                if (!notifiedProductIds.contains(p.getId())) {
                    notifiedProductIds.add(p.getId());
                    String alert = String.format("[%s] [ALERT] Low Stock Warning: '%s' (%s) only has %d unit(s) left! (Threshold: %d)",
                            timeStr, p.getName(), p.getId(), p.getStockQuantity(), p.getLowStockThreshold());
                    alertLogs.add(alert);

                    for (StockAlertListener listener : listeners) {
                        try {
                            listener.onLowStockDetected(p);
                        } catch (Exception ex) {
                            // Suppress listener error
                        }
                    }
                }
            }
        } catch (Exception e) {
            alertLogs.add("[ERROR] Background monitor encountered error: " + e.getMessage());
        }
    }

    public List<String> getAlertLogs() {
        return new ArrayList<>(alertLogs);
    }

    public int getActiveLowStockCount() {
        return inventoryService.getLowStockProducts().size();
    }

    public boolean isRunning() {
        return running;
    }
}
