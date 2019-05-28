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

package com.navercorp.pinpoint.collector.receiver.grpc.service;

import com.navercorp.pinpoint.collector.service.async.AgentEventAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentLifeCycleAsyncTaskService;
import com.navercorp.pinpoint.collector.service.async.AgentProperty;
import com.navercorp.pinpoint.collector.service.async.DefaultAgentProperty;
import com.navercorp.pinpoint.collector.util.ManagedAgentLifeCycle;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.grpc.AgentHeaderFactory;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.server.lifecycle.Lifecycle;
import com.navercorp.pinpoint.grpc.server.lifecycle.LifecycleRegistry;
import com.navercorp.pinpoint.grpc.trace.KeepAliveGrpc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import javax.annotation.PreDestroy;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class KeepAliveService extends KeepAliveGrpc.KeepAliveImplBase {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final AgentEventAsyncTaskService agentEventAsyncTask;
    private final AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask;
    private final LifecycleRegistry lifecycleRegistry;

    @Autowired
    public KeepAliveService(AgentEventAsyncTaskService agentEventAsyncTask,
                            AgentLifeCycleAsyncTaskService agentLifeCycleAsyncTask,
                            LifecycleRegistry lifecycleRegistry) {
        this.agentEventAsyncTask = Objects.requireNonNull(agentEventAsyncTask, "agentEventAsyncTask must not be null");
        this.agentLifeCycleAsyncTask = Objects.requireNonNull(agentLifeCycleAsyncTask, "agentLifeCycleAsyncTask must not be null");
        this.lifecycleRegistry = Objects.requireNonNull(lifecycleRegistry, "lifecycleRegistry must not be null");
    }


    public void updateState() {
        Collection<Lifecycle> lifecycles = lifecycleRegistry.values();
        for (Lifecycle lifecycle : lifecycles) {
            final AgentHeaderFactory.Header header = lifecycle.getRef();
            if (header != null) {
                logger.debug("updateState:{}", lifecycle);
                final TransportMetadata transportMetadata = lifecycle.getTransportMetadata();

                final long connectTime = transportMetadata.getConnectTime();
                final long pingTimestamp = System.currentTimeMillis();
                final long eventCount = transportMetadata.nextEventCount();
                final AgentProperty channelProperties = newChannelProperties(header);
                try {
                    if (!(eventCount < 0)) {
                        agentLifeCycleAsyncTask.handleLifeCycleEvent(channelProperties, pingTimestamp, AgentLifeCycleState.RUNNING, connectTime);
                    }
                    agentEventAsyncTask.handleEvent(channelProperties, pingTimestamp, AgentEventType.AGENT_PING);
                } catch (Exception e) {
                    logger.warn("Error handling client ping event", e);
                }
            }
        }
    }

    private AgentProperty newChannelProperties(AgentHeaderFactory.Header header) {
        final String agentId = header.getAgentId();
        final long agentStartTime = header.getAgentStartTime();
        return new DefaultAgentProperty(agentId, agentStartTime, Collections.emptyMap());
    }

    public void updateState(Lifecycle lifecycle, ManagedAgentLifeCycle managedAgentLifeCycle) {

        final AgentHeaderFactory.Header header = lifecycle.getRef();
        if (header == null) {
            logger.warn("Not found request header");
            return;
        }
        final TransportMetadata transportMetadata = lifecycle.getTransportMetadata();

        final long pingTimestamp = System.currentTimeMillis();
        final AgentProperty agentProperty = newChannelProperties(header);

        AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
        AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
        final long eventIdentifier = getEventIdentifier(managedAgentLifeCycle, transportMetadata);

        try {
            this.agentLifeCycleAsyncTask.handleLifeCycleEvent(agentProperty , pingTimestamp, agentLifeCycleState, eventIdentifier);
            this.agentEventAsyncTask.handleEvent(agentProperty, pingTimestamp, agentEventType);
        } catch (Exception e) {
            logger.warn("Failed to update state. header={}, lifeCycle={}", header, managedAgentLifeCycle);
        }
    }

//    @PreDestroy
    public void destroy() {
        final Collection<Lifecycle> lifecycles = lifecycleRegistry.values();
        for (Lifecycle lifecycle : lifecycles) {
            updateState(lifecycle, ManagedAgentLifeCycle.CLOSED_BY_SERVER);
        }
    }

    private static final int CLOSE_MASK = 1;
    private long getEventIdentifier(ManagedAgentLifeCycle managedAgentLifeCycle, TransportMetadata transportMetadata) {
        if (ManagedAgentLifeCycle.isClosedEvent(managedAgentLifeCycle)) {
            return transportMetadata.getConnectTime() + CLOSE_MASK;
        }
        return transportMetadata.getConnectTime();
    }
}