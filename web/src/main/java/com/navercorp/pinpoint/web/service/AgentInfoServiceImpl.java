
/*
 * Copyright 2014 NAVER Corp.
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

import com.navercorp.pinpoint.common.id.AgentId;
import com.navercorp.pinpoint.common.id.ApplicationId;
import com.navercorp.pinpoint.common.id.ServiceId;
import com.navercorp.pinpoint.common.server.bo.ApplicationSelector;
import com.navercorp.pinpoint.common.server.bo.stat.JvmGcBo;
import com.navercorp.pinpoint.common.server.util.AgentEventType;
import com.navercorp.pinpoint.common.server.util.time.DateTimeUtils;
import com.navercorp.pinpoint.common.server.util.time.Range;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.stat.AgentStatDao;
import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.hyperlink.HyperLinkFactory;
import com.navercorp.pinpoint.web.service.stat.AgentWarningStatService;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentInfoFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusAndLink;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusFilter;
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventTimeline;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventTimelineBuilder;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimeline;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineBuilder;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByApplication;
import com.navercorp.pinpoint.web.vo.tree.AgentsMapByHost;
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import com.navercorp.pinpoint.web.vo.tree.SortByAgentInfo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.time.Duration;
import java.time.Instant;
import java.time.Period;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentEventService agentEventService;

    private final AgentWarningStatService agentWarningStatService;

    private final ApplicationService applicationService;

    private final AgentInfoDao agentInfoDao;

    private final AgentLifeCycleDao agentLifeCycleDao;

    private final AgentStatDao<JvmGcBo> jvmGcDao;
    private final ApplicationInfoService applicationInfoService;
    private final HyperLinkFactory hyperLinkFactory;

    public AgentInfoServiceImpl(AgentEventService agentEventService,
                                AgentWarningStatService agentWarningStatService,
                                ApplicationService applicationService,
                                AgentInfoDao agentInfoDao,
                                AgentLifeCycleDao agentLifeCycleDao,
                                AgentStatDao<JvmGcBo> jvmGcDao,
                                ApplicationInfoService applicationInfoService,
                                HyperLinkFactory hyperLinkFactory) {
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        this.agentWarningStatService = Objects.requireNonNull(agentWarningStatService, "agentWarningStatService");
        this.applicationService = Objects.requireNonNull(applicationService, "applicationService");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.jvmGcDao = Objects.requireNonNull(jvmGcDao, "jvmGcDao");
        this.applicationInfoService = Objects.requireNonNull(applicationInfoService, "applicationInfoService");
        this.hyperLinkFactory = Objects.requireNonNull(hyperLinkFactory, "hyperLinkFactory");
    }

    @Override
    public AgentsMapByApplication<AgentAndStatus> getAllAgentsList(AgentStatusFilter filter, Range range) {
        Objects.requireNonNull(filter, "filter");

        List<Application> applications = applicationService.getApplications();
        List<AgentAndStatus> agents = new ArrayList<>();
        for (Application application : applications) {
            agents.addAll(getAgentsByApplicationName(application.name(), application.getServiceTypeCode(), range.getTo()));
        }

        return AgentsMapByApplication.newAgentAndStatusMap(
                filter,
                agents
        );
    }

    @Override
    public AgentsMapByApplication<DetailedAgentInfo> getAllAgentsStatisticsList(AgentStatusFilter filter, Range range) {
        Objects.requireNonNull(filter, "filter");

        List<Application> applications = this.applicationService.getApplications();
        List<DetailedAgentAndStatus> agents = new ArrayList<>();
        for (Application application : applications) {
            agents.addAll(getDetailedAgentsByApplicationName(application.name(), application.getServiceTypeCode(), range.getTo()));
        }

        return AgentsMapByApplication.newDetailedAgentInfoMap(
                filter,
                agents
        );
    }

    @Override
    public AgentsMapByHost getAgentsListByApplicationName(AgentStatusFilter agentStatusFilter,
                                                          AgentInfoFilter agentInfoPredicate,
                                                          String applicationName,
                                                          short serviceTypeCode,
                                                          Range range,
                                                          SortByAgentInfo.Rules sortBy) {
        Objects.requireNonNull(agentStatusFilter, "agentStatusFilter");
        Objects.requireNonNull(agentInfoPredicate, "agentInfoPredicate");
        Objects.requireNonNull(applicationName, "applicationName");

        Predicate<AgentStatus> agentStatusFilter0 = agentStatusFilter.and(x -> isActiveAgent(x.getAgentId(), range));
        Set<AgentAndStatus> agentInfoAndStatuses = getAgentsByApplicationName(applicationName, serviceTypeCode, range.getTo());

        if (agentInfoAndStatuses.isEmpty()) {
            logger.warn("agent list is empty for application: {}", applicationName);
        }

        AgentsMapByHost agentsMapByHost = AgentsMapByHost.newAgentsMapByHost(
                x -> agentInfoPredicate.test(x.getAgentInfo()) && agentStatusFilter0.test(x.getStatus()),
                SortByAgentInfo.comparing(AgentStatusAndLink::getAgentInfo, sortBy.getRule()),
                hyperLinkFactory,
                agentInfoAndStatuses
        );

        logger.debug("getAgentsMapByHostname={}", agentsMapByHost);
        return agentsMapByHost;
    }

    @Override
    public ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit, Period durationDays) {
        if (offset <= 0) {
            throw new IllegalArgumentException("offset must be greater than 0");
        }
        if (limit <= 0) {
            throw new IllegalArgumentException("limit must be greater than 0");
        }
        Objects.requireNonNull(durationDays, "durationDays");

        return getApplicationAgentHostList0(offset, limit, durationDays);
    }

    private ApplicationAgentHostList getApplicationAgentHostList0(int offset, int limit, Period durationDays) {
        List<Application> applications0 = this.applicationService.getApplications();
        List<Application> applications = getSortedApplicationList(applications0);
        if (offset > applications.size()) {
            ApplicationAgentHostList.Builder builder = newBuilder(offset, offset, applications.size());
            return builder.build();
        }

        final long timeStamp = System.currentTimeMillis();

        final int startIndex = offset - 1;
        final int endIndex = Math.min(startIndex + limit, applications.size());

        ApplicationAgentHostList.Builder builder = newBuilder(offset, endIndex, applications.size());
        for (int i = startIndex; i < endIndex; i++) {
            Application application = applications.get(i);
            String applicationName = application.name();
            List<String> agentIdList = getAgentIdList(application.id(), durationDays);
            List<AgentInfo> agentInfoList = this.agentInfoDao.getSimpleAgentInfos(agentIdList, timeStamp);
            builder.addAgentInfo(applicationName, agentInfoList);
        }
        return builder.build();
    }

    private ApplicationAgentHostList.Builder newBuilder(int offset, int endIndex, int totalApplications) {
        return ApplicationAgentHostList.newBuilder(offset, endIndex, totalApplications);
    }

    private List<String> getAgentIdList(ApplicationId applicationId, Period durationDays) {
        List<String> agentIds = this.applicationService.getAgents(applicationId);
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }
        if (durationDays.isNegative()) {
            return agentIds;
        }

        Instant now = DateTimeUtils.epochMilli();
        Instant before = now.minus(Duration.ofHours(1));
        Range fastRange = Range.between(before, now);

        Instant queryFrom =  now.minus(durationDays);
        Instant queryTo = before.plusMillis(1);
        Range queryRange = Range.between(queryFrom, queryTo);

        List<String> activeAgentIdList = new ArrayList<>();
        for (String agentId : agentIds) {
            // FIXME This needs to be done with a more accurate information.
            // If at any time a non-java agent is introduced, or an agent that does not collect jvm data,
            // this will fail
            boolean dataExists = isActiveAgent(AgentId.of(agentId), fastRange);
            if (dataExists) {
                activeAgentIdList.add(agentId);
                continue;
            }

            dataExists = isActiveAgent(AgentId.of(agentId), queryRange);
            if (dataExists) {
                activeAgentIdList.add(agentId);
            }
        }
        return activeAgentIdList;
    }

    private List<Application> getSortedApplicationList(List<Application> applications) {
        return applications.stream()
                .sorted(Comparator.comparing(Application::name))
                .collect(Collectors.toList());
    }

    @Override
    public Set<AgentAndStatus> getAgentsByApplicationName(String applicationName, short serviceTypeCode, long timestamp) {
        ApplicationId applicationId = this.applicationInfoService.getApplicationId(
                new ApplicationSelector(ServiceId.DEFAULT_ID, applicationName, serviceTypeCode));
        List<AgentInfo> agentInfos = this.getAgentsByApplicationNameWithoutStatus0(applicationId, timestamp);

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


    @Override
    public Set<AgentInfo> getAgentsByApplicationNameWithoutStatus(String applicationName, short serviceTypeCode, long timestamp) {
        ApplicationId applicationId = this.applicationInfoService.getApplicationId(new ApplicationSelector(ServiceId.DEFAULT_ID, applicationName, serviceTypeCode));
        List<AgentInfo> agentInfos = getAgentsByApplicationNameWithoutStatus0(applicationId, timestamp);
        return new HashSet<>(agentInfos);
    }

    public List<AgentInfo> getAgentsByApplicationNameWithoutStatus0(ApplicationId applicationId, long timestamp) {
        Objects.requireNonNull(applicationId, "applicationId");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        List<String> agentIds = this.applicationService.getAgents(applicationId);
        List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, timestamp);

        return agentInfos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    public Set<DetailedAgentAndStatus> getDetailedAgentsByApplicationName(String applicationName, short serviceTypeCode, long timestamp) {
        List<DetailedAgentInfo> agentInfos = this.getDetailedAgentsByApplicationNameWithoutStatus0(applicationName, serviceTypeCode, timestamp);

        List<DetailedAgentAndStatus> result = new ArrayList<>(agentInfos.size());

        AgentStatusQuery query = AgentStatusQuery.buildGenericQuery(agentInfos, DetailedAgentInfo::getAgentInfo, Instant.ofEpochMilli(timestamp));
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);

        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            DetailedAgentInfo agentInfo = agentInfos.get(i);
            result.add(new DetailedAgentAndStatus(agentInfo, status.orElse(null)));
        }

        return new HashSet<>(result);
    }

    public List<DetailedAgentInfo> getDetailedAgentsByApplicationNameWithoutStatus0(String applicationName, short serviceTypeCode, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        ApplicationId applicationId = this.applicationInfoService.getApplicationId(new ApplicationSelector(ServiceId.DEFAULT_ID, applicationName, serviceTypeCode));
        List<String> agentIds = this.applicationService.getAgents(applicationId);
        List<DetailedAgentInfo> agentInfos = this.agentInfoDao.getDetailedAgentInfos(agentIds, timestamp, false, true);

        return agentInfos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public AgentAndStatus getAgentInfo(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");

        AgentInfo agentInfo = getAgentInfoWithoutStatus(agentId, timestamp);
        if (agentInfo == null) {
            return null;
        }

        Optional<AgentStatus> agentStatus = this.agentLifeCycleDao.getAgentStatus(agentInfo.getAgentId(), agentInfo.getStartTimestamp(), timestamp);
        return new AgentAndStatus(agentInfo, agentStatus.orElse(null));
    }

    @Override
    public DetailedAgentAndStatus getDetailedAgentInfo(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        DetailedAgentInfo detailedAgentInfo = this.agentInfoDao.getDetailedAgentInfo(agentId, timestamp);
        if (detailedAgentInfo == null) {
            return null;
        }
        AgentInfo agentInfo = detailedAgentInfo.getAgentInfo();

        Optional<AgentStatus> agentStatus = this.agentLifeCycleDao.getAgentStatus(agentInfo.getAgentId(), agentInfo.getStartTimestamp(), timestamp);
        return new DetailedAgentAndStatus(detailedAgentInfo, agentStatus.orElse(null));

    }

    @Override
    public AgentInfo getAgentInfoWithoutStatus(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        return this.agentInfoDao.getAgentInfo(agentId, timestamp);
    }

    @Override
    public AgentInfo getAgentInfoWithoutStatus(String agentId, long agentStartTime, int deltaTimeInMilliSeconds) {
        Objects.requireNonNull(agentId, "agentId");

        return this.agentInfoDao.getAgentInfo(agentId, agentStartTime, deltaTimeInMilliSeconds);
    }

    @Override
    public AgentStatus getAgentStatus(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");

        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        return this.agentLifeCycleDao.getAgentStatus(agentId, timestamp);
    }

    @Override
    public List<Optional<AgentStatus>> getAgentStatus(AgentStatusQuery query) {
        Objects.requireNonNull(query, "query");
        if (query.getQueryTimestamp() < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        return this.agentLifeCycleDao.getAgentStatus(query);
    }

    @Override
    public boolean isActiveAgent(AgentId agentId, Range range) {
        Objects.requireNonNull(agentId, "agentId");
        return isActiveAgentByGcStat(agentId, range) ||
                isActiveAgentByPing(agentId, range);
    }

    private boolean isActiveAgentByGcStat(AgentId agentId, Range range) {
        return this.jvmGcDao.agentStatExists(agentId.value(), range);
    }

    private boolean isActiveAgentByPing(AgentId agentId, Range range) {
        return this.agentEventService.getAgentEvents(agentId.value(), range)
                .stream()
                .anyMatch(e -> e.getEventTypeCode() == AgentEventType.AGENT_PING.getCode());
    }

    @Override
    public InspectorTimeline getAgentStatusTimeline(String agentId, Range range, int... excludeAgentEventTypeCodes) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        AgentStatus initialStatus = getAgentStatus(agentId, range.getFrom());
        List<AgentEvent> agentEvents = agentEventService.getAgentEvents(agentId, range);

        List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList = agentWarningStatService.select(agentId, range);

        AgentStatusTimelineBuilder agentStatusTimelinebuilder = new AgentStatusTimelineBuilder(range, initialStatus, agentEvents, warningStatusTimelineSegmentList);
        AgentStatusTimeline agentStatusTimeline = agentStatusTimelinebuilder.build();

        AgentEventTimelineBuilder agentEventTimelineBuilder = new AgentEventTimelineBuilder(range);
        agentEventTimelineBuilder.from(agentEvents);
        agentEventTimelineBuilder.addFilter(new AgentEventFilter.ExcludeFilter(excludeAgentEventTypeCodes));
        AgentEventTimeline agentEventTimeline = agentEventTimelineBuilder.build();

        return new InspectorTimeline(agentStatusTimeline, agentEventTimeline);
    }

    @Override
    public boolean isExistAgentId(String agentId) {
        Objects.requireNonNull(agentId, "agentId");

        AgentInfo agentInfo = getAgentInfoWithoutStatus(agentId, System.currentTimeMillis());
        return agentInfo != null;
    }

}
