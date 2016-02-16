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

package com.navercorp.pinpoint.profiler;

import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.ProductInfo;
import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorInvokerHelper;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerBinder;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.navercorp.pinpoint.profiler.context.DefaultTraceContext;
import com.navercorp.pinpoint.profiler.context.TransactionCounter;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceLocator;
import com.navercorp.pinpoint.profiler.context.storage.BufferedStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.SpanStorageFactory;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.instrument.JavassistClassPool;
import com.navercorp.pinpoint.profiler.interceptor.registry.DefaultInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginLoader;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadService;
import com.navercorp.pinpoint.profiler.receiver.service.EchoService;
import com.navercorp.pinpoint.profiler.receiver.service.ThreadDumpService;
import com.navercorp.pinpoint.profiler.sampler.SamplerFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.sender.TcpDataSender;
import com.navercorp.pinpoint.profiler.sender.UdpDataSender;
import com.navercorp.pinpoint.profiler.util.ApplicationServerTypeResolver;
import com.navercorp.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.navercorp.pinpoint.rpc.ClassPreLoader;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import com.navercorp.pinpoint.rpc.util.ClientFactoryUtils;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class DefaultAgent implements Agent {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PLoggerBinder binder;

    private final ClassFileTransformerDispatcher classFileTransformer;
    
    private final ProfilerConfig profilerConfig;

    private final AgentInfoSender agentInfoSender;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private PinpointClientFactory clientFactory;
    private PinpointClient client;
    private final EnhancedDataSender tcpDataSender;

    private final DataSender statDataSender;
    private final DataSender spanDataSender;

    private final AgentInformation agentInformation;
    private final ServerMetaDataHolder serverMetaDataHolder;
    private final AgentOption agentOption;

    private volatile AgentStatus agentStatus;

    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final ServiceTypeRegistryService serviceTypeRegistryService;
    
    private final Instrumentation instrumentation;
    private final JavassistClassPool classPool;
    private final DynamicTransformService dynamicTransformService;
    private final List<DefaultProfilerPluginContext> pluginContexts;
    

    static {
        // Preload classes related to pinpoint-rpc module.
        ClassPreLoader.preload();
    }
    public DefaultAgent(AgentOption agentOption) {
        this(agentOption, createInterceptorRegistry(agentOption));
    }

    public static InterceptorRegistryBinder createInterceptorRegistry(AgentOption agentOption) {
        final int interceptorSize = getInterceptorSize(agentOption);
        return new DefaultInterceptorRegistryBinder(interceptorSize);
    }

    private static int getInterceptorSize(AgentOption agentOption) {
        if (agentOption == null) {
            return DefaultInterceptorRegistryBinder.DEFAULT_MAX;
        }
        final ProfilerConfig profilerConfig = agentOption.getProfilerConfig();
        return profilerConfig.getInterceptorRegistrySize();
    }

    public DefaultAgent(AgentOption agentOption, final InterceptorRegistryBinder interceptorRegistryBinder) {
        if (agentOption == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        if (agentOption.getInstrumentation() == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (agentOption.getProfilerConfig() == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        if (agentOption.getServiceTypeRegistryService() == null) {
            throw new NullPointerException("serviceTypeRegistryService must not be null");
        }

        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }
        logger.info("AgentOption:{}", agentOption);

        this.binder = new Slf4jLoggerBinder();
        bindPLoggerFactory(this.binder);

        this.interceptorRegistryBinder = interceptorRegistryBinder;
        interceptorRegistryBinder.bind();
        this.serviceTypeRegistryService = agentOption.getServiceTypeRegistryService();

        dumpSystemProperties();
        dumpConfig(agentOption.getProfilerConfig());

        changeStatus(AgentStatus.INITIALIZING);
        
        this.profilerConfig = agentOption.getProfilerConfig();
        this.instrumentation = agentOption.getInstrumentation();
        this.agentOption = agentOption;
        this.classPool = new JavassistClassPool(interceptorRegistryBinder, agentOption.getBootStrapCoreJarPath());
        
        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }

        pluginContexts = loadPlugins(agentOption);

        this.classFileTransformer = new ClassFileTransformerDispatcher(this, pluginContexts);
        this.dynamicTransformService = new DynamicTransformService(instrumentation, classFileTransformer);

        instrumentation.addTransformer(this.classFileTransformer, true);

        String applicationServerTypeString = profilerConfig.getApplicationServerType();
        ServiceType applicationServerType = this.serviceTypeRegistryService.findServiceTypeByName(applicationServerTypeString);

        final ApplicationServerTypeResolver typeResolver = new ApplicationServerTypeResolver(pluginContexts, applicationServerType, profilerConfig.getApplicationTypeDetectOrder());
        
        final AgentInformationFactory agentInformationFactory = new AgentInformationFactory();
        this.agentInformation = agentInformationFactory.createAgentInformation(typeResolver.resolve());
        logger.info("agentInformation:{}", agentInformation);
        
        CommandDispatcher commandDispatcher = new CommandDispatcher();

        this.tcpDataSender = createTcpDataSender(commandDispatcher);

        this.serverMetaDataHolder = createServerMetaDataHolder();

        this.spanDataSender = createUdpSpanDataSender(this.profilerConfig.getCollectorSpanServerPort(), "Pinpoint-UdpSpanDataExecutor",
                this.profilerConfig.getSpanDataSenderWriteQueueSize(), this.profilerConfig.getSpanDataSenderSocketTimeout(),
                this.profilerConfig.getSpanDataSenderSocketSendBufferSize());
        this.statDataSender = createUdpStatDataSender(this.profilerConfig.getCollectorStatServerPort(), "Pinpoint-UdpStatDataExecutor",
                this.profilerConfig.getStatDataSenderWriteQueueSize(), this.profilerConfig.getStatDataSenderSocketTimeout(),
                this.profilerConfig.getStatDataSenderSocketSendBufferSize());

        this.traceContext = createTraceContext();

        addCommandService(commandDispatcher, traceContext);

        AgentStatCollectorFactory agentStatCollectorFactory = new AgentStatCollectorFactory(this.traceContext);

        JvmInformationFactory jvmInformationFactory = new JvmInformationFactory(agentStatCollectorFactory.getGarbageCollector());

        this.agentInfoSender = new AgentInfoSender.Builder(tcpDataSender, this.agentInformation, jvmInformationFactory.createJvmInformation()).sendInterval(profilerConfig.getAgentInfoSendRetryInterval()).build();
        this.serverMetaDataHolder.addListener(this.agentInfoSender);
        this.agentStatMonitor = new AgentStatMonitor(this.statDataSender, this.agentInformation.getAgentId(), this.agentInformation.getStartTime(), agentStatCollectorFactory);
        
        InterceptorInvokerHelper.setPropagateException(profilerConfig.isPropagateInterceptorException());
    }

    public String getBootstrapCoreJar() {
        return agentOption.getBootStrapCoreJarPath();
    }

    protected List<DefaultProfilerPluginContext> loadPlugins(AgentOption agentOption) {
        final ProfilerPluginLoader loader = new ProfilerPluginLoader(this);
        return loader.load(agentOption.getPluginJars());
    }

    private void addCommandService(CommandDispatcher commandDispatcher, TraceContext traceContext) {
        commandDispatcher.registerCommandService(new ThreadDumpService());
        commandDispatcher.registerCommandService(new EchoService());

        if (traceContext instanceof DefaultTraceContext) {
            ActiveTraceLocator activeTraceLocator = ((DefaultTraceContext) traceContext).getActiveTraceLocator();
            if (activeTraceLocator != null) {
                commandDispatcher.registerCommandService(new ActiveThreadService(activeTraceLocator));
            }
        }
    }
    
    private TransactionCounter getTransactionCounter(TraceContext traceContext) {
        if (traceContext instanceof DefaultTraceContext) {
            return ((DefaultTraceContext) traceContext).getTransactionCounter();
        }
        return null;
    }
    
    public DynamicTransformService getDynamicTransformService() {
        return dynamicTransformService;
    }

    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    public ClassFileTransformerDispatcher getClassFileTransformerDispatcher() {
        return classFileTransformer;
    }
    
    public JavassistClassPool getClassPool() {
        return classPool;
    }

    private void dumpSystemProperties() {
        if (logger.isInfoEnabled()) {
            Properties properties = System.getProperties();
            Set<String> strings = properties.stringPropertyNames();
            for (String key : strings) {
                logger.info("SystemProperties {}={}", key, properties.get(key));
            }
        }
    }

    private void dumpConfig(ProfilerConfig profilerConfig) {
        if (logger.isInfoEnabled()) {
            logger.info("{}\n{}", "dumpConfig", profilerConfig);

        }
    }

    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
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

    private TraceContext createTraceContext() {
        final StorageFactory storageFactory = createStorageFactory();
        logger.info("StorageFactoryType:{}", storageFactory);

        final Sampler sampler = createSampler();
        logger.info("SamplerType:{}", sampler);
        
        final int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        final boolean traceActiveThread = profilerConfig.isTraceAgentActiveThread();
        final DefaultTraceContext traceContext = new DefaultTraceContext(jdbcSqlCacheSize, this.agentInformation, storageFactory, sampler, this.serverMetaDataHolder, traceActiveThread);
        traceContext.setPriorityDataSender(this.tcpDataSender);
        traceContext.setProfilerConfig(profilerConfig);

        return traceContext;
    }

    protected StorageFactory createStorageFactory() {
        if (profilerConfig.isIoBufferingEnable()) {
            return new BufferedStorageFactory(this.spanDataSender, this.profilerConfig, this.agentInformation);
        } else {
            return new SpanStorageFactory(spanDataSender);

        }
    }

    private Sampler createSampler() {
        boolean samplingEnable = this.profilerConfig.isSamplingEnable();
        int samplingRate = this.profilerConfig.getSamplingRate();

        SamplerFactory samplerFactory = new SamplerFactory();
        return samplerFactory.createSampler(samplingEnable, samplingRate);
    }
    
    protected ServerMetaDataHolder createServerMetaDataHolder() {
        List<String> vmArgs = RuntimeMXBeanUtils.getVmArgs();
        ServerMetaDataHolder serverMetaDataHolder = new DefaultServerMetaDataHolder(vmArgs);
        return serverMetaDataHolder;
    }

    protected PinpointClientFactory createPinpointClientFactory(CommandDispatcher commandDispatcher) {
        PinpointClientFactory pinpointClientFactory = new PinpointClientFactory();
        pinpointClientFactory.setTimeoutMillis(1000 * 5);

        Map<String, Object> properties = this.agentInformation.toMap();
        
        boolean isSupportServerMode = this.profilerConfig.isTcpDataSenderCommandAcceptEnable();
        
        if (isSupportServerMode) {
            pinpointClientFactory.setMessageListener(commandDispatcher);
            pinpointClientFactory.setServerStreamChannelMessageListener(commandDispatcher);

            properties.put(AgentHandshakePropertyType.SUPPORT_SERVER.getName(), true);
        } else {
            properties.put(AgentHandshakePropertyType.SUPPORT_SERVER.getName(), false);
        }

        pinpointClientFactory.setProperties(properties);
        return pinpointClientFactory;
    }

    protected EnhancedDataSender createTcpDataSender(CommandDispatcher commandDispatcher) {
        this.clientFactory = createPinpointClientFactory(commandDispatcher);
        this.client = ClientFactoryUtils.createPinpointClient(this.profilerConfig.getCollectorTcpServerIp(), this.profilerConfig.getCollectorTcpServerPort(), clientFactory);
        return new TcpDataSender(client);
    }

    protected DataSender createUdpStatDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new UdpDataSender(this.profilerConfig.getCollectorStatServerIp(), port, threadName, writeQueueSize, timeout, sendBufferSize);
    }
    
    protected DataSender createUdpSpanDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new UdpDataSender(this.profilerConfig.getCollectorSpanServerIp(), port, threadName, writeQueueSize, timeout, sendBufferSize);
    }

    protected EnhancedDataSender getTcpDataSender() {
        return tcpDataSender;
    }

    protected DataSender getStatDataSender() {
        return statDataSender;
    }

    protected DataSender getSpanDataSender() {
        return spanDataSender;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public AgentInformation getAgentInformation() {
        return agentInformation;
    }
    
    public ServiceTypeRegistryService getServiceTypeRegistryService() {
        return serviceTypeRegistryService;
    }
    
    @Override
    public void start() {
        synchronized (this) {
            if (this.agentStatus == AgentStatus.INITIALIZING) {
                changeStatus(AgentStatus.RUNNING);
            } else {
                logger.warn("Agent already started.");
                return;
            }
        }
        logger.info("Starting {} Agent.", ProductInfo.NAME);
        this.agentInfoSender.start();
        this.agentStatMonitor.start();
    }

    @Override
    public void stop() {
        stop(false);
    }

    public void stop(boolean staticResourceCleanup) {
        synchronized (this) {
            if (this.agentStatus == AgentStatus.RUNNING) {
                changeStatus(AgentStatus.STOPPED);
            } else {
                logger.warn("Cannot stop agent. Current status = [{}]", this.agentStatus);
                return;
            }
        }
        logger.info("Stopping {} Agent.", ProductInfo.NAME);

        this.agentInfoSender.stop();
        this.agentStatMonitor.stop();

        // Need to process stop
        this.spanDataSender.stop();
        this.statDataSender.stop();

        closeTcpDataSender();
        // for testcase
        if (staticResourceCleanup) {
            PLoggerFactory.unregister(this.binder);
            this.interceptorRegistryBinder.unbind();
        }
    }

    private void closeTcpDataSender() {
        if (this.tcpDataSender != null) {
            this.tcpDataSender.stop();
        }
        if (this.client != null) {
            this.client.close();
        }
        if (this.clientFactory != null) {
            this.clientFactory.release();
        }
    }

}
