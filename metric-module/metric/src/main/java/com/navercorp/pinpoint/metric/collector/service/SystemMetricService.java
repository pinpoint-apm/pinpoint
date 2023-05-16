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

package com.navercorp.pinpoint.metric.collector.service;

import com.navercorp.pinpoint.metric.collector.dao.SystemMetricDao;

import com.navercorp.pinpoint.metric.common.model.DoubleMetric;
import com.navercorp.pinpoint.metric.common.model.Metrics;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricService {
    private final SystemMetricDao<DoubleMetric> systemMetricDoubleDao;

    public SystemMetricService(SystemMetricDao<DoubleMetric> systemMetricDoubleDao) {
        this.systemMetricDoubleDao = Objects.requireNonNull(systemMetricDoubleDao, "systemMetricDoubleDao");
    }

    public void insert(Metrics systemMetrics) {
        Objects.requireNonNull(systemMetrics, "systemMetrics");
        List<DoubleMetric> doubleMetrics = filterDoubleCounter(systemMetrics);
        systemMetricDoubleDao.insert(systemMetrics.getTenantId(), systemMetrics.getHostGroupName(), systemMetrics.getHostName(), doubleMetrics);
    }

    public List<DoubleMetric> filterDoubleCounter(Metrics systemMetrics) {
        return systemMetrics.stream()
                .filter(DoubleMetric.class::isInstance)
                .map(DoubleMetric.class::cast)
                .collect(Collectors.toList());
    }
}
