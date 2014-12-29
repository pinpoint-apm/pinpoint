/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.thrift.util;

import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseSerializer;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;

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
                LOGGER.warn("Serialize " + object + " failed. Error:" + e.getMessage(), e);
            }
        }

        return defaultValue;
    }

    public static byte[] serialize(TBase object, HeaderTBaseSerializer serializer, byte[] defaultValue) {
        try {
            return serialize(object, serializer);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Serialize " + object + " failed. Error:" + e.getMessage(), e);
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
                LOGGER.warn("Deserialize failed. Error:" + e.getMessage(), e);
            }
        }

        return defaultValue;
    }

    public static TBase deserialize(byte[] objectData, HeaderTBaseDeserializer deserializer, TBase defaultValue) {
        try {
            return deserialize(objectData, deserializer);
        } catch (Exception e) {
            if (LOGGER.isWarnEnabled()) {
                LOGGER.warn("Deserialize failed. Error:" + e.getMessage(), e);
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
