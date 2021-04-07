/*
 * Copyright 2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.LongMetric;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Service
public class SystemMetricDataTypeServiceImpl implements SystemMetricDataTypeService {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final MetricDataTypeCache metricDataTypeCache;

    public SystemMetricDataTypeServiceImpl(MetricDataTypeCache metricDataTypeCache) {
        this.metricDataTypeCache = Objects.requireNonNull(metricDataTypeCache, "metricDataTypeCache");
    }

    @Override
    public void saveMetricDataType(SystemMetric systemMetric) {
        MetricDataName metricDataName = new MetricDataName(systemMetric.getMetricName(), systemMetric.getFieldName());
        MetricData metricData = metricDataTypeCache.getMetricDataType(metricDataName);
        if (!Objects.isNull(metricData)) {
            // cache hit
            return;
        }

        if (systemMetric instanceof LongMetric) {
            metricDataTypeCache.saveMetricDataType(metricDataName, new MetricData(systemMetric.getMetricName(), systemMetric.getFieldName(), MetricDataType.LONG));
        } else if (systemMetric instanceof DoubleMetric) {
            metricDataTypeCache.saveMetricDataType(metricDataName, new MetricData(systemMetric.getMetricName(), systemMetric.getFieldName(), MetricDataType.DOUBLE));
        } else {
            logger.error("can not find metric data type.  systemMetric : {}", systemMetric);
        }
    }
}
