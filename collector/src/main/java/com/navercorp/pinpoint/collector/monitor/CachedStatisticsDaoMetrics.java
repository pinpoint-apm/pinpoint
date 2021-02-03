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

package com.navercorp.pinpoint.collector.monitor;

import com.navercorp.pinpoint.collector.dao.hbase.MonitoredCachedStatisticsDao;
import com.navercorp.pinpoint.common.util.StringUtils;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricSet;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CachedStatisticsDaoMetrics implements MetricSet {

    private static final String FLUSH_COUNT = ".flush.count";
    private static final String FLUSH_LAST_TIME_MILLIS = ".flush.lasttimemillis";

    private final List<MonitoredCachedStatisticsDao> monitoredCachedStatisticsDaos;

    public CachedStatisticsDaoMetrics(List<MonitoredCachedStatisticsDao> monitoredCachedStatisticsDaos) {
        this.monitoredCachedStatisticsDaos = Objects.requireNonNull(monitoredCachedStatisticsDaos, "monitoredCachedStatisticsDaos");
    }

    @Override
    public Map<String, Metric> getMetrics() {
        final Map<String, Metric> metrics = new HashMap<>(monitoredCachedStatisticsDaos.size());

        for (MonitoredCachedStatisticsDao monitoredCachedStatisticsDao : monitoredCachedStatisticsDaos) {
            String clazzName = monitoredCachedStatisticsDao.getClass().getSimpleName();

            String[] splittedName = clazzName.split("\\$");
            if (splittedName.length > 1 && StringUtils.hasText(splittedName[0])) {
                clazzName = splittedName[0];
            }

            metrics.put(clazzName + FLUSH_COUNT, new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return monitoredCachedStatisticsDao.getFlushAllCount();
                }
            });

            metrics.put(clazzName + FLUSH_LAST_TIME_MILLIS, new Gauge<Long>() {
                @Override
                public Long getValue() {
                    return monitoredCachedStatisticsDao.getLastFlushTimeMillis();
                }
            });
        }

        return Collections.unmodifiableMap(metrics);
    }

}
