package com.rana.inventory_service.controller;

import com.rana.inventory_service.model.Product;
import com.rana.inventory_service.service.InventoryService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/inventory")
public class InventoryController {
    
    private final InventoryService inventoryService;
    
    public InventoryController(InventoryService inventoryService) {
        this.inventoryService = inventoryService;
    }
    
    @GetMapping("/products")
    public ResponseEntity<Map<String, Product>> getAllProducts() {
        return ResponseEntity.ok(inventoryService.getAllProducts());
    }
    
    @GetMapping("/check/{productId}/{quantity}")
    public ResponseEntity<Boolean> checkAvailability(
            @PathVariable String productId, 
            @PathVariable int quantity) {
        boolean available = inventoryService.checkAvailability(productId, quantity);
        return ResponseEntity.ok(available);
    }
    
    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Inventory Service is running");
    }
}
