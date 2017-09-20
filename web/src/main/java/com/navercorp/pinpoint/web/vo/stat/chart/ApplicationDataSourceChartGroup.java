/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.web.vo.stat.chart;

import com.navercorp.pinpoint.web.util.TimeWindow;
import com.navercorp.pinpoint.web.vo.stat.chart.DataSourcePoint.UncollectedDataSourcePointCreater;
import com.navercorp.pinpoint.web.vo.stat.AggreJoinDataSourceBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author minwoo.jung
 */
public class ApplicationDataSourceChartGroup implements ApplicationStatChartGroup {

    private static final UncollectedDataSourcePointCreater UNCOLLECTED_DATASOURCE_POINT = new UncollectedDataSourcePointCreater();

    private final String url;
    private final String serviceTypeCodeName;
    private final Map<ChartType, Chart> dataSourceChartMap;

    public enum DataSourceChartType implements ChartType {
        ACTIVE_CONNECTION_SIZE
    }

    public ApplicationDataSourceChartGroup(TimeWindow timeWindow, String url, String serviceTypeCodeName, List<AggreJoinDataSourceBo> aggreJoinDataSourceBoList) {
        this.url = url;
        this.serviceTypeCodeName = serviceTypeCodeName;
        this.dataSourceChartMap = new HashMap<>();
        List<Point> activeConnectionCountList = new ArrayList<>(aggreJoinDataSourceBoList.size());

        for (AggreJoinDataSourceBo aggreJoinDataSourceBo : aggreJoinDataSourceBoList) {
            activeConnectionCountList.add(new DataSourcePoint(aggreJoinDataSourceBo.getTimestamp(), aggreJoinDataSourceBo.getMinActiveConnectionSize(), aggreJoinDataSourceBo.getMinActiveConnectionAgentId(), aggreJoinDataSourceBo.getMaxActiveConnectionSize(), aggreJoinDataSourceBo.getMaxActiveConnectionAgentId(), aggreJoinDataSourceBo.getAvgActiveConnectionSize()));
        }

        dataSourceChartMap.put(DataSourceChartType.ACTIVE_CONNECTION_SIZE, new TimeSeriesChartBuilder(timeWindow, UNCOLLECTED_DATASOURCE_POINT).build(activeConnectionCountList));
    }

    public String getJdbcUrl() {
        return url;
    }

    public String getServiceTypeCodeName() {
        return serviceTypeCodeName;
    }

    @Override
    public Map<ChartType, Chart> getCharts() {
        return dataSourceChartMap;
    }
}
