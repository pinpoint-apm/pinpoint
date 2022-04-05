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

import com.navercorp.pinpoint.metric.collector.cache.MetricCacheConfiguration;
import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.Objects;

/**
 * @author minwoo.jung
 */
@Component
public class MetricDataTypeCache {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final SystemMetricDataTypeDao systemMetricDataTypeDao;

    public MetricDataTypeCache(SystemMetricDataTypeDao systemMetricDataTypeDao) {
        this.systemMetricDataTypeDao = Objects.requireNonNull(systemMetricDataTypeDao, "systemMetricDataTypeDao");
    }

    @Cacheable(cacheNames = "metricDataType", key = "#metricDataName", cacheManager = MetricCacheConfiguration.METRIC_DATA_TYPE_CACHE_NAME)
    public MetricData getMetricDataType(MetricDataName metricDataName) {
        MetricData metricData =  systemMetricDataTypeDao.selectMetricDataType(metricDataName);

        if (logger.isDebugEnabled()) {
            logger.debug("called getMetricDataType method. metricDataName: {}, metricData : {}", metricDataName, metricData);
        }

        return metricData;
    }

    @CachePut(cacheNames = "metricDataType", key = "#metricDataName", cacheManager = MetricCacheConfiguration.METRIC_DATA_TYPE_CACHE_NAME)
    public MetricData saveMetricDataType(MetricDataName metricDataName, MetricData metricData) {
        if (logger.isDebugEnabled()) {
            logger.debug("called saveMetricDataType method. metricDataName: {}, metricData : {}", metricDataName, metricData);
        }

        systemMetricDataTypeDao.updateMetricDataType(metricData);
        return metricData;
    }
}
