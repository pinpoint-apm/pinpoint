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

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class AgentListV2ServiceImpl implements AgentListV2Service {
    private static final int UNDEFINED_SERVICE_TYPE_CODE = -1;

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
        List<AgentIdEntry> agentIdEntryList = queryAllEntries(serviceUid.getUid(), applicationName, serviceType.getCode());
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
        List<AgentIdEntry> agentIdEntryList = queryAllEntries(serviceUid.getUid(), applicationName, serviceType.getCode());
        agentIdEntryList = filterByAgentStartTime(agentIdEntryList, range);
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);

        // TODO use serviceUid to create Application
        Application searchApplication = new Application(applicationName, serviceType);
        Set<String> statisticsAgentIds = mapAgentResponseDao.selectAgentIds(searchApplication, new TimeWindow(range));
        return agentIdEntryList.stream()
                .filter(entry -> !isInactiveCandidate(entry, range) || statisticsAgentIds.contains(entry.getAgentId()))
                .toList();
    }

    /**
     * For service types that send pings — filter by status timestamp.
     */
    private List<AgentIdEntry> getActiveAgentListByStatus(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList = queryByMinStateTimestamp(serviceUid.getUid(), applicationName, serviceType.getCode(), range);
        agentIdEntryList = filterByAgentStartTime(agentIdEntryList, range);
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);
        return agentIdEntryList;
    }

    private List<AgentIdEntry> queryAllEntries(int serviceUid, String applicationName, int serviceTypeCode) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, serviceTypeCode);
        if (!agentProperties.getMissingHeaderServiceTypeCodes().contains(serviceTypeCode)) {
            return entries;
        }
        List<AgentIdEntry> undefinedEntries = agentIdDao.getAgentIdEntry(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE);
        return dedupeByKey(entries, undefinedEntries);
    }

    private List<AgentIdEntry> queryByMinStateTimestamp(int serviceUid, String applicationName, int serviceTypeCode, Range range) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntryByMinStateTimestamp(serviceUid, applicationName, serviceTypeCode, range.getFrom());
        if (!agentProperties.getMissingHeaderServiceTypeCodes().contains(serviceTypeCode)) {
            return entries;
        }
        List<AgentIdEntry> undefinedEntries = agentIdDao.getAgentIdEntryByMinStateTimestamp(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE, range.getFrom());
        return dedupeByKey(entries, undefinedEntries);
    }

    private List<AgentIdEntry> filterByAgentStartTime(List<AgentIdEntry> entries, Range range) {
        return entries.stream()
                .filter(entry -> entry.getAgentStartTime() <= range.getTo())
                .toList();
    }

    @Override
    public List<AgentIdEntry> getInactiveAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range activeRange) {
        List<AgentIdEntry> agentIdEntryList = queryByMaxStateTimestamp(serviceUid.getUid(), applicationName, serviceType.getCode(), activeRange.getFrom());
        // TODO use serviceUid to create Application
        Application application = new Application(applicationName, serviceType);
        return getInactiveAgentList(application, agentIdEntryList, activeRange);
    }

    private List<AgentIdEntry> queryByMaxStateTimestamp(int serviceUid, String applicationName, int serviceTypeCode, long maxStateTimestamp) {
        List<AgentIdEntry> entries = agentIdDao.getAgentIdEntryByMaxStateTimestamp(serviceUid, applicationName, serviceTypeCode, maxStateTimestamp);
        if (!agentProperties.getMissingHeaderServiceTypeCodes().contains(serviceTypeCode)) {
            return entries;
        }
        List<AgentIdEntry> undefinedEntries = agentIdDao.getAgentIdEntryByMaxStateTimestamp(serviceUid, applicationName, UNDEFINED_SERVICE_TYPE_CODE, maxStateTimestamp);
        // No dedup — duplicates are acceptable for inactive agent cleanup
        List<AgentIdEntry> combined = new ArrayList<>(entries.size() + undefinedEntries.size());
        combined.addAll(entries);
        combined.addAll(undefinedEntries);
        return combined;
    }

    @Override
    public List<AgentIdEntry> getInactiveAgentList(Application application, List<AgentIdEntry> agentIdEntries, Range activeRange) {
        if (agentProperties.getStatisticsCheckServiceTypeCodes().contains(application.getServiceTypeCode())) {
            return getInactiveByStatistics(application, agentIdEntries, activeRange);
        }
        return agentIdEntries.stream()
                .filter(entry -> isInactiveCandidate(entry, activeRange))
                .toList();
    }

    private List<AgentIdEntry> getInactiveByStatistics(Application application, List<AgentIdEntry> agentIdEntries, Range activeRange) {
        Set<String> activeAgentIds = mapAgentResponseDao.selectAgentIds(application, new TimeWindow(activeRange));
        // Entries are ordered by agentId — keep the first (most recent startTime) per agentId found in statistics
        String lastKeptAgentId = null;
        List<AgentIdEntry> inactiveEntries = new ArrayList<>();
        for (AgentIdEntry entry : agentIdEntries) {
            if (!isInactiveCandidate(entry, activeRange)) {
                continue;
            }
            if (activeAgentIds.contains(entry.getAgentId()) && !entry.getAgentId().equals(lastKeptAgentId)) {
                lastKeptAgentId = entry.getAgentId();
                continue;
            }
            inactiveEntries.add(entry);
        }
        return inactiveEntries;
    }

    private static boolean isInactiveCandidate(AgentIdEntry entry, Range range) {
        return entry.getAgentStartTime() < range.getFrom()
                && entry.getCurrentStateTimestamp() < range.getFrom();
    }

    /**
     * Dedup by agentId + startTime, keeping the entry with the most recent state.
     */
    private static List<AgentIdEntry> dedupeByKey(List<AgentIdEntry> entries, List<AgentIdEntry> undefinedEntries) {
        Map<SimpleAgentKey, AgentIdEntry> deduped = new LinkedHashMap<>();
        for (AgentIdEntry entry : entries) {
            deduped.put(toAgentKey(entry), entry);
        }
        for (AgentIdEntry entry : undefinedEntries) {
            deduped.merge(toAgentKey(entry), entry, (existing, incoming) ->
                    incoming.getCurrentStateTimestamp() > existing.getCurrentStateTimestamp() ? incoming : existing);
        }
        return new ArrayList<>(deduped.values());
    }

    private static SimpleAgentKey toAgentKey(AgentIdEntry entry) {
        return new SimpleAgentKey(entry.getAgentId(), entry.getAgentStartTime());
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