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

import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitor;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.DataSourceMonitorRegistry;

/**
 * @author Taejin Koo
 */
public class DisabledPluginMonitorContext implements PluginMonitorContext {

    public static DisabledDataSourceMonitorRegistry DISABLED_DATASOURCE_MONITOR_REGISTRY = new DisabledDataSourceMonitorRegistry();

    @Override
    public DataSourceMonitorRegistry getDataSourceMonitorRegistry() {
        return DISABLED_DATASOURCE_MONITOR_REGISTRY;
    }

    private static class DisabledDataSourceMonitorRegistry implements DataSourceMonitorRegistry {

        @Override
        public boolean register(DataSourceMonitor pluginMonitor) {
            return false;
        }

        @Override
        public boolean unregister(DataSourceMonitor pluginMonitor) {
            return false;
        }

    }

}
