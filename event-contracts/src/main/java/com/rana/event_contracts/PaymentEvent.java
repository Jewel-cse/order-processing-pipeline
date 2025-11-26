package com.rana.event_contracts;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentEvent {
    private String orderId;
    private String customerId;
    private double amount;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime timestamp;
    private String errorMessage;
}
