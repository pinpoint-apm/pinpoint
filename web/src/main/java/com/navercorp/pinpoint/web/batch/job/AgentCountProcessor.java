/*
 * Copyright 2016 NAVER Corp.
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

package com.navercorp.pinpoint.web.batch.job;

import com.navercorp.pinpoint.common.util.DateUtils;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.AgentInfo;
import com.navercorp.pinpoint.web.vo.ApplicationAgentList;
import org.springframework.batch.item.ItemProcessor;

import java.util.List;
import java.util.Map;

/**
 * @author Taejin Koo
 */
public class AgentCountProcessor implements ItemProcessor<ApplicationAgentList, AgentCountStatistics> {

    @Override
    public AgentCountStatistics process(ApplicationAgentList item) throws Exception {
        if (item == null) {
            return null;
        }

        int agentCount = getAgentCount(item.getApplicationAgentList());
        AgentCountStatistics agentCountStatistics = new AgentCountStatistics(agentCount, DateUtils.timestampToMidNight(System.currentTimeMillis()));
        return agentCountStatistics;
    }

    private int getAgentCount(Map<String, List<AgentInfo>> applicationAgentListMap) {
        int agentCount = 0;
        for (Map.Entry<String, List<AgentInfo>> eachApplicationAgentList : applicationAgentListMap.entrySet()) {
            agentCount += eachApplicationAgentList.getValue().size();
        }

        return agentCount;
    }

}
