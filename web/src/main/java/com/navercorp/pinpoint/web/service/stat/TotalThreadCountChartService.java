/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.web.service.stat;

import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledTotalThreadCount;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.TotalThreadCountChart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Service
public class TotalThreadCountChartService implements AgentStatChartService{
    private final SampledAgentStatDao<SampledTotalThreadCount> sampledTotalThreadCountDao;

    public TotalThreadCountChartService(@Qualifier("sampledTotalThreadCountDaoFactory") SampledAgentStatDao<SampledTotalThreadCount> sampledTotalThreadCountDao) {
        this.sampledTotalThreadCountDao = Objects.requireNonNull(sampledTotalThreadCountDao, "sampledTotalThreadCountDao");
    }
    @Override
    public StatChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");
        List<SampledTotalThreadCount> sampledTotalThreadCounts = this.sampledTotalThreadCountDao.getSampledAgentStatList(agentId, timeWindow);
        return new TotalThreadCountChart(timeWindow, sampledTotalThreadCounts);
    }

    @Override
    public List<StatChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        StatChart agentStatChart = selectAgentChart(agentId, timeWindow);
        return List.of(agentStatChart);
    }
}
