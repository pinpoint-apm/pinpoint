
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

import com.navercorp.pinpoint.common.server.bo.SimpleAgentKey;
import com.navercorp.pinpoint.common.timeseries.time.Range;
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
import com.navercorp.pinpoint.web.vo.agent.AgentStatusQuery;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentAndStatus;
import com.navercorp.pinpoint.web.vo.agent.DetailedAgentInfo;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventTimeline;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventTimelineBuilder;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimeline;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineBuilder;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
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

    private final AgentInfoDao agentInfoDao;
    private final AgentLifeCycleDao agentLifeCycleDao;

    private final ApplicationIndexService applicationIndexService;

    public AgentInfoServiceImpl(AgentEventService agentEventService,
                                AgentWarningStatService agentWarningStatService,
                                AgentInfoDao agentInfoDao,
                                AgentLifeCycleDao agentLifeCycleDao,
                                ApplicationIndexService applicationIndexService) {
        this.agentEventService = Objects.requireNonNull(agentEventService, "agentEventService");
        this.agentWarningStatService = Objects.requireNonNull(agentWarningStatService, "agentWarningStatService");
        this.agentInfoDao = Objects.requireNonNull(agentInfoDao, "agentInfoDao");
        this.agentLifeCycleDao = Objects.requireNonNull(agentLifeCycleDao, "agentLifeCycleDao");
        this.applicationIndexService = Objects.requireNonNull(applicationIndexService, "applicationIndexService");
    }

    @Override
    public List<DetailedAgentAndStatus> getAgentsStatisticsList(Range range) {
        List<Application> applicationList = applicationIndexService.selectAllApplications();

        List<DetailedAgentAndStatus> agents = new ArrayList<>();
        for (Application application : applicationList) {
            List<DetailedAgentAndStatus> detailedAgents = getDetailedAgents(application, range.getTo());
            for (DetailedAgentAndStatus agent : detailedAgents) {
                AgentStatus status = agent.getStatus();
                if (status == null || status.getEventTimestamp() > range.getFrom()) {
                    agents.add(agent);
                }
            }
        }
        return agents;
    }

    private List<DetailedAgentAndStatus> getDetailedAgents(Application application, long timestamp) {
        List<DetailedAgentInfo> agentInfos = this.getDetailedAgentsWithoutStatus0(application, timestamp);

        List<DetailedAgentAndStatus> result = new ArrayList<>(agentInfos.size());
        AgentStatusQuery query = AgentStatusQuery.buildGenericQuery(agentInfos, DetailedAgentInfo::getAgentInfo, timestamp);
        List<Optional<AgentStatus>> agentStatus = this.agentLifeCycleDao.getAgentStatus(query);

        for (int i = 0; i < agentStatus.size(); i++) {
            Optional<AgentStatus> status = agentStatus.get(i);
            DetailedAgentInfo agentInfo = agentInfos.get(i);
            result.add(new DetailedAgentAndStatus(agentInfo, status.orElse(null)));
        }
        return result;
    }

    private List<DetailedAgentInfo> getDetailedAgentsWithoutStatus0(Application application, long timestamp) {
        Objects.requireNonNull(application, "applicationName");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        List<String> agentIds = this.applicationIndexService.selectAgentIds(application.getApplicationName(), application.getServiceTypeCode());
        List<DetailedAgentInfo> agentInfos = this.agentInfoDao.findDetailedAgentInfos(agentIds, timestamp, AgentInfoQuery.jvm());

        return agentInfos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Deprecated
    @Override
    public List<AgentInfo> getAgentInfoByApplicationName(String applicationName, long timestamp) {
        Objects.requireNonNull(applicationName, "applicationName");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        List<String> agentIds = this.applicationIndexService.selectAgentIds(applicationName);
        List<AgentInfo> agentInfos = this.agentInfoDao.findAgentInfos(agentIds, timestamp);
        return agentInfos.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Override
    public AgentAndStatus findAgentInfoAndStatus(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");

        AgentInfo agentInfo = findAgentInfo(agentId, timestamp);
        if (agentInfo == null) {
            return null;
        }

        AgentStatus agentStatus = this.agentLifeCycleDao.getAgentStatus(agentInfo.getAgentId(), agentInfo.getStartTimestamp(), timestamp);
        return new AgentAndStatus(agentInfo, agentStatus);
    }

    @Override
    public DetailedAgentAndStatus findDetailedAgentInfoAndStatus(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        DetailedAgentInfo detailedAgentInfo = this.agentInfoDao.findDetailedAgentInfo(agentId, timestamp);
        if (detailedAgentInfo == null) {
            return null;
        }
        AgentInfo agentInfo = detailedAgentInfo.getAgentInfo();

        AgentStatus agentStatus = this.agentLifeCycleDao.getAgentStatus(agentInfo.getAgentId(), agentInfo.getStartTimestamp(), timestamp);
        return new DetailedAgentAndStatus(detailedAgentInfo, agentStatus);

    }

    @Override
    public AgentInfo findAgentInfo(String agentId, long timestamp) {
        Objects.requireNonNull(agentId, "agentId");
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        return this.agentInfoDao.findAgentInfo(agentId, timestamp);
    }

    @Override
    public AgentInfo findAgentInfo(String agentId, long fromTimestamp, long toTimestamp) {
        Objects.requireNonNull(agentId, "agentId");
        if (fromTimestamp < 0) {
            throw new IllegalArgumentException("fromTimestamp must not be less than 0");
        }
        if (toTimestamp <= fromTimestamp) {
            throw new IllegalArgumentException("toTimestamp must be greater than fromTimestamp");
        }
        return this.agentInfoDao.findAgentInfo(agentId, fromTimestamp, toTimestamp);
    }

    @Override
    public AgentInfo getAgentInfo(String agentId, long agentStartTime) {
        Objects.requireNonNull(agentId, "agentId");
        if (agentStartTime < 0) {
            throw new IllegalArgumentException("agentStartTime must not be less than 0");
        }
        return this.agentInfoDao.getAgentInfo(agentId, agentStartTime);
    }

    @Override
    public List<AgentInfo> getAgentInfos(List<SimpleAgentKey> simpleAgentKeyList) {
        Objects.requireNonNull(simpleAgentKeyList, "simpleAgentKeyList");
        if (simpleAgentKeyList.isEmpty()) {
            return List.of();
        }

        return this.agentInfoDao.getAgentInfos(simpleAgentKeyList);
    }

    @Override
    public AgentStatus findAgentStatus(String agentId, long timestamp) {
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

        AgentStatus initialStatus = findAgentStatus(agentId, range.getFrom());
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
}
