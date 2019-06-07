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
import com.navercorp.pinpoint.grpc.server.LastAccessTime;
import com.navercorp.pinpoint.grpc.server.TransportMetadata;
import com.navercorp.pinpoint.grpc.server.lifecycle.Lifecycle;
import com.navercorp.pinpoint.grpc.server.lifecycle.LifecycleRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collection;
import java.util.Collections;
import java.util.Objects;

public class KeepAliveService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final int CLOSE_MASK = 1;

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
        final Collection<Lifecycle> lifecycles = lifecycleRegistry.values();
        for (Lifecycle lifecycle : lifecycles) {
            boolean closeState = false;
            AgentLifeCycleState agentLifeCycleState = AgentLifeCycleState.RUNNING;
            AgentEventType agentEventType = AgentEventType.AGENT_PING;
            updateState(lifecycle, closeState, agentLifeCycleState, agentEventType);
        }
    }

    private AgentProperty newChannelProperties(AgentHeaderFactory.Header header) {
        final String agentId = header.getAgentId();
        final long agentStartTime = header.getAgentStartTime();
        return new DefaultAgentProperty(agentId, agentStartTime, Collections.emptyMap());
    }

    public void updateState(Lifecycle lifecycle, ManagedAgentLifeCycle managedAgentLifeCycle) {

        boolean closeState = managedAgentLifeCycle.isClosedEvent();
        AgentLifeCycleState agentLifeCycleState = managedAgentLifeCycle.getMappedState();
        AgentEventType agentEventType = managedAgentLifeCycle.getMappedEvent();
        updateState(lifecycle, closeState, agentLifeCycleState, agentEventType);
    }

    public void updateState(Lifecycle lifecycle, boolean closeState, AgentLifeCycleState agentLifeCycleState, AgentEventType agentEventType) {

        final AgentHeaderFactory.Header header = lifecycle.getRef();
        if (header == null) {
            logger.warn("Not found request header");
            return;
        }

        logger.debug("updateState:{} {} {}/{}", lifecycle, closeState, agentLifeCycleState, agentEventType);
        final TransportMetadata transportMetadata = lifecycle.getTransportMetadata();

        final long pingTimestamp = System.currentTimeMillis();
        final LastAccessTime lastAccessTime = transportMetadata.getLastAccessTime();


        final long eventIdentifier = getEventIdentifier(lastAccessTime, pingTimestamp, closeState);
        if (eventIdentifier == -1) {
            // skip
            return;
        }
        final AgentProperty agentProperty = newChannelProperties(header);

        try {
            this.agentLifeCycleAsyncTask.handleLifeCycleEvent(agentProperty , pingTimestamp, agentLifeCycleState, eventIdentifier);
            this.agentEventAsyncTask.handleEvent(agentProperty, pingTimestamp, agentEventType);
        } catch (Exception e) {
            logger.warn("Failed to update state. closeState:{} lifeCycle={} {}/{}", lifecycle, closeState, agentLifeCycleState, agentEventType);
        }
    }

    private long getEventIdentifier(LastAccessTime lastAccessTime, long eventTime, boolean closeState) {
        synchronized (lastAccessTime) {
            if (closeState) {
                if (lastAccessTime.expire(eventTime)) {
                    return eventTime + CLOSE_MASK;
                } else {
                    // event runs after expire.
                    return -1;
                }
            } else {
                if (lastAccessTime.isExpire()) {
                    return -1;
                }
                lastAccessTime.update(eventTime);
                return eventTime;
            }
        }
    }

//    @PreDestroy
    public void destroy() {
        final Collection<Lifecycle> lifecycles = lifecycleRegistry.values();
        for (Lifecycle lifecycle : lifecycles) {
            updateState(lifecycle, ManagedAgentLifeCycle.CLOSED_BY_SERVER);
        }
    }

}