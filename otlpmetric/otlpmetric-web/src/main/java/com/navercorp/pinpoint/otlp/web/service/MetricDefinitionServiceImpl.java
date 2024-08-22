/*
 * Copyright 2024 NAVER Corp.
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

package com.navercorp.pinpoint.otlp.web.service;

import com.navercorp.pinpoint.otlp.common.web.definition.property.AggregationFunction;
import com.navercorp.pinpoint.otlp.common.web.definition.property.ChartType;
import com.navercorp.pinpoint.otlp.common.web.definition.property.MetricDefinitionProperty;
import com.navercorp.pinpoint.otlp.common.web.definition.property.MetricGroup;
import com.navercorp.pinpoint.otlp.web.dao.MetricDefinitionDao;
import jakarta.validation.Valid;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author minwoo-jung
 */
@Service
public class MetricDefinitionServiceImpl implements MetricMetadataService {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final MetricDefinitionDao metricDefinitionDao;
    private final List<String> chartTypeList;
    private final List<String> aggregationFunctionList;

    public MetricDefinitionServiceImpl(@Valid MetricDefinitionDao metricDefinitionDao) {
        this.metricDefinitionDao = metricDefinitionDao;
        this.chartTypeList = ChartType.getChartNameList();
        this.aggregationFunctionList = AggregationFunction.getAggregationFunctionNameList();
    }

    @Override
    public MetricDefinitionProperty getMetricDefinitionInfo(String applicationName) {
        List<MetricGroup> metricGroupList = metricDefinitionDao.getMetricGroupList(applicationName);

        MetricDefinitionProperty metricDefinitionProperty = new MetricDefinitionProperty(metricGroupList, chartTypeList, aggregationFunctionList);
        return metricDefinitionProperty;
    }

}
