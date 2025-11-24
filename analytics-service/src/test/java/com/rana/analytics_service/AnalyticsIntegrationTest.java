package com.rana.analytics_service;

import com.rana.event_contracts.OrderEvent;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringSerializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;
import org.springframework.kafka.test.context.EmbeddedKafka;
import org.springframework.lang.NonNull;
import org.springframework.test.annotation.DirtiesContext;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@EmbeddedKafka(topics = "order-events-topic", partitions = 1, bootstrapServersProperty = "spring.kafka.streams.bootstrap-servers")
@DirtiesContext
public class AnalyticsIntegrationTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private KafkaTemplate<String, OrderEvent> kafkaTemplate;

    @Test
    public void testAnalyticsFlow() {
        String customerId = "cust1";
        OrderEvent event1 = new OrderEvent("order1", customerId, 100.0, "USD", "CREATED");
        OrderEvent event2 = new OrderEvent("order2", customerId, 50.0, "USD", "CREATED");

        kafkaTemplate.send("order-events-topic", Objects.requireNonNull(event1.getOrderId()), event1);
        kafkaTemplate.send("order-events-topic", Objects.requireNonNull(event2.getOrderId()), event2);

        // Wait for Kafka Streams to process
        await().atMost(20, TimeUnit.SECONDS).untilAsserted(() -> {
            Double spend = restTemplate.getForObject("/analytics/spend/" + customerId, Double.class);
            assertThat(spend).isEqualTo(150.0);
        });
    }

    @Configuration
    static class TestConfig {
        @Bean
        public @NonNull ProducerFactory<String, OrderEvent> producerFactory() {
            Map<String, Object> configProps = new HashMap<>();
            configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
                    System.getProperty("spring.kafka.streams.bootstrap-servers"));
            configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
            configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
            return new DefaultKafkaProducerFactory<>(configProps);
        }

        @Bean
        public KafkaTemplate<String, OrderEvent> kafkaTemplate() {
            return new KafkaTemplate<>(producerFactory());
        }
    }
}
