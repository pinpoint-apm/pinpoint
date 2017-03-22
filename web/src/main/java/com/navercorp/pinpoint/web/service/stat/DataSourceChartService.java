/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.dao.stat.SampledDataSourceDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.chart.AgentStatChartGroup;
import com.navercorp.pinpoint.web.vo.stat.chart.DataSourceChartGroup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Service
public class DataSourceChartService implements AgentStatChartService {

    private final SampledDataSourceDao sampledDataSourceDao;
    @Autowired
    private ServiceTypeRegistryService serviceTypeRegistryService;

    @Autowired
    public DataSourceChartService(@Qualifier("sampledDataSourceDaoFactory") SampledDataSourceDao sampledDataSourceDao) {
        this.sampledDataSourceDao = sampledDataSourceDao;
    }

    @Override
    public AgentStatChartGroup selectAgentChart(String agentId, TimeWindow timeWindow) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timeWindow == null) {
            throw new NullPointerException("timeWindow must not be null");
        }

        List<SampledDataSourceList> sampledAgentStatList = this.sampledDataSourceDao.getSampledAgentStatList(agentId, timeWindow);
        if (CollectionUtils.isEmpty(sampledAgentStatList)) {
            return new DataSourceChartGroup(timeWindow, Collections.<SampledDataSource>emptyList(), serviceTypeRegistryService);
        } else {
            return new DataSourceChartGroup(timeWindow, ListUtils.getFirst(sampledAgentStatList).getSampledDataSourceList(), serviceTypeRegistryService);
        }
    }

    @Override
    public List<AgentStatChartGroup> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        if (agentId == null) {
            throw new NullPointerException("agentId must not be null");
        }
        if (timeWindow == null) {
            throw new NullPointerException("timeWindow must not be null");
        }

        List<SampledDataSourceList> sampledAgentStatList = this.sampledDataSourceDao.getSampledAgentStatList(agentId, timeWindow);
        if (CollectionUtils.isEmpty(sampledAgentStatList)) {
            List<AgentStatChartGroup> result = new ArrayList<>(sampledAgentStatList.size());
            result.add(new DataSourceChartGroup(timeWindow, Collections.<SampledDataSource>emptyList(), serviceTypeRegistryService));
            return result;
        } else {
            List<AgentStatChartGroup> result = new ArrayList<>(sampledAgentStatList.size());
            for (SampledDataSourceList sampledDataSourceList : sampledAgentStatList) {
                result.add(new DataSourceChartGroup(timeWindow, sampledDataSourceList.getSampledDataSourceList(), serviceTypeRegistryService));
            }
            return result;
        }
    }

}
