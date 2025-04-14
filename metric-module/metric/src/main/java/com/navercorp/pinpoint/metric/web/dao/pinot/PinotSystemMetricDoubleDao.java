/*
 * Copyright 2021 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.dao.pinot;

import com.navercorp.pinpoint.common.server.metric.dao.TableNameManager;
import com.navercorp.pinpoint.common.timeseries.point.DataPoint;
import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotSystemMetricDoubleDao.class.getName() + ".";

    private final PinotAsyncTemplate asyncTemplate;
    private final TableNameManager tableNameManager;

    public PinotSystemMetricDoubleDao(@Qualifier("pinotAsyncTemplate") PinotAsyncTemplate asyncTemplate,
                                      @Qualifier("systemMetricDoubleTableNameManager") TableNameManager tableNameManager) {
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
        this.tableNameManager = Objects.requireNonNull(tableNameManager, "tableNameManager");
    }

    @Override
    public CompletableFuture<List<DataPoint<Double>>> getAsyncSampledSystemMetricData(MetricDataSearchKey metricDataSearchKey, MetricTag metricTag) {
        String tableName = tableNameManager.getTableName(metricDataSearchKey.getHostGroupName());
        SystemMetricDataSearchKey systemMetricDataSearchKey = new SystemMetricDataSearchKey(tableName, metricDataSearchKey, metricTag);
        return asyncTemplate.selectList(NAMESPACE + "selectSampledSystemMetricData", systemMetricDataSearchKey);
    }
}
