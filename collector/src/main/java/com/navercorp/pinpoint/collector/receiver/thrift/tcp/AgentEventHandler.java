/*
 * Copyright 2018 NAVER Corp.
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

package com.navercorp.pinpoint.collector.receiver.thrift.tcp;

import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.common.server.bo.event.AgentEventBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 * @author jaehong.kim - Remove AgentEventMessageSerializer
 */
@Service
public class AgentEventHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentEventService agentEventService;

    @Async("agentEventWorker")
    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType) {
        Objects.requireNonNull(pinpointServer, "pinpointServer must not be null");
        Objects.requireNonNull(eventType, "pinpointServer must not be null");

        final Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();
        if (MapUtils.isEmpty(channelProperties)) {
            // It can occurs CONNECTED -> RUN_WITHOUT_HANDSHAKE -> CLOSED(UNEXPECTED_CLOSE_BY_CLIENT, ERROR_UNKNOWN)
            logger.warn("maybe not yet received the handshake data - pinpointServer:{}", pinpointServer);
            return;
        }

        final String agentId = MapUtils.getString(channelProperties, HandshakePropertyType.AGENT_ID.getName());
        final long startTimestamp = MapUtils.getLong(channelProperties, HandshakePropertyType.START_TIMESTAMP.getName());
        final AgentEventBo agentEventBo = newAgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        this.agentEventService.insert(agentEventBo);
    }

    private AgentEventBo newAgentEventBo(String agentId, long startTimestamp, long eventTimestamp, AgentEventType eventType) {
        final AgentEventBo agentEventBo = new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        agentEventBo.setEventBody(new byte[0]);
        return agentEventBo;
    }
}