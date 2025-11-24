package com.rana.analytics_service;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    private final StreamsBuilderFactoryBean factoryBean;

    public AnalyticsController(StreamsBuilderFactoryBean factoryBean) {
        this.factoryBean = factoryBean;
    }

    @GetMapping("/spend/{customerId}")
    public ResponseEntity<String> getSpend(@PathVariable String customerId) {
        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new RuntimeException("Kafka Streams is not started");
        }
        // Ensure the streams are in a RUNNING state before querying the store
        if (kafkaStreams.state() != org.apache.kafka.streams.KafkaStreams.State.RUNNING) {
            // You may choose to return a default value or throw an exception
            throw new IllegalStateException("Kafka Streams is not running. Current state: " + kafkaStreams.state());
        }
        ReadOnlyKeyValueStore<String, Double> store = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("customer-spend-store", QueryableStoreTypes.keyValueStore()));
        return ResponseEntity.ok(String.format("Customer %s has spent %s", customerId, store.get(customerId)));
    }
}
