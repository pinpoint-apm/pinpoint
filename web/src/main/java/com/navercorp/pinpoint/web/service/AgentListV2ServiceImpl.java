package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.config.AgentListProperties;
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

    private final AgentListProperties agentListProperties;

    private final AgentIdDao agentIdDao;
    private final MapAgentResponseDao mapAgentResponseDao;

    public AgentListV2ServiceImpl(AgentListProperties agentListProperties, AgentIdDao agentIdDao, MapAgentResponseDao mapAgentResponseDao) {
        this.agentListProperties = Objects.requireNonNull(agentListProperties, "agentListProperties");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
    }

    @Override
    public List<AgentIdEntry> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType) {
        List<AgentIdEntry> agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid.getUid(), applicationName, serviceType.getCode());
        return dedupeConsecutiveAgentId(agentIdEntryList);
    }

    @Override
    public List<AgentIdEntry> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList = queryAgentList(serviceUid, applicationName, serviceType, range);
        // dedupe by agentId, keeping the one with the latest startTime
        agentIdEntryList = dedupeConsecutiveAgentId(agentIdEntryList);

        // filter by statistics existence (only for some service types)
        agentIdEntryList = filterStatistics(agentIdEntryList, range);
        return agentIdEntryList;
    }

    private List<AgentIdEntry> queryAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        List<AgentIdEntry> agentIdEntryList;
        if (agentListProperties.getFilterLastStatusExcludeServiceTypeCodes().contains((int) serviceType.getCode())) {
            agentIdEntryList = agentIdDao.getAgentIdEntry(serviceUid.getUid(), applicationName, serviceType.getCode());
        } else {
            // Exclude entries where lastStatusTimestamp < range.getFrom() using an HBase filter
            // Entries with no state are marked as UNKNOWN state and will not be excluded
            agentIdEntryList = agentIdDao.getAgentIdEntryByMinStatusTimestamp(serviceUid.getUid(), applicationName, serviceType.getCode(), range.getFrom());
        }

        // Exclude entries where agentStartTime > range.getTo()
        // This could also be handled by an HBase filter if BinaryComponentComparator (HBase 2.2+) is available.
        return agentIdEntryList.stream()
                .filter(entry -> entry.getAgentStartTime() <= range.getTo())
                .toList();
    }

    // Assumes the list is ordered by application, agentId, startTime
    private List<AgentIdEntry> dedupeConsecutiveAgentId(List<AgentIdEntry> orderedAgentIdEntryList) {
        List<AgentIdEntry> result = new ArrayList<>();
        AgentIdEntry previous = null;
        for (AgentIdEntry current : orderedAgentIdEntryList) {
            if (!isPreviousAgentId(previous, current)) {
                result.add(current);
                previous = current;
            }
        }
        logger.debug("removeConsecutiveDuplicateAgentId input size: {}, result size: {}", orderedAgentIdEntryList.size(), result.size());
        return result;
    }

    private boolean isPreviousAgentId(AgentIdEntry previous, AgentIdEntry current) {
        return previous != null &&
                previous.getAgentId().equals(current.getAgentId()) &&
                previous.getApplication().equals(current.getApplication());
    }

    // Assumes the list is ordered by application, agentId
    private List<AgentIdEntry> filterStatistics(List<AgentIdEntry> orderedAgents, Range range) {
        if (range == null) {
            return orderedAgents;
        }
        List<AgentIdEntry> filtered = new ArrayList<>();
        Application lastStatisticsApplication = null;
        Set<String> statisticsAgentIds = null;
        for (AgentIdEntry entry : orderedAgents) {
            Application application = entry.getApplication();
            if (agentListProperties.getFilterStatisticsExistenceServiceTypeCodes().contains(application.getServiceTypeCode()) &&
                    entry.getCurrentStateTimestamp() < range.getFrom()) {
                if (!application.equals(lastStatisticsApplication)) {
                    lastStatisticsApplication = application;
                    statisticsAgentIds = mapAgentResponseDao.selectAgentIds(application, new TimeWindow(range));
                }

                if (!statisticsAgentIds.contains(entry.getAgentId())) {
                    continue;
                }
            }
            filtered.add(entry);
        }
        return filtered;
    }
}
