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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializer;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.io.request.Message;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.thrift.TBase;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;

import java.util.Map;
import java.util.Objects;
import java.util.Set;


/**
 * @author HyunGil Jeong
 */
public class AgentEventService {

    private static final Set<AgentEventType> RESPONSE_EVENT_TYPES = AgentEventType.getTypesByCategory(AgentEventTypeCategory.USER_REQUEST);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentEventDao agentEventDao;

    @Autowired
    private AgentEventMessageSerializer agentEventMessageSerializer;

    @Autowired
    @Qualifier("commandHeaderTBaseDeserializerFactory")
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    // sync method
    public void service(AgentEventBo agentEventBo) {
        Object eventMessage = getEventMessage(agentEventBo);
        insertEvent(agentEventBo, eventMessage);
    }

    @Async("agentEventWorker")
    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType) {
        handleEvent(pinpointServer, eventTimestamp, eventType, null);
    }

    // for test
    void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType, Object eventMessage) {
        Objects.requireNonNull(pinpointServer, "pinpointServer must not be null");
        Objects.requireNonNull(eventType, "pinpointServer must not be null");

        Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();
        if (MapUtils.isEmpty(channelProperties)) {
            // It can occurs CONNECTED -> RUN_WITHOUT_HANDSHAKE -> CLOSED(UNEXPECTED_CLOSE_BY_CLIENT, ERROR_UNKNOWN)
            logger.warn("maybe not yet received the handshake data - pinpointServer:{}", pinpointServer);
            return;
        }

        final String agentId = MapUtils.getString(channelProperties, HandshakePropertyType.AGENT_ID.getName());
        final long startTimestamp = MapUtils.getLong(channelProperties,
                HandshakePropertyType.START_TIMESTAMP.getName());

        AgentEventBo agentEventBo = newAgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        insertEvent(agentEventBo, eventMessage);
    }

    private AgentEventBo newAgentEventBo(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType) {
        return new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
    }


    private void insertEvent(AgentEventBo agentEventBo, Object eventMessage) {
        Objects.requireNonNull(agentEventBo, "agentEventBo must not be null");

        try {
            final byte[] eventBody = agentEventMessageSerializer.serialize(agentEventBo.getEventType(), eventMessage);
            agentEventBo.setEventBody(eventBody);
        } catch (Exception e) {
            logger.warn("error handling agent event", e);
            return;
        }
        logger.info("handle event: {}", agentEventBo);
        agentEventDao.insert(agentEventBo);
    }

    @Async("agentEventWorker")
    public void handleResponseEvent(ResponseEvent responseEvent, long eventTimestamp) {
        Objects.requireNonNull(responseEvent, "responseEvent must not be null");

        TCommandTransferResponse response = responseEvent.getRouteResult();
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
                AgentEventBo agentEventBo = new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
                agentEventBo.setEventBody(payload);
                agentEventDao.insert(agentEventBo);
            }
        }
    }

    private Class<?> readPayload(byte[] payload) {
        if (payload == null) {
            return Void.class;
        }

        try {
            Message<TBase<?, ?>> deserialize = SerializationUtils.deserialize(payload, commandDeserializerFactory);
            final TBase tBase = deserialize.getData();
            return tBase.getClass();
        } catch (TException e) {
            logger.warn("Error deserializing ResponseEvent payload", e);
        }
        return null;
    }

    private Object getEventMessage(AgentEventBo agentEventBo) {
        if (agentEventBo instanceof DeadlockEventBo) {
            return ((DeadlockEventBo) agentEventBo).getDeadlock();
        }
        return null;
    }

}
