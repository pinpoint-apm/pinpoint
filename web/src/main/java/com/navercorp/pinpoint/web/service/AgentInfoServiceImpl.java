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

import java.util.*;

import com.navercorp.pinpoint.common.util.AgentLifeCycleState;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.AgentLifeCycleDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.AgentStatus;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

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
    public Set<AgentInfo> getAgentsByApplicationName(String applicationName, long timestamp) {
        return this.getAgentsByApplicationName(applicationName, timestamp, timestamp);
    }

    @Override
    public Set<AgentInfo> getAgentsByApplicationName(String applicationName, long timestamp, long timeDiff) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (timestamp < 0) {
            throw new IllegalArgumentException("timeDiff must not be less than 0");
        }
        if (timeDiff > timestamp) {
            throw new IllegalArgumentException("timeDiff must not be greater than timestamp");
        }
        final long eventTimestampFloor = timestamp - timeDiff;

        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        List<AgentInfo> unfilteredAgentInfos = this.agentInfoDao.getAgentInfos(agentIds, timestamp);
        if (unfilteredAgentInfos == null || unfilteredAgentInfos.isEmpty()) {
            return Collections.emptySet();
        }
        this.agentLifeCycleDao.populateAgentStatuses(unfilteredAgentInfos, timestamp);
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
}
