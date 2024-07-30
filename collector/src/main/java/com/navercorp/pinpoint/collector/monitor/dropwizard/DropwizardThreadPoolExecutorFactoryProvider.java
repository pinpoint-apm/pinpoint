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
package com.navercorp.pinpoint.collector.monitor.dropwizard;

import com.codahale.metrics.MetricRegistry;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactory;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactoryProvider;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author intr3p1d
 */
public class DropwizardThreadPoolExecutorFactoryProvider implements MonitoredThreadPoolExecutorFactoryProvider {

    private final MetricRegistry metricRegistry;

    public DropwizardThreadPoolExecutorFactoryProvider(
            @Autowired(required = false) MetricRegistry metricRegistry
    ) {
        this.metricRegistry = metricRegistry;
    }

    public MonitoredThreadPoolExecutorFactory newFactory(String beanName, MonitoringExecutorProperties properties) {
        if (properties.isMonitorEnable()) {
            return new DropwizardThreadPoolExecutorFactory(beanName, metricRegistry, properties.getLogRate());
        }
        return null;
    }
}
