package com.rana.order_service.listener;

import com.rana.event_contracts.PaymentEvent;
import com.rana.order_service.saga.SagaOrchestrator;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class PaymentEventListener {

    private final SagaOrchestrator sagaOrchestrator;

    public PaymentEventListener(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @KafkaListener(topics = "${app.payment-topic}", groupId = "${spring.kafka.consumer.group-id}", containerFactory = "paymentEventKafkaListenerContainerFactory")
    public void handlePaymentEvent(PaymentEvent paymentEvent) {
        System.out.println("OrderService received PaymentEvent for order: " + paymentEvent.getOrderId() +
                ", status: " + paymentEvent.getStatus());
        sagaOrchestrator.handlePaymentResult(paymentEvent);
    }
}
