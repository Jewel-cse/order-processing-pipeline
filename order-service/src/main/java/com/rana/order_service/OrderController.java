package com.rana.order_service;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {
    private final OrderProducerServices producer;

    public OrderController(OrderProducerServices producer) { this.producer = producer; }

    @PostMapping("/create")
    public ResponseEntity<String> create(@RequestBody OrderEvent event) {
        event.setStatus("CREATED");
        producer.publish(event);
        return ResponseEntity.ok("published");
    }
}

