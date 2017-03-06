/*
 * Copyright 2017 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.monitor;

import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;

/**
 * @author Taejin Koo
 */
public class DefaultPluginMonitorContext implements PluginMonitorContext {

    private static final int DEFAULT_LIMIT_SIZE = 20;

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final DataSourceMonitorList dataSourceMonitorList;

    // it will be changed using ProfilerConfig
    public DefaultPluginMonitorContext() {
        this(DEFAULT_LIMIT_SIZE);
    }

    public DefaultPluginMonitorContext(int dataSourceTraceLimitSize) {
        if (dataSourceTraceLimitSize <= 0) {
            logger.info("dataSourceTraceLimitSize must greater than 0. It will be set default size {}", DEFAULT_LIMIT_SIZE);
            dataSourceMonitorList = new DataSourceMonitorList(DEFAULT_LIMIT_SIZE);
        } else {
            dataSourceMonitorList = new DataSourceMonitorList(dataSourceTraceLimitSize);
        }
    }

    @Override
    public DataSourceMonitorRegistry getDataSourceMonitorRegistry() {
        return dataSourceMonitorList;
    }

    public PluginMonitorWrapperLocator<DataSourceMonitorWrapper> getDataSourceMonitorLocator() {
        return dataSourceMonitorList;
    }

}
