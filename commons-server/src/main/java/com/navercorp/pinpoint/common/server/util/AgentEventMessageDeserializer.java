/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.server.util;

import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;

import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.List;

/**
 * @author HyunGil Jeong
 * @deprecated Only AgentEventBo.version is 0
 */
public class AgentEventMessageDeserializer {

    private final List<DeserializerFactory<HeaderTBaseDeserializer>> deserializerFactoryList;

    public AgentEventMessageDeserializer(DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory) {
        this.deserializerFactoryList = Collections.singletonList(deserializerFactory);
    }

    public AgentEventMessageDeserializer(List<DeserializerFactory<HeaderTBaseDeserializer>> deserializerFactoryList) {
        this.deserializerFactoryList = deserializerFactoryList;
    }

    public Object deserialize(AgentEventType agentEventType, byte[] eventBody) throws UnsupportedEncodingException {
        if (agentEventType == null) {
            throw new NullPointerException("agentEventType");
        }
        Class<?> eventMessageType = agentEventType.getMessageType();
        if (eventMessageType == Void.class) {
            return null;
        }
        if (TBase.class.isAssignableFrom(eventMessageType)) {
            for (DeserializerFactory<HeaderTBaseDeserializer> deserializerFactory : deserializerFactoryList) {
                try {
                    Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(eventBody, deserializerFactory);
                    return deserialize.getData();
                } catch (TException e) {
                    // ignore
                }
            }
        } else if (String.class.isAssignableFrom(eventMessageType)) {
            return BytesUtils.toString(eventBody);
        }
        throw new UnsupportedEncodingException("Unsupported event message type [" + eventMessageType.getName() + "]");
    }

}
