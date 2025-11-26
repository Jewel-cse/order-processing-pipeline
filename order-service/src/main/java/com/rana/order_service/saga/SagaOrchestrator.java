package com.rana.order_service.saga;

import com.rana.event_contracts.*;
import com.rana.order_service.producer.SagaEventProducer;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class SagaOrchestrator {

    private final SagaStateManager stateManager;
    private final SagaEventProducer sagaEventProducer;

    public SagaOrchestrator(SagaStateManager stateManager, SagaEventProducer sagaEventProducer) {
        this.stateManager = stateManager;
        this.sagaEventProducer = sagaEventProducer;
    }

    /**
     * Start a new order processing saga
     */
    public String startOrderSaga(OrderEvent orderEvent) {
        String sagaId = UUID.randomUUID().toString();
        System.out.println("\n=== Starting Order Saga ===");
        System.out.println("Saga ID: " + sagaId);
        System.out.println("Order ID: " + orderEvent.getOrderId());
        System.out.println("Customer ID: " + orderEvent.getCustomerId());
        System.out.println("Amount: " + orderEvent.getAmount());

        // Create saga state
        SagaState sagaState = new SagaState(sagaId, orderEvent.getOrderId(), orderEvent);
        stateManager.createSaga(sagaState);

        // Transition to PAYMENT_PENDING
        stateManager.updateSagaStatus(sagaId, SagaStatus.PAYMENT_PENDING);

        // Publish saga event for payment service
        SagaEvent sagaEvent = createSagaEvent(sagaId, orderEvent.getOrderId(),
                SagaStatus.PAYMENT_PENDING, "Awaiting payment processing");
        sagaEvent.getPayload().put("orderEvent", convertOrderEventToMap(orderEvent));
        sagaEventProducer.publishSagaEvent(sagaEvent);

        return sagaId;
    }

    /**
     * Handle payment result
     */
    public void handlePaymentResult(PaymentEvent paymentEvent) {
        System.out.println("\n=== Handling Payment Result ===");
        System.out.println("Order ID: " + paymentEvent.getOrderId());
        System.out.println("Payment Status: " + paymentEvent.getStatus());

        var sagaOpt = stateManager.getSagaByOrderId(paymentEvent.getOrderId());
        if (sagaOpt.isEmpty()) {
            System.err.println("No saga found for order: " + paymentEvent.getOrderId());
            return;
        }

        SagaState saga = sagaOpt.get();
        saga.setPaymentEvent(paymentEvent);

        if (paymentEvent.getStatus() == PaymentStatus.SUCCESS) {
            // Payment successful, proceed to inventory
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.PAYMENT_COMPLETED);
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.INVENTORY_PENDING);

            // Publish saga event for inventory service
            SagaEvent sagaEvent = createSagaEvent(saga.getSagaId(), saga.getOrderId(),
                    SagaStatus.INVENTORY_PENDING, "Awaiting inventory reservation");
            sagaEvent.getPayload().put("items", convertItemsToMapList(saga.getOrderEvent().getItems()));
            sagaEventProducer.publishSagaEvent(sagaEvent);

        } else {
            // Payment failed, mark saga as failed
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.PAYMENT_FAILED);
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.FAILED);

            System.out.println("Saga failed due to payment failure: " + saga.getSagaId());
        }
    }

    /**
     * Handle inventory result
     */
    public void handleInventoryResult(InventoryEvent inventoryEvent) {
        System.out.println("\n=== Handling Inventory Result ===");
        System.out.println("Order ID: " + inventoryEvent.getOrderId());
        System.out.println("Inventory Status: " + inventoryEvent.getStatus());

        var sagaOpt = stateManager.getSagaByOrderId(inventoryEvent.getOrderId());
        if (sagaOpt.isEmpty()) {
            System.err.println("No saga found for order: " + inventoryEvent.getOrderId());
            return;
        }

        SagaState saga = sagaOpt.get();
        saga.setInventoryEvent(inventoryEvent);

        if (inventoryEvent.getStatus() == InventoryStatus.SUCCESS) {
            // Inventory reserved successfully, saga completed
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.INVENTORY_COMPLETED);
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.COMPLETED);

            System.out.println("✓ Saga completed successfully: " + saga.getSagaId());
            System.out.println("Order " + saga.getOrderId() + " processed successfully!");

        } else {
            // Inventory reservation failed, trigger compensation
            stateManager.updateSagaStatus(saga.getSagaId(), SagaStatus.INVENTORY_FAILED);
            compensate(saga.getSagaId());
        }
    }

    /**
     * Trigger compensation (rollback)
     */
    public void compensate(String sagaId) {
        System.out.println("\n=== Starting Compensation ===");
        System.out.println("Saga ID: " + sagaId);

        var sagaOpt = stateManager.getSaga(sagaId);
        if (sagaOpt.isEmpty()) {
            System.err.println("No saga found: " + sagaId);
            return;
        }

        SagaState saga = sagaOpt.get();
        stateManager.updateSagaStatus(sagaId, SagaStatus.COMPENSATING);

        // Compensate payment (refund) if payment was successful
        if (saga.getPaymentEvent() != null && saga.getPaymentEvent().getStatus() == PaymentStatus.SUCCESS) {
            System.out.println("Compensating payment for order: " + saga.getOrderId());

            SagaEvent compensationEvent = createSagaEvent(sagaId, saga.getOrderId(),
                    SagaStatus.COMPENSATING, "Compensating payment");
            compensationEvent.getPayload().put("compensationType", "PAYMENT_REFUND");
            compensationEvent.getPayload().put("paymentEvent", convertPaymentEventToMap(saga.getPaymentEvent()));
            compensationEvent.getPayload().put("items", convertItemsToMapList(saga.getOrderEvent().getItems()));
            sagaEventProducer.publishSagaEvent(compensationEvent);
        }

        stateManager.updateSagaStatus(sagaId, SagaStatus.COMPENSATED);
        System.out.println("Compensation completed for saga: " + sagaId);
    }

    /**
     * Get saga status
     */
    public SagaState getSagaStatus(String sagaId) {
        return stateManager.getSaga(sagaId).orElse(null);
    }

    // Helper methods

    private SagaEvent createSagaEvent(String sagaId, String orderId, SagaStatus status, String step) {
        SagaEvent event = new SagaEvent(sagaId, orderId, SagaType.ORDER_PROCESSING, status, step);
        event.setTimestamp(LocalDateTime.now());
        return event;
    }

    private Map<String, Object> convertOrderEventToMap(OrderEvent orderEvent) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderEvent.getOrderId());
        map.put("customerId", orderEvent.getCustomerId());
        map.put("amount", orderEvent.getAmount());
        map.put("currency", orderEvent.getCurrency());
        map.put("status", orderEvent.getStatus());
        return map;
    }

    private Map<String, Object> convertPaymentEventToMap(PaymentEvent paymentEvent) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", paymentEvent.getOrderId());
        map.put("customerId", paymentEvent.getCustomerId());
        map.put("amount", paymentEvent.getAmount());
        map.put("status", paymentEvent.getStatus().toString());
        map.put("transactionId", paymentEvent.getTransactionId());
        return map;
    }

    private java.util.List<Map<String, Object>> convertItemsToMapList(java.util.List<OrderItem> items) {
        if (items == null)
            return java.util.List.of();
        return items.stream().map(item -> {
            Map<String, Object> map = new HashMap<>();
            map.put("productId", item.getProductId());
            map.put("quantity", item.getQuantity());
            map.put("price", item.getPrice());
            return map;
        }).collect(Collectors.toList());
    }
}
