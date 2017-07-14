
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

import com.google.common.collect.Ordering;
import com.navercorp.pinpoint.common.server.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.filter.agent.AgentEventFilter;
import com.navercorp.pinpoint.web.service.stat.AgentWarningStatService;
import com.navercorp.pinpoint.web.vo.AgentEvent;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationAgentHostList;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventTimeline;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentEventTimelineBuilder;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimeline;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineBuilder;
import com.navercorp.pinpoint.web.vo.timeline.inspector.AgentStatusTimelineSegment;
import com.navercorp.pinpoint.web.vo.timeline.inspector.InspectorTimeline;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.PredicateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private AgentEventService agentEventService;

    @Autowired
    private AgentWarningStatService agentWarningStatService;

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private AgentInfoDao agentInfoDao;

    @Autowired
    private AgentLifeCycleDao agentLifeCycleDao;

    @Override
    public ApplicationAgentList getApplicationAgentList(ApplicationAgentList.Key key) {
        return this.getApplicationAgentList(key, System.currentTimeMillis());
    }

    @Override
    public ApplicationAgentList getApplicationAgentList(ApplicationAgentList.Key key, long timestamp) {
        ApplicationAgentList applicationAgentList = new ApplicationAgentList();
        List<Application> applications = applicationIndexDao.selectAllApplicationNames();
        for (Application application : applications) {
            applicationAgentList.merge(this.getApplicationAgentList(key, application.getName(), timestamp));
        }
        return applicationAgentList;
    }

    @Override
    public ApplicationAgentList getApplicationAgentList(ApplicationAgentList.Key key, String applicationName) {
        return this.getApplicationAgentList(key, applicationName, System.currentTimeMillis());
    }

    @Override
    public ApplicationAgentList getApplicationAgentList(ApplicationAgentList.Key applicationAgentListKey, String applicationName, long timestamp) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (applicationAgentListKey == null) {
            throw new NullPointerException("applicationAgentListKey must not be null");
        }
        final List<String> agentIdList = this.applicationIndexDao.selectAgentIds(applicationName);
        if (logger.isDebugEnabled()) {
            logger.debug("agentIdList={}", agentIdList);
        }

        if (CollectionUtils.isEmpty(agentIdList)) {
            logger.debug("agentIdList is empty. applicationName={}", applicationName);
            return new ApplicationAgentList(new TreeMap<String, List<AgentInfo>>());
        }

        // key = hostname
        // value= list fo agentinfo
        SortedMap<String, List<AgentInfo>> result = new TreeMap<>();

        List<AgentInfo> agentInfos = this.agentInfoDao.getAgentInfos(agentIdList, timestamp);
        this.agentLifeCycleDao.populateAgentStatuses(agentInfos, timestamp);
        for (AgentInfo agentInfo : agentInfos) {
            if (agentInfo != null) {
                String hostname = applicationAgentListKey.getKey(agentInfo);

                if (result.containsKey(hostname)) {
                    result.get(hostname).add(agentInfo);
                } else {
                    List<AgentInfo> list = new ArrayList<>();
                    list.add(agentInfo);
                    result.put(hostname, list);
                }
            }
        }

        for (List<AgentInfo> agentInfoList : result.values()) {
            Collections.sort(agentInfoList, AgentInfo.AGENT_NAME_ASC_COMPARATOR);
        }

        logger.info("getApplicationAgentList={}", result);

        return new ApplicationAgentList(result);
    }

    @Override
    public ApplicationAgentHostList getApplicationAgentHostList(int offset, int limit) {
        if (offset <= 0 || limit <= 0) {
            throw new IllegalArgumentException("Value must be greater than 0.");
        }

        List<String> applicationNameList = getApplicationNameList(applicationIndexDao.selectAllApplicationNames());
        if (offset > applicationNameList.size()) {
            return new ApplicationAgentHostList(offset, offset, applicationNameList.size());
        }

        long timeStamp = System.currentTimeMillis();

        int startIndex = offset - 1;
        int endIndex = Math.min(startIndex + limit, applicationNameList.size());
        ApplicationAgentHostList applicationAgentHostList = new ApplicationAgentHostList(offset, endIndex, applicationNameList.size());
        for (int i = startIndex ; i < endIndex; i++) {
            String applicationName = applicationNameList.get(i);

            List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
            List<AgentInfo> agentInfoList = this.agentInfoDao.getAgentInfos(agentIds, timeStamp);
            applicationAgentHostList.put(applicationName, agentInfoList);
        }
        return applicationAgentHostList;
    }

    private List<String> getApplicationNameList(List<Application> applications) {
        List<String> applicationNameList = new ArrayList<>(applications.size());
        for (Application application : applications) {
            if (!applicationNameList.contains(application.getName())) {
                applicationNameList.add(application.getName());
            }
        }

        Collections.sort(applicationNameList, Ordering.usingToString());
        return applicationNameList;
    }

    @Override
    public Set<AgentInfo> getAgentsByApplicationName(String applicationName, long timestamp) {
        Set<AgentInfo> agentInfos = this.getAgentsByApplicationNameWithoutStatus(applicationName, timestamp);
        this.agentLifeCycleDao.populateAgentStatuses(agentInfos, timestamp);
        return agentInfos;
    }

    @Override
    public Set<AgentInfo> getAgentsByApplicationNameWithoutStatus(String applicationName, long timestamp) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }

        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        List<AgentInfo> agentInfos = this.agentInfoDao.getAgentInfos(agentIds, timestamp);
        CollectionUtils.filter(agentInfos, PredicateUtils.notNullPredicate());
        if (CollectionUtils.isEmpty(agentInfos)) {
            return Collections.emptySet();
        }
        return new HashSet<>(agentInfos);
    }

    @Override
    public Set<AgentInfo> getRecentAgentsByApplicationName(String applicationName, long timestamp, long timeDiff) {
        if (timeDiff > timestamp) {
            throw new IllegalArgumentException("timeDiff must not be greater than timestamp");
        }

        Set<AgentInfo> unfilteredAgentInfos = this.getAgentsByApplicationName(applicationName, timestamp);

        final long eventTimestampFloor = timestamp - timeDiff;

        Set<AgentInfo> filteredAgentInfos = new HashSet<>();
        for (AgentInfo agentInfo : unfilteredAgentInfos) {
            AgentStatus agentStatus = agentInfo.getStatus();
            if (AgentLifeCycleState.UNKNOWN == agentStatus.getState() || eventTimestampFloor <= agentStatus.getEventTimestamp()) {
                filteredAgentInfos.add(agentInfo);
            }
        }
        return filteredAgentInfos;
    }

    @Override
    public AgentInfo getAgentInfo(String agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        AgentInfo agentInfo = this.agentInfoDao.getAgentInfo(agentId, timestamp);
        if (agentInfo != null) {
            this.agentLifeCycleDao.populateAgentStatus(agentInfo, timestamp);
        }
        return agentInfo;
    }

    @Override
    public AgentStatus getAgentStatus(String agentId, long timestamp) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timestamp must not be less than 0");
        }
        return this.agentLifeCycleDao.getAgentStatus(agentId, timestamp);
    }

    @Override
    public void populateAgentStatuses(Collection<AgentInfo> agentInfos, long timestamp) {
        this.agentLifeCycleDao.populateAgentStatuses(agentInfos, timestamp);
    }

    @Override
    public InspectorTimeline getAgentStatusTimeline(String agentId, Range range, int... excludeAgentEventTypeCodes) {
        Assert.notNull(agentId, "agentId must not be null");
        Assert.notNull(range, "range must not be null");

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
}
