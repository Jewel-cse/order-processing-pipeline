package com.rana.order_service.listener;

import com.rana.event_contracts.InventoryEvent;
import com.rana.order_service.saga.SagaOrchestrator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventListener {

    private final SagaOrchestrator sagaOrchestrator;

    public InventoryEventListener(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @KafkaListener(topics = "${app.inventory-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleInventoryEvent(InventoryEvent inventoryEvent) {
        System.out.println("OrderService received InventoryEvent for order: " + inventoryEvent.getOrderId() +
                ", status: " + inventoryEvent.getStatus());
        sagaOrchestrator.handleInventoryResult(inventoryEvent);
    }
}
