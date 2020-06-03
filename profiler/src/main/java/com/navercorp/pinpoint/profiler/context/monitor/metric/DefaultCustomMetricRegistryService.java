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

package com.navercorp.pinpoint.profiler.context.monitor.metric;

import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.CustomMetric;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.DoubleGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.IntGauge;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongCounter;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.metric.LongGauge;
import com.navercorp.pinpoint.common.util.Assert;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Taejin Koo
 */
public class DefaultCustomMetricRegistryService implements CustomMetricRegistryService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCustomMetricRegistryService.class);

    private final CustomMetricWrapperFactory customMetricWrapperFactory = new CustomMetricWrapperFactory();

    private final Map<String, CustomMetricWrapper> customMetricWrapperMap = new ConcurrentHashMap<String, CustomMetricWrapper>();

    private final CustomMetricIdGenerator customMetricIdGenerator;
    private final CustomMetricRegistryFilter filter;

    public DefaultCustomMetricRegistryService(int limitIdNumber) {
        this(limitIdNumber, new DefaultCustomMetricRegistryFilter());
    }

    public DefaultCustomMetricRegistryService(int limitIdNumber, CustomMetricRegistryFilter filter) {
        this.customMetricIdGenerator = new CustomMetricIdGenerator(limitIdNumber);
        this.filter = Assert.requireNonNull(filter, "filter");
    }

    @Override
    public boolean register(IntCounter intCounter) {
        Assert.requireNonNull(intCounter, "intCount");

        boolean filter = this.filter.filter(intCounter);
        if (filter) {
            LOGGER.warn("Failed to register CustomMetric({}). message:not allowed metric", intCounter);
            return false;
        }

        int id = customMetricIdGenerator.create(intCounter.getName());
        if (id == CustomMetricIdGenerator.NOT_REGISTERED) {
            LOGGER.warn("Failed to create metricId. metric:{}", intCounter);
            return false;
        }

        IntCounterWrapper customMetricWrapper = customMetricWrapperFactory.create(id, intCounter);
        return add(customMetricWrapper);
    }

    @Override
    public boolean register(LongCounter longCounter) {
        Assert.requireNonNull(longCounter, "longCount");

        boolean filter = this.filter.filter(longCounter);
        if (filter) {
            LOGGER.warn("Failed to register CustomMetric({}). message:not allowed metric", longCounter);
            return false;
        }

        int id = customMetricIdGenerator.create(longCounter.getName());
        if (id == CustomMetricIdGenerator.NOT_REGISTERED) {
            LOGGER.warn("Failed to create metricId. metric:{}", longCounter);
            return false;
        }

        LongCounterWrapper customMetricWrapper = customMetricWrapperFactory.create(id, longCounter);
        return add(customMetricWrapper);
    }

    @Override
    public boolean register(IntGauge intGauge) {
        Assert.requireNonNull(intGauge, "intGauge");

        boolean filter = this.filter.filter(intGauge);
        if (filter) {
            LOGGER.warn("Failed to register CustomMetric({}). message:not allowed metric", intGauge);
            return false;
        }

        int id = customMetricIdGenerator.create(intGauge.getName());
        if (id == CustomMetricIdGenerator.NOT_REGISTERED) {
            LOGGER.warn("Failed to create metricId. metric:{}", intGauge);
            return false;
        }

        IntGaugeWrapper customMetricWrapper = customMetricWrapperFactory.create(id, intGauge);
        return add(customMetricWrapper);
    }

    @Override
    public boolean register(LongGauge longGauge) {
        Assert.requireNonNull(longGauge, "longGauge");

        boolean filter = this.filter.filter(longGauge);
        if (filter) {
            LOGGER.warn("Failed to register CustomMetric({}). message:not allowed metric", longGauge);
            return false;
        }

        int id = customMetricIdGenerator.create(longGauge.getName());
        if (id == CustomMetricIdGenerator.NOT_REGISTERED) {
            LOGGER.warn("Failed to create metricId. metric:{}", longGauge);
            return false;
        }

        LongGaugeWrapper customMetricWrapper = customMetricWrapperFactory.create(id, longGauge);
        return add(customMetricWrapper);
    }

    @Override
    public boolean register(DoubleGauge doubleGauge) {
        Assert.requireNonNull(doubleGauge, "doubleGauge");

        boolean filter = this.filter.filter(doubleGauge);
        if (filter) {
            LOGGER.warn("Failed to register CustomMetric({}). message:not allowed metric", doubleGauge);
            return false;
        }

        int id = customMetricIdGenerator.create(doubleGauge.getName());
        if (id == CustomMetricIdGenerator.NOT_REGISTERED) {
            LOGGER.warn("Failed to create metricId. metric:{}", doubleGauge);
            return false;
        }

        DoubleGaugeWrapper customMetricWrapper = customMetricWrapperFactory.create(id, doubleGauge);
        return add(customMetricWrapper);
    }

    private boolean add(CustomMetricWrapper customMetricWrapper) {
        CustomMetricWrapper put = customMetricWrapperMap.put(customMetricWrapper.getName(), customMetricWrapper);
        return put == null;
    }

    @Override
    public boolean unregister(IntCounter customMetric) {
        return remove(customMetric);
    }

    @Override
    public boolean unregister(LongCounter customMetric) {
        return remove(customMetric);
    }

    @Override
    public boolean unregister(IntGauge customMetric) {
        return remove(customMetric);
    }

    @Override
    public boolean unregister(LongGauge customMetric) {
        return remove(customMetric);
    }

    @Override
    public boolean unregister(DoubleGauge customMetric) {
        return remove(customMetric);
    }

    private boolean remove(CustomMetric customMetric) {
        CustomMetricWrapper customMetricWrapper = customMetricWrapperMap.get(customMetric.getName());

        if (customMetricWrapper == null) {
            return false;
        }

        if (customMetricWrapper.equalsWithUnwrap(customMetric)) {
            CustomMetricWrapper remove = customMetricWrapperMap.remove(customMetric.getName());
            return remove != null;
        }
        return false;
    }

    @Override
    public Map<String, CustomMetricWrapper> getCustomMetricMap() {
        return customMetricWrapperMap;
    }

}

