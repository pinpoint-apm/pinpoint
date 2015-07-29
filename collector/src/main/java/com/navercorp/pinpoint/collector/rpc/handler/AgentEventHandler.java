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

import java.util.Map;
import java.util.concurrent.Executor;

import javax.annotation.Resource;

import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.collector.dao.AgentEventDao;
import com.navercorp.pinpoint.collector.receiver.tcp.AgentHandshakePropertyType;
import com.navercorp.pinpoint.common.bo.AgentEventBo;
import com.navercorp.pinpoint.common.util.AgentEventMessageSerializer;
import com.navercorp.pinpoint.common.util.AgentEventType;
import com.navercorp.pinpoint.rpc.server.PinpointServer;

/**
 * @author HyunGil Jeong
 */
public class AgentEventHandler {

    private static final byte[] EMPTY_BODY = new byte[0]; 

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource(name="agentEventWorker")
    private Executor executor;

    @Resource
    private AgentEventDao agentEventDao;

    @Resource
    private AgentEventMessageSerializer agentEventMessageSerializer;
    
    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType) {
        handleEvent(pinpointServer, eventTimestamp, eventType, EMPTY_BODY);
    }

    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType, Object eventMessage) {
        try {
            byte[] eventBody = this.agentEventMessageSerializer.serialize(eventType, eventMessage);
            handleEvent(pinpointServer, eventTimestamp, eventType, eventBody);
        } catch (Exception e) {
            logger.warn("error serializing event message", e);
            handleEvent(pinpointServer, eventTimestamp, eventType, EMPTY_BODY);
        }
    }
    
    public void handleEvent(PinpointServer pinpointServer, long eventTimestamp, AgentEventType eventType, byte[] eventBody) {
        if (pinpointServer == null) {
            throw new IllegalArgumentException("pinpointServer cannot be null");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("eventType cannot be null");
        }

        Map<Object, Object> channelProperties = pinpointServer.getChannelProperties();

        final String agentId = MapUtils.getString(channelProperties, AgentHandshakePropertyType.AGENT_ID.getName());
        final long startTimestamp = MapUtils.getLong(channelProperties, AgentHandshakePropertyType.START_TIMESTAMP.getName());

        final AgentEventBo agentEventBo = new AgentEventBo(agentId, startTimestamp, eventTimestamp, eventType);
        if (eventBody == null) {
            agentEventBo.setEventBody(EMPTY_BODY);
        } else {
            agentEventBo.setEventBody(eventBody);
        }
        
        logger.info("handle event - pinpointServer:{}, event:{}", pinpointServer, agentEventBo);

        this.executor.execute(new AgentEventHandlerDispatch(agentEventBo));
    }

    class AgentEventHandlerDispatch implements Runnable {
        private final AgentEventBo agentEventBo;

        private AgentEventHandlerDispatch(AgentEventBo agentEventBo) {
            if (agentEventBo == null) {
                throw new IllegalArgumentException("agentEventBo cannot be null");
            }
            this.agentEventBo = agentEventBo;
        }

        @Override
        public void run() {
            agentEventDao.insert(this.agentEventBo);
        }
    }

}
