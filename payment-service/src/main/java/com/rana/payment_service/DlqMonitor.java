package com.rana.payment_service;

import com.rana.event_contracts.OrderEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class DlqMonitor {
    @KafkaListener(topics = "order-events-topic.DLT", groupId = "dlq-monitor-group")
    public void onDlq(ConsumerRecord<String, OrderEvent> record) {
        System.err.println("DLQ received: key=" + record.key() + ", value=" + record.value());
    }
}
