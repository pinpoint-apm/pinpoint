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

import com.google.inject.util.Providers;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.id.DefaultAsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.DefaultCallStackFactory;
import com.navercorp.pinpoint.profiler.context.recorder.DefaultRecorderFactory;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.context.DefaultSpanFactory;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceIdFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.SpanFactory;
import com.navercorp.pinpoint.profiler.context.id.TraceIdFactory;
import com.navercorp.pinpoint.profiler.context.provider.TraceFactoryProvider;
import com.navercorp.pinpoint.profiler.context.monitor.DisabledJdbcContext;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
import com.navercorp.pinpoint.profiler.context.active.DefaultActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.storage.LogStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.DefaultApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.DefaultSqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.DefaultStringMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
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

    private final ServerMetaDataHolder serverMetaDataHolder;

    private final EnhancedDataSender enhancedDataSender;

    private final ApiMetaDataService apiMetaDataService;
    private final StringMetaDataService stringMetaDataService;
    private final SqlMetaDataService sqlMetaDataService;

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

        final AsyncIdGenerator asyncIdGenerator = new DefaultAsyncIdGenerator();
        this.serverMetaDataHolder = new DefaultServerMetaDataHolder(RuntimeMXBeanUtils.getVmArgs());

        final String applicationName = agentInformation.getAgentId();
        final String agentId = agentInformation.getAgentId();
        final long agentStartTime = agentInformation.getStartTime();
        final ServiceType agentServiceType = agentInformation.getServerType();
        this.enhancedDataSender = new LoggingDataSender();

        this.apiMetaDataService = new DefaultApiMetaDataService(agentId, agentStartTime, enhancedDataSender);
        this.stringMetaDataService = new DefaultStringMetaDataService(agentId, agentStartTime, enhancedDataSender);

        final int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        this.sqlMetaDataService = new DefaultSqlMetaDataService(agentId, agentStartTime, enhancedDataSender, jdbcSqlCacheSize);


        CallStackFactory callStackFactory = new DefaultCallStackFactory(64);
        TraceIdFactory traceIdFactory = new DefaultTraceIdFactory(agentId, agentStartTime, idGenerator);
        SpanFactory spanFactory = new DefaultSpanFactory(applicationName, agentId, agentStartTime, agentServiceType);

        RecorderFactory recorderFactory = new DefaultRecorderFactory(stringMetaDataService, sqlMetaDataService);

        final TraceFactoryProvider traceFactoryBuilder = new TraceFactoryProvider(callStackFactory, storageFactory, sampler, idGenerator, traceIdFactory, asyncIdGenerator,
                Providers.of(activeTraceRepository), spanFactory, recorderFactory);
        TraceFactory traceFactory = traceFactoryBuilder.get();
        this.traceContext = new DefaultTraceContext(profilerConfig, agentInformation,
                traceIdFactory, traceFactory, asyncIdGenerator, serverMetaDataHolder,
                apiMetaDataService, stringMetaDataService, sqlMetaDataService,
                DisabledJdbcContext.INSTANCE
        );
    }

    private Sampler createSampler(ProfilerConfig profilerConfig, SamplerFactory samplerFactory) {
        boolean samplingEnable = profilerConfig.isSamplingEnable();
        int samplingRate = profilerConfig.getSamplingRate();
        return samplerFactory.createSampler(samplingEnable, samplingRate);
    }


    private static ActiveTraceRepository newActiveTraceRepository() {
        if (TRACE_ACTIVE_THREAD) {
            return new DefaultActiveTraceRepository();
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


    public ServerMetaDataHolder getServerMetaDataHolder() {
        return serverMetaDataHolder;
    }

    public EnhancedDataSender getEnhancedDataSender() {
        return enhancedDataSender;
    }

    public ApiMetaDataService getApiMetaDataCacheService() {
        return apiMetaDataService;
    }

    public StringMetaDataService getStringMetaDataCacheService() {
        return stringMetaDataService;
    }

    public SqlMetaDataService getSqlMetaDataCacheService() {
        return sqlMetaDataService;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }
}
