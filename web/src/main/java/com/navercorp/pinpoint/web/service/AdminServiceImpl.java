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
import com.navercorp.pinpoint.web.dao.stat.JvmGcDao;
import com.navercorp.pinpoint.web.vo.Application;
import com.navercorp.pinpoint.web.vo.Range;
import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * @author netspider
 * @author HyunGil Jeong
 */
@Service
public class AdminServiceImpl implements AdminService {

    private static final int MIN_DURATION_DAYS_FOR_INACTIVITY = 30;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    @Qualifier("jvmGcDaoFactory")
    private JvmGcDao jvmGcDao;

    @Override
    public void removeApplicationName(String applicationName) {
        applicationIndexDao.deleteApplicationName(applicationName);
    }

    @Override
    public void removeAgentId(String applicationName, String agentId) {
        applicationIndexDao.deleteAgentId(applicationName, agentId);
    }

    @Override
    public void removeInactiveAgents(int durationDays) {
        if (durationDays < MIN_DURATION_DAYS_FOR_INACTIVITY) {
            throw new IllegalArgumentException("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }
        Map<String, List<String>> inactiveAgentMap = new TreeMap<>(Ordering.usingToString());

        List<Application> applications = this.applicationIndexDao.selectAllApplicationNames();
        Set<String> applicationNames = new TreeSet<>(Ordering.usingToString());
        // remove duplicates (same application name but different service type)
        for (Application application : applications) {
            applicationNames.add(application.getName());
        }
        for (String applicationName : applicationNames) {
            List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
            Collections.sort(agentIds);
            List<String> inactiveAgentIds = filterInactiveAgents(agentIds, durationDays);
            if (!CollectionUtils.isEmpty(inactiveAgentIds)) {
                inactiveAgentMap.put(applicationName, inactiveAgentIds);
            }
        }
        // map may become big, but realistically won't cause OOM
        // if it becomes an issue, consider deleting inside the loop above
        logger.info("deleting {}", inactiveAgentMap);
        this.applicationIndexDao.deleteAgentIds(inactiveAgentMap);
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
        if (durationDays < MIN_DURATION_DAYS_FOR_INACTIVITY) {
            throw new IllegalArgumentException("duration may not be less than " + MIN_DURATION_DAYS_FOR_INACTIVITY + " days");
        }
        List<String> agentIds = this.applicationIndexDao.selectAgentIds(applicationName);
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyMap();
        }
        Map<String, List<Application>> agentIdMap = this.getAgentIdMap();
        Map<String, List<Application>> inactiveAgentMap = new TreeMap<>(Ordering.usingToString());
        List<String> inactiveAgentIds = filterInactiveAgents(agentIds, durationDays);
        for (String inactiveAgentId : inactiveAgentIds) {
            List<Application> applications = agentIdMap.get(inactiveAgentId);
            inactiveAgentMap.put(inactiveAgentId, applications);
        }
        return inactiveAgentMap;
    }

    private List<String> filterInactiveAgents(List<String> agentIds, int durationDays) {
        if (CollectionUtils.isEmpty(agentIds)) {
            return Collections.emptyList();
        }
        List<String> inactiveAgentIds = new ArrayList<>();
        final long toTimestamp = System.currentTimeMillis();
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE, durationDays * -1);
        final long fromTimestamp = cal.getTimeInMillis();
        Range queryRange = new Range(fromTimestamp, toTimestamp);
        for (String agentId : agentIds) {
            // FIXME This needs to be done with a more accurate information.
            // If at any time a non-java agent is introduced, or an agent that does not collect jvm data,
            // this will fail
            boolean dataExists = this.jvmGcDao.agentStatExists(agentId, queryRange);
            if (!dataExists) {
                inactiveAgentIds.add(agentId);
            }
        }
        return inactiveAgentIds;
    }

}
