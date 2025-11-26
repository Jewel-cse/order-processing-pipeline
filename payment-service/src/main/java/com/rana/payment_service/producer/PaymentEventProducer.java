package com.rana.payment_service.producer;

import com.rana.event_contracts.PaymentEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventProducer {

    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${app.payment-topic}")
    private String paymentTopic;

    public PaymentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishPaymentEvent(PaymentEvent event) {
        System.out.println("Publishing PaymentEvent to topic: " + paymentTopic +
                " for order: " + event.getOrderId() +
                ", status: " + event.getStatus());
        kafkaTemplate.send(paymentTopic, event.getOrderId(), event);
    }
}
