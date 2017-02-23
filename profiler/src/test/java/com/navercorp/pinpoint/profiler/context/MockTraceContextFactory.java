/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.profiler.context;

import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.provider.PluginMonitorContextProvider;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataCacheService;
import com.navercorp.pinpoint.profiler.sampler.SamplerFactory;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.LoggingDataSender;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;

/**
 * @author emeroad
 */
public class MockTraceContextFactory {

    private static final boolean TRACE_ACTIVE_THREAD = true;
    private static final boolean TRACE_DATASOURCE = false;

    private final AgentInformation agentInformation;

    private final StorageFactory storageFactory;

    private final AtomicIdGenerator idGenerator;
    private final Sampler sampler;
    private final ActiveTraceRepository activeTraceRepository;

    private final PluginMonitorContext pluginMonitorContext;

    private final ServerMetaDataHolder serverMetaDataHolder;

    private final EnhancedDataSender enhancedDataSender;

    private final ApiMetaDataCacheService apiMetaDataCacheService;
    private final StringMetaDataCacheService stringMetaDataCacheService;
    private final SqlMetaDataCacheService sqlMetaDataCacheService;

    private final TraceContext traceContext;

    public static TraceContext newTestTraceContext(ProfilerConfig profilerConfig) {
        MockTraceContextFactory mockTraceContextFactory = newTestTraceContextFactory(profilerConfig);
        return mockTraceContextFactory.getTraceContext();
    }

    public static MockTraceContextFactory newTestTraceContextFactory(ProfilerConfig profilerConfig) {

        return new MockTraceContextFactory(profilerConfig);
    }


    public MockTraceContextFactory(ProfilerConfig profilerConfig) {
        this.agentInformation = new TestAgentInformation();

        this.storageFactory = new LogStorageFactory();

        final SamplerFactory samplerFactory = new SamplerFactory();
        this.sampler = createSampler(profilerConfig, samplerFactory);

        this.idGenerator = new AtomicIdGenerator();
        this.activeTraceRepository = newActiveTraceRepository();

        final TraceFactoryBuilder traceFactoryBuilder = new DefaultTraceFactoryBuilder(storageFactory, sampler, idGenerator, activeTraceRepository);
        final PluginMonitorContextProvider pluginMonitorContextBuilder = new PluginMonitorContextProvider(TRACE_DATASOURCE);
        this.pluginMonitorContext = pluginMonitorContextBuilder.get();

        this.serverMetaDataHolder = new DefaultServerMetaDataHolder(RuntimeMXBeanUtils.getVmArgs());

        final String agentId = agentInformation.getAgentId();
        final long agentStartTime = agentInformation.getStartTime();
        this.enhancedDataSender = new LoggingDataSender();

        this.apiMetaDataCacheService = new ApiMetaDataCacheService(agentId, agentStartTime, enhancedDataSender);
        this.stringMetaDataCacheService = new StringMetaDataCacheService(agentId, agentStartTime, enhancedDataSender);

        final int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        this.sqlMetaDataCacheService = new SqlMetaDataCacheService(agentId, agentStartTime, enhancedDataSender, jdbcSqlCacheSize);

        this.traceContext = new DefaultTraceContext(profilerConfig, agentInformation,
                traceFactoryBuilder, pluginMonitorContext, serverMetaDataHolder,
                apiMetaDataCacheService, stringMetaDataCacheService, sqlMetaDataCacheService,
                DisabledJdbcUrlParserContext.INSTANCE
        );
    }

    private Sampler createSampler(ProfilerConfig profilerConfig, SamplerFactory samplerFactory) {
        boolean samplingEnable = profilerConfig.isSamplingEnable();
        int samplingRate = profilerConfig.getSamplingRate();
        return samplerFactory.createSampler(samplingEnable, samplingRate);
    }


    private static ActiveTraceRepository newActiveTraceRepository() {
        if (TRACE_ACTIVE_THREAD) {
            return new ActiveTraceRepository();
        }
        return null;
    }

    public AgentInformation getAgentInformation() {
        return agentInformation;
    }

    public StorageFactory getStorageFactory() {
        return storageFactory;
    }

    public IdGenerator getIdGenerator() {
        return idGenerator;
    }

    public Sampler getSampler() {
        return sampler;
    }

    public ActiveTraceRepository getActiveTraceRepository() {
        return activeTraceRepository;
    }

    public PluginMonitorContext getPluginMonitorContext() {
        return pluginMonitorContext;
    }

    public ServerMetaDataHolder getServerMetaDataHolder() {
        return serverMetaDataHolder;
    }

    public EnhancedDataSender getEnhancedDataSender() {
        return enhancedDataSender;
    }

    public ApiMetaDataCacheService getApiMetaDataCacheService() {
        return apiMetaDataCacheService;
    }

    public StringMetaDataCacheService getStringMetaDataCacheService() {
        return stringMetaDataCacheService;
    }

    public SqlMetaDataCacheService getSqlMetaDataCacheService() {
        return sqlMetaDataCacheService;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }
}
