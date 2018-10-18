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

import com.navercorp.pinpoint.collector.service.AgentLifeCycleService;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.util.BytesUtils;
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
 */
@Service
public class AgentLifeCycleEventHandler {

    public static final String SOCKET_ID_KEY = "socketId";

    private static final int INTEGER_BIT_COUNT = BytesUtils.INT_BYTE_LENGTH * 8;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentLifeCycleService agentLifeCycleService;

    @Async("agentEventWorker")
    public void handleLifeCycleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentLifeCycleState agentLifeCycleState, int eventCounter) {
        Objects.requireNonNull(pinpointServer, "pinpointServer must not be null");
        Objects.requireNonNull(agentLifeCycleState, "agentLifeCycleState must not be null");
        if (eventCounter < 0) {
            throw new IllegalArgumentException("eventCounter may not be negative");
        }
        logger.info("Handle lifecycle event - pinpointServer:{}, state:{}", pinpointServer, agentLifeCycleState);

        // TODO
        Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();
        final Integer socketId = MapUtils.getInteger(channelProperties, SOCKET_ID_KEY);
        if (socketId == null) {
            logger.debug("socketId not found, agent does not support life cycle management - pinpointServer:{}",
                    pinpointServer);
            return;
        }

        final String agentId = MapUtils.getString(channelProperties, HandshakePropertyType.AGENT_ID.getName());
        final long startTimestamp = MapUtils.getLong(channelProperties, HandshakePropertyType.START_TIMESTAMP.getName());
        final long eventIdentifier = createEventIdentifier(socketId, eventCounter);
        final AgentLifeCycleBo agentLifeCycleBo = new AgentLifeCycleBo(agentId, startTimestamp, eventTimestamp,
                eventIdentifier, agentLifeCycleState);

        agentLifeCycleService.insert(agentLifeCycleBo);
    }

    long createEventIdentifier(int socketId, int eventCounter) {
        if (socketId < 0) {
            throw new IllegalArgumentException("socketId may not be less than 0");
        }
        if (eventCounter < 0) {
            throw new IllegalArgumentException("eventCounter may not be less than 0");
        }
        return ((long) socketId << INTEGER_BIT_COUNT) | eventCounter;
    }
}