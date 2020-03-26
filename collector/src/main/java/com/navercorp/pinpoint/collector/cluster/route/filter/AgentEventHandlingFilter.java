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

package com.navercorp.pinpoint.collector.cluster.route.filter;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Set;

/**
 * @author HyunGil Jeong
 */
@Service
public class AgentEventHandlingFilter implements RouteFilter<ResponseEvent> {
    private static final Set<AgentEventType> RESPONSE_EVENT_TYPES = AgentEventType.getTypesByCategory(AgentEventTypeCategory.USER_REQUEST);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentEventService agentEventService;

    private final DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public AgentEventHandlingFilter(AgentEventService agentEventService,
                                    @Qualifier("commandHeaderTBaseDeserializerFactory") DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory) {
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        this.commandDeserializerFactory = Objects.requireNonNull(commandDeserializerFactory, "commandDeserializerFactory");
    }

    @Override
    public void doEvent(ResponseEvent event) {
        if (event == null) {
            return;
        }
        final long eventTimestamp = System.currentTimeMillis();
        handleResponseEvent(event, eventTimestamp);
    }

    @Async("agentEventWorker")
    public void handleResponseEvent(ResponseEvent responseEvent, long eventTimestamp) {
        Objects.requireNonNull(responseEvent, "responseEvent");
        if (logger.isDebugEnabled()) {
            logger.debug("Handle response event {}", responseEvent);
        }
        final TCommandTransferResponse response = responseEvent.getRouteResult();
        if (response.getRouteResult() != TRouteResult.OK) {
            return;
        }
        insertResponseEvent(responseEvent, eventTimestamp);
    }

    private void insertResponseEvent(ResponseEvent responseEvent, long eventTimestamp) {
        final TCommandTransfer command = responseEvent.getDeliveryCommand();
        final String agentId = command.getAgentId();
        final long startTimestamp = command.getStartTime();

        final TCommandTransferResponse response = responseEvent.getRouteResult();
        final byte[] payload = response.getPayload();

        final Class<?> payloadType = readPayload(payload);
        if (payload == null) {
            return;
        }

        for (AgentEventType eventType : RESPONSE_EVENT_TYPES) {
            if (eventType.getMessageType() == payloadType) {
                final AgentEventBo agentEventBo = new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
                agentEventBo.setEventBody(payload);
                this.agentEventService.insert(agentEventBo);
            }
        }
    }

    private Class<?> readPayload(byte[] payload) {
        if (payload == null) {
            return Void.class;
        }

        try {
            final Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(payload, commandDeserializerFactory);
            final TBase tBase = deserialize.getData();
            return tBase.getClass();
        } catch (TException e) {
            logger.warn("Error deserializing ResponseEvent payload", e);
        }
        return null;
    }
}