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

import com.navercorp.pinpoint.bootstrap.AgentOption;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.config.ProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClassPool;
import com.navercorp.pinpoint.bootstrap.sampler.Sampler;
import com.navercorp.pinpoint.common.service.ServiceTypeRegistryService;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.AgentInfoSender;
import com.navercorp.pinpoint.profiler.AgentInformation;
import com.navercorp.pinpoint.profiler.AgentInformationFactory;
import com.navercorp.pinpoint.profiler.ClassFileTransformerDispatcher;
import com.navercorp.pinpoint.profiler.DynamicTransformService;
import com.navercorp.pinpoint.profiler.JvmInformationFactory;
import com.navercorp.pinpoint.profiler.context.active.ActiveTraceRepository;
import com.navercorp.pinpoint.profiler.context.monitor.PluginMonitorContext;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.PinpointClientProvider;
import com.navercorp.pinpoint.profiler.context.provider.Provider;
import com.navercorp.pinpoint.profiler.context.provider.ServerMetaDataHolderProvider;
import com.navercorp.pinpoint.profiler.context.provider.StorageFactoryProvider;
import com.navercorp.pinpoint.profiler.context.provider.TcpDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.UdpSpanDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.provider.UdpStatDataSenderProvider;
import com.navercorp.pinpoint.profiler.context.storage.StorageFactory;
import com.navercorp.pinpoint.profiler.instrument.ASMBytecodeDumpService;
import com.navercorp.pinpoint.profiler.instrument.ASMClassPool;
import com.navercorp.pinpoint.profiler.instrument.BytecodeDumpTransformer;
import com.navercorp.pinpoint.profiler.instrument.JavassistClassPool;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.SqlMetaDataService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataCacheService;
import com.navercorp.pinpoint.profiler.metadata.StringMetaDataService;
import com.navercorp.pinpoint.profiler.monitor.AgentStatMonitor;
import com.navercorp.pinpoint.profiler.monitor.codahale.AgentStatCollectorFactory;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.ProfilerPluginLoader;
import com.navercorp.pinpoint.profiler.receiver.CommandDispatcher;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandLocatorBuilder;
import com.navercorp.pinpoint.profiler.receiver.ProfilerCommandServiceLocator;
import com.navercorp.pinpoint.profiler.receiver.service.ActiveThreadService;
import com.navercorp.pinpoint.profiler.receiver.service.EchoService;
import com.navercorp.pinpoint.profiler.sampler.SamplerFactory;
import com.navercorp.pinpoint.profiler.sender.DataSender;
import com.navercorp.pinpoint.profiler.sender.EnhancedDataSender;
import com.navercorp.pinpoint.profiler.util.ApplicationServerTypeResolver;
import com.navercorp.pinpoint.rpc.client.PinpointClient;
import com.navercorp.pinpoint.rpc.client.PinpointClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.List;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DefaultApplicationContext implements ApplicationContext {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ProfilerConfig profilerConfig;

    private final AgentInfoSender agentInfoSender;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private final PinpointClientFactory clientFactory;
    private final PinpointClient client;
    private final EnhancedDataSender tcpDataSender;

    private final DataSender statDataSender;
    private final DataSender spanDataSender;

    private final AgentInformation agentInformation;
    private final ServerMetaDataHolder serverMetaDataHolder;
    private final AgentOption agentOption;

    private final ServiceTypeRegistryService serviceTypeRegistryService;

    private final ClassFileTransformerDispatcher classFileTransformer;

    private final Instrumentation instrumentation;
    private final InstrumentClassPool classPool;
    private final DynamicTransformService dynamicTransformService;

    public DefaultApplicationContext(AgentOption agentOption, final InterceptorRegistryBinder interceptorRegistryBinder) {
        if (agentOption == null) {
            throw new NullPointerException("agentOption must not be null");
        }
        if (interceptorRegistryBinder == null) {
            throw new NullPointerException("interceptorRegistryBinder must not be null");
        }

        this.agentOption = agentOption;
        this.profilerConfig = agentOption.getProfilerConfig();
        this.instrumentation = agentOption.getInstrumentation();
        this.serviceTypeRegistryService = agentOption.getServiceTypeRegistryService();

        this.classPool = createInstrumentEngine(this.profilerConfig, agentOption, interceptorRegistryBinder);

        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }

        List<DefaultProfilerPluginContext> pluginContexts = loadPlugins(agentOption);

        this.classFileTransformer = new ClassFileTransformerDispatcher(this, pluginContexts);
        this.dynamicTransformService = new DynamicTransformService(instrumentation, classFileTransformer);

        ClassFileTransformer wrappedTransformer = wrapClassFileTransformer(this.profilerConfig, classFileTransformer);
        instrumentation.addTransformer(wrappedTransformer, true);

        String applicationServerTypeString = profilerConfig.getApplicationServerType();
        ServiceType applicationServerType = this.serviceTypeRegistryService.findServiceTypeByName(applicationServerTypeString);

        final ApplicationServerTypeResolver typeResolver = new ApplicationServerTypeResolver(pluginContexts, applicationServerType, profilerConfig.getApplicationTypeDetectOrder());

        final AgentInformationFactory agentInformationFactory = new AgentInformationFactory(agentOption.getAgentId(), agentOption.getApplicationName());
        this.agentInformation = agentInformationFactory.createAgentInformation(typeResolver.resolve());
        logger.info("agentInformation:{}", agentInformation);

        final Provider<ServerMetaDataHolder> serverMetaDataHolderProvider = newServerMetaDataHolderProvider();
        this.serverMetaDataHolder = serverMetaDataHolderProvider.get();

        Provider<DataSender> udpSpanDataSenderProvider = newUdpSpanDataSenderProvider();
        this.spanDataSender = udpSpanDataSenderProvider.get();
        logger.info("spanDataSender:{}", spanDataSender);

        Provider<DataSender> udpStatDataSenderProvider = newUdpStatDataSenderProvider();
        this.statDataSender = udpStatDataSenderProvider.get();
        logger.info("statDataSender:{}", statDataSender);

        final ActiveTraceRepository activeTraceRepository = createActiveTraceRepository(profilerConfig);
        final CommandDispatcher commandService = createCommandService(profilerConfig, activeTraceRepository);

        Provider<PinpointClientFactory> pinpointClientFactoryProvider = newPinpointClientFactoryProvider(profilerConfig, this.agentInformation, commandService);
        this.clientFactory = pinpointClientFactoryProvider.get();
        logger.info("clientFactory:{}", clientFactory);

        Provider<PinpointClient> pinpointClientProvider = newPinpointClientProvider(profilerConfig, clientFactory);
        this.client = pinpointClientProvider.get();
        logger.info("client:{}", client);

        Provider<EnhancedDataSender> tcpDataSenderProvider = newTcpDataSenderProvider(client);
        this.tcpDataSender = tcpDataSenderProvider.get();
        logger.info("tcpDataSender:{}", tcpDataSender);

        final IdGenerator idGenerator = new IdGenerator();
        final TransactionCounter transactionCounter = new DefaultTransactionCounter(idGenerator);

        final PluginMonitorContext pluginMonitorContext = createPluginMonitorContext(this.profilerConfig);

        Provider<StorageFactory> storageFactoryProvider = newStorageFactoryProvider(profilerConfig, spanDataSender, agentInformation);
        final StorageFactory storageFactory = storageFactoryProvider.get();
        this.traceContext = newTraceContext(this.profilerConfig, storageFactory, this.serverMetaDataHolder, this.tcpDataSender, idGenerator, activeTraceRepository, pluginMonitorContext);
        final AgentStatCollectorFactory agentStatCollectorFactory = new AgentStatCollectorFactory(profilerConfig, activeTraceRepository, transactionCounter, pluginMonitorContext);

        final JvmInformationFactory jvmInformationFactory = new JvmInformationFactory(agentStatCollectorFactory.getGarbageCollector());

        this.agentInfoSender = new AgentInfoSender.Builder(this.tcpDataSender, this.agentInformation, jvmInformationFactory.createJvmInformation()).sendInterval(profilerConfig.getAgentInfoSendRetryInterval()).build();
        this.serverMetaDataHolder.addListener(this.agentInfoSender);
        this.agentStatMonitor = new AgentStatMonitor(this.statDataSender, this.agentInformation.getAgentId(), this.agentInformation.getStartTime(), agentStatCollectorFactory);
    }

    protected Provider<DataSender> newUdpStatDataSenderProvider() {
        return new UdpStatDataSenderProvider(profilerConfig);
    }

    protected Provider<DataSender> newUdpSpanDataSenderProvider() {
        return new UdpSpanDataSenderProvider(profilerConfig);
    }

    @Override
    public ProfilerConfig getProfilerConfig() {
        return profilerConfig;
    }

    protected Provider<ServerMetaDataHolder> newServerMetaDataHolderProvider() {
        return new ServerMetaDataHolderProvider();
    }

    @Override
    public TraceContext getTraceContext() {
        return traceContext;
    }

    public DataSender getSpanDataSender() {
        return spanDataSender;
    }

    @Override
    public InstrumentClassPool getClassPool() {
        return classPool;
    }


    private ActiveTraceRepository createActiveTraceRepository(ProfilerConfig profilerConfig) {
        if (profilerConfig.isTraceAgentActiveThread()) {
            return new ActiveTraceRepository();
        }
        return null;
    }

    private InstrumentClassPool createInstrumentEngine(ProfilerConfig profilerConfig, AgentOption agentOption, InterceptorRegistryBinder interceptorRegistryBinder) {

        final String instrumentEngine = profilerConfig.getProfileInstrumentEngine().toUpperCase();

        if (DefaultProfilerConfig.INSTRUMENT_ENGINE_ASM.equals(instrumentEngine)) {
            logger.info("ASM InstrumentEngine.");

            return new ASMClassPool(interceptorRegistryBinder, agentOption.getBootstrapJarPaths());

        } else if (DefaultProfilerConfig.INSTRUMENT_ENGINE_JAVASSIST.equals(instrumentEngine)) {
            logger.info("JAVASSIST InstrumentEngine.");

            return new JavassistClassPool(interceptorRegistryBinder, agentOption.getBootstrapJarPaths());
        } else {
            logger.warn("Unknown InstrumentEngine:{}", instrumentEngine);

            throw new IllegalArgumentException("Unknown InstrumentEngine:" + instrumentEngine);
        }
    }

    private ClassFileTransformer wrapClassFileTransformer(ProfilerConfig profilerConfig, ClassFileTransformer classFileTransformerDispatcher) {
        final boolean enableBytecodeDump = profilerConfig.readBoolean(ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP, ASMBytecodeDumpService.ENABLE_BYTECODE_DUMP_DEFAULT_VALUE);
        if (enableBytecodeDump) {
            logger.info("wrapBytecodeDumpTransformer");
            return BytecodeDumpTransformer.wrap(classFileTransformerDispatcher, profilerConfig);
        }
        return classFileTransformerDispatcher;
    }

    @Override
    public List<String> getBootstrapJarPaths() {
        return agentOption.getBootstrapJarPaths();
    }

    protected List<DefaultProfilerPluginContext> loadPlugins(AgentOption agentOption) {
        final ProfilerPluginLoader loader = new ProfilerPluginLoader(this);
        return loader.load(agentOption.getPluginJars());
    }

    private CommandDispatcher createCommandService(ProfilerConfig profilerConfig, ActiveTraceRepository activeTraceRepository) {
        ProfilerCommandLocatorBuilder builder = new ProfilerCommandLocatorBuilder();
        builder.addService(new EchoService());
        if (activeTraceRepository != null) {
            ActiveThreadService activeThreadService = new ActiveThreadService(profilerConfig, activeTraceRepository);
            builder.addService(activeThreadService);
        }

        ProfilerCommandServiceLocator commandServiceLocator = builder.build();
        CommandDispatcher commandDispatcher = new CommandDispatcher(commandServiceLocator);
        return commandDispatcher;
    }

    @Override
    public DynamicTransformService getDynamicTransformService() {
        return dynamicTransformService;
    }

    @Override
    public Instrumentation getInstrumentation() {
        return instrumentation;
    }

    @Override
    public ClassFileTransformerDispatcher getClassFileTransformerDispatcher() {
        return classFileTransformer;
    }

    @Override
    public AgentInformation getAgentInformation() {
        return this.agentInformation;
    }



    private TraceContext newTraceContext(ProfilerConfig profilerConfig, StorageFactory storageFactory, ServerMetaDataHolder serverMetaDataHolder, EnhancedDataSender enhancedDataSender, IdGenerator idGenerator, ActiveTraceRepository activeTraceRepository, PluginMonitorContext pluginMonitorContext) {


        logger.info("StorageFactoryType:{}", storageFactory);

        final Sampler sampler = createSampler(profilerConfig);
        logger.info("SamplerType:{}", sampler);

        final TraceFactoryBuilder traceFactoryBuilder = createTraceFactory(storageFactory, sampler, idGenerator, activeTraceRepository);

        final String agentId = this.agentInformation.getAgentId();
        final long agentStartTime = this.agentInformation.getStartTime();

        final ApiMetaDataService apiMetaDataService = newApiMetaDataCacheService(profilerConfig, agentId, agentStartTime, enhancedDataSender);
        final StringMetaDataService stringMetaDataService = newStringMetaDataCacheService(profilerConfig, agentId, agentStartTime, enhancedDataSender);
        final SqlMetaDataService sqlMetaDataService = newSqlMetaDataService(profilerConfig, agentId, agentStartTime, enhancedDataSender);

        final TraceContext traceContext = new DefaultTraceContext(profilerConfig, this.agentInformation,
                traceFactoryBuilder, pluginMonitorContext, serverMetaDataHolder,
                apiMetaDataService, stringMetaDataService, sqlMetaDataService
        );

        return traceContext;
    }

    private SqlMetaDataService newSqlMetaDataService(ProfilerConfig profilerConfig, String agentId, long agentStartTime, EnhancedDataSender enhancedDataSender) {
        int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        return new SqlMetaDataCacheService(agentId, agentStartTime, enhancedDataSender, jdbcSqlCacheSize);
    }

    private StringMetaDataCacheService newStringMetaDataCacheService(ProfilerConfig profilerConfig, String agentId, long agentStartTime, EnhancedDataSender enhancedDataSender) {
        return new StringMetaDataCacheService(agentId, agentStartTime, enhancedDataSender);
    }

    protected ApiMetaDataService newApiMetaDataCacheService(ProfilerConfig profilerConfig, String agentId, long agentStartTime, EnhancedDataSender enhancedDataSender) {
        return new ApiMetaDataCacheService(agentId, agentStartTime, enhancedDataSender);
    }


    private PluginMonitorContext createPluginMonitorContext(ProfilerConfig profilerConfig) {
        final boolean traceDataSource = profilerConfig.isTraceAgentDataSource();
        final PluginMonitorContextBuilder monitorContextBuilder = new PluginMonitorContextBuilder(traceDataSource);
        return monitorContextBuilder.build();
    }

    private TraceFactoryBuilder createTraceFactory(StorageFactory storageFactory, Sampler sampler, IdGenerator idGenerator, ActiveTraceRepository activeTraceRepository) {

        final TraceFactoryBuilder builder = new DefaultTraceFactoryBuilder(storageFactory, sampler, idGenerator, activeTraceRepository);
        return builder;
    }


    protected Provider<StorageFactory> newStorageFactoryProvider(ProfilerConfig profilerConfig, DataSender spanDataSender, AgentInformation agentInformation) {
        return new StorageFactoryProvider(profilerConfig, spanDataSender, agentInformation);
    }

    private Sampler createSampler(ProfilerConfig profilerConfig) {
        boolean samplingEnable = profilerConfig.isSamplingEnable();
        int samplingRate = profilerConfig.getSamplingRate();

        SamplerFactory samplerFactory = new SamplerFactory();
        return samplerFactory.createSampler(samplingEnable, samplingRate);
    }

    protected Provider<PinpointClientFactory> newPinpointClientFactoryProvider(ProfilerConfig profilerConfig, AgentInformation agentInformation, CommandDispatcher commandDispatcher) {
        Provider<PinpointClientFactory> pinpointClientFactoryProvider = new PinpointClientFactoryProvider(profilerConfig, agentInformation, commandDispatcher);
        return pinpointClientFactoryProvider;
    }

    protected Provider<PinpointClient> newPinpointClientProvider(ProfilerConfig profilerConfig, PinpointClientFactory clientFactory) {
        return new PinpointClientProvider(profilerConfig, clientFactory);
    }

    protected Provider<EnhancedDataSender> newTcpDataSenderProvider(PinpointClient client) {
        return new TcpDataSenderProvider(client);
    }



    @Override
    public void start() {
        this.agentInfoSender.start();
        this.agentStatMonitor.start();
    }

    @Override
    public void close() {
        this.agentInfoSender.stop();
        this.agentStatMonitor.stop();

        // Need to process stop
        this.spanDataSender.stop();
        this.statDataSender.stop();

        closeTcpDataSender();
    }

    private void closeTcpDataSender() {
        final EnhancedDataSender tcpDataSender = this.tcpDataSender;
        if (tcpDataSender != null) {
            tcpDataSender.stop();
        }
        final PinpointClient client = this.client;
        if (client != null) {
            client.close();
        }
        final PinpointClientFactory clientFactory = this.clientFactory;
        if (clientFactory != null) {
            clientFactory.release();
        }
    }

}
