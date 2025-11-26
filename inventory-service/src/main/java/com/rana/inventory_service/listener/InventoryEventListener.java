package com.rana.inventory_service.listener;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rana.event_contracts.*;
import com.rana.inventory_service.producer.InventoryEventProducer;
import com.rana.inventory_service.service.InventoryService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class InventoryEventListener {
    
    private final InventoryService inventoryService;
    private final InventoryEventProducer inventoryEventProducer;
    private final ObjectMapper objectMapper;
    
    public InventoryEventListener(InventoryService inventoryService, 
                                   InventoryEventProducer inventoryEventProducer) {
        this.inventoryService = inventoryService;
        this.inventoryEventProducer = inventoryEventProducer;
        this.objectMapper = new ObjectMapper();
    }
    
    @KafkaListener(topics = "${app.saga-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSagaEvent(SagaEvent sagaEvent) {
        System.out.println("Received SagaEvent: " + sagaEvent.getSagaId() + 
                ", status: " + sagaEvent.getSagaStatus() + 
                ", orderId: " + sagaEvent.getOrderId());
        
        SagaStatus status = sagaEvent.getSagaStatus();
        
        if (status == SagaStatus.INVENTORY_PENDING) {
            handleInventoryReservation(sagaEvent);
        } else if (status == SagaStatus.COMPENSATING) {
            handleCompensation(sagaEvent);
        }
    }
    
    private void handleInventoryReservation(SagaEvent sagaEvent) {
        try {
            // Extract order items from payload
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) sagaEvent.getPayload().get("items");
            
            if (itemsData == null || itemsData.isEmpty()) {
                System.out.println("No items found in saga payload for order: " + sagaEvent.getOrderId());
                InventoryEvent failureEvent = new InventoryEvent(
                        sagaEvent.getOrderId(), 
                        List.of(), 
                        InventoryOperation.RESERVE, 
                        InventoryStatus.FAILED,
                        java.time.LocalDateTime.now(),
                        "No items in order"
                );
                inventoryEventProducer.publishInventoryEvent(failureEvent);
                return;
            }
            
            // Convert to OrderItem list
            List<OrderItem> items = itemsData.stream()
                    .map(itemData -> objectMapper.convertValue(itemData, OrderItem.class))
                    .toList();
            
            // Reserve inventory
            InventoryEvent result = inventoryService.reserveInventory(sagaEvent.getOrderId(), items);
            inventoryEventProducer.publishInventoryEvent(result);
            
        } catch (Exception e) {
            System.err.println("Error processing inventory reservation: " + e.getMessage());
            e.printStackTrace();
            InventoryEvent failureEvent = new InventoryEvent(
                    sagaEvent.getOrderId(), 
                    List.of(), 
                    InventoryOperation.RESERVE, 
                    InventoryStatus.FAILED,
                    java.time.LocalDateTime.now(),
                    "Error: " + e.getMessage()
            );
            inventoryEventProducer.publishInventoryEvent(failureEvent);
        }
    }
    
    private void handleCompensation(SagaEvent sagaEvent) {
        try {
            System.out.println("Handling compensation for order: " + sagaEvent.getOrderId());
            
            // Extract order items from payload
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> itemsData = (List<Map<String, Object>>) sagaEvent.getPayload().get("items");
            
            List<OrderItem> items = itemsData != null ? 
                    itemsData.stream()
                            .map(itemData -> objectMapper.convertValue(itemData, OrderItem.class))
                            .toList() : List.of();
            
            // Release reservation
            InventoryEvent result = inventoryService.releaseReservation(sagaEvent.getOrderId(), items);
            inventoryEventProducer.publishInventoryEvent(result);
            
        } catch (Exception e) {
            System.err.println("Error processing compensation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
