package com.rana.inventory_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;
    private String productName;
    private int availableStock;
    private int reservedStock;
    private double price;
    
    public Product(String productId, String productName, int availableStock, double price) {
        this.productId = productId;
        this.productName = productName;
        this.availableStock = availableStock;
        this.reservedStock = 0;
        this.price = price;
    }
    
    public int getTotalStock() {
        return availableStock + reservedStock;
    }
}
