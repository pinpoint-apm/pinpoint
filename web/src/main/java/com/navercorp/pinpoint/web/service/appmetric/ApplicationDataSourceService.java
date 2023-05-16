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
package com.navercorp.pinpoint.web.service.appmetric;

import com.navercorp.pinpoint.common.server.bo.stat.join.JoinDataSourceListBo.DataSourceKey;
import com.navercorp.pinpoint.loader.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.web.dao.appmetric.ApplicationMetricDao;
import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceListBo;
import com.navercorp.pinpoint.web.vo.stat.chart.StatChart;
import com.navercorp.pinpoint.web.vo.stat.chart.application.ApplicationDataSourceChart;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class ApplicationDataSourceService {

    private final ApplicationMetricDao<AggreJoinDataSourceListBo> metricDao;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private static final Comparator<AggreJoinDataSourceBo> comparator
            = Comparator.comparingLong(AggreJoinDataSourceBo::getTimestamp);

    public ApplicationDataSourceService(ApplicationMetricDao<AggreJoinDataSourceListBo> metricDao, ServiceTypeRegistryService serviceTypeRegistryService) {
        this.metricDao = Objects.requireNonNull(metricDao, "metricDao");
        this.serviceTypeRegistryService = Objects.requireNonNull(serviceTypeRegistryService, "serviceTypeRegistryService");
    }

    public List<StatChart> selectApplicationChart(String applicationId, TimeWindow timeWindow) {
        Objects.requireNonNull(applicationId, "applicationId");
        Objects.requireNonNull(timeWindow, "timeWindow");

        List<StatChart> result = new ArrayList<>();
        List<AggreJoinDataSourceListBo> aggreJoinDataSourceListBoList = this.metricDao.getApplicationStatList(applicationId, timeWindow);

        if (aggreJoinDataSourceListBoList.isEmpty()) {
            result.add(new ApplicationDataSourceChart(timeWindow, "", "", Collections.emptyList()));
            return result;
        }

        Map<DataSourceKey, List<AggreJoinDataSourceBo>> aggreJoinDataSourceBoMap = classifyByDataSourceUrl(aggreJoinDataSourceListBoList);

        for (Map.Entry<DataSourceKey, List<AggreJoinDataSourceBo>> entry: aggreJoinDataSourceBoMap.entrySet()) {
            DataSourceKey dataSourceKey = entry.getKey();
            String serviceTypeName = serviceTypeRegistryService.findServiceType(dataSourceKey.getServiceTypeCode()).getName();
            result.add(new ApplicationDataSourceChart(timeWindow, dataSourceKey.getUrl(), serviceTypeName, entry.getValue()));
        }

        return result;
    }


    protected Map<DataSourceKey, List<AggreJoinDataSourceBo>> classifyByDataSourceUrl(List<AggreJoinDataSourceListBo> aggreJoinDataSourceListBoList) {

        Map<DataSourceKey, List<AggreJoinDataSourceBo>> aggreJoinDataSourceBoMap = new HashMap<>();

        for (AggreJoinDataSourceListBo aggreJoinDataSourceListBo : aggreJoinDataSourceListBoList) {
            for (AggreJoinDataSourceBo aggreJoinDataSourceBo : aggreJoinDataSourceListBo.getAggreJoinDataSourceBoList()) {
                DataSourceKey dataSourceKey = new DataSourceKey(aggreJoinDataSourceBo.getUrl(), aggreJoinDataSourceBo.getServiceTypeCode());
                List<AggreJoinDataSourceBo> aggreJoinDataSourceBoList = aggreJoinDataSourceBoMap.computeIfAbsent(dataSourceKey, k -> new ArrayList<>());

                aggreJoinDataSourceBoList.add(aggreJoinDataSourceBo);
            }
        }

        for (List<AggreJoinDataSourceBo> aggreJoinDataSourceBoList : aggreJoinDataSourceBoMap.values()) {
            aggreJoinDataSourceBoList.sort(comparator);
        }

        return aggreJoinDataSourceBoMap;
    }

}
