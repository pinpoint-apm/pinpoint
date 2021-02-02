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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.navercorp.pinpoint.metric.collector.dao.pinot.PinotSystemMetricDoubleDao;
import com.navercorp.pinpoint.metric.collector.dao.pinot.PinotSystemMetricLongDao;

import com.navercorp.pinpoint.metric.common.model.DoubleCounter;
import com.navercorp.pinpoint.metric.common.model.LongCounter;
import com.navercorp.pinpoint.metric.common.model.SystemMetric;
import com.navercorp.pinpoint.metric.common.model.SystemMetricMetadata;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author Hyunjoon Cho
 */
@Service
public class SystemMetricService<T extends SystemMetric> {
    private final PinotSystemMetricLongDao pinotSystemMetricLongDao;
    private final PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao;

    public SystemMetricService(PinotSystemMetricLongDao pinotSystemMetricLongDao,
                               PinotSystemMetricDoubleDao pinotSystemMetricDoubleDao) {
        this.pinotSystemMetricLongDao = Objects.requireNonNull(pinotSystemMetricLongDao, "pinotSystemMetricLongDao");
        this.pinotSystemMetricDoubleDao = Objects.requireNonNull(pinotSystemMetricDoubleDao, "pinotSystemMetricDoubleDao");
    }

    public void insert(String applicationName, List<T> systemMetricList) throws JsonProcessingException {
        List<LongCounter> longCounters = filterLongCounter(systemMetricList);
        List<DoubleCounter> doubleCounters = filterDoubleCounter(systemMetricList);
        pinotSystemMetricLongDao.insert(applicationName, longCounters);
        pinotSystemMetricDoubleDao.insert(applicationName, doubleCounters);
    }

    public List<LongCounter> filterLongCounter(List<T> systemMetrics) {
        return systemMetrics.stream().filter(LongCounter.class::isInstance)
                .map(LongCounter.class::cast).collect(Collectors.toList());
    }

    public List<DoubleCounter> filterDoubleCounter(List<T> systemMetrics) {
        return systemMetrics.stream().filter(DoubleCounter.class::isInstance)
                .map(DoubleCounter.class::cast).collect(Collectors.toList());
    }
}
