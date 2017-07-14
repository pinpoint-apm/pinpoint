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

import com.navercorp.pinpoint.web.dao.stat.SampledTransactionDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledTransaction;
import com.navercorp.pinpoint.web.vo.stat.chart.AgentStatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.TransactionChartGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * @author HyunGil Jeong
 */
@Service
public class TransactionChartService implements AgentStatChartService {

    private final SampledTransactionDao sampledTransactionDao;

    @Autowired
    public TransactionChartService(@Qualifier("sampledTransactionDaoFactory") SampledTransactionDao sampledTransactionDao) {
        this.sampledTransactionDao = sampledTransactionDao;
    }

    @Override
    public AgentStatChartGroup selectAgentChart(String agentId, TimeWindow timeWindow) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timeWindow == null) {
            throw new NullPointerException("timeWindow must not be null");
        }
        List<SampledTransaction> sampledTransactions = this.sampledTransactionDao.getSampledAgentStatList(agentId, timeWindow);
        return new TransactionChartGroup(timeWindow, sampledTransactions);
    }

    @Override
    public List<AgentStatChartGroup> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        AgentStatChartGroup agentStatChartGroup = selectAgentChart(agentId, timeWindow);

        List<AgentStatChartGroup> result = new ArrayList<>(1);
        result.add(agentStatChartGroup);

        return result;
    }

}
