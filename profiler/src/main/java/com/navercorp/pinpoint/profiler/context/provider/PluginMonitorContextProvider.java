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
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcUrlParserManager;
import com.navercorp.pinpoint.profiler.context.monitor.DefaultPluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.monitor.DisabledPluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorContext;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PluginMonitorContextProvider implements Provider<PluginMonitorContext> {

    private final boolean traceAgentDataSource;
    private final JdbcUrlParserManager jdbcUrlParserManager;

    @Inject
    public PluginMonitorContextProvider(ProfilerConfig profilerConfig, JdbcUrlParserManager jdbcUrlParserManager) {
        this(profilerConfig.isTraceAgentDataSource(), jdbcUrlParserManager);
    }

    public PluginMonitorContextProvider(boolean traceAgentDataSource, JdbcUrlParserManager jdbcUrlParserManager) {
        this.traceAgentDataSource = traceAgentDataSource;
        this.jdbcUrlParserManager = jdbcUrlParserManager;
    }


    public PluginMonitorContext get() {
        if (traceAgentDataSource) {
            return new DefaultPluginMonitorContext();
        }

        return new DisabledPluginMonitorContext();
    }

}
