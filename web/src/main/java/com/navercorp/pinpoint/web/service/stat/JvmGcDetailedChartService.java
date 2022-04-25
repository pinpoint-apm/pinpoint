/*
 * Copyright 2016 Naver Corp.
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
import com.navercorp.pinpoint.web.vo.stat.SampledJvmGcDetailed;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.JvmGcDetailedChart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
@Service
public class JvmGcDetailedChartService implements AgentStatChartService {

    private final SampledAgentStatDao<SampledJvmGcDetailed> sampledJvmGcDetailedDao;

    public JvmGcDetailedChartService(@Qualifier("sampledJvmGcDetailedDaoFactory") SampledAgentStatDao<SampledJvmGcDetailed> sampledJvmGcDetailedDao) {
        this.sampledJvmGcDetailedDao = Objects.requireNonNull(sampledJvmGcDetailedDao, "sampledJvmGcDetailedDao");
    }

    @Override
    public StatChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledJvmGcDetailed> sampledJvmGcDetaileds = this.sampledJvmGcDetailedDao.getSampledAgentStatList(agentId, timeWindow);
        return new JvmGcDetailedChart(timeWindow, sampledJvmGcDetaileds);
    }

    @Override
    public List<StatChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        StatChart agentStatChart = selectAgentChart(agentId, timeWindow);
        return Collections.singletonList(agentStatChart);
    }

}