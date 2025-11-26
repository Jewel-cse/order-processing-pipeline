package com.rana.event_contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SagaEvent {
    private String sagaId;
    private String orderId;
    private SagaType sagaType;
    private SagaStatus sagaStatus;
    private String currentStep;
    private LocalDateTime timestamp;
    private Map<String, Object> payload;

    public SagaEvent(String sagaId, String orderId, SagaType sagaType, SagaStatus sagaStatus, String currentStep) {
        this.sagaId = sagaId;
        this.orderId = orderId;
        this.sagaType = sagaType;
        this.sagaStatus = sagaStatus;
        this.currentStep = currentStep;
        this.timestamp = LocalDateTime.now();
        this.payload = new HashMap<>();
    }
}
