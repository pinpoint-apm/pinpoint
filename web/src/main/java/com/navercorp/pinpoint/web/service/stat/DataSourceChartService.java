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

import com.navercorp.pinpoint.common.util.CollectionUtils;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.rpc.util.ListUtils;
import com.navercorp.pinpoint.web.dao.stat.SampledDataSourceDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DataSourceChart;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class DataSourceChartService implements AgentStatChartService {

    private final SampledDataSourceDao sampledDataSourceDao;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public DataSourceChartService(@Qualifier("sampledDataSourceDaoFactory") SampledDataSourceDao sampledDataSourceDao,
                                  ServiceTypeRegistryService serviceTypeRegistryService) {
        this.sampledDataSourceDao = Objects.requireNonNull(sampledDataSourceDao, "sampledDataSourceDao");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    @Override
    public StatChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledDataSourceList> sampledAgentStatList = this.sampledDataSourceDao.getSampledAgentStatList(agentId, timeWindow);
        if (CollectionUtils.isEmpty(sampledAgentStatList)) {
            return new DataSourceChart(timeWindow, Collections.emptyList(), serviceTypeRegistryService);
        } else {
            return new DataSourceChart(timeWindow, ListUtils.getFirst(sampledAgentStatList).getSampledDataSourceList(), serviceTypeRegistryService);
        }
    }

    @Override
    public List<StatChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledDataSourceList> sampledAgentStatList = this.sampledDataSourceDao.getSampledAgentStatList(agentId, timeWindow);
        if (CollectionUtils.isEmpty(sampledAgentStatList)) {
            List<StatChart> result = new ArrayList<>(1);
            result.add(new DataSourceChart(timeWindow, Collections.emptyList(), serviceTypeRegistryService));
            return result;
        } else {
            List<StatChart> result = new ArrayList<>(sampledAgentStatList.size());
            for (SampledDataSourceList sampledDataSourceList : sampledAgentStatList) {
                result.add(new DataSourceChart(timeWindow, sampledDataSourceList.getSampledDataSourceList(), serviceTypeRegistryService));
            }
            return result;
        }
    }

}
