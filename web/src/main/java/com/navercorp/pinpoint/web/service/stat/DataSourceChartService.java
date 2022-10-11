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

import com.navercorp.pinpoint.common.server.bo.stat.AgentStatType;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.SampledAgentStatDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSourceList;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.DataSourceChart;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
@Service
public class DataSourceChartService implements AgentStatChartService<DataSourceChart> {

    private final SampledAgentStatDao<SampledDataSourceList> sampledDataSourceDao;
    private final ServiceTypeRegistryService serviceTypeRegistryService;

    public DataSourceChartService(SampledAgentStatDao<SampledDataSourceList> sampledDataSourceDao,
                                  ServiceTypeRegistryService serviceTypeRegistryService) {
        this.sampledDataSourceDao = Objects.requireNonNull(sampledDataSourceDao, "sampledDataSourceDao");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    @Override
    public DataSourceChart selectAgentChart(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        final List<SampledDataSourceList> sampledAgentStatList = this.sampledDataSourceDao.getSampledAgentStatList(agentId, timeWindow);
        if (Boolean.FALSE == CollectionUtils.isEmpty(sampledAgentStatList)) {
            final SampledDataSourceList sampledDataSourceList = CollectionUtils.firstElement(sampledAgentStatList);
            if (sampledDataSourceList != null) {
                final List<SampledDataSource> list = sampledDataSourceList.getSampledDataSourceList();
                return new DataSourceChart(timeWindow, list, serviceTypeRegistryService);
            }
        }
        return new DataSourceChart(timeWindow, Collections.emptyList(), serviceTypeRegistryService);
    }

    @Override
    public List<DataSourceChart> selectAgentChartList(String agentId, TimeWindow timeWindow) {
        Objects.requireNonNull(agentId, "agentId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<SampledDataSourceList> sampledAgentStatList = this.sampledDataSourceDao.getSampledAgentStatList(agentId, timeWindow);
        if (CollectionUtils.isEmpty(sampledAgentStatList)) {
            List<DataSourceChart> result = new ArrayList<>(1);
            result.add(new DataSourceChart(timeWindow, Collections.emptyList(), serviceTypeRegistryService));
            return result;
        } else {
            List<DataSourceChart> result = new ArrayList<>(sampledAgentStatList.size());
            for (SampledDataSourceList sampledDataSourceList : sampledAgentStatList) {
                result.add(new DataSourceChart(timeWindow, sampledDataSourceList.getSampledDataSourceList(), serviceTypeRegistryService));
            }
            return result;
        }
    }

    @Override
    public String getChartType() {
        return AgentStatType.DATASOURCE.getChartType();
    }
}
