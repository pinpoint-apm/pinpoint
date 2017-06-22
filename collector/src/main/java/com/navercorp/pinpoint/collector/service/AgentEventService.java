/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.collector.service;

import com.navercorp.pinpoint.collector.cluster.route.ResponseEvent;
import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.bo.event.DeadlockEventBo;
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
public class AgentEventService {

    private static final Set<AgentEventType> RESPONSE_EVENT_TYPES = AgentEventType.getTypesByCategory(AgentEventTypeCategory.USER_REQUEST);

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name = "agentEventWorker")
    private Executor executor;

    @Resource
    private AgentEventDao agentEventDao;

    @Resource
    private AgentEventMessageSerializer agentEventMessageSerializer;

    @Resource
    private DeserializerFactory<HeaderTBaseDeserializer> commandDeserializerFactory;

    public void service(AgentEventBo agentEventBo) {
        this.executor.execute(new AgentEventHandlerDispatch(agentEventBo));
    }

    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType) {
        handleEvent(pinpointServer, eventTimestamp, eventType, null);
    }

    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType, Object eventMessage) {
        if (pinpointServer == null) {
            throw new NullPointerException("pinpointServer must not be null");
        }
        if (eventType == null) {
            throw new NullPointerException("eventType must not be null");
        }

        Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();

        final String agentId = MapUtils.getString(channelProperties, HandshakePropertyType.AGENT_ID.getName());
        final long startTimestamp = MapUtils.getLong(channelProperties,
                HandshakePropertyType.START_TIMESTAMP.getName());

        handleEvent(agentId, startTimestamp, eventTimestamp, eventType, eventMessage);
    }

    public void handleEvent(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType, Object eventMessage) {
        AgentEventBo agentEventBo = new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        this.executor.execute(new AgentEventHandlerDispatch(agentEventBo, eventMessage));
    }

    public void handleResponseEvent(ResponseEvent responseEvent, long eventTimestamp) {
        if (responseEvent == null) {
            throw new NullPointerException("responseEvent must not be null");
        }
        TCommandTransferResponse response = responseEvent.getRouteResult();
        if (response.getRouteResult() != TRouteResult.OK) {
            return;
        }
        this.executor.execute(new AgentResponseEventHandlerDispatch(responseEvent, eventTimestamp));
    }

    private class AgentEventHandlerDispatch implements Runnable {

        private final AgentEventBo agentEventBo;
        private final Object agentEventMessage;

        private AgentEventHandlerDispatch(AgentEventBo agentEventBo) {
            this.agentEventBo = agentEventBo;
            if (agentEventBo instanceof DeadlockEventBo) {
                this.agentEventMessage = ((DeadlockEventBo) agentEventBo).getDeadlock();
            } else {
                agentEventMessage = null;
            }
        }

        private AgentEventHandlerDispatch(AgentEventBo agentEventBo, Object agentEventMessage) {
            this.agentEventBo = agentEventBo;
            this.agentEventMessage = agentEventMessage;
        }

        @Override
        public void run() {
            try {
                byte[] eventBody = agentEventMessageSerializer.serialize(agentEventBo.getEventType(), agentEventMessage);
                agentEventBo.setEventBody(eventBody);
            } catch (Exception e) {
                logger.warn("error handling agent event", e);
                return;
            }
            logger.info("handle event: {}", agentEventBo);
            agentEventDao.insert(agentEventBo);
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
