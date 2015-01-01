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

import com.navercorp.pinpoint.common.bo.AgentInfoBo;
import com.navercorp.pinpoint.web.dao.AgentInfoDao;
import com.navercorp.pinpoint.web.dao.ApplicationIndexDao;
import com.navercorp.pinpoint.web.vo.Range;

import org.apache.commons.collections.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * @author netspider
 * 
 */
@Service
public class AgentInfoServiceImpl implements AgentInfoService {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Autowired
    private ApplicationIndexDao applicationIndexDao;

    @Autowired
    private AgentInfoDao agentInfoDao;

    /**
     * FIXME from/to present in the interface but these values are not currently used. They should be used when agent list snapshot is implemented
     */
    @Override
    public SortedMap<String, List<AgentInfoBo>> getApplicationAgentList(String applicationName, Range range) {
        if (applicationName == null) {
            throw new NullPointerException("applicationName must not be null");
        }
       final List<String> agentIdList = applicationIndexDao.selectAgentIds(applicationName);
        if (logger.isDebugEnabled()) {
            logger.debug("agentIdList={}", agentIdList);
        }

        if (CollectionUtils.isEmpty(agentIdList)) {
            logger.debug("agentIdList is empty. applicationName={}, {}", applicationName, range);
            return new TreeMap<String, List<AgentInfoBo>>();
        }

        // key = hostname
        // value= list fo agentinfo
        SortedMap<String, List<AgentInfoBo>> result = new TreeMap<String, List<AgentInfoBo>>();

        for (String agentId : agentIdList) {
            List<AgentInfoBo> agentInfoList = agentInfoDao.getAgentInfo(agentId, range);

            if (agentInfoList.isEmpty()) {
                logger.debug("agentinfolist is empty. agentid={}, {}", agentId, range);
                continue;
            }

            // FIXME just using the first value for now. Might need to check and pick which one to use.
            AgentInfoBo agentInfo = agentInfoList.get(0);
            String hostname = agentInfo.getHostName();

            if (result.containsKey(hostname)) {
                result.get(hostname).add(agentInfo);
            } else {
                List<AgentInfoBo> list = new ArrayList<AgentInfoBo>();
                list.add(agentInfo);
                result.put(hostname, list);
            }
        }

        for (List<AgentInfoBo> agentInfoBoList : result.values()) {
            Collections.sort(agentInfoBoList, AgentInfoBo.AGENT_NAME_ASC_COMPARATOR);
        }

        logger.info("getApplicationAgentList={}", result);

        return result;
    }

    public Set<AgentInfoBo> selectAgent(String applicationId, Range range) {
        if (applicationId == null) {
            throw new NullPointerException("applicationId must not be null");
        }

        List<String> agentIds = applicationIndexDao.selectAgentIds(applicationId);
        Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
        for (String agentId : agentIds) {
            // TODO Temporarily scans for the most recent AgentInfo row starting from range's to value.
            // (As we do not yet have a way to accurately record the agent's lifecycle.)
            AgentInfoBo info = agentInfoDao.findAgentInfoBeforeStartTime(agentId, range.getTo());
            if (info != null) {
                agentSet.add(info);
            }
        }
        return agentSet;
    }
}
