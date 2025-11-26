package com.rana.inventory_service.producer;

import com.rana.event_contracts.InventoryEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InventoryEventProducer {
    
    private final KafkaTemplate<String, Object> kafkaTemplate;
    
    @Value("${app.inventory-topic}")
    private String inventoryTopic;
    
    public InventoryEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    
    public void publishInventoryEvent(InventoryEvent event) {
        System.out.println("Publishing InventoryEvent to topic: " + inventoryTopic + 
                " for order: " + event.getOrderId() + 
                ", operation: " + event.getOperation() + 
                ", status: " + event.getStatus());
        kafkaTemplate.send(inventoryTopic, event.getOrderId(), event);
    }
}
