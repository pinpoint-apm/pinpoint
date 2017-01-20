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

import com.navercorp.pinpoint.bootstrap.context.DataSourceMonitor;
import com.navercorp.pinpoint.bootstrap.context.PluginMonitorContext;
import com.navercorp.pinpoint.bootstrap.context.PluginMonitorRegistry;

/**
 * @author Taejin Koo
 */
public class DefaultPluginMonitorContext implements PluginMonitorContext {

    private final DataSourceMonitorList dataSourceMonitorList;

    // it will be changed using ProfilerConfig
    public DefaultPluginMonitorContext() {
        this(5);
    }

    public DefaultPluginMonitorContext(int limitIdNumber) {
        dataSourceMonitorList = new DataSourceMonitorList(limitIdNumber);
    }

    @Override
    public PluginMonitorRegistry<DataSourceMonitor> getDataSourceMonitorRegistry() {
        return dataSourceMonitorList;
    }

    public PluginMonitorWrapperLocator<DataSourceMonitorWrapper> getDataSourceMonitorLocator() {
        return dataSourceMonitorList;
    }

}
