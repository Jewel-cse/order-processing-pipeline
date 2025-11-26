package com.rana.inventory_service.repository;

import com.rana.inventory_service.model.InventoryReservation;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class ReservationRepository {
    
    private final Map<String, InventoryReservation> reservations = new ConcurrentHashMap<>();
    
    public InventoryReservation save(InventoryReservation reservation) {
        reservations.put(reservation.getReservationId(), reservation);
        return reservation;
    }
    
    public Optional<InventoryReservation> findByReservationId(String reservationId) {
        return Optional.ofNullable(reservations.get(reservationId));
    }
    
    public Optional<InventoryReservation> findByOrderId(String orderId) {
        return reservations.values().stream()
                .filter(r -> r.getOrderId().equals(orderId))
                .findFirst();
    }
    
    public void delete(String reservationId) {
        reservations.remove(reservationId);
    }
    
    public Map<String, InventoryReservation> getAllReservations() {
        return new ConcurrentHashMap<>(reservations);
    }
}
