package com.rana.analytics_service;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
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

    @GetMapping("/health")  
    public ResponseEntity<String> health() {
        KafkaStreams ks = factoryBean.getKafkaStreams();
        return ResponseEntity.ok("Kafka Streams state: " + (ks != null ? ks.state() : "null"));
    }

    @GetMapping("/daily-spend/{customerId}/{date}")
    public ResponseEntity<String> getDailySpend(@PathVariable String customerId,
            @PathVariable String date) {
        // Parse the date (expected format: yyyy-MM-dd) and compute window boundaries
        java.time.LocalDate localDate = java.time.LocalDate.parse(date);
        java.time.Instant startInstant = localDate.atStartOfDay(java.time.ZoneOffset.UTC).toInstant();
        java.time.Instant endInstant = startInstant.plus(java.time.Duration.ofDays(1));
        long start = startInstant.toEpochMilli();
        long end = endInstant.toEpochMilli();

        KafkaStreams kafkaStreams = factoryBean.getKafkaStreams();
        if (kafkaStreams == null) {
            throw new RuntimeException("Kafka Streams is not started");
        }
        // Ensure streams are running
        if (kafkaStreams.state() != org.apache.kafka.streams.KafkaStreams.State.RUNNING) {
            throw new IllegalStateException("Kafka Streams not running: " + kafkaStreams.state());
        }
        // Query the window store
        ReadOnlyWindowStore<String, Double> windowStore = kafkaStreams.store(
                StoreQueryParameters.fromNameAndType("customer-daily-spend-store",
                        QueryableStoreTypes.windowStore()));
        // Fetch the aggregate for the exact day (tumbling window, so there is at most
        // one entry)
        Double spend = windowStore.fetch(customerId, start);
        if (spend == null) {
            spend = 0.0;
        }
        String response = String.format("Customer %s spent %s on %s", customerId, spend, date);
        return ResponseEntity.ok(response);
    }
}
