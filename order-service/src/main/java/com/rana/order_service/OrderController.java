package com.rana.order_service;

import com.rana.event_contracts.OrderEvent;
import com.rana.event_contracts.OrderItem;
import com.rana.order_service.saga.SagaOrchestrator;
import com.rana.order_service.saga.SagaState;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/order")
public class OrderController {
    private final SagaOrchestrator sagaOrchestrator;

    public OrderController(SagaOrchestrator sagaOrchestrator) {
        this.sagaOrchestrator = sagaOrchestrator;
    }

    @GetMapping("/create")
    public ResponseEntity<String> create() {
        // Create sample order with items
        List<OrderItem> items = new ArrayList<>();
        items.add(new OrderItem("PROD-001", 2, 999.99)); // 2 Laptops
        items.add(new OrderItem("PROD-002", 5, 29.99)); // 5 Mice

        String orderId = "ORD-" + System.currentTimeMillis();
        OrderEvent event = new OrderEvent(
                orderId,
                "CUST789012",
                2099.93, // Total amount
                "USD",
                "PENDING",
                items);

        String sagaId = sagaOrchestrator.startOrderSaga(event);

        return ResponseEntity.ok("Order created! Saga ID: " + sagaId + ", Order ID: " + orderId);
    }

    @GetMapping("/create-bulk")
    public ResponseEntity<String> createBulk(@RequestParam(defaultValue = "10") int count) {
        List<String> sagaIds = new ArrayList<>();

        for (int i = 0; i < count; i++) {
            List<OrderItem> items = new ArrayList<>();
            items.add(new OrderItem("PROD-00" + ((i % 8) + 1), 1 + (i % 3), 99.99));

            String orderId = "ORD-" + System.currentTimeMillis() + "-" + i;
            OrderEvent event = new OrderEvent(
                    orderId,
                    "CUST" + i,
                    99.99 * (1 + (i % 3)),
                    "USD",
                    "PENDING",
                    items);

            String sagaId = sagaOrchestrator.startOrderSaga(event);
            sagaIds.add(sagaId);

            // Small delay to avoid overwhelming the system
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }

        return ResponseEntity.ok("Created " + count + " orders. First Saga ID: " + sagaIds.get(0));
    }

    @GetMapping("/saga/{sagaId}")
    public ResponseEntity<SagaState> getSagaStatus(@PathVariable String sagaId) {
        SagaState saga = sagaOrchestrator.getSagaStatus(sagaId);
        if (saga == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(saga);
    }

    @GetMapping("/health")
    public ResponseEntity<String> health() {
        return ResponseEntity.ok("Order Service is running with Saga Orchestration");
    }
}
