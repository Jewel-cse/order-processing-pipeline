package com.rana.event_contracts.serializer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;

public class EventDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper mapper = new ObjectMapper();
    private Class<T> targetType;

    public EventDeserializer() {}

    public EventDeserializer(Class<T> targetType) {
        this.targetType = targetType;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
        try {
            return mapper.readValue(data, targetType);
        } catch (Exception e) {
            throw new SerializationException("Deserialization error", e);
        }
    }
}
