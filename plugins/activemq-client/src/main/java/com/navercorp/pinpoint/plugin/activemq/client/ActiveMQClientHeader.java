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

import org.apache.activemq.command.ActiveMQMessage;

import javax.jms.JMSException;
import javax.jms.Message;

/**
 * @author HyunGil Jeong
 */
public enum ActiveMQClientHeader {
    ACTIVEMQ_TRACE_ID("Pinpoint-TraceID"),
    ACTIVEMQ_SPAN_ID("Pinpoint-SpanID"),
    ACTIVEMQ_PARENT_SPAN_ID("Pinpoint-pSpanID"),
    ACTIVEMQ_SAMPLED("Pinpoint-Sampled"),
    ACTIVEMQ_FLAGS("Pinpoint-Flags"),
    ACTIVEMQ_PARENT_APPLICATION_NAME("Pinpoint-pAppName"),
    ACTIVEMQ_PARENT_APPLICATION_TYPE("Pinpoint-pAppType");

    private final String id;

    ActiveMQClientHeader(String id) {
        this.id = id;
    }

    public String getId() {
        return this.id;
    }

    private interface MessageHandler<T> {
        void setMessage(Message message, ActiveMQClientHeader key, T value) throws JMSException;

        T getMessage(Message message, ActiveMQClientHeader key, T defaultValue);
    }

    private static abstract class MessageHandlerBase<T> implements MessageHandler<T> {

        @Override
        public final void setMessage(Message message, ActiveMQClientHeader key, T value) throws JMSException {
            String id = key.id;
            if (message instanceof ActiveMQMessage) {
                ActiveMQMessage activeMQMessage = (ActiveMQMessage) message;
                if (activeMQMessage.isReadOnlyProperties()) {
                    activeMQMessage.setReadOnlyProperties(false);
                    setMessage0(message, id, value);
                    activeMQMessage.setReadOnlyProperties(true);
                    return;
                }
            }
            setMessage0(message, id, value);
        }

        @Override
        public final T getMessage(Message message, ActiveMQClientHeader key, T defaultValue) {
            String id = key.id;
            try {
                if (message.propertyExists(id)) {
                    return getMessage0(message, id);
                }
            } catch (JMSException e) {
                // just ignore and return default value
            }
            return defaultValue;
        }

        protected abstract void setMessage0(Message message, String id, T value) throws JMSException;

        protected abstract T getMessage0(Message message, String id) throws JMSException;
    }

    private static final MessageHandler<String> STRING_MESSAGE_HANDLER = new MessageHandlerBase<String>() {

        @Override
        protected void setMessage0(Message message, String id, String value) throws JMSException {
            message.setStringProperty(id, value);
        }

        @Override
        protected String getMessage0(Message message, String id) throws JMSException {
            return message.getStringProperty(id);
        }
    };

    private static final MessageHandler<Long> LONG_MESSAGE_HANDLER = new MessageHandlerBase<Long>() {

        @Override
        protected void setMessage0(Message message, String id, Long value) throws JMSException {
            message.setLongProperty(id, value);
        }

        @Override
        protected Long getMessage0(Message message, String id) throws JMSException {
            return message.getLongProperty(id);
        }
    };

    private static final MessageHandler<Short> SHORT_MESSAGE_HANDLER = new MessageHandlerBase<Short>() {

        @Override
        protected void setMessage0(Message message, String id, Short value) throws JMSException {
            message.setShortProperty(id, value);
        }

        @Override
        protected Short getMessage0(Message message, String id) throws JMSException {
            return message.getShortProperty(id);
        }
    };

    private static final MessageHandler<Boolean> BOOLEAN_MESSAGE_HANDLER = new MessageHandlerBase<Boolean>() {

        @Override
        protected void setMessage0(Message message, String id, Boolean value) throws JMSException {
            message.setBooleanProperty(id, value);
        }

        @Override
        protected Boolean getMessage0(Message message, String id) throws JMSException {
            return message.getBooleanProperty(id);
        }
    };

    public static void setTraceId(Message message, String traceId) throws JMSException {
        STRING_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_TRACE_ID, traceId);
    }

    public static String getTraceId(Message message, String defaultValue) {
        return STRING_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_TRACE_ID, defaultValue);
    }

    public static void setSpanId(Message message, Long spanId) throws JMSException {
        LONG_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_SPAN_ID, spanId);
    }

    public static Long getSpanId(Message message, Long defaultValue) {
        return LONG_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_SPAN_ID, defaultValue);
    }

    public static void setParentSpanId(Message message, Long parentSpanId) throws JMSException {
        LONG_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_PARENT_SPAN_ID, parentSpanId);
    }

    public static Long getParentSpanId(Message message, Long defaultValue) {
        return LONG_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_PARENT_SPAN_ID, defaultValue);
    }

    public static void setSampled(Message message, Boolean sampled) throws JMSException {
        BOOLEAN_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_SAMPLED, sampled);
    }

    public static Boolean getSampled(Message message, Boolean defaultValue) {
        return BOOLEAN_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_SAMPLED, defaultValue);
    }

    public static void setFlags(Message message, Short flags) throws JMSException {
        SHORT_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_FLAGS, flags);
    }

    public static Short getFlags(Message message, Short defaultValue) {
        return SHORT_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_FLAGS, defaultValue);
    }

    public static void setParentApplicationName(Message message, String parentApplicationName) throws JMSException {
        STRING_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_PARENT_APPLICATION_NAME, parentApplicationName);
    }

    public static String getParentApplicationName(Message message, String defaultValue) {
        return STRING_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_PARENT_APPLICATION_NAME, defaultValue);
    }

    public static void setParentApplicationType(Message message, Short parentApplicationType) throws JMSException {
        SHORT_MESSAGE_HANDLER.setMessage(message, ACTIVEMQ_PARENT_APPLICATION_TYPE, parentApplicationType);
    }

    public static Short getParentApplicationType(Message message, Short defaultValue) {
        return SHORT_MESSAGE_HANDLER.getMessage(message, ACTIVEMQ_PARENT_APPLICATION_TYPE, defaultValue);
    }

}
