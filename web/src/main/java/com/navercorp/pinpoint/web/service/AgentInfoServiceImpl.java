
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

import com.navercorp.pinpoint.common.timeseries.time.Range;
import com.navercorp.pinpoint.common.timeseries.window.TimeWindow;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentInfoQuery;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.service.stat.AgentWarningStatService;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.agent.AgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.AgentInfo;
import com.navercorp.pinpoint.web.vo.agent.AgentStatus;
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
import com.navercorp.pinpoint.web.vo.tree.ApplicationAgentHostList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.navercorp.pinpoint.web.service.ApplicationAgentListService.ACTUAL_AGENT_INFO_PREDICATE;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final AgentEventService agentEventService;

    private final ApplicationAgentListService applicationAgentListService;

    private final AgentWarningStatService agentWarningStatService;

    private final AgentInfoDao agentInfoDao;

    private final AgentLifeCycleDao agentLifeCycleDao;

    private final ApplicationIndexService applicationIndexService;

    public AgentInfoServiceImpl(AgentEventService agentEventService,
                                ApplicationAgentListService applicationAgentListService, AgentWarningStatService agentWarningStatService,
                                AgentInfoDao agentInfoDao,
                                AgentLifeCycleDao agentLifeCycleDao,
                                ApplicationIndexService applicationIndexService) {
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        this.applicationAgentListService = Objects.requireNonNull(applicationAgentListService, "applicationAgentListService");
        this.agentWarningStatService = Objects.requireNonNull(agentWarningStatService, "agentWarningStatService");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.applicationIndexService = Objects.requireNonNull(applicationIndexService, "applicationIndexService");
    }

    @Override
    public List<DetailedAgentAndStatus> getAllAgentsStatisticsList(AgentStatusFilter filter, Range range) {
        Objects.requireNonNull(filter, "filter");

        List<Application> applicationList = applicationIndexService.selectAllApplications();
        List<DetailedAgentAndStatus> agents = new ArrayList<>();
        for (Application application : applicationList) {
            Set<DetailedAgentAndStatus> detailedAgents = getDetailedAgentsByApplicationName(application.getApplicationName(), range.getTo());
            for (DetailedAgentAndStatus detailedAgent : detailedAgents) {
                AgentStatus status = detailedAgent.getStatus();
                if (filter.test(status)) {
                    agents.add(detailedAgent);
                }
            }
        }
        return agents;
    }

    @Override
    public ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit, int durationHours, List<Application> applicationList, Predicate<AgentInfo> agentInfoFilter) {
        List<String> applicationNameList = getApplicationNameList(applicationList);
        return getApplicationAgentHostList2(offset, limit, durationHours, applicationNameList, agentInfoFilter);
    }

    private ApplicationAgentHostList getApplicationAgentHostList2(int offset, int limit, int durationHours, List<String> applicationNameList, Predicate<AgentInfo> agentInfoFilter) {
        if (offset > applicationNameList.size()) {
            ApplicationAgentHostList.Builder builder = newBuilder(offset, offset, applicationNameList.size());
            return builder.build();
        }
        final long currentTime = System.currentTimeMillis();
        final int startIndex = offset - 1;
        final int endIndex = Math.min(startIndex + limit, applicationNameList.size());

        ApplicationAgentHostList.Builder builder = newBuilder(offset, endIndex, applicationNameList.size());
        for (int i = startIndex; i < endIndex; i++) {
            String applicationName = applicationNameList.get(i);

            List<AgentInfo> agentInfoList = getAgentInfoList(applicationName, currentTime, durationHours, agentInfoFilter);
            builder.addAgentInfo(applicationName, agentInfoList);
        }
        return builder.build();
    }

    private List<AgentInfo> getAgentInfoList(String applicationName, long timestamp, int durationHours, Predicate<AgentInfo> agentInfoFilter) {
        Range range = Range.between(timestamp - TimeUnit.HOURS.toMillis(durationHours), timestamp);
        TimeWindow timeWindow = new TimeWindow(range);
        if (durationHours <= 0) {
            return applicationAgentListService.allAgentList(applicationName, null, range, agentInfoFilter.and(ACTUAL_AGENT_INFO_PREDICATE)).stream()
                    .map(AgentAndStatus::getAgentInfo)
                    .collect(Collectors.toList());
        }
        return applicationAgentListService.activeStatusAgentList(applicationName, null, timeWindow, agentInfoFilter).stream()
                .map(AgentAndStatus::getAgentInfo)
                .collect(Collectors.toList());
    }

    private ApplicationAgentHostList.Builder newBuilder(int offset, int endIndex, int totalApplications) {
        return ApplicationAgentHostList.newBuilder(offset, endIndex, totalApplications);
    }

    private List<String> getApplicationNameList(List<Application> applications) {
        return applications.stream()
                .map(Application::getName)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .collect(Collectors.toList());
    }

    @Override
    public Set<AgentAndStatus> getAgentsByApplicationName(String applicationName, long timestamp) {
        List<AgentInfo> agentInfos = this.getAgentsByApplicationNameWithoutStatus0(applicationName, timestamp);
        List<AgentAndStatus> result = getAgentAndStatuses(agentInfos, timestamp);
        return new HashSet<>(result);
    }

    private List<AgentAndStatus> getAgentAndStatuses(List<AgentInfo> agentInfoList, long timestamp) {
        List<AgentAndStatus> result = new ArrayList<>(agentInfoList.size());

        AgentStatusQuery query = AgentStatusQuery.buildQuery(agentInfoList, timestamp);
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);
        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            AgentInfo agentInfo = agentInfoList.get(i);
            result.add(new AgentAndStatus(agentInfo, status.orElse(null)));
        }
        return result;
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

        List<String> agentIds = this.applicationIndexService.selectAgentIds(applicationName);
        List<AgentInfo> agentInfos = this.agentInfoDao.getSimpleAgentInfos(agentIds, timestamp);

        return agentInfos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

    }

    public Set<DetailedAgentAndStatus> getDetailedAgentsByApplicationName(String applicationName, long timestamp) {
        List<DetailedAgentInfo> agentInfos = this.getDetailedAgentsByApplicationNameWithoutStatus0(applicationName, timestamp);

        List<DetailedAgentAndStatus> result = new ArrayList<>(agentInfos.size());

        AgentStatusQuery query = AgentStatusQuery.buildGenericQuery(agentInfos, DetailedAgentInfo::getAgentInfo, timestamp);
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);

        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            DetailedAgentInfo agentInfo = agentInfos.get(i);
            result.add(new DetailedAgentAndStatus(agentInfo, status.orElse(null)));
        }

        return new HashSet<>(result);
    }

    public List<DetailedAgentInfo> getDetailedAgentsByApplicationNameWithoutStatus0(String applicationName, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        List<String> agentIds = this.applicationIndexService.selectAgentIds(applicationName);
        List<DetailedAgentInfo> agentInfos = this.agentInfoDao.getDetailedAgentInfos(agentIds, timestamp, AgentInfoQuery.jvm());

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
    public InspectorTimeline getAgentStatusTimeline(String applicationName, String agentId, Range range, int... excludeAgentEventTypeCodes) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(range, "range");

        AgentStatus initialStatus = getAgentStatus(agentId, range.getFrom());
        List<AgentEvent> agentEvents = agentEventService.getAgentEvents(agentId, range);

        List<AgentStatusTimelineSegment> warningStatusTimelineSegmentList = agentWarningStatService.select(applicationName, agentId, range);

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
