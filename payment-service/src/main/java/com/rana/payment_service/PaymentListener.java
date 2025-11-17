package com.rana.payment_service;

import com.rana.event_contracts.OrderEvent;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;
import org.springframework.retry.annotation.Backoff;
import org.springframework.stereotype.Service;

@Service
public class PaymentListener {

    @RetryableTopic(
            attempts = "3",
            backoff = @Backoff(delay = 5000),
            include = { RuntimeException.class },
            dltTopicSuffix = ".DLT"
    )
    @KafkaListener(topics = "${app.topic}", groupId = "payment-service-group")
    public void listen(OrderEvent event) {
        System.out.println("PaymentService received: " + event.getOrderId() + ", amount=" + event.getAmount());

        // Simulate business error
        if (Double.compare(event.getAmount(), 13.00) == 0) {
            System.out.println("Simulating business error for amount 13.00");
            throw new RuntimeException("Unlucky amount");
        }
        // simulate processing
        System.out.println("Processed order: " + event.getOrderId());
    }
}

