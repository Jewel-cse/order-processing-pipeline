package com.rana.inventory_service.service;

import com.rana.event_contracts.*;
import com.rana.inventory_service.model.InventoryReservation;
import com.rana.inventory_service.model.Product;
import com.rana.inventory_service.model.ReservationStatus;
import com.rana.inventory_service.repository.InventoryRepository;
import com.rana.inventory_service.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.*;

@Service
public class InventoryService {
    
    private final InventoryRepository inventoryRepository;
    private final ReservationRepository reservationRepository;
    
    public InventoryService(InventoryRepository inventoryRepository, ReservationRepository reservationRepository) {
        this.inventoryRepository = inventoryRepository;
        this.reservationRepository = reservationRepository;
    }
    
    /**
     * Reserve inventory for an order
     */
    public InventoryEvent reserveInventory(String orderId, List<OrderItem> items) {
        System.out.println("Attempting to reserve inventory for order: " + orderId);
        
        // Check availability for all items first
        for (OrderItem item : items) {
            Optional<Product> productOpt = inventoryRepository.findByProductId(item.getProductId());
            if (productOpt.isEmpty()) {
                String error = "Product not found: " + item.getProductId();
                System.out.println(error);
                return createInventoryEvent(orderId, items, InventoryOperation.RESERVE, 
                        InventoryStatus.FAILED, error);
            }
            
            Product product = productOpt.get();
            if (product.getAvailableStock() < item.getQuantity()) {
                String error = "Insufficient stock for product: " + item.getProductId() + 
                        " (available: " + product.getAvailableStock() + ", requested: " + item.getQuantity() + ")";
                System.out.println(error);
                return createInventoryEvent(orderId, items, InventoryOperation.RESERVE, 
                        InventoryStatus.INSUFFICIENT_STOCK, error);
            }
        }
        
        // All items available, proceed with reservation
        Map<String, Integer> productQuantities = new HashMap<>();
        for (OrderItem item : items) {
            boolean reserved = inventoryRepository.reserveStock(item.getProductId(), item.getQuantity());
            if (!reserved) {
                // Rollback previous reservations
                rollbackReservations(productQuantities);
                String error = "Failed to reserve stock for product: " + item.getProductId();
                System.out.println(error);
                return createInventoryEvent(orderId, items, InventoryOperation.RESERVE, 
                        InventoryStatus.FAILED, error);
            }
            productQuantities.put(item.getProductId(), item.getQuantity());
        }
        
        // Create reservation record
        String reservationId = UUID.randomUUID().toString();
        InventoryReservation reservation = new InventoryReservation(reservationId, orderId, productQuantities);
        reservationRepository.save(reservation);
        
        System.out.println("Successfully reserved inventory for order: " + orderId + 
                " (reservation: " + reservationId + ")");
        return createInventoryEvent(orderId, items, InventoryOperation.RESERVE, 
                InventoryStatus.SUCCESS, null);
    }
    
    /**
     * Confirm reservation (finalize the stock deduction)
     */
    public InventoryEvent confirmReservation(String orderId, List<OrderItem> items) {
        System.out.println("Confirming reservation for order: " + orderId);
        
        Optional<InventoryReservation> reservationOpt = reservationRepository.findByOrderId(orderId);
        if (reservationOpt.isEmpty()) {
            String error = "No reservation found for order: " + orderId;
            System.out.println(error);
            return createInventoryEvent(orderId, items, InventoryOperation.CONFIRM, 
                    InventoryStatus.FAILED, error);
        }
        
        InventoryReservation reservation = reservationOpt.get();
        
        // Confirm each product reservation
        for (Map.Entry<String, Integer> entry : reservation.getProductQuantities().entrySet()) {
            boolean confirmed = inventoryRepository.confirmReservation(entry.getKey(), entry.getValue());
            if (!confirmed) {
                String error = "Failed to confirm reservation for product: " + entry.getKey();
                System.out.println(error);
                return createInventoryEvent(orderId, items, InventoryOperation.CONFIRM, 
                        InventoryStatus.FAILED, error);
            }
        }
        
        // Update reservation status
        reservation.setStatus(ReservationStatus.CONFIRMED);
        reservationRepository.save(reservation);
        
        System.out.println("Successfully confirmed reservation for order: " + orderId);
        return createInventoryEvent(orderId, items, InventoryOperation.CONFIRM, 
                InventoryStatus.SUCCESS, null);
    }
    
    /**
     * Release reservation (compensation/rollback)
     */
    public InventoryEvent releaseReservation(String orderId, List<OrderItem> items) {
        System.out.println("Releasing reservation for order: " + orderId);
        
        Optional<InventoryReservation> reservationOpt = reservationRepository.findByOrderId(orderId);
        if (reservationOpt.isEmpty()) {
            System.out.println("No reservation found for order: " + orderId + " - nothing to release");
            return createInventoryEvent(orderId, items, InventoryOperation.RELEASE, 
                    InventoryStatus.SUCCESS, null);
        }
        
        InventoryReservation reservation = reservationOpt.get();
        
        // Release each product reservation
        for (Map.Entry<String, Integer> entry : reservation.getProductQuantities().entrySet()) {
            boolean released = inventoryRepository.releaseStock(entry.getKey(), entry.getValue());
            if (!released) {
                System.out.println("Warning: Failed to release stock for product: " + entry.getKey());
            }
        }
        
        // Update reservation status and delete
        reservation.setStatus(ReservationStatus.RELEASED);
        reservationRepository.delete(reservation.getReservationId());
        
        System.out.println("Successfully released reservation for order: " + orderId);
        return createInventoryEvent(orderId, items, InventoryOperation.RELEASE, 
                InventoryStatus.SUCCESS, null);
    }
    
    /**
     * Check if products are available
     */
    public boolean checkAvailability(String productId, int quantity) {
        Optional<Product> productOpt = inventoryRepository.findByProductId(productId);
        return productOpt.isPresent() && productOpt.get().getAvailableStock() >= quantity;
    }
    
    /**
     * Get all products
     */
    public Map<String, Product> getAllProducts() {
        return inventoryRepository.getAllProducts();
    }
    
    // Helper methods
    
    private void rollbackReservations(Map<String, Integer> productQuantities) {
        for (Map.Entry<String, Integer> entry : productQuantities.entrySet()) {
            inventoryRepository.releaseStock(entry.getKey(), entry.getValue());
        }
    }
    
    private InventoryEvent createInventoryEvent(String orderId, List<OrderItem> items, 
            InventoryOperation operation, InventoryStatus status, String errorMessage) {
        return new InventoryEvent(orderId, items, operation, status, LocalDateTime.now(), errorMessage);
    }
}
