/*
 * Copyright 2014 NAVER Corp.
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

package com.navercorp.pinpoint.test;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.plugin.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.DefaultTraceFactoryBuilder;
import com.navercorp.pinpoint.profiler.context.IdGenerator;
import com.navercorp.pinpoint.profiler.context.PluginMonitorContextBuilder;
import com.navercorp.pinpoint.profiler.context.TraceFactoryBuilder;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.sampler.TrueSampler;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;

/**
 * @author emeroad
 */
public class MockTraceContextFactory {

    private static final boolean TRACE_ACTIVE_THREAD = true;
    private static final boolean TRACE_DATASOURCE = false;

    private EnhancedDataSender priorityDataSender = new LoggingDataSender();

    public void setPriorityDataSender(EnhancedDataSender priorityDataSender) {
        if (priorityDataSender == null) {
            throw new NullPointerException("priorityDataSender must not be null");
        }
        this.priorityDataSender = priorityDataSender;
    }

    public TraceContext create() {
        ProfilerConfig profilerConfig = new DefaultProfilerConfig();
        TraceContext traceContext = newTestTraceContext(profilerConfig) ;
        ((DefaultTraceContext)traceContext).setPriorityDataSender(priorityDataSender);

        return traceContext;
    }

    public static TraceContext newTestTraceContext(ProfilerConfig profilerConfig) {

        AgentInformation agentInformation = new TestAgentInformation();

        StorageFactory logStorageFactory = new LogStorageFactory();
        Sampler sampler = new TrueSampler();
        IdGenerator idGenerator = new IdGenerator();
        ActiveTraceRepository activeTraceLocator = newActiveTraceRepository();
        TraceFactoryBuilder traceFactoryBuilder = new DefaultTraceFactoryBuilder(logStorageFactory, sampler, idGenerator, activeTraceLocator);


        PluginMonitorContextBuilder pluginMonitorContextBuilder = new PluginMonitorContextBuilder(TRACE_DATASOURCE);
        PluginMonitorContext pluginMonitorContext = pluginMonitorContextBuilder.build();

        ServerMetaDataHolder serverMetaDataHolder = new DefaultServerMetaDataHolder(RuntimeMXBeanUtils.getVmArgs());

        return new DefaultTraceContext(profilerConfig, idGenerator, agentInformation, traceFactoryBuilder, pluginMonitorContext, serverMetaDataHolder);
    }

    private static ActiveTraceRepository newActiveTraceRepository() {
        if (TRACE_ACTIVE_THREAD) {
            return new ActiveTraceRepository();
        }
        return null;
    }
}
