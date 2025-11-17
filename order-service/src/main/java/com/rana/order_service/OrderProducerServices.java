package com.rana.order_service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class OrderProducerServices {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    @Value("${app.topic}")
    private String topic;

    public OrderProducerServices(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publish(OrderEvent event) {
        kafkaTemplate.send(topic, event.getOrderId(), event);
    }


}
