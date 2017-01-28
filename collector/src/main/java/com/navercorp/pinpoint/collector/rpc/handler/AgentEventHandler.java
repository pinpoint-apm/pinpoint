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

package com.navercorp.pinpoint.collector.rpc.handler;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.bo.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventMessageSerializer;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentEventTypeCategory;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransfer;
import com.navercorp.pinpoint.thrift.dto.command.TCommandTransferResponse;
import com.navercorp.pinpoint.thrift.dto.command.TRouteResult;
import com.navercorp.pinpoint.thrift.io.DeserializerFactory;
import com.navercorp.pinpoint.thrift.io.HeaderTBaseDeserializer;
import com.navercorp.pinpoint.thrift.util.SerializationUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.thrift.TException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;

/**
 * @author HyunGil Jeong
 */
public class AgentEventHandler {

    private static final Set<AgentEventType> RESPONSE_EVENT_TYPES = AgentEventType
            .getTypesByCatgory(AgentEventTypeCategory.USER_REQUEST);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "agentEventWorker")
    private Executor executor;

    @Resource
    private AgentEventDao agentEventDao;

    @Resource
    private AgentEventMessageSerializer agentEventMessageSerializer;

    @Resource
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType) {
        handleEvent(pinpointServer, eventTimestamp, eventType, null);
    }

    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType,
            Object eventMessage) {
        if (pinpointServer == null) {
            throw new NullPointerException("pinpointServer may not be null");
        }
        if (eventType == null) {
            throw new NullPointerException("eventType may not be null");
        }

        Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();

        final String agentId = MapUtils.getString(channelProperties, HandshakePropertyType.AGENT_ID.getName());
        final long startTimestamp = MapUtils.getLong(channelProperties,
                HandshakePropertyType.START_TIMESTAMP.getName());

        this.executor.execute(new AgentEventHandlerDispatch(agentId, startTimestamp, eventTimestamp, eventType,
                eventMessage));
    }

    public void handleResponseEvent(ResponseEvent responseEvent, long eventTimestamp) {
        if (responseEvent == null) {
            throw new NullPointerException("responseEvent may not be null");
        }
        TCommandTransferResponse response = responseEvent.getRouteResult();
        if (response.getRouteResult() != TRouteResult.OK) {
            return;
        }
        this.executor.execute(new AgentResponseEventHandlerDispatch(responseEvent, eventTimestamp));
    }

    private class AgentEventHandlerDispatch implements Runnable {
        private final String agentId;
        private final long startTimestamp;
        private final long eventTimestamp;
        private final AgentEventType eventType;
        private final Object eventMessage;

        private AgentEventHandlerDispatch(String agentId, long startTimestamp, long eventTimestamp,
                AgentEventType eventType, Object eventMessage) {
            this.agentId = agentId;
            this.startTimestamp = startTimestamp;
            this.eventTimestamp = eventTimestamp;
            this.eventType = eventType;
            this.eventMessage = eventMessage;
        }

        @Override
        public void run() {
            AgentEventBo event = new AgentEventBo(this.agentId, this.startTimestamp,
                    this.eventTimestamp, this.eventType);
            try {
                byte[] eventBody = agentEventMessageSerializer.serialize(this.eventType, this.eventMessage);
                event.setEventBody(eventBody);
            } catch (Exception e) {
                logger.warn("error handling agent event", e);
                return;
            }
            logger.info("handle event: {}", event);
            agentEventDao.insert(event);
        }

    }

    private class AgentResponseEventHandlerDispatch implements Runnable {
        private final String agentId;
        private final long startTimestamp;
        private final long eventTimestamp;
        private final byte[] payload;

        private AgentResponseEventHandlerDispatch(ResponseEvent responseEvent, long eventTimestamp) {
            final TCommandTransfer command = responseEvent.getDeliveryCommand();
            this.agentId = command.getAgentId();
            this.startTimestamp = command.getStartTime();
            this.eventTimestamp = eventTimestamp;
            final TCommandTransferResponse response = responseEvent.getRouteResult();
            this.payload = response.getPayload();
        }

        @Override
        public void run() {
            Class<?> payloadType = Void.class;
            if (this.payload != null) {
                try {
                    payloadType = SerializationUtils.deserialize(this.payload, commandDeserializerFactory).getClass();
                } catch (TException e) {
                    logger.warn("Error deserializing ResponseEvent payload", e);
                    return;
                }
            }
            for (AgentEventType eventType : RESPONSE_EVENT_TYPES) {
                if (eventType.getMessageType() == payloadType) {
                    AgentEventBo agentEventBo = new AgentEventBo(this.agentId, this.startTimestamp,
                            this.eventTimestamp, eventType);
                    agentEventBo.setEventBody(this.payload);
                    agentEventDao.insert(agentEventBo);
                }
            }
        }
    }

}
