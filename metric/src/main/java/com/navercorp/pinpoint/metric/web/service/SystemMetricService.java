/*
 * Copyright 2020 NAVER Corp.
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

package com.navercorp.pinpoint.metric.web.service;


import com.navercorp.pinpoint.metric.common.model.MetricType;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.SystemMetricMetadata;
import com.navercorp.pinpoint.metric.web.dao.pinot.PinotSystemMetricDoubleDao;
import com.navercorp.pinpoint.metric.web.dao.pinot.PinotSystemMetricLongDao;
import com.navercorp.pinpoint.metric.web.util.TimeWindow;
import com.navercorp.pinpoint.metric.web.util.QueryParameter;
import com.navercorp.pinpoint.metric.web.model.SampledSystemMetric;
import com.navercorp.pinpoint.metric.web.model.chart.SystemMetricChart;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricService {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PinotSystemMetricLongDao pinotSystemMetricLongDao;
    private final PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao;
    private final SystemMetricMetadata systemMetricMetadata;

    public SystemMetricService(PinotSystemMetricLongDao pinotSystemMetricLongDao,
                               PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao,
                               SystemMetricMetadata systemMetricMetadata) {
        this.pinotSystemMetricLongDao = Objects.requireNonNull(pinotSystemMetricLongDao, "pinotSystemMetricLongDao");
        this.pinotSystemMetricDoubleDao = Objects.requireNonNull(pinotSystemMetricDoubleDao, "pinotSystemMetricDoubleDao");
        this.systemMetricMetadata = Objects.requireNonNull(systemMetricMetadata, "systemMetricMetadata");
    }

    public List<SystemMetric> getSystemMetricBoList(QueryParameter queryParameter) {
        MetricType metricType = systemMetricMetadata.get(queryParameter.getMetricName(), queryParameter.getFieldName());

        switch (metricType) {
            case LongCounter:
                return pinotSystemMetricLongDao.getSystemMetric(queryParameter);
            case DoubleCounter:
                return pinotSystemMetricDoubleDao.getSystemMetric(queryParameter);
            default:
                throw new RuntimeException("No Such Metric");
        }
    }

    public SystemMetricChart getSystemMetricChart(TimeWindow timeWindow, QueryParameter queryParameter) {
        String metricName = queryParameter.getMetricName();
        String fieldName = queryParameter.getFieldName();

        MetricType metricType = systemMetricMetadata.get(metricName, fieldName);
        String chartName = getChartName(metricName, fieldName);

        switch (metricType) {
            case LongCounter:
                List<SampledSystemMetric<Long>> sampledLongSystemMetrics = pinotSystemMetricLongDao.getSampledSystemMetric(queryParameter);
                return new SystemMetricChart(timeWindow, chartName, sampledLongSystemMetrics);
            case DoubleCounter:
                List<SampledSystemMetric<Double>> sampledDoubleSystemMetrics = pinotSystemMetricDoubleDao.getSampledSystemMetric(queryParameter);
                return new SystemMetricChart(timeWindow, chartName, sampledDoubleSystemMetrics);
            default:
                throw new RuntimeException("No Such Metric");
        }
    }

    private String getChartName(String metricName, String fieldName) {
        return metricName + "_" + fieldName;
    }
}
