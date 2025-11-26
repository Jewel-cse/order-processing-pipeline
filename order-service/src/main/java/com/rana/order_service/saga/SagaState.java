package com.rana.order_service.saga;

import com.rana.event_contracts.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SagaState {
    private String sagaId;
    private String orderId;
    private SagaStatus status;
    private OrderEvent orderEvent;
    private PaymentEvent paymentEvent;
    private InventoryEvent inventoryEvent;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public SagaState(String sagaId, String orderId, OrderEvent orderEvent) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.orderEvent = orderEvent;
        this.status = SagaStatus.STARTED;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public void updateStatus(SagaStatus newStatus) {
        this.status = newStatus;
        this.updatedAt = LocalDateTime.now();
    }
}
