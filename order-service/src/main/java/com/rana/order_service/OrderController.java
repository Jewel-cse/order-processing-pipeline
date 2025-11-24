package com.rana.order_service;

import com.rana.event_contracts.OrderEvent;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final OrderProducerServices producer;

    public OrderController(OrderProducerServices producer) {
        this.producer = producer;
    }

    @GetMapping("/create")
    public ResponseEntity<String> create() {
        for (int i = 0; i < 100; i++) {
            OrderEvent event = new OrderEvent(
                    "ORD123456-" + i, 
                    "CUST789012", 
                     99.99 + i,
                    "USD",
                    "CONFIRMED");
            producer.publish(event);
        }

        return ResponseEntity.ok("published");
    }
}
