package com.rana.payment_service;

import com.rana.event_contracts.*;
import com.rana.payment_service.producer.PaymentEventProducer;
import com.rana.payment_service.service.PaymentService;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentListener {

    private final PaymentService paymentService;
    private final PaymentEventProducer paymentEventProducer;

    public PaymentListener(PaymentService paymentService, PaymentEventProducer paymentEventProducer) {
        this.paymentService = paymentService;
        this.paymentEventProducer = paymentEventProducer;
    }

    @KafkaListener(topics = "${app.saga-topic}", groupId = "${spring.kafka.consumer.group-id}")
    public void handleSagaEvent(SagaEvent sagaEvent) {
        System.out.println("Received SagaEvent: " + sagaEvent.getSagaId() +
                ", status: " + sagaEvent.getSagaStatus() +
                ", orderId: " + sagaEvent.getOrderId());

        SagaStatus status = sagaEvent.getSagaStatus();

        if (status == SagaStatus.PAYMENT_PENDING) {
            // Process payment
            PaymentEvent paymentEvent = paymentService.processPayment(sagaEvent);
            paymentEventProducer.publishPaymentEvent(paymentEvent);

        } else if (status == SagaStatus.COMPENSATING) {
            // Check if this is a payment compensation
            String compensationType = (String) sagaEvent.getPayload().get("compensationType");
            if ("PAYMENT_REFUND".equals(compensationType)) {
                paymentService.processRefund(sagaEvent);
            }
        }
    }
}
