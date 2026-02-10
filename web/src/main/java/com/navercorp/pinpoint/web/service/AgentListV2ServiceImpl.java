package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.server.uid.ServiceUid;
import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.web.applicationmap.dao.MapAgentResponseDao;
import com.navercorp.pinpoint.web.config.AgentListProperties;
import com.navercorp.pinpoint.web.dao.AgentIdDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentListItem;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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


    public AgentListV2ServiceImpl(AgentListProperties agentListProperties,
                                  AgentIdDao agentIdDao,
                                  AgentLifeCycleDao agentLifeCycleDao,
                                  MapAgentResponseDao mapAgentResponseDao) {
        this.agentListProperties = Objects.requireNonNull(agentListProperties, "agentListProperties");
        this.agentIdDao = Objects.requireNonNull(agentIdDao, "agentIdDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.mapAgentResponseDao = Objects.requireNonNull(mapAgentResponseDao, "mapAgentResponseDao");
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName) {
        return getAgentItemList(serviceUid, applicationName, null, null);
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType) {
        return getAgentItemList(serviceUid, applicationName, serviceType, null);
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, Range range) {
        return getAgentItemList(serviceUid, applicationName, null, range);
    }

    @Override
    public List<AgentListItem> getAgentList(ServiceUid serviceUid, String applicationName, ServiceType serviceType, Range range) {
        return getAgentItemList(serviceUid, applicationName, serviceType, range);
    }

    private List<AgentListItem> getAgentItemList(
            ServiceUid serviceUid,
            String applicationName,
            @Nullable ServiceType serviceType,
            @Nullable Range range) {
        List<AgentListItem> agentList = queryAgentList(serviceUid, applicationName, serviceType);

        if (range != null) {
            agentList = filterStartTime(agentList, range);
            agentList = filterLastUpdatedTime(agentList, range, agentListProperties.getFilterUpdateTimeThresholdMillis(),
                    agentListProperties.getFilterUpdateTimeExcludeServiceTypeCodes(),
                    agentListProperties.getCheckStatisticsServiceTypeCodes());
        }
        agentList = removeConsecutiveDuplicateAgentId(agentList);
        if (range != null) {
            agentList = checkStatisticsServiceTypes(agentList, range, agentListProperties.getCheckStatisticsServiceTypeCodes());
        }

        // query using filtered agentList
        addLatestStatus(agentList);
        if (range != null) {
            agentList = filterLastStatus(agentList, range, agentListProperties.getFilterLastStatusThresholdMillis(),
                    agentListProperties.getFilterLastStatusExcludeServiceTypeCodes(),
                    agentListProperties.getCheckStatisticsServiceTypeCodes());
        }
        return agentList;
    }

    // keep only the latest entry per agentId based on pre-ordered result.
    private List<AgentListItem> queryAgentList(
            ServiceUid serviceUid,
            String applicationName,
            @Nullable ServiceType serviceType) {
        if (serviceType == null) {
            return agentIdDao.getAgentListItems(serviceUid.getUid(), applicationName);
        } else {
            return agentIdDao.getAgentListItems(serviceUid.getUid(), applicationName, serviceType.getCode());
        }
    }

    private List<AgentListItem> filterStartTime(List<AgentListItem> agentListItems, Range range) {
        return agentListItems.stream()
                .filter(item -> item.getStartTime() <= range.getTo())
                .toList();
    }

    private List<AgentListItem> removeConsecutiveDuplicateAgentId(List<AgentListItem> sortedItems) {
        List<AgentListItem> result = new ArrayList<>();
        AgentListItem previous = null;
        for (AgentListItem current : sortedItems) {
            if (!isPreviousAgentId(previous, current)) {
                result.add(current);
                previous = current;
            }
        }
        logger.debug("removeConsecutiveDuplicateAgentId input size: {}, result size: {}", sortedItems.size(), result.size());
        return result;
    }

    private boolean isPreviousAgentId(AgentListItem previous, AgentListItem current) {
        if (previous == null) {
            return false;
        }
        if (!previous.getAgentId().equals(current.getAgentId())) {
            return false;
        }
        if (!previous.getApplication().equals(current.getApplication())) {
            return false;
        }
        if (previous.getServiceUid() != current.getServiceUid()) {
            return false;
        }
        return true;
    }

    private List<AgentListItem> filterLastUpdatedTime(List<AgentListItem> agentListItems, Range range, long inactiveThresholdMillis,
                                                      Set<Integer> filterUpdateTimeExcludeServiceTypes,
                                                      Set<Integer> checkStatisticsServiceTypes) {
        return agentListItems.stream()
                .filter(item -> {
                    if (checkStatisticsServiceTypes.contains(item.getApplication().getServiceTypeCode())) {
                        // require statistics check, always include
                        return true;
                    }
                    if (!filterUpdateTimeExcludeServiceTypes.contains(item.getApplication().getServiceTypeCode())) {
                        if (item.getLastUpdated() < range.getFrom() - inactiveThresholdMillis) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }

    // query agent status in parallel and apply
    private void addLatestStatus(List<AgentListItem> agentListItems) {
        List<SimpleAgentKey> queryKeys = new ArrayList<>(agentListItems.size());
        for (AgentListItem agent : agentListItems) {
            SimpleAgentKey key = new SimpleAgentKey(agent.getAgentId(), agent.getStartTime());
            queryKeys.add(key);
        }
        List<Optional<AgentStatus>> agentStatusList = this.agentLifeCycleDao.getLatestAgentStatus(queryKeys);

        for (int i = 0; i < agentListItems.size(); i++) {
            agentListItems.get(i).setAgentStatus(agentStatusList.get(i).orElse(AgentStatus.UNKNOWN));
        }
    }

    private List<AgentListItem> filterLastStatus(List<AgentListItem> agentListItems, Range range, long lastStatusThresholdMillis,
                                                 Set<Integer> filterLastStatusExcludeServiceTypes,
                                                 Set<Integer> checkStatisticsServiceTypes) {
        return agentListItems.stream()
                .filter(agent -> {
                    if (checkStatisticsServiceTypes.contains(agent.getApplication().getServiceTypeCode())) {
                        // require statistics check, always include
                        return true;
                    }
                    if (!filterLastStatusExcludeServiceTypes.contains(agent.getApplication().getServiceTypeCode())) {
                        AgentStatus agentStatus = agent.getAgentStatus();
                        if (agentStatus != null && agentStatus.getEventTimestamp() < range.getFrom() - lastStatusThresholdMillis) {
                            return false;
                        }
                    }
                    return true;
                })
                .toList();
    }

    private List<AgentListItem> checkStatisticsServiceTypes(List<AgentListItem> agentListItems, Range range,
                                                            Set<Integer> statisticsCheckServiceTypes) {
        List<AgentListItem> filtered = new ArrayList<>();
        Map<Application, Set<String>> agentIdMap = new HashMap<>();
        for (AgentListItem item : agentListItems) {
            Application application = item.getApplication();
            if (statisticsCheckServiceTypes.contains(application.getServiceTypeCode())) {
                Set<String> statisticAgentIds = agentIdMap.computeIfAbsent(application, app -> getAgentIdsFromStatistics(app, range));
                if (statisticAgentIds.contains(item.getAgentId())) {
                    filtered.add(item);
                }
            } else {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private Set<String> getAgentIdsFromStatistics(Application application, Range range) {
        return mapAgentResponseDao.selectAgentIds(application, new TimeWindow(range));
    }
}
