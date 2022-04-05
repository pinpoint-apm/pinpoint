/*
 * Copyright 2018 Naver Corp.
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
import com.navercorp.pinpoint.web.vo.stat.SampledDirectBuffer;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DirectBufferChart;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Roy Kim
 */
@Service
public class DirectBufferChartService implements AgentStatChartService {

    private final SampledAgentStatDao<SampledDirectBuffer> sampledDirectBufferDao;

    public DirectBufferChartService(@Qualifier("sampledDirectBufferDaoFactory") SampledAgentStatDao<SampledDirectBuffer> sampledDirectBufferDao) {
        this.sampledDirectBufferDao = Objects.requireNonNull(sampledDirectBufferDao, "sampledDirectBufferDao");
    }

    @Override
    public StatChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledDirectBuffer> sampledDirectBuffers = this.sampledDirectBufferDao.getSampledAgentStatList(agentId, timeWindow);
        return new DirectBufferChart(timeWindow, sampledDirectBuffers);
    }

    @Override
    public List<StatChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        StatChart agentStatChart = selectAgentChart(agentId, timeWindow);
        return Collections.singletonList(agentStatChart);
    }

}
