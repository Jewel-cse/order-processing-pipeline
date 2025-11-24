package com.rana.analytics_service;

import com.rana.event_contracts.OrderEvent;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.apache.kafka.streams.state.KeyValueStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.support.serializer.JsonSerde;

@Configuration
@EnableKafka
@EnableKafkaStreams
public class AnalyticsConfig {

    @Value("${app.topic}")
    private String topic;

    @Bean
    public KStream<String, OrderEvent> kStream(StreamsBuilder builder) {
        JsonSerde<OrderEvent> orderEventSerde = new JsonSerde<>(OrderEvent.class);

        // -----------------------------------------------------------------
        // 1️⃣  Original stream – forward raw events (optional)
        // -----------------------------------------------------------------
        KStream<String, OrderEvent> stream = builder.stream(
                topic,
                Consumed.with(Serdes.String(), orderEventSerde));

        // -----------------------------------------------------------------
        // 2️⃣  Aggregate per‑customer spend and write to a regular topic
        // -----------------------------------------------------------------
        stream.groupBy(
                (key, value) -> value.getCustomerId(),
                Grouped.with(Serdes.String(), orderEventSerde))
                .aggregate(
                        () -> 0.0,
                        (key, value, aggregate) -> aggregate + value.getAmount(),
                        Materialized.with(Serdes.String(), Serdes.Double()))
                .toStream()
                .to("customer-spend-topic");

        // -----------------------------------------------------------------
        // 3️⃣  Materialize a queryable state store (used by the controller)
        // -----------------------------------------------------------------
        stream.groupBy(
                (key, value) -> value.getCustomerId(),
                Grouped.with(Serdes.String(), orderEventSerde))
                .aggregate(
                        () -> 0.0,
                        (key, value, aggregate) -> aggregate + value.getAmount(),
                        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("customer-spend-store")
                                .withKeySerde(Serdes.String())
                                .withValueSerde(Serdes.Double()));

        return stream;
    }
}