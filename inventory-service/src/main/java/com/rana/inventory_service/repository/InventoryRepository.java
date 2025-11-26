package com.rana.inventory_service.repository;

import com.rana.inventory_service.model.Product;
import org.springframework.stereotype.Repository;
import jakarta.annotation.PostConstruct;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InventoryRepository {
    
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    
    @PostConstruct
    public void init() {
        // Pre-populate with sample products
        products.put("PROD-001", new Product("PROD-001", "Laptop", 50, 999.99));
        products.put("PROD-002", new Product("PROD-002", "Mouse", 200, 29.99));
        products.put("PROD-003", new Product("PROD-003", "Keyboard", 150, 79.99));
        products.put("PROD-004", new Product("PROD-004", "Monitor", 75, 299.99));
        products.put("PROD-005", new Product("PROD-005", "Headphones", 100, 149.99));
        products.put("PROD-006", new Product("PROD-006", "Webcam", 80, 89.99));
        products.put("PROD-007", new Product("PROD-007", "USB Cable", 500, 9.99));
        products.put("PROD-008", new Product("PROD-008", "Desk Lamp", 120, 39.99));
        
        System.out.println("Initialized inventory with " + products.size() + " products");
    }
    
    public Optional<Product> findByProductId(String productId) {
        return Optional.ofNullable(products.get(productId));
    }
    
    public synchronized boolean reserveStock(String productId, int quantity) {
        Product product = products.get(productId);
        if (product == null) {
            return false;
        }
        
        if (product.getAvailableStock() >= quantity) {
            product.setAvailableStock(product.getAvailableStock() - quantity);
            product.setReservedStock(product.getReservedStock() + quantity);
            return true;
        }
        return false;
    }
    
    public synchronized boolean releaseStock(String productId, int quantity) {
        Product product = products.get(productId);
        if (product == null) {
            return false;
        }
        
        if (product.getReservedStock() >= quantity) {
            product.setReservedStock(product.getReservedStock() - quantity);
            product.setAvailableStock(product.getAvailableStock() + quantity);
            return true;
        }
        return false;
    }
    
    public synchronized boolean confirmReservation(String productId, int quantity) {
        Product product = products.get(productId);
        if (product == null) {
            return false;
        }
        
        if (product.getReservedStock() >= quantity) {
            product.setReservedStock(product.getReservedStock() - quantity);
            return true;
        }
        return false;
    }
    
    public Map<String, Product> getAllProducts() {
        return new ConcurrentHashMap<>(products);
    }
}
