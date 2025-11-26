package com.rana.order_service.producer;

import com.rana.event_contracts.SagaEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class SagaEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.saga-topic}")
    private String sagaTopic;

    public SagaEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishSagaEvent(SagaEvent event) {
        System.out.println("Publishing SagaEvent to topic: " + sagaTopic +
                ", sagaId: " + event.getSagaId() +
                ", status: " + event.getSagaStatus() +
                ", orderId: " + event.getOrderId());
        kafkaTemplate.send(sagaTopic, event.getSagaId(), event);
    }
}
