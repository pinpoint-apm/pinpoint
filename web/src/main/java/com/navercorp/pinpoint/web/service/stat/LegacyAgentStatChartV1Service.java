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

import com.navercorp.pinpoint.web.dao.stat.LegacyAgentStatDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.AgentStat;
import com.navercorp.pinpoint.web.vo.Range;
import com.navercorp.pinpoint.web.vo.stat.chart.LegacyAgentStatChartGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Deprecated
@Service
public class LegacyAgentStatChartV1Service implements LegacyAgentStatChartService {

    @Autowired
    private LegacyAgentStatDao legacyAgentStatDao;

    @Override
    public LegacyAgentStatChartGroup selectAgentStatList(String agentId, TimeWindow timeWindow) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timeWindow == null) {
            throw new NullPointerException("timeWindow must not be null");
        }
        long scanFrom = timeWindow.getWindowRange().getFrom();
        long scanTo = timeWindow.getWindowRange().getTo() + timeWindow.getWindowSlotSize();
        Range rangeToScan = new Range(scanFrom, scanTo);
        List<AgentStat> agentStats = legacyAgentStatDao.getAgentStatList(agentId, rangeToScan);
        LegacyAgentStatChartGroup chartGroup = new LegacyAgentStatChartGroup(timeWindow);
        chartGroup.addAgentStats(agentStats);
        chartGroup.buildCharts();
        return chartGroup;
    }
}
