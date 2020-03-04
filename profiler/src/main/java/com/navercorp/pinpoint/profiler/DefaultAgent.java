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

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.Profiles;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.util.SocketAddressUtils;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.profiler.concurrent.PinpointThreadFactory;
import com.navercorp.pinpoint.profiler.context.module.ApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.context.module.DefaultModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactory;
import com.navercorp.pinpoint.profiler.context.module.ModuleFactoryResolver;
import com.navercorp.pinpoint.profiler.context.provider.ShutdownHookRegisterProvider;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.navercorp.pinpoint.profiler.util.SystemPropertyDumper;
import com.navercorp.pinpoint.rpc.ClassPreLoader;
import org.apache.log4j.xml.DOMConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class DefaultAgent implements Agent {

    private final Logger logger;

    private final PLoggerBinder binder;

    private final ProfilerConfig profilerConfig;

    private final ApplicationContext applicationContext;

    private final Object agentStatusLock = new Object();
    private volatile AgentStatus agentStatus;


    public DefaultAgent(AgentOption agentOption) {
        if (agentOption == null) {
            throw new NullPointerException("agentOption");
        }
        if (agentOption.getInstrumentation() == null) {
            throw new NullPointerException("instrumentation");
        }
        if (agentOption.getProfilerConfig() == null) {
            throw new NullPointerException("profilerConfig");
        }

        initLogger(agentOption.getProfilerConfig());

        logger = LoggerFactory.getLogger(this.getClass());
        logger.info("AgentOption:{}", agentOption);

        this.binder = new Slf4jLoggerBinder();
        bindPLoggerFactory(this.binder);

        dumpSystemProperties();
        dumpConfig(agentOption.getProfilerConfig());

        changeStatus(AgentStatus.INITIALIZING);

        if (Boolean.valueOf(System.getProperty("pinpoint.profiler.ClassPreLoader", "false"))) {
            // Preload classes related to pinpoint-rpc module.
            ClassPreLoader.preload();
        }
        preloadOnStartup();

        this.profilerConfig = agentOption.getProfilerConfig();

        this.applicationContext = newApplicationContext(agentOption);

    }

    protected ApplicationContext newApplicationContext(AgentOption agentOption) {
        Assert.requireNonNull(agentOption, "agentOption");
        ProfilerConfig profilerConfig = Assert.requireNonNull(agentOption.getProfilerConfig(), "profilerConfig");

        ModuleFactoryResolver moduleFactoryResolver = new DefaultModuleFactoryResolver(profilerConfig.getInjectionModuleFactoryClazzName());
        ModuleFactory moduleFactory = moduleFactoryResolver.resolve();
        return new DefaultApplicationContext(agentOption, moduleFactory);
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
            logger.info("{}\n{}", "dumpConfig", profilerConfig);

        }
    }

    private void changeStatus(AgentStatus status) {
        this.agentStatus = status;
        if (logger.isDebugEnabled()) {
            logger.debug("Agent status is changed. {}", status);
        }
    }

    private void bindPLoggerFactory(PLoggerBinder binder) {
        final String binderClassName = binder.getClass().getName();
        PLogger pLogger = binder.getLogger(binder.getClass().getName());
        pLogger.info("PLoggerFactory.initialize() bind:{} cl:{}", binderClassName, binder.getClass().getClassLoader());
        // Set binder to static LoggerFactory
        // Should we unset binder at shutdown hook or stop()?
        PLoggerFactory.initialize(binder);
    }

    private void initLogger(ProfilerConfig config) {
        final String location = config.readString(Profiles.LOG_CONFIG_LOCATION_KEY, null);
        if (location == null) {
            throw new IllegalStateException("$PINPOINT_DIR/profiles/${profile}/log4j.xml not found");
        }
        // log4j init
        DOMConfigurator.configure(location);
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
        logger.info("Starting {} Agent.", ProductInfo.NAME);
        this.applicationContext.start();
    }

    @Override
    public void registerStopHandler() {
        logger.info("registerStopHandler");
        ShutdownHookRegisterProvider shutdownHookRegisterProvider = new ShutdownHookRegisterProvider(profilerConfig);
        ShutdownHookRegister shutdownHookRegister = shutdownHookRegisterProvider.get();

        PinpointThreadFactory pinpointThreadFactory = new PinpointThreadFactory("Pinpoint-shutdown-hook", false);
        Thread shutdownThread = pinpointThreadFactory.newThread(new Runnable() {
            @Override
            public void run() {
                logger.info("stop() started. threadName:" + Thread.currentThread().getName());
                DefaultAgent.this.stop();
            }
        });

        shutdownHookRegister.register(shutdownThread);
    }

    @Override
    public void stop() {
        synchronized (agentStatusLock) {
            if (this.agentStatus == AgentStatus.RUNNING) {
                changeStatus(AgentStatus.STOPPED);
            } else {
                logger.warn("Cannot stop agent. Current status = [{}]", this.agentStatus);
                return;
            }
        }
        logger.info("Stopping {} Agent.", ProductInfo.NAME);
        this.applicationContext.close();

        // for testcase
        if (profilerConfig.getStaticResourceCleanup()) {
            PLoggerFactory.unregister(this.binder);
        }
    }

}
