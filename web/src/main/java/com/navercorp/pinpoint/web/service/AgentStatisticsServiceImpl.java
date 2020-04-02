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

package com.navercorp.pinpoint.web.service;

import com.navercorp.pinpoint.web.dao.AgentStatisticsDao;
import com.navercorp.pinpoint.web.vo.AgentCountStatistics;
import com.navercorp.pinpoint.web.vo.Range;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class AgentStatisticsServiceImpl implements AgentStatisticsService {

    private final AgentStatisticsDao agentStatisticsDao;

    public AgentStatisticsServiceImpl(AgentStatisticsDao agentStatisticsDao) {
        this.agentStatisticsDao = Objects.requireNonNull(agentStatisticsDao, "agentStatisticsDao");
    }

    @Override
    public boolean insertAgentCount(AgentCountStatistics agentCountStatistics) {
        return agentStatisticsDao.insertAgentCount(agentCountStatistics);
    }

    @Override
    public List<AgentCountStatistics> selectAgentCount(Range range) {
        return agentStatisticsDao.selectAgentCount(range);
    }

}
