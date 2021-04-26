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

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDataTypeDao;
import com.navercorp.pinpoint.metric.collector.model.MetricTagCollection;
import com.navercorp.pinpoint.metric.common.model.MetricData;
import com.navercorp.pinpoint.metric.common.model.MetricDataName;
import com.navercorp.pinpoint.metric.common.model.MetricDataType;
import com.navercorp.pinpoint.metric.common.model.MetricTagKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CachePut;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

/**
 * @author minwoo.jung
 */
@Component
@Transactional(transactionManager="metricTransactionManager")
public class MetricDataTypeCache {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final SystemMetricDataTypeDao systemMetricDataTypeDao;

    public MetricDataTypeCache(SystemMetricDataTypeDao systemMetricDataTypeDao) {
        this.systemMetricDataTypeDao = Objects.requireNonNull(systemMetricDataTypeDao, "systemMetricDataTypeDao");
    }

    @Cacheable(value="metricDataType", key="#metricDataName")
    public MetricData getMetricDataType(MetricDataName metricDataName) {
        MetricData metricData =  systemMetricDataTypeDao.selectMetricDataType(metricDataName);

        if (logger.isDebugEnabled()) {
            logger.debug("called getMetricDataType method. metricDataName: {}, metricData : {}", metricDataName, metricData);
        }

        return metricData;
    }

    @CachePut(value="metricDataType", key="#metricDataName")
    public MetricData saveMetricDataType(MetricDataName metricDataName, MetricData metricData) {
        if (logger.isDebugEnabled()) {
            logger.debug("called saveMetricDataType method. metricDataName: {}, metricData : {}", metricDataName, metricData);
        }

        systemMetricDataTypeDao.updateMetricDataType(metricData);
        return metricData;
    }
}
