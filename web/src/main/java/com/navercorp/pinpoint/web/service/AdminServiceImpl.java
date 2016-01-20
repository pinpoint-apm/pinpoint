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
import com.navercorp.pinpoint.web.dao.AgentStatDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private AgentStatDao agentStatDao;

    @Override
    public void removeApplicationName(String applicationName) {
        applicationIndexDao.deleteApplicationName(applicationName);
    }

    @Override
    public void removeAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
    }

    @Override
    public Map<String, List<Application>> getAgentIdMap() {
        Map<String, List<Application>> agentIdMap = new TreeMap<>(Ordering.usingToString());
        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        for (Application application : applications) {
            List<String> agentIds = this.applicationIndexDao.selectAgentIds(application.getName());
            for (String agentId : agentIds) {
                if (!agentIdMap.containsKey(agentId)) {
                    agentIdMap.put(agentId, new ArrayList<Application>());
                }
                agentIdMap.get(agentId).add(application);
            }
        }
        return agentIdMap;
    }

    @Override
    public Map<String, List<Application>> getDuplicateAgentIdMap() {
        Map<String, List<Application>> duplicateAgentIdMap = new TreeMap<>(Ordering.usingToString());
        Map<String, List<Application>> agentIdMap = this.getAgentIdMap();
        for (Map.Entry<String, List<Application>> entry : agentIdMap.entrySet()) {
            String agentId = entry.getKey();
            List<Application> applications = entry.getValue();
            if (applications.size() > 1) {
                duplicateAgentIdMap.put(agentId, applications);
            }
        }
        return duplicateAgentIdMap;
    }

    @Override
    public Map<String, List<Application>> getInactiveAgents(String applicationName, int durationDays) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
        if (durationDays < 30) {
            throw new IllegalArgumentException("duration may not be less than 30 days");
        }
        if (durationDays > 180) {
            throw new IllegalArgumentException("duration may not be greater than 180 days");
        }
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyMap();
        }

        final long toTimestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, durationDays * -1);
        final long fromTimestamp = cal.getTimeInMillis();
        Range queryRange = new Range(fromTimestamp, toTimestamp);

        Map<String, List<Application>> agentIdMap = this.getAgentIdMap();

        Map<String, List<Application>> inactiveAgentMap = new TreeMap<>(Ordering.usingToString());
        for (String agentId : agentIds) {
            boolean dataExists = this.agentStatDao.agentStatExists(agentId, queryRange);
            if (!dataExists) {
                List<Application> applications = agentIdMap.get(agentId);
                inactiveAgentMap.put(agentId, applications);
            }
        }
        return inactiveAgentMap;
    }

}
