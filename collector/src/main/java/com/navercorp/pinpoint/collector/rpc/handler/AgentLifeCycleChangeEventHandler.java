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

import com.navercorp.pinpoint.collector.service.AgentEventService;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.common.SocketStateCode;
import com.navercorp.pinpoint.rpc.server.PinpointServer;
import com.navercorp.pinpoint.rpc.server.handler.ServerStateChangeEventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author HyunGil Jeong
 */
public class AgentLifeCycleChangeEventHandler implements ServerStateChangeEventHandler {

    public static final ManagedAgentLifeCycle STATE_NOT_MANAGED = null;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentLifeCycleHandler agentLifeCycleHandler;

    @Autowired
    private AgentEventService agentEventService;

    @Override
    public void eventPerformed(PinpointServer pinpointServer, SocketStateCode stateCode) throws Exception {
        ManagedAgentLifeCycle managedAgentLifeCycle = ManagedAgentLifeCycle.getManagedAgentLifeCycleByStateCode(stateCode);
        if (managedAgentLifeCycle == STATE_NOT_MANAGED) {
            return;
        } else {
            logger.info("{} eventPerformed(). pinpointServer:{}, code:{}", this.getClass().getSimpleName(), pinpointServer, stateCode);
            
            long eventTimestamp = System.currentTimeMillis();

            AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
            this.agentLifeCycleHandler.handleLifeCycleEvent(pinpointServer, eventTimestamp, agentLifeCycleState, managedAgentLifeCycle.getEventCounter());

            AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
            this.agentEventService.handleEvent(pinpointServer, eventTimestamp, agentEventType);
        }
    }

    @Override
    public void exceptionCaught(PinpointServer pinpointServer, SocketStateCode stateCode, Throwable e) {
        logger.warn("{} exceptionCaught(). pinpointServer:{}, code:{}. error: {}.",
                this.getClass().getSimpleName(), pinpointServer, stateCode, e.getMessage(), e);
    }

}
