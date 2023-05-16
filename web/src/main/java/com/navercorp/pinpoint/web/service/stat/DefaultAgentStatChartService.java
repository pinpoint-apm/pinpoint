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
import com.navercorp.pinpoint.web.vo.stat.SampledAgentStatDataPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author HyunGil Jeong
 */
//@Service
public class DefaultAgentStatChartService<IN extends SampledAgentStatDataPoint, OUT extends StatChart> implements AgentStatChartService<OUT> {

    private final SampledAgentStatDao<IN> statDao;
    private final SampledChartFunction<IN, OUT> chartFunction;

    public DefaultAgentStatChartService(SampledAgentStatDao<IN> statDao, SampledChartFunction<IN, OUT> chartFunction) {
        this.statDao = Objects.requireNonNull(statDao, "sampledActiveTraceDao");
        this.chartFunction = Objects.requireNonNull(chartFunction, "chartFunction");
    }

    @Override
    public OUT selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");
        List<IN> dataPoint = this.statDao.getSampledAgentStatList(agentId, timeWindow);
        return chartFunction.apply(timeWindow, dataPoint);
    }

    @Override
    public List<OUT> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        OUT agentStatChart = selectAgentChart(agentId, timeWindow);
        return Collections.singletonList(agentStatChart);
    }

    @Override
    public String getChartType() {
        return statDao.getChartType();
    }
}
