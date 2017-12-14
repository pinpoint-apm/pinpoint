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

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import org.springframework.batch.core.JobExecutionException;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author Taejin Koo
 */
public class AgentCountWriter implements ItemWriter<AgentCountStatistics> {

    @Autowired
    AgentStatisticsDao agentStatisticsDao;

    @Override
    public void write(List<? extends AgentCountStatistics> items) throws Exception {
        if (items.size() == 1) {
            AgentCountStatistics agentCountStatistics = items.get(0);
            if (agentCountStatistics == null || agentCountStatistics.getAgentCount() < 0) {
                throw new JobExecutionException("Bad parameter");
            }
            boolean success = agentStatisticsDao.insertAgentCount(agentCountStatistics);
            if (!success) {
                throw new JobExecutionException("insert AgentCount failed.");
            }
        } else {
            throw new JobExecutionException("Bad parameter");
        }
    }

}
