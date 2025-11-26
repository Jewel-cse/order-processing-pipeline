package com.rana.event_contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventoryEvent {
    private String orderId;
    private List<OrderItem> items;
    private InventoryOperation operation;
    private InventoryStatus status;
    private LocalDateTime timestamp;
    private String errorMessage;
}
