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

package com.navercorp.pinpoint.profiler;

import com.navercorp.pinpoint.banner.Banner;
import com.navercorp.pinpoint.banner.Mode;
import com.navercorp.pinpoint.banner.PinpointBanner;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfigLoader;
import com.navercorp.pinpoint.bootstrap.config.Profiles;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.config.AgentSystemConfig;
import com.navercorp.pinpoint.profiler.config.LogConfig;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.context.provider.ShutdownHookRegisterProvider;
import com.navercorp.pinpoint.profiler.logging.Log4j2LoggingSystem;
import com.navercorp.pinpoint.profiler.logging.LoggingSystem;
import com.navercorp.pinpoint.profiler.name.AgentIdResolver;
import com.navercorp.pinpoint.profiler.name.AgentIdResolverBuilder;
import com.navercorp.pinpoint.profiler.name.AgentIdSourceType;
import com.navercorp.pinpoint.profiler.name.AgentIds;
import com.navercorp.pinpoint.profiler.util.SystemPropertyDumper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;


/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class DefaultAgent implements Agent {

    private final LoggingSystem loggingSystem;
    private final Logger logger;

    private final AgentOption agentOption;
    private final ProfilerConfig profilerConfig;

    private final ApplicationContext applicationContext;

    private final Object agentStatusLock = new Object();
    private volatile AgentStatus agentStatus;


    public DefaultAgent(Map<String, Object> map) {
        Objects.requireNonNull(map, "map");

        this.agentOption = AgentOption.of(map);

        Properties properties = agentOption.getProperties();
        this.profilerConfig = ProfilerConfigLoader.load(properties);

        final Path agentPath = agentOption.getAgentPath();
        final Path logConfigPath = getLogConfigPath(profilerConfig, agentPath);

        LogConfig logConfig = new LogConfig(logConfigPath);
        logConfig.saveLogFilePath();
        logConfig.cleanLogDir(properties);

        this.loggingSystem = newLoggingSystem(logConfigPath);
        this.loggingSystem.start();

        logger = LogManager.getLogger(this.getClass());

        logger.info("logConfig path:{}", loggingSystem.getConfigLocation());

        AgentContextOption agentContextOption = buildContextOption(agentOption, profilerConfig);

        dumpAgentOption(agentContextOption);

        dumpSystemProperties();
        dumpConfig(profilerConfig);

        // save system config
        saveSystemConfig(agentContextOption);

        changeStatus(AgentStatus.INITIALIZING);

        preloadOnStartup();

        this.applicationContext = newApplicationContext(agentContextOption);
    }

    protected AgentContextOption buildContextOption(AgentOption agentOption, ProfilerConfig profilerConfig) {
        final AgentIds agentIds = resolveAgentIds();
        if (agentIds == null) {
            logger.warn("Failed to resolve AgentId and ApplicationId");
            throw new RuntimeException("Failed to resolve AgentId and ApplicationId");
        }

        final String agentId = agentIds.getAgentId();
        if (agentId == null) {
            logger.warn("agentId is null");
            throw new RuntimeException("agentId is null");
        }
        final String agentName = agentIds.getAgentName();
        final String applicationName = agentIds.getApplicationName();
        if (applicationName == null) {
            logger.warn("applicationName is null");
            throw new RuntimeException("applicationName is null");
        }

        return AgentContextOptionBuilder.build(agentOption,
                agentId, agentName, applicationName,
                profilerConfig);
    }

    private AgentIds resolveAgentIds() {
        AgentIdResolverBuilder builder = new AgentIdResolverBuilder();
        builder.addProperties(AgentIdSourceType.SYSTEM, System.getProperties()::getProperty);
        builder.addProperties(AgentIdSourceType.SYSTEM_ENV, System.getenv()::get);
        builder.addProperties(AgentIdSourceType.AGENT_ARGUMENT, agentOption.getAgentArgs()::get);
        AgentIdResolver agentIdResolver = builder.build();
        return agentIdResolver.resolve();
    }


    private void saveSystemConfig(AgentContextOption option) {
        // set the path of log file as a system property
        AgentSystemConfig agentSystemConfig = new AgentSystemConfig();
        agentSystemConfig.saveAgentIdForLog(option.getAgentId());
        agentSystemConfig.savePinpointVersion(Version.VERSION);
    }

    private void dumpAgentOption(AgentContextOption agentOption) {
        logger.warn("AgentOption");
        logger.warn("- agentId: {}", agentOption.getAgentId());
        logger.warn("- agentName: {}", agentOption.getAgentName());
        logger.warn("- applicationName: {}", agentOption.getApplicationName());
        logger.info("- instrumentation: {}", agentOption.getInstrumentation());
    }

    private LoggingSystem newLoggingSystem(Path agentPath) {
        return Log4j2LoggingSystem.searchPath(agentPath);
    }

    protected ApplicationContext newApplicationContext(AgentContextOption agentOption) {
        Objects.requireNonNull(agentOption, "agentOption");
        ProfilerConfig profilerConfig = Objects.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig");

        String factoryClazzName = getInjectionModuleFactoryClazzName(profilerConfig);
        ModuleFactoryResolver moduleFactoryResolver = new DefaultModuleFactoryResolver(factoryClazzName);
        ModuleFactory moduleFactory = moduleFactoryResolver.resolve();
        return new DefaultApplicationContext(agentOption, moduleFactory);
    }

    private String getInjectionModuleFactoryClazzName(ProfilerConfig profilerConfig) {
        return profilerConfig.readString("profiler.guice.module.factory", null);
    }

    protected ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    private void dumpSystemProperties() {
        SystemPropertyDumper dumper = new SystemPropertyDumper();
        dumper.dump();
    }

    private void dumpConfig(ProfilerConfig profilerConfig) {
        if (logger.isInfoEnabled()) {
            logger.info("{}", profilerConfig);
            Properties properties = profilerConfig.getProperties();
            for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                logger.info("- {}={}", entry.getKey(), entry.getValue());
            }
        }
    }

    private void changeStatus(AgentStatus status) {
        this.agentStatus = status;
        if (logger.isDebugEnabled()) {
            logger.debug("Agent status is changed. {}", status);
        }
    }

    protected Path getLogConfigPath(ProfilerConfig config, Path agentPath) {
        final String location = config.readString(Profiles.LOG_CONFIG_LOCATION_KEY, null);
        if (location != null) {
            return Paths.get(location);
        }
        return agentPath;
    }


    private void preloadOnStartup() {
        // Preload to fail fast on startup. This won't be necessary once JDK 6 support ends
        // and reflective method handle is not needed.
        SocketAddressUtils.getHostNameFirst(null);
    }

    @Override
    public void start() {
        synchronized (agentStatusLock) {
            if (this.agentStatus == AgentStatus.INITIALIZING) {
                changeStatus(AgentStatus.RUNNING);
            } else {
                logger.warn("Agent already started.");
                return;
            }
        }

        logger.info("Starting pinpoint Agent.");
        this.applicationContext.start();
        printBanner();
    }

    private void printBanner() {
        List<String> dumpKeys = profilerConfig.readList("pinpoint.banner.configs");
        Mode mode = Mode.valueOf(profilerConfig.readString("pinpoint.banner.mode", "CONSOLE").toUpperCase());

        PinpointBanner.Builder builder = PinpointBanner.newBuilder();
        builder.setBannerMode(mode);
        builder.setDumpKeys(dumpKeys);
        builder.setProperties(profilerConfig::readString);
        builder.setLoggerWriter(logger::info);
        final Banner banner = builder.build();
        banner.printBanner();
    }

    @Override
    public void registerStopHandler() {
        if (applicationContext instanceof DefaultApplicationContext) {
            logger.info("registerStopHandler");

            DefaultApplicationContext context = (DefaultApplicationContext) applicationContext;
            ShutdownHookRegisterProvider shutdownHookRegisterProvider = context.getShutdownHookRegisterProvider();
            ShutdownHookRegister shutdownHookRegister = shutdownHookRegisterProvider.get();

            PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory("Pinpoint-shutdown-hook", false);
            Thread shutdownThread = pinpointThreadFactory.newThread(new Runnable() {
                @Override
                public void run() {
                    logger.info("stop() started. threadName:" + Thread.currentThread().getName());
                    DefaultAgent.this.close();
                }
            });

            shutdownHookRegister.register(shutdownThread);

        }

    }

    @Override
    public void close() {
        synchronized (agentStatusLock) {
            if (this.agentStatus == AgentStatus.RUNNING) {
                changeStatus(AgentStatus.STOPPED);
            } else {
                logger.warn("Cannot stop agent. Current status = [{}]", this.agentStatus);
                return;
            }
        }
        logger.info("Stopping pinpoint Agent.");
        this.applicationContext.close();

        // for testcase
        if (agentOption.isStaticResourceCleanup()) {
            this.loggingSystem.close();
        }
    }

}
