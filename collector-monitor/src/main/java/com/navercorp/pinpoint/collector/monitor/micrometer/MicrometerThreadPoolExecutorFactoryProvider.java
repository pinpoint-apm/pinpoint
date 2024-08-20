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
package com.navercorp.pinpoint.collector.monitor.micrometer;

import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactoryProvider;
import com.navercorp.pinpoint.collector.monitor.MonitoredThreadPoolExecutorFactory;
import com.navercorp.pinpoint.common.server.thread.MonitoringExecutorProperties;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author intr3p1d
 */
public class MicrometerThreadPoolExecutorFactoryProvider implements MonitoredThreadPoolExecutorFactoryProvider {
    private final MeterRegistry meterRegistry;

    public MicrometerThreadPoolExecutorFactoryProvider(
            @Autowired(required = false) MeterRegistry meterRegistry
    ) {
        this.meterRegistry = meterRegistry;
    }

    public MonitoredThreadPoolExecutorFactory newFactory(String beanName, MonitoringExecutorProperties properties) {
        if (properties.isMonitorEnable()) {
            return new MicrometerThreadPoolExecutorFactory(beanName, meterRegistry, properties.getLogRate());
        }
        return null;
    }
}
