package com.rana.event_contracts.serializer;


import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Serializer;

public class EventSerializer<T> implements Serializer<T> {

    private final ObjectMapper mapper = new ObjectMapper();

    public EventSerializer() { }

    @Override
    public byte[] serialize(String topic, T data) {
        try {
            return mapper.writeValueAsBytes(data);
        } catch (Exception e) {
            throw new SerializationException("Serialization error", e);
        }
    }
}

