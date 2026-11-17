/*
 * Copyright 2016 Naver Corp.
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

package com.navercorp.pinpoint.plugin.activemq.client;

import org.apache.activemq.command.Message;

import java.io.IOException;

/**
 * Reads and writes the trace headers through the ActiveMQ message itself rather than the JMS API:
 * ActiveMQ 5.x implements javax.jms and 6.x implements jakarta.jms, while org.apache.activemq.command.Message
 * keeps the same property methods on both.
 *
 * @author HyunGil Jeong
 */
public enum ActiveMQClientHeader {
    ACTIVEMQ_TRACE_ID("Pinpoint-TraceID"),
    ACTIVEMQ_SPAN_ID("Pinpoint-SpanID"),
    ACTIVEMQ_PARENT_SPAN_ID("Pinpoint-pSpanID"),
    ACTIVEMQ_SAMPLED("Pinpoint-Sampled"),
    ACTIVEMQ_FLAGS("Pinpoint-Flags"),
    ACTIVEMQ_PARENT_APPLICATION_NAME("Pinpoint-pAppName"),
    ACTIVEMQ_PARENT_APPLICATION_TYPE("Pinpoint-pAppType"),
    ACTIVEMQ_PARENT_SERVICE_NAME("Pinpoint-pServiceName");

    private final String id;

    ActiveMQClientHeader(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    private static void setProperty(Message message, ActiveMQClientHeader key, Object value) throws IOException {
        message.setProperty(key.id, value);
    }

    private static Object getProperty(Message message, ActiveMQClientHeader key) {
        try {
            return message.getProperty(key.id);
        } catch (IOException ignored) {
            // just ignore and let the caller return the default value
            return null;
        }
    }

    // A property that came over the wire is not always the type it was set with (a String arrives as a UTF8Buffer,
    // and another client may have set a number as text), so read it leniently the way the JMS getters do.
    private static String getString(Message message, ActiveMQClientHeader key, String defaultValue) {
        final Object value = getProperty(message, key);
        if (value == null) {
            return defaultValue;
        }
        return value.toString();
    }

    private static Long getLong(Message message, ActiveMQClientHeader key, Long defaultValue) {
        final Object value = getProperty(message, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        try {
            return Long.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static Short getShort(Message message, ActiveMQClientHeader key, Short defaultValue) {
        final Object value = getProperty(message, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Number) {
            return ((Number) value).shortValue();
        }
        try {
            return Short.valueOf(value.toString());
        } catch (NumberFormatException ignored) {
            return defaultValue;
        }
    }

    private static Boolean getBoolean(Message message, ActiveMQClientHeader key, Boolean defaultValue) {
        final Object value = getProperty(message, key);
        if (value == null) {
            return defaultValue;
        }
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return Boolean.valueOf(value.toString());
    }

    public static void setTraceId(Message message, String traceId) throws IOException {
        setProperty(message, ACTIVEMQ_TRACE_ID, traceId);
    }

    public static String getTraceId(Message message, String defaultValue) {
        return getString(message, ACTIVEMQ_TRACE_ID, defaultValue);
    }

    public static void setSpanId(Message message, Long spanId) throws IOException {
        setProperty(message, ACTIVEMQ_SPAN_ID, spanId);
    }

    public static Long getSpanId(Message message, Long defaultValue) {
        return getLong(message, ACTIVEMQ_SPAN_ID, defaultValue);
    }

    public static void setParentSpanId(Message message, Long parentSpanId) throws IOException {
        setProperty(message, ACTIVEMQ_PARENT_SPAN_ID, parentSpanId);
    }

    public static Long getParentSpanId(Message message, Long defaultValue) {
        return getLong(message, ACTIVEMQ_PARENT_SPAN_ID, defaultValue);
    }

    public static void setSampled(Message message, Boolean sampled) throws IOException {
        setProperty(message, ACTIVEMQ_SAMPLED, sampled);
    }

    public static Boolean getSampled(Message message, Boolean defaultValue) {
        return getBoolean(message, ACTIVEMQ_SAMPLED, defaultValue);
    }

    public static void setFlags(Message message, Short flags) throws IOException {
        setProperty(message, ACTIVEMQ_FLAGS, flags);
    }

    public static Short getFlags(Message message, Short defaultValue) {
        return getShort(message, ACTIVEMQ_FLAGS, defaultValue);
    }

    public static void setParentApplicationName(Message message, String parentApplicationName) throws IOException {
        setProperty(message, ACTIVEMQ_PARENT_APPLICATION_NAME, parentApplicationName);
    }

    public static String getParentApplicationName(Message message, String defaultValue) {
        return getString(message, ACTIVEMQ_PARENT_APPLICATION_NAME, defaultValue);
    }

    public static void setParentApplicationType(Message message, Short parentApplicationType) throws IOException {
        setProperty(message, ACTIVEMQ_PARENT_APPLICATION_TYPE, parentApplicationType);
    }

    public static Short getParentApplicationType(Message message, Short defaultValue) {
        return getShort(message, ACTIVEMQ_PARENT_APPLICATION_TYPE, defaultValue);
    }

    public static void setParentServiceName(Message message, String parentServiceName) throws IOException {
        setProperty(message, ACTIVEMQ_PARENT_SERVICE_NAME, parentServiceName);
    }

    public static String getParentServiceName(Message message, String defaultValue) {
        return getString(message, ACTIVEMQ_PARENT_SERVICE_NAME, defaultValue);
    }
}
