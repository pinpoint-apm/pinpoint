package com.nhn.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.ServerMetaDataHolder;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerBinder;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.sampler.Sampler;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.context.DefaultServerMetaDataHolder;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.context.storage.BufferedStorageFactory;
import com.nhn.pinpoint.profiler.context.storage.SpanStorageFactory;
import com.nhn.pinpoint.profiler.context.storage.StorageFactory;
import com.nhn.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.monitor.AgentStatMonitor;
import com.nhn.pinpoint.profiler.receiver.CommandDispatcher;
import com.nhn.pinpoint.profiler.receiver.service.EchoService;
import com.nhn.pinpoint.profiler.receiver.service.ThreadDumpService;
import com.nhn.pinpoint.profiler.sampler.SamplerFactory;
import com.nhn.pinpoint.profiler.sender.BufferedUdpDataSender;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;
import com.nhn.pinpoint.profiler.util.ApplicationServerTypeResolver;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;
import com.nhn.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.nhn.pinpoint.rpc.ClassPreLoader;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;

/**
 * @author emeroad
 * @author koo.taejin
 * @author hyungil.jeong
 */
public class DefaultAgent implements Agent {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PLoggerBinder binder;

    private final ByteCodeInstrumentor byteCodeInstrumentor;
    private final ClassFileTransformer classFileTransformer;
    
    private final String agentPath;
    private final ProfilerConfig profilerConfig;

    private final AgentInfoSender agentInfoSender;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private final PinpointSocketFactory factory;
    private final PinpointSocket socket;

    private final EnhancedDataSender tcpDataSender;
    private final DataSender statDataSender;
    private final DataSender spanDataSender;

    private final AgentInformation agentInformation;
    private final ServerMetaDataHolder serverMetaDataHolder;

    // agent의 상태,
    private volatile AgentStatus agentStatus;

    static {
        // rpc쪽 preload
        ClassPreLoader.preload();
    }

    public DefaultAgent(String agentPath, String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        if (agentPath == null) {
            throw new NullPointerException("agentPath must not be null");
        }
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }
        
        this.binder = new Slf4jLoggerBinder();
        bindPLoggerFactory(this.binder);

        dumpSystemProperties();
        dumpConfig(profilerConfig);

        changeStatus(AgentStatus.INITIALIZING);

        this.agentPath = agentPath;
        this.profilerConfig = profilerConfig;

        final ApplicationServerTypeResolver typeResolver = new ApplicationServerTypeResolver(profilerConfig.getApplicationServerType());
        if (!typeResolver.resolve()) {
            throw new PinpointException("ApplicationServerType not found.");
        }
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(typeResolver.getServerLibPath(), this);
        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }
        
        final AgentInformationFactory agentInformationFactory = new AgentInformationFactory();
        this.agentInformation = agentInformationFactory.createAgentInformation(typeResolver.getServerType());
        logger.info("agentInformation:{}", agentInformation);

        CommandDispatcher commandDispatcher = new CommandDispatcher();
        commandDispatcher.registerCommandService(new ThreadDumpService());
        commandDispatcher.registerCommandService(new EchoService());
        
        this.factory = createPinpointSocketFactory(commandDispatcher);
        this.socket = createPinpointSocket(this.profilerConfig.getCollectorTcpServerIp(), this.profilerConfig.getCollectorTcpServerPort(), factory);

        this.serverMetaDataHolder = createServerMetaDataHolder();
        
        this.tcpDataSender = createTcpDataSender(socket);

        this.spanDataSender = createBufferedUdpSpanDataSender(this.profilerConfig.getCollectorSpanServerPort(), "Pinpoint-UdpSpanDataExecutor",
                this.profilerConfig.getSpanDataSenderWriteQueueSize(), this.profilerConfig.getSpanDataSenderSocketTimeout(),
                this.profilerConfig.getSpanDataSenderSocketSendBufferSize(), this.profilerConfig.getSpanDataSenderChunkSize());
        this.statDataSender = createUdpStatDataSender(this.profilerConfig.getCollectorStatServerPort(), "Pinpoint-UdpStatDataExecutor",
                this.profilerConfig.getStatDataSenderWriteQueueSize(), this.profilerConfig.getStatDataSenderSocketTimeout(),
                this.profilerConfig.getStatDataSenderSocketSendBufferSize());

        this.traceContext = createTraceContext(agentInformation.getServerType());

        this.agentInfoSender = new AgentInfoSender(tcpDataSender, profilerConfig.getAgentInfoSendRetryInterval(), this.agentInformation, this.serverMetaDataHolder);

        // JVM 통계 등을 주기적으로 수집하여 collector에 전송하는 monitor를 초기화한다.
        this.agentStatMonitor = new AgentStatMonitor(this.statDataSender, this.agentInformation.getAgentId(), this.agentInformation.getStartTime());
        
        
        ClassFileRetransformer retransformer = new ClassFileRetransformer(instrumentation);
        instrumentation.addTransformer(retransformer, true);
        this.classFileTransformer = new ClassFileTransformerDispatcher(this, byteCodeInstrumentor, retransformer);
        instrumentation.addTransformer(this.classFileTransformer);


        preLoadClass();

        /**
         * FIXME tomcat의 경우에는 com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.CatalinaAwaitInterceptor가 org/apache/catalina/startup/Catalina/await함수가 실행되기
         * 전에 실행해주나. stand alone application은 그렇지 않으므로..
         */
        if (typeResolver.isManuallyStartupRequired()) {
            start();
        }
    }

    private void preLoadClass() {
        logger.debug("preLoadClass:{}", PreparedStatementUtils.class.getName(), PreparedStatementUtils.findBindVariableSetMethod());
    }

    public ByteCodeInstrumentor getByteCodeInstrumentor() {
        return byteCodeInstrumentor;
    }

    public ClassFileTransformer getClassFileTransformer() {
        return classFileTransformer;
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
        // static LoggerFactory에 binder를 붙임.
        PLoggerFactory.initialize(binder);
        // shutdown hook이나 stop에 LoggerBinder의 연결을 풀어야 되는가?
    }

    private TraceContext createTraceContext(short serverType) {
        final StorageFactory storageFactory = createStorageFactory();
        logger.info("StorageFactoryType:{}", storageFactory);

        final Sampler sampler = createSampler();
        logger.info("SamplerType:{}", sampler);
        
        final int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        final DefaultTraceContext traceContext = new DefaultTraceContext(jdbcSqlCacheSize, serverType, storageFactory, sampler, this.serverMetaDataHolder);
        traceContext.setAgentInformation(this.agentInformation);
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

    protected PinpointSocketFactory createPinpointSocketFactory(CommandDispatcher commandDispatcher) {
    	PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);

        Map<String, Object> properties = this.agentInformation.toMap();
        
        boolean isSupportServerMode = this.profilerConfig.isTcpDataSenderCommandAcceptEnable();
        
        if (isSupportServerMode) {
        	pinpointSocketFactory.setMessageListener(commandDispatcher);
        	pinpointSocketFactory.setServerStreamChannelMessageListener(commandDispatcher);

        	properties.put(AgentHandshakePropertyType.SUPPORT_SERVER.getName(), true);
        } else {
        	properties.put(AgentHandshakePropertyType.SUPPORT_SERVER.getName(), false);
        }

        pinpointSocketFactory.setProperties(properties);
        return pinpointSocketFactory;
    }

    protected PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
        // 1.2 버전이 Tcp Data Command 허용하는 버전이 아니기 떄문에 true이던 false이던 무조건 SimpleLoggingMessageListener를 이용하게 함
        // SimpleLoggingMessageListener.LISTENER 는 서로 통신을 하지 않게 설정되어 있음 (테스트코드는 pinpoint-rpc에 존재)
        // 1.3 버전으로 할 경우 아래 분기에서 MessageListener 변경 필요

        PinpointSocket socket = null;
        for (int i = 0; i < 3; i++) {
            try {
                socket = factory.connect(host, port);
                logger.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        socket = factory.scheduledConnect(host, port);

        return socket;
    }

    protected EnhancedDataSender createTcpDataSender(PinpointSocket socket) {
        return new TcpDataSender(socket);
    }

    protected DataSender createUdpStatDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new UdpDataSender(this.profilerConfig.getCollectorStatServerIp(), port, threadName, writeQueueSize, timeout, sendBufferSize);
    }
    
    protected DataSender createBufferedUdpSpanDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize, int chunkSize) {
        return new BufferedUdpDataSender(this.profilerConfig.getCollectorSpanServerIp(), port, threadName, writeQueueSize, timeout, sendBufferSize, chunkSize);
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
    
    public String getAgentPath() {
        return agentPath;
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
        logger.info("Starting {} Agent.", ProductInfo.CAMEL_NAME);
        this.agentInfoSender.start();
        this.agentStatMonitor.start();
    }

    @Override
    public void stop() {
        synchronized (this) {
            if (this.agentStatus == AgentStatus.RUNNING) {
                changeStatus(AgentStatus.STOPPED);
            } else {
                logger.warn("Cannot stop agent. Current status = [{}]", this.agentStatus);
                return;
            }
        }
        logger.info("Stopping {} Agent.", ProductInfo.CAMEL_NAME);

        this.agentInfoSender.stop();
        this.agentStatMonitor.stop();

        // 종료 처리 필요.
        this.spanDataSender.stop();
        this.statDataSender.stop();
        this.tcpDataSender.stop();

        if (this.socket != null) {
            this.socket.close();
        }
        if (this.factory != null) {
            this.factory.release();
        }
    }

}
