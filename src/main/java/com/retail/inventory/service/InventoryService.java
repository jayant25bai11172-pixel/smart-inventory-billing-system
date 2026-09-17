package com.retail.inventory.service;

import com.retail.inventory.exception.InsufficientStockException;
import com.retail.inventory.exception.InvalidInputException;
import com.retail.inventory.exception.ProductNotFoundException;
import com.retail.inventory.model.Product;
import com.retail.inventory.repository.ProductRepository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Service orchestrating all inventory catalog operations.
 */
public class InventoryService {
    private final ProductRepository productRepository;

    public InventoryService(ProductRepository productRepository) {
        this.productRepository = Objects.requireNonNull(productRepository, "ProductRepository cannot be null.");
    }

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Product getProductById(String id) throws ProductNotFoundException {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException(id));
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findLowStockProducts();
    }

    public synchronized Product addProduct(String id, String name, String category, double price, int initialStock, int threshold)
            throws InvalidInputException {
        if (id == null || id.trim().isEmpty()) {
            throw new InvalidInputException("id", "Product ID cannot be blank.");
        }
        String cleanId = id.trim().toUpperCase();
        if (productRepository.existsById(cleanId)) {
            throw new InvalidInputException("id", "A product with ID '" + cleanId + "' already exists.");
        }
        if (name == null || name.trim().isEmpty()) {
            throw new InvalidInputException("name", "Product name cannot be blank.");
        }
        if (price <= 0) {
            throw new InvalidInputException("price", "Product price must be greater than zero.");
        }
        if (initialStock < 0) {
            throw new InvalidInputException("initialStock", "Initial stock cannot be negative.");
        }
        if (threshold < 0) {
            throw new InvalidInputException("threshold", "Low-stock threshold cannot be negative.");
        }

        Product product = new Product(cleanId, name.trim(), category, price, initialStock, threshold);
        productRepository.save(product);
        return product;
    }

    public synchronized void updateProductPrice(String id, double newPrice)
            throws ProductNotFoundException, InvalidInputException {
        if (newPrice <= 0) {
            throw new InvalidInputException("price", "New price must be greater than zero.");
        }
        Product product = getProductById(id);
        product.setPrice(newPrice);
        productRepository.save(product);
    }

    public synchronized void updateProductThreshold(String id, int newThreshold)
            throws ProductNotFoundException, InvalidInputException {
        if (newThreshold < 0) {
            throw new InvalidInputException("threshold", "Low-stock threshold cannot be negative.");
        }
        Product product = getProductById(id);
        product.setLowStockThreshold(newThreshold);
        productRepository.save(product);
    }

    public synchronized void replenishStock(String id, int quantityToAdd)
            throws ProductNotFoundException, InvalidInputException {
        if (quantityToAdd <= 0) {
            throw new InvalidInputException("quantity", "Replenishment quantity must be greater than zero.");
        }
        Product product = getProductById(id);
        product.addStock(quantityToAdd);
        productRepository.save(product);
    }

    public synchronized void deductStock(String id, int quantityToDeduct)
            throws ProductNotFoundException, InsufficientStockException {
        Product product = getProductById(id);
        synchronized (product) {
            if (product.getStockQuantity() < quantityToDeduct) {
                throw new InsufficientStockException(product.getId(), product.getName(),
                        product.getStockQuantity(), quantityToDeduct);
            }
            product.reduceStock(quantityToDeduct);
        }
        productRepository.save(product);
    }

    public synchronized boolean deleteProduct(String id) throws ProductNotFoundException {
        if (!productRepository.existsById(id)) {
            throw new ProductNotFoundException(id);
        }
        return productRepository.delete(id);
    }
}
