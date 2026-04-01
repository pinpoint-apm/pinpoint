/*
 * Copyright 2026 NAVER Corp.
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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.config.AgentProperties;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class AgentListV2ServiceImpl implements AgentListV2Service {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentProperties agentProperties;

    private final AgentIdDao agentIdDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    public AgentListV2ServiceImpl(AgentProperties agentProperties, AgentIdDao agentIdDao, MapAgentResponseDao mapAgentResponseDao) {
        this.agentProperties = Objects.requireNonNull(agentProperties, "agentProperties");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
    }

    @Override
    public List<AgentIdEntry> getAllAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid.getUid(), applicationName, serviceType.getCode());
        return dedupeConsecutiveAgentId(agentIdEntryList);
    }

    @Override
    public List<AgentIdEntry> getActiveAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        if (agentProperties.getStatisticsCheckServiceTypeCodes().contains((int) serviceType.getCode())) {
            return getActiveAgentListByStatistics(serviceUid, applicationName, serviceType, range);
        }
        return getActiveAgentListByStatus(serviceUid, applicationName, serviceType, range);
    }

    /**
     * For service types that may not send steady pings — fetch all entries, then filter by span statistics.
     */
    private List<AgentIdEntry> getActiveAgentListByStatistics(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid.getUid(), applicationName, serviceType.getCode());
        agentIdEntryList = filterByAgentStartTime(agentIdEntryList, range);
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);

        // TODO use serviceUid to create Application
        Application searchApplication = new Application(applicationName, serviceType);
        Set<String> statisticsAgentIds = mapAgentResponseDao.selectAgentIds(searchApplication, new TimeWindow(range));
        return agentIdEntryList.stream()
                .filter(entry -> !isInactiveCandidate(entry, range.getFrom()) || statisticsAgentIds.contains(entry.getAgentId()))
                .toList();
    }

    /**
     * For service types that send pings — filter by status timestamp.
     */
    private List<AgentIdEntry> getActiveAgentListByStatus(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntryByMinStateTimestamp(serviceUid.getUid(), applicationName, serviceType.getCode(), range.getFrom());
        agentIdEntryList = filterByAgentStartTime(agentIdEntryList, range);
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);
        return agentIdEntryList;
    }

    private List<AgentIdEntry> filterByAgentStartTime(List<AgentIdEntry> entries, Range range) {
        return entries.stream()
                .filter(entry -> entry.getAgentStartTime() <= range.getTo())
                .toList();
    }

    private boolean isInactiveCandidate(AgentIdEntry entry, long from) {
        return entry.getAgentStartTime() < from
                && entry.getCurrentStateTimestamp() < from;
    }

    private List<AgentIdEntry> dedupeConsecutiveAgentId(List<AgentIdEntry> orderedAgentIdEntryList) {
        List<AgentIdEntry> result = new ArrayList<>();
        AgentIdEntry previous = null;
        for (AgentIdEntry current : orderedAgentIdEntryList) {
            if (!isSameAgentId(previous, current)) {
                result.add(current);
                previous = current;
            }
        }
        logger.debug("dedupeConsecutiveAgentId input={}, result={}", orderedAgentIdEntryList.size(), result.size());
        return result;
    }

    private static boolean isSameAgentId(AgentIdEntry previous, AgentIdEntry current) {
        return previous != null &&
                previous.getAgentId().equals(current.getAgentId()) &&
                previous.getApplication().equals(current.getApplication());
    }
}