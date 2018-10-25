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

package com.navercorp.pinpoint.collector.receiver.grpc;

import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.rpc.packet.HandshakePropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.HashMap;
import java.util.Map;

public class GrpcAgentLifeCycleChangeEventHandler {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask;
    @Autowired
    private AgentEventAsyncTaskService agentEventAsyncTask;

    public void open(final String agentId, final long startTimestamp, final int transportId) {
        ManagedAgentLifeCycle managedAgentLifeCycle = ManagedAgentLifeCycle.RUNNING;

        long eventTimestamp = System.currentTimeMillis();
        Map<Object, Object> properties = new HashMap<>();
        properties.put("socketId", 1);
        properties.put(HandshakePropertyType.AGENT_ID.getName(), "");
        properties.put(HandshakePropertyType.START_TIMESTAMP.getName(), 1L);
        properties.put("licenseKey", "");

        AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
        this.agentLifeCycleAsyncTask.handleLifeCycleEvent(properties, eventTimestamp, agentLifeCycleState, managedAgentLifeCycle.getEventCounter());
        AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
        this.agentEventAsyncTask.handleEvent(properties, eventTimestamp, agentEventType);
    }
}
