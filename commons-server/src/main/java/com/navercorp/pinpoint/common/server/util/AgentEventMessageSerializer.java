/*
 * Copyright 2015 NAVER Corp.
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

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.thrift.io.SerializerFactory;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.UnsupportedEncodingException;
import java.util.List;

/**
 * @author HyunGil Jeong
 * @deprecated Only AgentEventBo.version is 0
 */
public class AgentEventMessageSerializer {

    private static final byte[] EMPTY_BYTES = new byte[0];

    private final List<SerializerFactory> serializerFactoryList;

    public AgentEventMessageSerializer(List<SerializerFactory> serializerFactoryList) {
        this.serializerFactoryList = serializerFactoryList;
    }

    public byte[] serialize(AgentEventType agentEventType, Object eventMessage) throws UnsupportedEncodingException {
        if (agentEventType == null) {
            throw new NullPointerException("agentEventType");
        }

        Class<?> eventMessageType = agentEventType.getMessageType();
        if (eventMessageType == Void.class) {
            return EMPTY_BYTES;
        } else {
            if (eventMessage == null) {
                throw new NullPointerException("eventMessage of type [" + eventMessageType.getName() + "] expected, but was null");
            }
        }

        if (!eventMessageType.isAssignableFrom(eventMessage.getClass())) {
            throw new IllegalArgumentException("Unexpected eventMessage of type [" + eventMessage.getClass().getName() + "] received. Expected : ["
                    + eventMessageType.getClass().getName() + "]");
        }

        if (eventMessage instanceof TBase) {
            for (SerializerFactory serializerFactory : serializerFactoryList) {
                if (serializerFactory.isSupport(eventMessage)) {
                    try {
                        return SerializationUtils.serialize((TBase<?, ?>) eventMessage, serializerFactory);
                    } catch (TException e) {
                        throw new UnsupportedEncodingException(e.getMessage());
                    }
                }
            }
        } else if (eventMessage instanceof String) {
            return BytesUtils.toBytes((String) eventMessage);
        }
        throw new UnsupportedEncodingException("Unsupported event message type [" + eventMessage.getClass().getName() + "]");
    }

}
