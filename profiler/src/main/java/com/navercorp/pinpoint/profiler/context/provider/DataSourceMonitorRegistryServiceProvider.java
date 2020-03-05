/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.provider;

import com.google.inject.Inject;
import com.google.inject.Provider;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.DefaultDataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.DisabledDataSourceMonitorRegistryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DataSourceMonitorRegistryServiceProvider implements Provider<DataSourceMonitorRegistryService> {

    private static final int DEFAULT_LIMIT_SIZE = 20;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final boolean traceAgentDataSource;
    private final int dataSourceTraceLimitSize;

    @Inject
    public DataSourceMonitorRegistryServiceProvider(ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig");
        }
        this.traceAgentDataSource = profilerConfig.isTraceAgentDataSource();
        this.dataSourceTraceLimitSize = profilerConfig.getDataSourceTraceLimitSize();

    }

    @Override
    public DataSourceMonitorRegistryService get() {
        if (!traceAgentDataSource) {
            return new DisabledDataSourceMonitorRegistryService();
        }

        if (dataSourceTraceLimitSize <= 0) {
            logger.info("dataSourceTraceLimitSize must greater than 0. It will be set default size {}", DEFAULT_LIMIT_SIZE);
            return new DefaultDataSourceMonitorRegistryService(DEFAULT_LIMIT_SIZE);
        } else {
            return new DefaultDataSourceMonitorRegistryService(dataSourceTraceLimitSize);
        }
    }

}
