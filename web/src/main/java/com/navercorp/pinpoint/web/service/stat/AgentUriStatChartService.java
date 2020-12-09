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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.dao.stat.SampledAgentUriStatDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledAgentUriStat;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentUriStatChart;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class AgentUriStatChartService implements AgentStatChartService {

    private final SampledAgentUriStatDao sampledAgentUriStatDao;

    public AgentUriStatChartService(@Qualifier("sampledAgentUriStatDaoFactory") SampledAgentUriStatDao sampledAgentUriStatDao) {
        this.sampledAgentUriStatDao = Objects.requireNonNull(sampledAgentUriStatDao, "sampledAgentUriStatDao");
    }

    @Override
    public StatChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledAgentUriStat> sampledAgentUriStatList = sampledAgentUriStatDao.getSampledAgentStatList(agentId, timeWindow);

        if (CollectionUtils.isEmpty(sampledAgentUriStatList)) {
            return new AgentUriStatChart(timeWindow, Collections.emptyList());
        } else {
            SampledAgentUriStat first = ListUtils.getFirst(sampledAgentUriStatList);
            return new AgentUriStatChart(timeWindow, first.getSampledEachUriStatBoList());
        }
    }

    @Override
    public List<StatChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledAgentUriStat> sampledAgentUriStatList = sampledAgentUriStatDao.getSampledAgentStatList(agentId, timeWindow);


        if (CollectionUtils.isEmpty(sampledAgentUriStatList)) {
            return Arrays.asList(new AgentUriStatChart(timeWindow, Collections.emptyList()));
        } else {
            List<StatChart> result = new ArrayList<>(sampledAgentUriStatList.size());
            for (SampledAgentUriStat sampledAgentUriStat : sampledAgentUriStatList) {
                result.add(new AgentUriStatChart(timeWindow, sampledAgentUriStat.getSampledEachUriStatBoList()));
            }
            Collections.sort(result, new Comparator<StatChart>() {
                @Override
                public int compare(StatChart o1, StatChart o2) {
                    AgentUriStatChart chart1 = (AgentUriStatChart) o1;
                    AgentUriStatChart chart2 = (AgentUriStatChart) o2;
                    return Long.compare(chart2.getTotalCount(), chart1.getTotalCount());
                }
            });
            return result;
        }
    }

}
