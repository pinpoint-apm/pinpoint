package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author intr3p1d
 */
@Service
public class AgentCollectionServiceImpl implements AgentCollectionService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentInfoService agentInfoService;

    private final ApplicationIndexDao applicationIndexDao;

    private final HyperLinkFactory hyperLinkFactory;

    private final AgentInfoDao agentInfoDao;

    private final AgentLifeCycleDao agentLifeCycleDao;


    public AgentCollectionServiceImpl(AgentInfoService agentInfoService,
                                      ApplicationIndexDao applicationIndexDao,
                                      AgentInfoDao agentInfoDao,
                                      AgentLifeCycleDao agentLifeCycleDao,
                                      HyperLinkFactory hyperLinkFactory) {
        this.agentInfoService = Objects.requireNonNull(agentInfoService, "agentInfoService");
        this.applicationIndexDao = Objects.requireNonNull(applicationIndexDao, "applicationIndexDao");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @Override
    public AgentsMapByApplication getAllAgentsList(AgentInfoFilter filter, long timestamp) {
        Objects.requireNonNull(filter, "filter");

        List<Application> applications = applicationIndexDao.selectAllApplicationNames();
        List<AgentAndStatus> agents = new ArrayList<>();
        for (Application application : applications) {
            agents.addAll(getAgentsByApplicationName(application.getName(), timestamp));
        }

        return AgentsMapByApplication.newAgentsMapByApplication(
                filter,
                agents
        );
    }

    @Override
    public AgentsMapByHost getAgentsListByApplicationName(AgentInfoFilter filter, String applicationName, long timestamp) {
        return getAgentsListByApplicationName(filter, applicationName, timestamp, SortByAgentInfo.Rules.AGENT_ID_ASC);
    }

    @Override
    public AgentsMapByHost getAgentsListByApplicationName(AgentInfoFilter filter,
                                                          String applicationName,
                                                          long timestamp,
                                                          SortByAgentInfo.Rules sortBy) {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(applicationName, "applicationName");

        Set<AgentAndStatus> agentInfoAndStatuses = getAgentsByApplicationName(applicationName, timestamp);
        if (agentInfoAndStatuses.isEmpty()) {
            logger.warn("agent list is empty for application:{}", applicationName);
        }

        AgentsMapByHost agentsMapByHost = AgentsMapByHost.newAgentsMapByHost(filter,
                SortByAgentInfo.agentIdAsc(AgentStatusAndLink::getAgentInfo),
                hyperLinkFactory,
                agentInfoAndStatuses);

        logger.debug("getAgentsMapByHostname={}", agentsMapByHost);
        return agentsMapByHost;
    }


    @Override
    public ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit, int durationDays) {
        if (offset <= 0) {
            throw new IllegalArgumentException("offset must be greater than 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }

        return getApplicationAgentHostList0(offset, limit, durationDays);
    }

    private ApplicationAgentHostList getApplicationAgentHostList0(int offset, int limit, int durationDays) {
        List<String> applicationNameList = getApplicationNameList(applicationIndexDao.selectAllApplicationNames());
        if (offset > applicationNameList.size()) {
            ApplicationAgentHostList.Builder builder = newBuilder(offset, offset, applicationNameList.size());
            return builder.build();
        }

        final long timeStamp = System.currentTimeMillis();

        final int startIndex = offset - 1;
        final int endIndex = Math.min(startIndex + limit, applicationNameList.size());

        ApplicationAgentHostList.Builder builder = newBuilder(offset, endIndex, applicationNameList.size());
        for (int i = startIndex; i < endIndex; i++) {
            String applicationName = applicationNameList.get(i);

            List<String> agentIdList = getAgentIdList(applicationName, durationDays);
            List<AgentInfo> agentInfoList = this.agentInfoDao.getSimpleAgentInfos(agentIdList, timeStamp);
            builder.addAgentInfo(applicationName, agentInfoList);
        }
        return builder.build();
    }

    private ApplicationAgentHostList.Builder newBuilder(int offset, int endIndex, int totalApplications) {
        return ApplicationAgentHostList.newBuilder(offset, endIndex, totalApplications);
    }

    @Override
    public Set<AgentAndStatus> getAgentsByApplicationName(String applicationName, long timestamp) {
        List<AgentInfo> agentInfos = this.getAgentsByApplicationNameWithoutStatus0(applicationName, timestamp);

        List<AgentAndStatus> result = new ArrayList<>(agentInfos.size());

        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentInfos, Instant.ofEpochMilli(timestamp));
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);
        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            AgentInfo agentInfo = agentInfos.get(i);
            result.add(new AgentAndStatus(agentInfo, status.orElse(null)));
        }

        return new HashSet<>(result);
    }


    private List<String> getAgentIdList(String applicationName, int durationDays) {
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }

        if (durationDays <= 0) {
            return agentIds;
        }

        List<String> activeAgentIdList = new ArrayList<>();

        Range fastRange = Range.newRange(TimeUnit.HOURS, 1, System.currentTimeMillis());

        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, durationDays * -1);
        final long fromTimestamp = cal.getTimeInMillis();
        Range queryRange = Range.between(fromTimestamp, fastRange.getFrom() + 1);

        for (String agentId : agentIds) {
            // FIXME This needs to be done with a more accurate information.
            // If at any time a non-java agent is introduced, or an agent that does not collect jvm data,
            // this will fail
            boolean dataExists = agentInfoService.isActiveAgent(agentId, fastRange);
            if (dataExists) {
                activeAgentIdList.add(agentId);
                continue;
            }

            dataExists = agentInfoService.isActiveAgent(agentId, queryRange);
            if (dataExists) {
                activeAgentIdList.add(agentId);
            }
        }
        return activeAgentIdList;
    }

    private List<String> getApplicationNameList(List<Application> applications) {
        List<String> applicationNameList = applications.stream()
                .map(Application::getName)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
        return applicationNameList;
    }


    @Override
    public Set<AgentInfo> getAgentsByApplicationNameWithoutStatus(String applicationName, long timestamp) {
        List<AgentInfo> agentInfos = getAgentsByApplicationNameWithoutStatus0(applicationName, timestamp);
        return new HashSet<>(agentInfos);
    }

    public List<AgentInfo> getAgentsByApplicationNameWithoutStatus0(String applicationName, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, timestamp);

        return agentInfos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    @Override
    public Set<AgentAndStatus> getRecentAgentsByApplicationName(String applicationName, long timestamp, long timeDiff) {
        if (timeDiff > timestamp) {
            throw new IllegalArgumentException("timeDiff must not be greater than timestamp");
        }

        Set<AgentAndStatus> unfilteredAgentInfos = this.getAgentsByApplicationName(applicationName, timestamp);

        final long eventTimestampFloor = timestamp - timeDiff;

        Set<AgentAndStatus> filteredAgentInfos = new HashSet<>();
        for (AgentAndStatus agentInfoAndStatus : unfilteredAgentInfos) {
            AgentStatus agentStatus = agentInfoAndStatus.getStatus();
            if (AgentLifeCycleState.UNKNOWN == agentStatus.getState() || eventTimestampFloor <= agentStatus.getEventTimestamp()) {
                filteredAgentInfos.add(agentInfoAndStatus);
            }
        }
        return filteredAgentInfos;
    }


    @Override
    public List<Optional<AgentStatus>> getAgentStatusList(AgentStatusQuery query) {
        Objects.requireNonNull(query, "query");
        if (query.getQueryTimestamp() < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        return this.agentLifeCycleDao.getAgentStatus(query);
    }

}
