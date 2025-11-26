package com.rana.order_service.saga;

import com.rana.event_contracts.SagaStatus;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SagaStateManager {

    private final Map<String, SagaState> sagas = new ConcurrentHashMap<>();

    public SagaState createSaga(SagaState sagaState) {
        sagas.put(sagaState.getSagaId(), sagaState);
        System.out.println("Created saga: " + sagaState.getSagaId() + " for order: " + sagaState.getOrderId());
        return sagaState;
    }

    public void updateSagaStatus(String sagaId, SagaStatus status) {
        SagaState saga = sagas.get(sagaId);
        if (saga != null) {
            saga.updateStatus(status);
            System.out.println("Updated saga: " + sagaId + " to status: " + status);
        }
    }

    public Optional<SagaState> getSaga(String sagaId) {
        return Optional.ofNullable(sagas.get(sagaId));
    }

    public Optional<SagaState> getSagaByOrderId(String orderId) {
        return sagas.values().stream()
                .filter(s -> s.getOrderId().equals(orderId))
                .findFirst();
    }

    public void deleteSaga(String sagaId) {
        sagas.remove(sagaId);
        System.out.println("Deleted saga: " + sagaId);
    }

    public Map<String, SagaState> getAllSagas() {
        return new ConcurrentHashMap<>(sagas);
    }
}
