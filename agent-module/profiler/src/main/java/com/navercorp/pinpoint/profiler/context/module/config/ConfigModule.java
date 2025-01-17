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
import com.google.inject.name.Names;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentContextOption;
import com.navercorp.pinpoint.profiler.context.TraceDataFormatVersion;
import com.navercorp.pinpoint.profiler.context.config.ContextConfig;
import com.navercorp.pinpoint.profiler.context.config.DefaultContextConfig;
import com.navercorp.pinpoint.profiler.context.module.AgentId;
import com.navercorp.pinpoint.profiler.context.module.AgentName;
import com.navercorp.pinpoint.profiler.context.module.AgentStartTime;
import com.navercorp.pinpoint.profiler.context.module.ApplicationName;
import com.navercorp.pinpoint.profiler.context.module.BootstrapJarPaths;
import com.navercorp.pinpoint.profiler.context.module.ClusterNamespace;
import com.navercorp.pinpoint.profiler.context.module.ConfiguredApplicationType;
import com.navercorp.pinpoint.profiler.context.module.Container;
import com.navercorp.pinpoint.profiler.context.module.PluginJarPaths;
import com.navercorp.pinpoint.profiler.context.module.PluginJars;
import com.navercorp.pinpoint.profiler.context.monitor.config.DefaultMonitorConfig;
import com.navercorp.pinpoint.profiler.context.monitor.config.MonitorConfig;
import com.navercorp.pinpoint.profiler.context.provider.AgentStartTimeProvider;
import com.navercorp.pinpoint.profiler.context.provider.ConfiguredApplicationTypeProvider;
import com.navercorp.pinpoint.profiler.context.provider.InterceptorRegistryBinderProvider;
import com.navercorp.pinpoint.profiler.context.provider.ShutdownHookRegisterProvider;
import com.navercorp.pinpoint.profiler.context.provider.TraceDataFormatVersionProvider;
import com.navercorp.pinpoint.profiler.context.provider.plugin.PluginJarsProvider;
import com.navercorp.pinpoint.profiler.instrument.classloading.BootstrapCore;
import com.navercorp.pinpoint.profiler.instrument.config.DefaultInstrumentConfig;
import com.navercorp.pinpoint.profiler.instrument.config.DefaultInstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentConfig;
import com.navercorp.pinpoint.profiler.instrument.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.PluginJar;
import com.navercorp.pinpoint.profiler.plugin.config.DefaultPluginLoadingConfig;
import com.navercorp.pinpoint.profiler.plugin.config.PluginLoadingConfig;
import com.navercorp.pinpoint.profiler.util.ContainerResolver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.Instrumentation;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;

/**
 * @author Woonduk Kang(emeroad)
 */
public class ConfigModule extends AbstractModule {

    private final Logger logger = LogManager.getLogger(this.getClass());
    private final AgentContextOption agentOption;

    public ConfigModule(AgentContextOption agentOption) {
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

        Properties properties = profilerConfig.getProperties();
        ConfigurationLoader configurationLoader = new ConfigurationLoader(properties);

        ContextConfig contextConfig = new DefaultContextConfig();
        configurationLoader.load(contextConfig);
        logger.info("{}", contextConfig);
        bind(ContextConfig.class).toInstance(contextConfig);

        bindConstants(contextConfig);

        PluginLoadingConfig pluginLoadingConfig = new DefaultPluginLoadingConfig();
        configurationLoader.load(pluginLoadingConfig);
        logger.info("{}", pluginLoadingConfig);
        bind(PluginLoadingConfig.class).toInstance(pluginLoadingConfig);

        InstrumentConfig instrumentConfig = new DefaultInstrumentConfig();
        configurationLoader.load(instrumentConfig);
        logger.info("{}", instrumentConfig);
        bind(InstrumentConfig.class).toInstance(instrumentConfig);

        InstrumentMatcherCacheConfig instrumentMatcherCacheConfig = new DefaultInstrumentMatcherCacheConfig();
        configurationLoader.load(instrumentMatcherCacheConfig);
        logger.info("{}", instrumentMatcherCacheConfig);
        bind(InstrumentMatcherCacheConfig.class).toInstance(instrumentMatcherCacheConfig);


        MonitorConfig monitorConfig = new DefaultMonitorConfig();
        configurationLoader.load(monitorConfig);
        logger.info("{}", monitorConfig);
        bind(MonitorConfig.class).toInstance(monitorConfig);

        bind(Instrumentation.class).toInstance(agentOption.getInstrumentation());

        bind(InterceptorRegistryBinder.class).toProvider(InterceptorRegistryBinderProvider.class).in(Scopes.SINGLETON);

        TypeLiteral<List<Path>> pluginJarFile = new TypeLiteral<List<Path>>() {};
        bind(pluginJarFile).annotatedWith(PluginJarPaths.class).toInstance(agentOption.getPluginJars());
        TypeLiteral<List<PluginJar>> pluginJars = new TypeLiteral<List<PluginJar>>() {};
        bind(pluginJars).annotatedWith(PluginJars.class).toProvider(PluginJarsProvider.class).in(Scopes.SINGLETON);


        bindBootstrapCoreInformation(agentOption);

        bindAgentInformation(agentOption);

        bindShutdownHook(contextConfig);
    }

    private void bindBootstrapCoreInformation(AgentContextOption agentOption) {
        List<Path> bootstrapJarPaths = agentOption.getBootstrapJarPaths();

        TypeLiteral<List<Path>> bootstrapJarFIle = new TypeLiteral<List<Path>>() {};
        bind(bootstrapJarFIle).annotatedWith(BootstrapJarPaths.class).toInstance(bootstrapJarPaths);

        BootstrapCore bootstrapCore = new BootstrapCore(bootstrapJarPaths);
        bind(BootstrapCore.class).toInstance(bootstrapCore);
    }

    private void bindConstants(ContextConfig contextConfig) {

        bind(TraceDataFormatVersion.class).toProvider(TraceDataFormatVersionProvider.class).in(Scopes.SINGLETON);

        bindConstant().annotatedWith(TraceAgentActiveThread.class).to(contextConfig.isTraceAgentActiveThread());
        bindConstant().annotatedWith(DeadlockMonitorEnable.class).to(contextConfig.isDeadlockMonitorEnable());
        bindConstant().annotatedWith(DeadlockMonitorInterval.class).to(contextConfig.getDeadlockMonitorInterval());
    }

    private void bindAgentInformation(AgentContextOption agentOption) {

        bind(String.class).annotatedWith(AgentId.class).toInstance(agentOption.getAgentId());
        bind(String.class).annotatedWith(AgentName.class).toInstance(agentOption.getAgentName());
        bind(String.class).annotatedWith(ApplicationName.class).toInstance(agentOption.getApplicationName());

        bind(String.class).annotatedWith(Names.named("pinpoint.agentId")).toInstance(agentOption.getAgentId());
        bind(String.class).annotatedWith(Names.named("pinpoint.agentName")).toInstance(agentOption.getAgentName());
        bind(String.class).annotatedWith(Names.named("pinpoint.applicationName")).toInstance(agentOption.getApplicationName());

        final ContainerResolver containerResolver = new ContainerResolver();
        final boolean isContainer = containerResolver.isContainer();
        bind(Boolean.class).annotatedWith(Container.class).toInstance(isContainer);

        bind(Long.class).annotatedWith(AgentStartTime.class).toProvider(AgentStartTimeProvider.class).in(Scopes.SINGLETON);
        bind(ServiceType.class).annotatedWith(ConfiguredApplicationType.class).toProvider(ConfiguredApplicationTypeProvider.class).in(Scopes.SINGLETON);

        bind(String.class).annotatedWith(ClusterNamespace.class).toProvider(ClusterNamespaceProvider.class).in(Scopes.SINGLETON);
    }


    private void bindShutdownHook(ContextConfig contextConfig) {
        // for lazy init
        ShutdownHookRegisterProvider instance = new ShutdownHookRegisterProvider(contextConfig);
        bind(ShutdownHookRegisterProvider.class).toInstance(instance);
    }
}
