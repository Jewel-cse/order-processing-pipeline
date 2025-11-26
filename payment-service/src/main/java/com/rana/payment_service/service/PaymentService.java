package com.rana.payment_service.service;

import com.rana.event_contracts.*;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class PaymentService {

    /**
     * Process payment for an order
     */
    public PaymentEvent processPayment(SagaEvent sagaEvent) {
        System.out.println("Processing payment for order: " + sagaEvent.getOrderId());

        try {
            // Extract order details from payload
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> orderData = (java.util.Map<String, Object>) sagaEvent.getPayload()
                    .get("orderEvent");

            String orderId = (String) orderData.get("orderId");
            String customerId = (String) orderData.get("customerId");
            Double amount = (Double) orderData.get("amount");

            // Simulate payment processing
            Thread.sleep(500); // Simulate API call delay

            // Simulate failure for specific amounts (for testing)
            if (amount != null && Double.compare(amount, 13.00) == 0) {
                System.out.println("Payment failed for amount 13.00 (simulated failure)");
                return new PaymentEvent(
                        orderId,
                        customerId,
                        amount,
                        PaymentStatus.FAILED,
                        null,
                        LocalDateTime.now(),
                        "Unlucky amount - payment declined");
            }

            // Payment successful
            String transactionId = "TXN-" + UUID.randomUUID().toString().substring(0, 8);
            System.out.println("Payment successful! Transaction ID: " + transactionId);

            return new PaymentEvent(
                    orderId,
                    customerId,
                    amount,
                    PaymentStatus.SUCCESS,
                    transactionId,
                    LocalDateTime.now(),
                    null);

        } catch (Exception e) {
            System.err.println("Error processing payment: " + e.getMessage());
            e.printStackTrace();

            return new PaymentEvent(
                    sagaEvent.getOrderId(),
                    "UNKNOWN",
                    0.0,
                    PaymentStatus.FAILED,
                    null,
                    LocalDateTime.now(),
                    "Error: " + e.getMessage());
        }
    }

    /**
     * Process refund (compensation)
     */
    public void processRefund(SagaEvent sagaEvent) {
        System.out.println("Processing refund for order: " + sagaEvent.getOrderId());

        try {
            @SuppressWarnings("unchecked")
            java.util.Map<String, Object> paymentData = (java.util.Map<String, Object>) sagaEvent.getPayload()
                    .get("paymentEvent");

            if (paymentData != null) {
                String transactionId = (String) paymentData.get("transactionId");
                Double amount = (Double) paymentData.get("amount");

                // Simulate refund processing
                Thread.sleep(300);

                System.out.println("Refund processed successfully for transaction: " + transactionId +
                        ", amount: " + amount);
            }

        } catch (Exception e) {
            System.err.println("Error processing refund: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
