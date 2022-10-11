/*
 * Copyright 2020 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentUriStatChart;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class AgentUriStatChartService implements AgentStatChartService<AgentUriStatChart> {

    private final SampledAgentStatDao<SampledAgentUriStat> sampledAgentUriStatDao;

    public AgentUriStatChartService(SampledAgentStatDao<SampledAgentUriStat> sampledAgentUriStatDao) {
        this.sampledAgentUriStatDao = Objects.requireNonNull(sampledAgentUriStatDao, "sampledAgentUriStatDao");
    }

    @Override
    public AgentUriStatChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledAgentUriStat> sampledAgentUriStatList = sampledAgentUriStatDao.getSampledAgentStatList(agentId, timeWindow);

        if (Boolean.FALSE == CollectionUtils.isEmpty(sampledAgentUriStatList)) {
            SampledAgentUriStat first = CollectionUtils.firstElement(sampledAgentUriStatList);
            if(first != null) {
                return new AgentUriStatChart(timeWindow, first.getSampledEachUriStatBoList());
            }
        }
        return new AgentUriStatChart(timeWindow, Collections.emptyList());
    }

    @Override
    public List<AgentUriStatChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledAgentUriStat> sampledAgentUriStatList = sampledAgentUriStatDao.getSampledAgentStatList(agentId, timeWindow);

        if (CollectionUtils.isEmpty(sampledAgentUriStatList)) {
            return List.of(new AgentUriStatChart(timeWindow, Collections.emptyList()));
        } else {
            List<AgentUriStatChart> result = new ArrayList<>(sampledAgentUriStatList.size());
            for (SampledAgentUriStat sampledAgentUriStat : sampledAgentUriStatList) {
                result.add(new AgentUriStatChart(timeWindow, sampledAgentUriStat.getSampledEachUriStatBoList()));
            }
            result.sort(Comparator.comparing(this::getTotalCount).reversed());
            return result;
        }
    }

    private long getTotalCount(StatChart statChart) {
        AgentUriStatChart chart = (AgentUriStatChart) statChart;
        return chart.getTotalCount();
    }

    @Override
    public String getChartType() {
        return sampledAgentUriStatDao.getChartType();
    }
}
