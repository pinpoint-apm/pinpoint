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

package com.navercorp.pinpoint.collector.service.async;

import com.navercorp.pinpoint.collector.config.CollectorProperties;
import com.navercorp.pinpoint.collector.service.AgentLifeCycleService;
import com.navercorp.pinpoint.collector.service.StatisticsService;
import com.navercorp.pinpoint.common.server.bo.AgentLifeCycleBo;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.common.util.BytesUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Service
public class AgentLifeCycleAsyncTaskService {
    private static final int INTEGER_BIT_COUNT = BytesUtils.INT_BYTE_LENGTH * 8;

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentLifeCycleService agentLifeCycleService;
    private final StatisticsService statisticsService;
    private final ServiceTypeRegistryService registry;
    private final CollectorProperties collectorProperties;

    public AgentLifeCycleAsyncTaskService(AgentLifeCycleService agentLifeCycleService,
                                          StatisticsService statisticsService,
                                          ServiceTypeRegistryService registry,
                                          CollectorProperties collectorProperties) {
        this.agentLifeCycleService = agentLifeCycleService;
        this.statisticsService = statisticsService;
        this.registry = registry;
        this.collectorProperties = collectorProperties;
    }

    @Async("agentEventWorker")
    public void handleLifeCycleEvent(AgentProperty agentProperty, long eventTimestamp, AgentLifeCycleState agentLifeCycleState, long eventIdentifier) {
        Objects.requireNonNull(agentProperty, "agentProperty");
        Objects.requireNonNull(agentLifeCycleState, "agentLifeCycleState");

        final String agentId = agentProperty.getAgentId();
        final String applicationName = agentProperty.getApplicationName();

        final long startTimestamp = agentProperty.getStartTime();
        final AgentLifeCycleBo agentLifeCycleBo = new AgentLifeCycleBo(agentId, startTimestamp, eventTimestamp, eventIdentifier, agentLifeCycleState);
        agentLifeCycleService.insert(agentLifeCycleBo);

        updateAgentState(agentProperty.getServiceType(), eventTimestamp, applicationName, agentId);
    }

    @Async("agentEventWorker")
    public void handlePingEvent(AgentProperty agentProperty, long eventTimestamp) {
        Objects.requireNonNull(agentProperty, "agentProperty");

        final String agentId = agentProperty.getAgentId();
        final String applicationName = agentProperty.getApplicationName();

        updateAgentState(agentProperty.getServiceType(), eventTimestamp, applicationName, agentId);
    }

    private void updateAgentState(int serviceTypeCode, long eventTimestamp, String applicationName, String agentId) {
        final ServiceType serviceType = registry.findServiceType((short) serviceTypeCode);
        if (isUpdateAgentState(serviceType)) {
            statisticsService.updateAgentState(eventTimestamp, applicationName, serviceType, agentId);
        }
    }

    private boolean isUpdateAgentState(ServiceType serviceType) {
        if (!collectorProperties.isStatisticsAgentStateEnable()) {
            return false;
        }
        if (serviceType == null || serviceType == ServiceType.UNDEFINED) {
            return false;
        }
        return true;
    }

    public static long createEventIdentifier(int socketId, int eventCounter) {
        if (socketId < 0) {
            throw new IllegalArgumentException("socketId may not be less than 0");
        }
        if (eventCounter < 0) {
            throw new IllegalArgumentException("eventCounter may not be less than 0");
        }
        return ((long) socketId << INTEGER_BIT_COUNT) | eventCounter;
    }
}