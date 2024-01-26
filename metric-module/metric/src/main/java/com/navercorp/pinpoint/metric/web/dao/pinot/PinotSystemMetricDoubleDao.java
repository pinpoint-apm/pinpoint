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

import com.navercorp.pinpoint.metric.common.model.MetricTag;
import com.navercorp.pinpoint.metric.web.dao.SystemMetricDao;
import com.navercorp.pinpoint.metric.web.dao.model.SystemMetricDataSearchKey;
import com.navercorp.pinpoint.metric.web.model.MetricDataSearchKey;
import com.navercorp.pinpoint.metric.common.model.chart.SystemMetricPoint;
import com.navercorp.pinpoint.pinot.mybatis.PinotAsyncTemplate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Future;

/**
 * @author Hyunjoon Cho
 */
@Repository
public class PinotSystemMetricDoubleDao implements SystemMetricDao<Double> {
    private final Logger logger = LogManager.getLogger(this.getClass());

    private static final String NAMESPACE = PinotSystemMetricDoubleDao.class.getName() + ".";

    private final SqlSessionTemplate sqlPinotSessionTemplate;
    private final PinotAsyncTemplate asyncTemplate;

    public PinotSystemMetricDoubleDao(SqlSessionTemplate sqlPinotSessionTemplate, @Qualifier("pinotAsyncTemplate") PinotAsyncTemplate asyncTemplate) {
        this.sqlPinotSessionTemplate = Objects.requireNonNull(sqlPinotSessionTemplate, "sqlPinotSessionTemplate");
        this.asyncTemplate = Objects.requireNonNull(asyncTemplate, "asyncTemplate");
    }

    @Override
    public Future<List<SystemMetricPoint<Double>>> getAsyncSampledSystemMetricData(MetricDataSearchKey metricDataSearchKey, MetricTag metricTag) {
        SystemMetricDataSearchKey systemMetricDataSearchKey = new SystemMetricDataSearchKey(metricDataSearchKey, metricTag);
        return asyncTemplate.selectList(NAMESPACE + "selectSampledSystemMetricData", systemMetricDataSearchKey);
    }
}
