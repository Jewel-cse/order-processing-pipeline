package com.rana.inventory_service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryReservation {
    private String reservationId;
    private String orderId;
    private Map<String, Integer> productQuantities; // productId -> quantity
    private ReservationStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
    
    public InventoryReservation(String reservationId, String orderId, Map<String, Integer> productQuantities) {
        this.reservationId = reservationId;
        this.orderId = orderId;
        this.productQuantities = productQuantities;
        this.status = ReservationStatus.RESERVED;
        this.createdAt = LocalDateTime.now();
        this.expiresAt = LocalDateTime.now().plusMinutes(30); // 30 minute expiry
    }
}
