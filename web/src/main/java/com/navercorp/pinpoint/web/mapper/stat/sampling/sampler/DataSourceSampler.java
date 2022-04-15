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

package com.navercorp.pinpoint.web.mapper.stat.sampling.sampler;

import com.navercorp.pinpoint.common.server.bo.stat.DataSourceBo;
import com.navercorp.pinpoint.web.vo.stat.SampledDataSource;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPoint;
import com.navercorp.pinpoint.web.vo.stat.chart.agent.AgentStatPointSummary;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Taejin Koo
 */
@Component
public class DataSourceSampler implements AgentStatSampler<DataSourceBo, SampledDataSource> {

    @Override
    public SampledDataSource sampleDataPoints(int timeWindowIndex, long timestamp, List<DataSourceBo> dataSourceBoList, DataSourceBo previousDataSourceBo) {
        if (CollectionUtils.isEmpty(dataSourceBoList)) {
            return null;
        }

        final List<Integer> activeConnectionSizes = new ArrayList<>(dataSourceBoList.size());
        final List<Integer> maxConnectionSizes = new ArrayList<>(dataSourceBoList.size());

        final DataSourceBo defaultDataSourceBo = dataSourceBoList.get(0);
        final int id = defaultDataSourceBo.getId();
        final short serviceTypeCode = defaultDataSourceBo.getServiceTypeCode();
        String databaseName = defaultDataSourceBo.getDatabaseName();
        String jdbcUrl = defaultDataSourceBo.getJdbcUrl();
        for (DataSourceBo dataSourceBo : dataSourceBoList) {

            final int activeConnectionSize = dataSourceBo.getActiveConnectionSize();
            if (activeConnectionSize >= 0) {
                activeConnectionSizes.add(activeConnectionSize);
            }

            final int maxConnectionSize = dataSourceBo.getMaxConnectionSize();
            if (maxConnectionSize >= 0) {
                maxConnectionSizes.add(maxConnectionSize);
            }

            if (dataSourceBo.getId() != id) {
                throw new IllegalArgumentException("id must be same");
            }
            if (dataSourceBo.getServiceTypeCode() != serviceTypeCode) {
                throw new IllegalArgumentException("serviceTypeCode must be same");
            }

            if (databaseName == null && dataSourceBo.getDatabaseName() != null) {
                databaseName = dataSourceBo.getDatabaseName();
            }

            if (jdbcUrl == null && dataSourceBo.getJdbcUrl() != null) {
                jdbcUrl = dataSourceBo.getJdbcUrl();
            }
        }

        SampledDataSource sampledDataSource = new SampledDataSource();
        sampledDataSource.setId(id);
        sampledDataSource.setServiceTypeCode(serviceTypeCode);
        sampledDataSource.setDatabaseName(databaseName);
        sampledDataSource.setJdbcUrl(jdbcUrl);
        sampledDataSource.setActiveConnectionSize(createPoint(timestamp, activeConnectionSizes));
        sampledDataSource.setMaxConnectionSize(createPoint(timestamp, maxConnectionSizes));
        return sampledDataSource;
    }

    private AgentStatPoint<Integer> createPoint(long timestamp, List<Integer> values) {
        if (CollectionUtils.isEmpty(values)) {
            return SampledDataSource.UNCOLLECTED_POINT_CREATOR.createUnCollectedPoint(timestamp);
        }

        return AgentStatPointSummary.intSummary(timestamp, values, 3);
    }

}
