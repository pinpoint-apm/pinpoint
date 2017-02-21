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

package com.navercorp.pinpoint.profiler.context;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DefaultDynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.DynamicTransformerRegistry;
import com.navercorp.pinpoint.profiler.JvmInformation;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.ApplicationServerType;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.context.module.SpanDataSender;
import com.navercorp.pinpoint.profiler.context.module.StatDataSender;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.provider.AgentInfoSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.AgentInformationProvider;
import com.navercorp.pinpoint.profiler.context.provider.ApplicationServerTypeProvider;
import com.navercorp.pinpoint.profiler.context.provider.AgentStartTimeProvider;
import com.navercorp.pinpoint.profiler.context.provider.ClassFileTransformerDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.CommandDispatcherProvider;
import com.navercorp.pinpoint.profiler.context.provider.DynamicTransformTriggerProvider;
import com.navercorp.pinpoint.profiler.context.provider.InstrumentEngineProvider;
import com.navercorp.pinpoint.profiler.context.provider.JvmInformationProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientProvider;
import com.navercorp.pinpoint.profiler.context.provider.PluginContextLoadResultProvider;
import com.navercorp.pinpoint.profiler.context.provider.PluginMonitorContextProvider;
import com.navercorp.pinpoint.profiler.context.provider.SamplerProvider;
import com.navercorp.pinpoint.profiler.context.provider.ServerMetaDataHolderProvider;
import com.navercorp.pinpoint.profiler.context.provider.StorageFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.TcpDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.UdpSpanDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.UdpStatDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.DefaultAgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.monitor.codahale.DefaultAgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.plugin.PluginContextLoadResult;
import com.navercorp.pinpoint.profiler.plugin.PluginSetup;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;

import java.lang.instrument.Instrumentation;
import java.net.URL;
import java.util.List;


/**
 * @author Woonduk Kang(emeroad)
 */
public class ApplicationContextModule extends AbstractModule {
    private final ProfilerConfig profilerConfig;
    private final ApplicationContext applicationContext;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    private final AgentOption agentOption;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    public ApplicationContextModule(ApplicationContext applicationContext, AgentOption agentOption, ProfilerConfig profilerConfig,
                                    ServiceTypeRegistryService serviceTypeRegistryService, InterceptorRegistryBinder interceptorRegistryBinder) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        this.agentOption = agentOption;
        this.applicationContext = applicationContext;
        this.profilerConfig = profilerConfig;
        this.serviceTypeRegistryService = serviceTypeRegistryService;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
    }

    @Override
    protected void configure() {
        bind(ApplicationContext.class).toInstance(applicationContext);
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

        bind(ServerMetaDataHolder.class).toProvider(ServerMetaDataHolderProvider.class).in(Scopes.SINGLETON);
        bind(StorageFactory.class).toProvider(StorageFactoryProvider.class).in(Scopes.SINGLETON);


        bindServiceComponent();

        bind(PluginMonitorContext.class).toProvider(PluginMonitorContextProvider.class).in(Scopes.SINGLETON);

        bind(IdGenerator.class).to(AtomicIdGenerator.class);
        bind(TransactionCounter.class).to(DefaultTransactionCounter.class).in(Scopes.SINGLETON);

        bind(Sampler.class).toProvider(SamplerProvider.class).in(Scopes.SINGLETON);
        bind(TraceFactoryBuilder.class).to(DefaultTraceFactoryBuilder.class).in(Scopes.SINGLETON);
        bind(TraceContext.class).to(DefaultTraceContext.class).in(Scopes.SINGLETON);
        bind(AgentStatCollectorFactory.class).to(DefaultAgentStatCollectorFactory.class).in(Scopes.SINGLETON);
        bind(AgentStatMonitor.class).to(DefaultAgentStatMonitor.class).in(Scopes.SINGLETON);

        bind(PluginContextLoadResult.class).toProvider(PluginContextLoadResultProvider.class).in(Scopes.SINGLETON);
        bind(AgentInformation.class).toProvider(AgentInformationProvider.class).in(Scopes.SINGLETON);

        bind(JvmInformation.class).toProvider(JvmInformationProvider.class).in(Scopes.SINGLETON);
        bind(AgentInfoSender.class).toProvider(AgentInfoSenderProvider.class).in(Scopes.SINGLETON);


        bind(InstrumentEngine.class).toProvider(InstrumentEngineProvider.class).in(Scopes.SINGLETON);
        bind(ClassFileTransformerDispatcher.class).toProvider(ClassFileTransformerDispatcherProvider.class).in(Scopes.SINGLETON);
        bind(DynamicTransformerRegistry.class).to(DefaultDynamicTransformerRegistry.class).in(Scopes.SINGLETON);
        bind(DynamicTransformTrigger.class).toProvider(DynamicTransformTriggerProvider.class).in(Scopes.SINGLETON);
//        bind(ClassFileTransformer.class).toProvider(ClassFileTransformerWrapProvider.class).in(Scopes.SINGLETON);
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

        bind(StringMetaDataService.class).to(StringMetaDataCacheService.class).in(Scopes.SINGLETON);
        bind(ApiMetaDataService.class).to(ApiMetaDataCacheService.class).in(Scopes.SINGLETON);
        bind(SqlMetaDataService.class).to(SqlMetaDataCacheService.class).in(Scopes.SINGLETON);
    }

    private void bindAgentInformation(String agentId, String applicationName) {

        bind(String.class).annotatedWith(AgentId.class).toInstance(agentId);
        bind(String.class).annotatedWith(ApplicationName.class).toInstance(applicationName);
        bind(Long.class).annotatedWith(AgentStartTime.class).toProvider(AgentStartTimeProvider.class);
        bind(ServiceType.class).annotatedWith(ApplicationServerType.class).toProvider(ApplicationServerTypeProvider.class);
    }
}