package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.config.AgentListProperties;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntry;
import com.navercorp.pinpoint.web.vo.agent.AgentIdEntryAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class AgentListV2ServiceImpl implements AgentListV2Service {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentListProperties agentListProperties;

    private final AgentIdDao agentIdDao;
    private final AgentLifeCycleDao agentLifeCycleDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    public AgentListV2ServiceImpl(AgentListProperties agentListProperties, AgentIdDao agentIdDao, AgentLifeCycleDao agentLifeCycleDao, MapAgentResponseDao mapAgentResponseDao) {
        this.agentListProperties = Objects.requireNonNull(agentListProperties, "agentListProperties");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
    }

    @Override
    public List<AgentIdEntryAndStatus> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType) {
        return getAgentsWithServiceTypeSpecificFiltering(serviceUid, applicationName, serviceType, null);
    }

    @Override
    public List<AgentIdEntryAndStatus> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        return getAgentsWithServiceTypeSpecificFiltering(serviceUid, applicationName, serviceType, range);
    }

    private List<AgentIdEntryAndStatus> getAgentsWithServiceTypeSpecificFiltering(ServiceUid serviceUid, String applicationName, ServiceType serviceType, @Nullable Range range) {
        List<AgentIdEntry> agentIdEntryList = queryAgentList(serviceUid, applicationName, serviceType, range);
        // 1. filter by startTime
        agentIdEntryList = filterStartTime(agentIdEntryList, range);
        // 2. dedupe by agentId, keeping the one with the latest startTime
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);

        // filter by statistics existence (only for some service types)
        agentIdEntryList = filterStatistics(agentIdEntryList, range);

        // add status after all pre-filtering to minimize status queries
        // filter by latest status (only for some service types)
        List<AgentIdEntryAndStatus> agentIdEntryAndStatusList = addStatus(agentIdEntryList, range);
        agentIdEntryAndStatusList = filterLastStatus(agentIdEntryAndStatusList, range);
        return agentIdEntryAndStatusList;
    }

    private List<AgentIdEntry> queryAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, @Nullable Range range) {
        if (range == null || agentListProperties.getFilterUpdateTimeExcludeServiceTypeCodes().contains((int) serviceType.getCode())) {
            return agentIdDao.getAgentIdEntry(serviceUid.getUid(), applicationName, serviceType.getCode());
        } else {
            // filter lastUpdateTime with hbase filter
            long minUpdateTime = range.getFrom() - agentListProperties.getFilterUpdateTimeThresholdMillis();
            return agentIdDao.getAgentIdEntryByInsertTimeAfter(serviceUid.getUid(), applicationName, serviceType.getCode(), minUpdateTime);
        }
    }

    private List<AgentIdEntry> filterStartTime(List<AgentIdEntry> agentSummaries, Range range) {
        if (range == null) {
            return agentSummaries;
        }
        return agentSummaries.stream().filter(item -> item.getAgentStartTime() <= range.getTo()).toList();
    }

    // Assumes the list is ordered by application -> agentId -> startTime
    private List<AgentIdEntry> dedupeConsecutiveAgentId(List<AgentIdEntry> orderedAgentSummaries) {
        List<AgentIdEntry> result = new ArrayList<>();
        AgentIdEntry previous = null;
        for (AgentIdEntry current : orderedAgentSummaries) {
            if (!isPreviousAgentId(previous, current)) {
                result.add(current);
                previous = current;
            }
        }
        logger.debug("removeConsecutiveDuplicateAgentId input size: {}, result size: {}", orderedAgentSummaries.size(), result.size());
        return result;
    }

    private boolean isPreviousAgentId(AgentIdEntry previous, AgentIdEntry current) {
        return previous != null &&
                previous.getAgentId().equals(current.getAgentId()) &&
                previous.getApplication().equals(current.getApplication());
    }

    // Assumes the list is ordered by application -> agentId
    private List<AgentIdEntry> filterStatistics(List<AgentIdEntry> orderedAgents, Range range) {
        if (range == null) {
            return orderedAgents;
        }
        List<AgentIdEntry> filtered = new ArrayList<>();
        Application lastStatisticsApplication = null;
        Set<String> statisticsAgentIds = null;
        for (AgentIdEntry item : orderedAgents) {
            Application application = item.getApplication();
            if (agentListProperties.getFilterStatisticsExistenceServiceTypeCodes().contains(application.getServiceTypeCode())) {
                if (!application.equals(lastStatisticsApplication)) {
                    lastStatisticsApplication = application;
                    statisticsAgentIds = mapAgentResponseDao.selectAgentIds(application, new TimeWindow(range));
                }

                if (!statisticsAgentIds.contains(item.getAgentId())) {
                    continue;
                }
            }
            filtered.add(item);
        }
        return filtered;
    }

    // query agent status in parallel and apply
    private List<AgentIdEntryAndStatus> addStatus(List<AgentIdEntry> agentIdEntryList, Range range) {
        List<SimpleAgentKey> queryKeys = new ArrayList<>(agentIdEntryList.size());
        for (AgentIdEntry agent : agentIdEntryList) {
            SimpleAgentKey key = new SimpleAgentKey(agent.getAgentId(), agent.getAgentStartTime());
            queryKeys.add(key);
        }

        List<Optional<AgentStatus>> agentStatusList;
        if (range == null) {
            agentStatusList = this.agentLifeCycleDao.getLatestAgentStatus(queryKeys);
        } else {
            AgentStatusQuery.Builder builder = AgentStatusQuery.newBuilder();
            for (SimpleAgentKey key : queryKeys) {
                builder.addAgentKey(key);
            }
            AgentStatusQuery query = builder.build(range.getTo());
            agentStatusList = this.agentLifeCycleDao.getAgentStatus(query);
        }

        List<AgentIdEntryAndStatus> result = new ArrayList<>(agentIdEntryList.size());
        for (int i = 0; i < agentIdEntryList.size(); i++) {
            AgentIdEntry agentIdEntry = agentIdEntryList.get(i);
            AgentStatus agentStatus = agentStatusList.get(i)
                    .orElseGet(() -> new AgentStatus(agentIdEntry.getAgentId(), AgentLifeCycleState.UNKNOWN, 0L));
            result.add(new AgentIdEntryAndStatus(agentIdEntry, agentStatus));
        }
        return result;
    }

    private List<AgentIdEntryAndStatus> filterLastStatus(List<AgentIdEntryAndStatus> agentIdEntryAndStatusList, Range range) {
        if (range == null) {
            return agentIdEntryAndStatusList;
        }
        return agentIdEntryAndStatusList.stream().filter(agent -> {
            Application application = agent.getAgentIdEntry().getApplication();
            if (agentListProperties.getFilterLastStatusExcludeServiceTypeCodes().contains(application.getServiceTypeCode())) {
                AgentStatus agentStatus = agent.getAgentStatus();
                return agentStatus.getEventTimestamp() >= range.getFrom() - agentListProperties.getFilterLastStatusThresholdMillis();
            }
            return true;
        }).toList();
    }
}
