/*
 * Copyright 2019 NAVER Corp.
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

package com.navercorp.pinpoint.profiler.context.module.config;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.TypeLiteral;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.TransportModule;
import com.navercorp.pinpoint.bootstrap.config.util.ValueAnnotationProcessor;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.context.module.Container;
import com.navercorp.pinpoint.profiler.context.module.PluginJarPaths;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.context.provider.AgentStartTimeProvider;
import com.navercorp.pinpoint.profiler.context.provider.ConfiguredApplicationTypeProvider;
import com.navercorp.pinpoint.profiler.context.provider.InterceptorRegistryBinderProvider;
import com.navercorp.pinpoint.profiler.context.provider.TraceDataFormatVersionProvider;
import com.navercorp.pinpoint.profiler.context.provider.plugin.PluginJarsProvider;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConfigModule extends AbstractModule {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final AgentOption agentOption;

    public ConfigModule(AgentOption agentOption) {
        this.agentOption = Objects.requireNonNull(agentOption, "profilerConfig");
        Objects.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig");
    }

    @Override
    protected void configure() {
        logger.info("configure {}", this.getClass().getSimpleName());
        binder().requireExplicitBindings();
        binder().requireAtInjectOnConstructors();
        binder().disableCircularProxies();

        ProfilerConfig profilerConfig = agentOption.getProfilerConfig();
        bind(ProfilerConfig.class).toInstance(profilerConfig);

        InstrumentMatcherCacheConfig instrumentMatcherCacheConfig = loadInstrumentMatcherCacheConfig(profilerConfig);
        bind(InstrumentMatcherCacheConfig.class).toInstance(instrumentMatcherCacheConfig);

        bind(TransportModule.class).toInstance(profilerConfig.getTransportModule());

        bindConstants(profilerConfig);


        bind(Instrumentation.class).toInstance(agentOption.getInstrumentation());

        bind(InterceptorRegistryBinder.class).toProvider(InterceptorRegistryBinderProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<List<String>> pluginJarFile = new TypeLiteral<List<String>>() {};
        bind(pluginJarFile).annotatedWith(PluginJarPaths.class).toInstance(agentOption.getPluginJars());
        TypeLiteral<List<PluginJar>> pluginJars = new TypeLiteral<List<PluginJar>>() {};
        bind(pluginJars).annotatedWith(PluginJars.class).toProvider(PluginJarsProvider.class).in(Scopes.SINGLETON);


        bindBootstrapCoreInformation();

        bindAgentInformation(agentOption.getAgentId(), agentOption.getApplicationName(), agentOption.isContainer());
    }

    private void bindBootstrapCoreInformation() {
        List<String> bootstrapJarPaths = agentOption.getBootstrapJarPaths();

        TypeLiteral<List<String>> bootstrapJarFIle = new TypeLiteral<List<String>>() {};
        bind(bootstrapJarFIle).annotatedWith(BootstrapJarPaths.class).toInstance(bootstrapJarPaths);

        BootstrapCore bootstrapCore = new BootstrapCore(bootstrapJarPaths);
        bind(BootstrapCore.class).toInstance(bootstrapCore);
    }

    private void bindConstants(ProfilerConfig profilerConfig) {

        bind(TraceDataFormatVersion.class).toProvider(TraceDataFormatVersionProvider.class).in(Scopes.SINGLETON);

        Named callstackMaxDepth = Names.named("profiler.callstack.max.depth");
        bindConstant().annotatedWith(callstackMaxDepth).to(profilerConfig.getCallStackMaxDepth());

        bindConstant().annotatedWith(TraceAgentActiveThread.class).to(profilerConfig.isTraceAgentActiveThread());

        bindConstant().annotatedWith(DeadlockMonitorEnable.class).to(profilerConfig.isDeadlockMonitorEnable());
        bindConstant().annotatedWith(DeadlockMonitorInterval.class).to(profilerConfig.getDeadlockMonitorInterval());
    }

    private void bindAgentInformation(String agentId, String applicationName, boolean isContainer) {

        bind(String.class).annotatedWith(AgentId.class).toInstance(agentId);
        bind(String.class).annotatedWith(ApplicationName.class).toInstance(applicationName);
        bind(Boolean.class).annotatedWith(Container.class).toInstance(isContainer);
        bind(Long.class).annotatedWith(AgentStartTime.class).toProvider(AgentStartTimeProvider.class).in(Scopes.SINGLETON);
        bind(ServiceType.class).annotatedWith(ConfiguredApplicationType.class).toProvider(ConfiguredApplicationTypeProvider.class).in(Scopes.SINGLETON);
    }


    private InstrumentMatcherCacheConfig loadInstrumentMatcherCacheConfig(ProfilerConfig config) {
        InstrumentMatcherCacheConfig instrumentMatcherCacheConfig = new InstrumentMatcherCacheConfig();

        Properties properties = config.getProperties();
        ValueAnnotationProcessor instrumentProcessor = new ValueAnnotationProcessor();
        instrumentProcessor.process(instrumentProcessor, properties);

        logger.info("{}", instrumentMatcherCacheConfig);
        return instrumentMatcherCacheConfig;
    }
}
