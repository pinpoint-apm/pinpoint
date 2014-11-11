package com.nhn.pinpoint.thrift.util;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.thrift.io.DeserializerFactory;
import com.nhn.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.nhn.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.nhn.pinpoint.thrift.io.SerializerFactory;

public final class SerializationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SerializationUtils.class);

    private SerializationUtils() {
    }

    public static byte[] serialize(TBase object, SerializerFactory<HeaderTBaseSerializer> factory) throws TException {
        assertNotNull(factory, "SerializerFactory may note be null.");

        return serialize(object, factory.createSerializer());
    }

    public static byte[] serialize(TBase object, HeaderTBaseSerializer serializer) throws TException {
        assertNotNull(object, "TBase may note be null.");
        assertNotNull(serializer, "Serializer may note be null.");

        return serializer.serialize(object);
    }

    public static byte[] serialize(TBase object, SerializerFactory<HeaderTBaseSerializer> factory, byte[] defaultValue) {
        try {
            return serialize(object, factory);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Serialize " + object + " fail. caused=" + e.getMessage(), e);
            }
        }

        return defaultValue;
    }

    public static byte[] serialize(TBase object, HeaderTBaseSerializer serializer, byte[] defaultValue) {
        try {
            return serialize(object, serializer);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Serialize " + object + " fail. caused=" + e.getMessage(), e);
            }
        }

        return defaultValue;
    }

    public static TBase deserialize(byte[] objectData, DeserializerFactory<HeaderTBaseDeserializer> factory) throws TException {
        assertNotNull(factory, "DeserializerFactory may note be null.");

        return deserialize(objectData, factory.createDeserializer());
    }

    public static TBase deserialize(byte[] objectData, HeaderTBaseDeserializer deserializer) throws TException {
        assertNotNull(objectData, "TBase may note be null.");
        assertNotNull(deserializer, "Deserializer may note be null.");

        return deserializer.deserialize(objectData);
    }

    public static TBase deserialize(byte[] objectData, DeserializerFactory<HeaderTBaseDeserializer> factory, TBase defaultValue) {
        try {
            return deserialize(objectData, factory);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Deserialize fail. caused=" + e.getMessage(), e);
            }
        }

        return defaultValue;
    }

    public static TBase deserialize(byte[] objectData, HeaderTBaseDeserializer deserializer, TBase defaultValue) {
        try {
            return deserialize(objectData, deserializer);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Deserialize fail. caused=" + e.getMessage(), e);
            }
        }

        return defaultValue;
    }

    private static void assertNotNull(Object object, String message) {

        if (object == null) {
            throw new NullPointerException(message);
        }
    }
}
