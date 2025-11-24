package com.rana.event_contracts.serializer;

import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class EventSerde<T> implements Serde<T> {
    @Override public Serializer<T> serializer() { return new EventSerializer<>(); }
    @Override public Deserializer<T> deserializer() { return new EventDeserializer<>(); }
}


