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

package com.navercorp.pinpoint.profiler.context.provider.metric;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.context.monitor.metric.CustomMetricRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.metric.DefaultCustomMetricRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.metric.DisabledCustomMetricRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

/**
 * @author Taejin Koo
 */
public class CustomMetricRegistryServiceProvider implements Provider<CustomMetricRegistryService> {

    private static final int DEFAULT_LIMIT_SIZE = 10;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final MonitorConfig monitorConfig;

    @Inject
    public CustomMetricRegistryServiceProvider(MonitorConfig monitorConfig) {
        this.monitorConfig = Objects.requireNonNull(monitorConfig, "monitorConfig");
    }

    @Override
    public CustomMetricRegistryService get() {
        if (!monitorConfig.isCustomMetricEnable()) {
            return new DisabledCustomMetricRegistryService();
        }

        int customMetricLimitSize = monitorConfig.getCustomMetricLimitSize();
        if (customMetricLimitSize <= 0) {
            logger.info("recordLimitSize must greater than 0. It will be set default size {}", DEFAULT_LIMIT_SIZE);
            return new DefaultCustomMetricRegistryService(DEFAULT_LIMIT_SIZE);
        } else {
            return new DefaultCustomMetricRegistryService(customMetricLimitSize);
        }
    }

}
