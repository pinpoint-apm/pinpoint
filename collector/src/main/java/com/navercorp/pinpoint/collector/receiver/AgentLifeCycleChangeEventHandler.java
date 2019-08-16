/*
 * Copyright 2019 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.collector.receiver;

import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.collector.service.async.AgentPropertyChannelAdaptor;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.ChannelProperties;
import com.navercorp.pinpoint.rpc.server.ChannelPropertiesFactory;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Map;

/**
 * @author HyunGil Jeong
 */
public class AgentLifeCycleChangeEventHandler extends ServerStateChangeEventHandler {

    public static final ManagedAgentLifeCycle STATE_NOT_MANAGED = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTaskService;

    @Autowired
    private AgentEventAsyncTaskService agentEventAsyncTaskService;

    @Autowired
    private ChannelPropertiesFactory channelPropertiesFactory;

    @Override
    public void stateUpdated(PinpointServer pinpointServer, SocketStateCode updatedStateCode) {
        ManagedAgentLifeCycle managedAgentLifeCycle = ManagedAgentLifeCycle.getManagedAgentLifeCycleByStateCode(updatedStateCode);
        if (managedAgentLifeCycle == STATE_NOT_MANAGED) {
            return;
        } else {
            logger.info("stateUpdated(). pinpointServer:{}, updatedStateCode:{}", pinpointServer, updatedStateCode);

            final long eventTimestamp = System.currentTimeMillis();

            final Map<Object, Object> channelPropertiesMap = pinpointServer.getChannelProperties();
            // nullable
            final ChannelProperties channelProperties = channelPropertiesFactory.newChannelProperties(channelPropertiesMap);
            if (channelProperties == null) {
                logger.debug("channelProperties is null {}", pinpointServer);
                return;
            }
            final AgentProperty agentProperty = new AgentPropertyChannelAdaptor(channelProperties);
            final AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
            final long eventIdentifier = AgentLifeCycleAsyncTaskService.createEventIdentifier(channelProperties.getSocketId(), managedAgentLifeCycle.getEventCounter());
            this.agentLifeCycleAsyncTaskService.handleLifeCycleEvent(agentProperty, eventTimestamp, agentLifeCycleState, eventIdentifier);
            AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
            this.agentEventAsyncTaskService.handleEvent(agentProperty, eventTimestamp, agentEventType);
        }
    }

}
