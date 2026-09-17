package com.retail.inventory.repository;

import com.retail.inventory.model.Invoice;

import java.io.IOException;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * Thread-safe repository for persisting and querying historical customer Invoices.
 */
public class InvoiceRepository {
    private final Path filePath;
    private final List<Invoice> invoiceList;
    private final ReentrantReadWriteLock lock;

    public InvoiceRepository(String dataDirectory) {
        this.filePath = Paths.get(dataDirectory, "invoices.csv");
        this.invoiceList = new CopyOnWriteArrayList<>();
        this.lock = new ReentrantReadWriteLock();
        initStorage();
    }

    private void initStorage() {
        lock.writeLock().lock();
        try {
            if (Files.exists(filePath)) {
                loadFromCsv();
            } else {
                if (filePath.getParent() != null) {
                    Files.createDirectories(filePath.getParent());
                }
                Files.write(filePath,
                        Collections.singletonList("# InvoiceID,DateTime,CustomerName,CustomerPhone,TotalQty,Subtotal,Discount,Tax,FinalTotal,PaymentDetails,Items"),
                        StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            }
        } catch (IOException e) {
            System.err.println("Error initializing invoice storage: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    private void loadFromCsv() {
        try {
            List<String> lines = Files.readAllLines(filePath);
            for (String line : lines) {
                if (line.trim().isEmpty() || line.startsWith("#")) continue;
                try {
                    Invoice inv = Invoice.fromCsvLine(line);
                    invoiceList.add(inv);
                } catch (Exception e) {
                    System.err.println("Warning: Skipping malformed invoice record: " + line);
                }
            }
        } catch (IOException e) {
            System.err.println("Error reading invoice CSV: " + e.getMessage());
        }
    }

    public void save(Invoice invoice) {
        if (invoice == null) return;
        lock.writeLock().lock();
        try {
            invoiceList.add(invoice);
            if (filePath.getParent() != null) {
                Files.createDirectories(filePath.getParent());
            }
            Files.write(filePath,
                    Collections.singletonList(invoice.toCsvLine()),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Error appending invoice to CSV: " + e.getMessage());
        } finally {
            lock.writeLock().unlock();
        }
    }

    public List<Invoice> findAll() {
        lock.readLock().lock();
        try {
            return new ArrayList<>(invoiceList);
        } finally {
            lock.readLock().unlock();
        }
    }

    public Optional<Invoice> findById(String invoiceId) {
        if (invoiceId == null) return Optional.empty();
        lock.readLock().lock();
        try {
            return invoiceList.stream()
                    .filter(inv -> inv.getInvoiceId().equalsIgnoreCase(invoiceId.trim()))
                    .findFirst();
        } finally {
            lock.readLock().unlock();
        }
    }

    public int getNextInvoiceSequence() {
        return invoiceList.size() + 1;
    }
}
