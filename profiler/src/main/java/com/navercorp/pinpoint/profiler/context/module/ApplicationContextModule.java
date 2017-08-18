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

package com.navercorp.pinpoint.profiler.context.module;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Names;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.Trace;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.JdbcContext;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DefaultDynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.DynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.AsyncContextFactory;
import com.navercorp.pinpoint.profiler.context.AsyncTraceContext;
import com.navercorp.pinpoint.profiler.context.BaseTraceFactory;
import com.navercorp.pinpoint.profiler.context.Binder;
import com.navercorp.pinpoint.profiler.context.CallStackFactory;
import com.navercorp.pinpoint.profiler.context.DefaultSpanFactory;
import com.navercorp.pinpoint.profiler.context.ServerMetaDataRegistryService;
import com.navercorp.pinpoint.profiler.context.SpanChunkFactory;
import com.navercorp.pinpoint.profiler.context.SpanFactory;
import com.navercorp.pinpoint.profiler.context.SpanPostProcessor;
import com.navercorp.pinpoint.profiler.context.ThreadLocalBinder;
import com.navercorp.pinpoint.profiler.context.TraceFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.id.AsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.AtomicIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.DefaultAsyncIdGenerator;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceIdFactory;
import com.navercorp.pinpoint.profiler.context.id.DefaultTraceRootFactory;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionCounter;
import com.navercorp.pinpoint.profiler.context.id.DefaultTransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.id.IdGenerator;
import com.navercorp.pinpoint.profiler.context.id.TraceIdFactory;
import com.navercorp.pinpoint.profiler.context.id.TraceRootFactory;
import com.navercorp.pinpoint.profiler.context.id.TransactionCounter;
import com.navercorp.pinpoint.profiler.context.id.TransactionIdEncoder;
import com.navercorp.pinpoint.profiler.context.method.DefaultPredefinedMethodDescriptorRegistry;
import com.navercorp.pinpoint.profiler.context.method.PredefinedMethodDescriptorRegistry;
import com.navercorp.pinpoint.profiler.context.monitor.DataSourceMonitorRegistryService;
import com.navercorp.pinpoint.profiler.context.monitor.DefaultJdbcContext;
import com.navercorp.pinpoint.profiler.context.monitor.JdbcUrlParsingService;
import com.navercorp.pinpoint.profiler.context.provider.ActiveTraceRepositoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.AgentInfoFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.AgentInfoSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.AgentInformationProvider;
import com.navercorp.pinpoint.profiler.context.provider.AgentStartTimeProvider;
import com.navercorp.pinpoint.profiler.context.provider.ApiMetaDataServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.ApplicationServerTypeProvider;
import com.navercorp.pinpoint.profiler.context.provider.AsyncContextFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.AsyncTraceContextProvider;
import com.navercorp.pinpoint.profiler.context.provider.BaseTraceFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.CallStackFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.ClassFileTransformerDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.CommandDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.DataSourceMonitorRegistryServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.DeadlockMonitorProvider;
import com.navercorp.pinpoint.profiler.context.provider.DeadlockThreadRegistryProvider;
import com.navercorp.pinpoint.profiler.context.provider.DynamicTransformTriggerProvider;
import com.navercorp.pinpoint.profiler.context.provider.InstrumentEngineProvider;
import com.navercorp.pinpoint.profiler.context.provider.JdbcUrlParsingServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.JvmInformationProvider;
import com.navercorp.pinpoint.profiler.context.provider.ObjectBinderFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientProvider;
import com.navercorp.pinpoint.profiler.context.provider.PluginContextLoadResultProvider;
import com.navercorp.pinpoint.profiler.context.provider.SamplerProvider;
import com.navercorp.pinpoint.profiler.context.provider.ServerMetaDataHolderProvider;
import com.navercorp.pinpoint.profiler.context.provider.ServerMetaDataRegistryServiceProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanChunkFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.SpanPostProcessorProvider;
import com.navercorp.pinpoint.profiler.context.provider.StorageFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.TcpDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.TraceContextProvider;
import com.navercorp.pinpoint.profiler.context.provider.TraceFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.UdpSpanDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.UdpStatDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.activethread.ActiveTraceMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.activethread.ActiveTraceMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.cpu.CpuLoadMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.cpu.CpuLoadMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.datasource.DataSourceMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.datasource.DataSourceMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.deadlock.DeadlockMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.deadlock.DeadlockMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedGarbageCollectorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.DetailedMemoryMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.GarbageCollectorMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.JvmGcMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.jvmgc.MemoryMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.response.ResponseTimeMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.response.ResponseTimeMetricProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.transaction.TransactionMetricCollectorProvider;
import com.navercorp.pinpoint.profiler.context.provider.stat.transaction.TransactionMetricProvider;
import com.navercorp.pinpoint.profiler.context.recorder.DefaultRecorderFactory;
import com.navercorp.pinpoint.profiler.context.recorder.RecorderFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.DefaultSqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.DefaultStringMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DeadlockMonitor;
import com.navercorp.pinpoint.profiler.monitor.DeadlockThreadRegistry;
import com.navercorp.pinpoint.profiler.monitor.DefaultAgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.AgentStatMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.activethread.ActiveTraceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.cpu.CpuLoadMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.datasource.DataSourceMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.deadlock.DeadlockMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.jvmgc.JvmGcMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.response.ResponseTimeMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.collector.transaction.TransactionMetricCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.activethread.ActiveTraceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.cpu.CpuLoadMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.datasource.DataSourceMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.deadlock.DeadlockMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.DetailedGarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.gc.GarbageCollectorMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.DetailedMemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.memory.MemoryMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ResponseTimeMetric;
import com.navercorp.pinpoint.profiler.monitor.metric.response.ReuseResponseTimeCollector;
import com.navercorp.pinpoint.profiler.monitor.metric.transaction.TransactionMetric;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.AgentInfoFactory;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.thrift.dto.TAgentStat;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextModule extends AbstractModule {
    private final ProfilerConfig profilerConfig;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AgentOption agentOption;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    public ApplicationContextModule(AgentOption agentOption, ProfilerConfig profilerConfig,
                                    ServiceTypeRegistryService serviceTypeRegistryService, InterceptorRegistryBinder interceptorRegistryBinder) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.agentOption = agentOption;
        this.profilerConfig = profilerConfig;
        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
    }

    @Override
    protected void configure() {
        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        bind(ProfilerConfig.class).toInstance(profilerConfig);
        bind(ServiceTypeRegistryService.class).toInstance(serviceTypeRegistryService);
        bind(AgentOption.class).toInstance(agentOption);
        bind(Instrumentation.class).toInstance(agentOption.getInstrumentation());
        bind(InterceptorRegistryBinder.class).toInstance(interceptorRegistryBinder);

        bind(URL[].class).annotatedWith(PluginJars.class).toInstance(agentOption.getPluginJars());

        TypeLiteral<List<String>> listString = new TypeLiteral<List<String>>() {};
        bind(listString).annotatedWith(BootstrapJarPaths.class).toInstance(agentOption.getBootstrapJarPaths());

        bindAgentInformation(agentOption.getAgentId(), agentOption.getApplicationName());

        bindDataTransferComponent();

        bind(ServerMetaDataRegistryService.class).toProvider(ServerMetaDataRegistryServiceProvider.class).in(Scopes.SINGLETON);
        bind(ServerMetaDataHolder.class).toProvider(ServerMetaDataHolderProvider.class).in(Scopes.SINGLETON);
        bind(StorageFactory.class).toProvider(StorageFactoryProvider.class).in(Scopes.SINGLETON);

        bindServiceComponent();

        bind(DataSourceMonitorRegistryService.class).toProvider(DataSourceMonitorRegistryServiceProvider.class).in(Scopes.SINGLETON);

        bind(IdGenerator.class).to(AtomicIdGenerator.class).in(Scopes.SINGLETON);
        bind(AsyncIdGenerator.class).to(DefaultAsyncIdGenerator.class).in(Scopes.SINGLETON);
        bind(TransactionCounter.class).to(DefaultTransactionCounter.class).in(Scopes.SINGLETON);
        bind(TransactionIdEncoder.class).to(DefaultTransactionIdEncoder.class).in(Scopes.SINGLETON);

        bind(Sampler.class).toProvider(SamplerProvider.class).in(Scopes.SINGLETON);


        final TypeLiteral<Binder<Trace>> binder = new TypeLiteral<Binder<Trace>>() {};
        final TypeLiteral<ThreadLocalBinder<Trace>> threadLocalBinder = new TypeLiteral<ThreadLocalBinder<Trace>>() {};
        bind(binder).to(threadLocalBinder).in(Scopes.SINGLETON);
        bind(TraceContext.class).toProvider(TraceContextProvider.class).in(Scopes.SINGLETON);
        bind(AsyncTraceContext.class).toProvider(AsyncTraceContextProvider.class).in(Scopes.SINGLETON);
        bind(AsyncContextFactory.class).toProvider(AsyncContextFactoryProvider.class).in(Scopes.SINGLETON);

        bind(DeadlockThreadRegistry.class).toProvider(DeadlockThreadRegistryProvider.class).in(Scopes.SINGLETON);

        bindTraceComponent();

        bind(ResponseTimeCollector.class).to(ReuseResponseTimeCollector.class).in(Scopes.SINGLETON);
        bind(ActiveTraceRepository.class).toProvider(ActiveTraceRepositoryProvider.class).in(Scopes.SINGLETON);

        bind(PluginContextLoadResult.class).toProvider(PluginContextLoadResultProvider.class).in(Scopes.SINGLETON);

        bind(JdbcContext.class).to(DefaultJdbcContext.class).in(Scopes.SINGLETON);
        bind(JdbcUrlParsingService.class).toProvider(JdbcUrlParsingServiceProvider.class).in(Scopes.SINGLETON);

        bind(AgentInformation.class).toProvider(AgentInformationProvider.class).in(Scopes.SINGLETON);

        bind(InstrumentEngine.class).toProvider(InstrumentEngineProvider.class).in(Scopes.SINGLETON);
        bind(ObjectBinderFactory.class).toProvider(ObjectBinderFactoryProvider.class).in(Scopes.SINGLETON);
        bind(ClassFileTransformerDispatcher.class).toProvider(ClassFileTransformerDispatcherProvider.class).in(Scopes.SINGLETON);
        bind(DynamicTransformerRegistry.class).to(DefaultDynamicTransformerRegistry.class).in(Scopes.SINGLETON);
        bind(DynamicTransformTrigger.class).toProvider(DynamicTransformTriggerProvider.class).in(Scopes.SINGLETON);
//        bind(ClassFileTransformer.class).toProvider(ClassFileTransformerWrapProvider.class).in(Scopes.SINGLETON);

        bindAgentStatComponent();

        bind(JvmInformation.class).toProvider(JvmInformationProvider.class).in(Scopes.SINGLETON);
        bind(AgentInfoFactory.class).toProvider(AgentInfoFactoryProvider.class).in(Scopes.SINGLETON);
        bind(DeadlockMonitor.class).toProvider(DeadlockMonitorProvider.class).in(Scopes.SINGLETON);
        bind(AgentInfoSender.class).toProvider(AgentInfoSenderProvider.class).in(Scopes.SINGLETON);
        bind(AgentStatMonitor.class).to(DefaultAgentStatMonitor.class).in(Scopes.SINGLETON);
    }

    private void bindTraceComponent() {
        bind(TraceRootFactory.class).to(DefaultTraceRootFactory.class).in(Scopes.SINGLETON);
        bind(TraceIdFactory.class).to(DefaultTraceIdFactory.class).in(Scopes.SINGLETON);
        bind(CallStackFactory.class).toProvider(CallStackFactoryProvider.class).in(Scopes.SINGLETON);

        bind(SpanFactory.class).to(DefaultSpanFactory.class).in(Scopes.SINGLETON);
        bind(SpanPostProcessor.class).toProvider(SpanPostProcessorProvider.class).in(Scopes.SINGLETON);
        bind(SpanChunkFactory.class).toProvider(SpanChunkFactoryProvider.class).in(Scopes.SINGLETON);

        bind(RecorderFactory.class).to(DefaultRecorderFactory.class).in(Scopes.SINGLETON);

        bind(BaseTraceFactory.class).toProvider(BaseTraceFactoryProvider.class).in(Scopes.SINGLETON);;
        bind(TraceFactory.class).toProvider(TraceFactoryProvider.class).in(Scopes.SINGLETON);
    }

    private void bindDataTransferComponent() {
        // create tcp channel

        bind(PinpointClientFactory.class).toProvider(PinpointClientFactoryProvider.class).in(Scopes.SINGLETON);
        bind(EnhancedDataSender.class).toProvider(TcpDataSenderProvider.class).in(Scopes.SINGLETON);
        bind(PinpointClient.class).toProvider(PinpointClientProvider.class).in(Scopes.SINGLETON);

        bind(CommandDispatcher.class).toProvider(CommandDispatcherProvider.class).in(Scopes.SINGLETON);

        bind(DataSender.class).annotatedWith(SpanDataSender.class)
                .toProvider(UdpSpanDataSenderProvider.class).in(Scopes.SINGLETON);
        bind(DataSender.class).annotatedWith(StatDataSender.class)
                .toProvider(UdpStatDataSenderProvider.class).in(Scopes.SINGLETON);
    }

    private void bindServiceComponent() {

        bind(StringMetaDataService.class).to(DefaultStringMetaDataService.class).in(Scopes.SINGLETON);
        bind(ApiMetaDataService.class).toProvider(ApiMetaDataServiceProvider.class).in(Scopes.SINGLETON);
        bind(SqlMetaDataService.class).to(DefaultSqlMetaDataService.class).in(Scopes.SINGLETON);
        bind(PredefinedMethodDescriptorRegistry.class).to(DefaultPredefinedMethodDescriptorRegistry.class).in(Scopes.SINGLETON);
    }

    private void bindAgentInformation(String agentId, String applicationName) {

        bind(String.class).annotatedWith(AgentId.class).toInstance(agentId);
        bind(String.class).annotatedWith(ApplicationName.class).toInstance(applicationName);
        bind(Long.class).annotatedWith(AgentStartTime.class).toProvider(AgentStartTimeProvider.class).in(Scopes.SINGLETON);
        bind(ServiceType.class).annotatedWith(ApplicationServerType.class).toProvider(ApplicationServerTypeProvider.class).in(Scopes.SINGLETON);
    }

    private void bindAgentStatComponent() {
        bind(MemoryMetric.class).toProvider(MemoryMetricProvider.class).in(Scopes.SINGLETON);
        bind(DetailedMemoryMetric.class).toProvider(DetailedMemoryMetricProvider.class).in(Scopes.SINGLETON);
        bind(GarbageCollectorMetric.class).toProvider(GarbageCollectorMetricProvider.class).in(Scopes.SINGLETON);
        bind(DetailedGarbageCollectorMetric.class).toProvider(DetailedGarbageCollectorMetricProvider.class).in(Scopes.SINGLETON);
        bind(JvmGcMetricCollector.class).toProvider(JvmGcMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(CpuLoadMetric.class).toProvider(CpuLoadMetricProvider.class).in(Scopes.SINGLETON);
        bind(CpuLoadMetricCollector.class).toProvider(CpuLoadMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(TransactionMetric.class).toProvider(TransactionMetricProvider.class).in(Scopes.SINGLETON);
        bind(TransactionMetricCollector.class).toProvider(TransactionMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(ActiveTraceMetric.class).toProvider(ActiveTraceMetricProvider.class).in(Scopes.SINGLETON);
        bind(ActiveTraceMetricCollector.class).toProvider(ActiveTraceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(ResponseTimeMetric.class).toProvider(ResponseTimeMetricProvider.class).in(Scopes.SINGLETON);
        bind(ResponseTimeMetricCollector.class).toProvider(ResponseTimeMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(DataSourceMetric.class).toProvider(DataSourceMetricProvider.class).in(Scopes.SINGLETON);
        bind(DataSourceMetricCollector.class).toProvider(DataSourceMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(DeadlockMetric.class).toProvider(DeadlockMetricProvider.class).in(Scopes.SINGLETON);
        bind(DeadlockMetricCollector.class).toProvider(DeadlockMetricCollectorProvider.class).in(Scopes.SINGLETON);

        bind(new TypeLiteral<AgentStatMetricCollector<TAgentStat>>() {})
                .annotatedWith(Names.named("AgentStatCollector"))
                .to(AgentStatCollector.class).in(Scopes.SINGLETON);
    }
}