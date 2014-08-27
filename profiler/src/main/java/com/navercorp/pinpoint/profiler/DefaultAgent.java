package com.nhn.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerBinder;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.bootstrap.sampler.Sampler;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.context.storage.BufferedStorageFactory;
import com.nhn.pinpoint.profiler.context.storage.SpanStorageFactory;
import com.nhn.pinpoint.profiler.context.storage.StorageFactory;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.modifier.arcus.ArcusMethodFilter;
import com.nhn.pinpoint.profiler.monitor.AgentStatMonitor;
import com.nhn.pinpoint.profiler.sampler.SamplerFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;
import com.nhn.pinpoint.profiler.util.ApplicationServerTypeResolver;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;
import com.nhn.pinpoint.rpc.ClassPreLoader;
import com.nhn.pinpoint.rpc.PinpointSocketException;
import com.nhn.pinpoint.rpc.client.MessageListener;
import com.nhn.pinpoint.rpc.client.PinpointSocket;
import com.nhn.pinpoint.rpc.client.PinpointSocketFactory;
import com.nhn.pinpoint.rpc.client.SimpleLoggingMessageListener;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;

/**
 * @author emeroad
 * @author koo.taejin
 */
public class DefaultAgent implements Agent {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PLoggerBinder binder;

    private final ByteCodeInstrumentor byteCodeInstrumentor;
    private final ClassFileTransformer classFileTransformer;

    private final ProfilerConfig profilerConfig;

    private final ServerInfo serverInfo;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private final PinpointSocketFactory factory;
    private final PinpointSocket socket;
    
    private final EnhancedDataSender tcpDataSender;
    private final DataSender statDataSender;
    private final DataSender spanDataSender;

    private final AgentInformation agentInformation;

    // agent info는 heartbeat에서 매번 사용한다.
    // TODO 잠재적 멀티Thread문제가 발생가능할수 있음
    // datasend할때 따로 생성해서 보도록 한다.
    private final TAgentInfo tAgentInfo;

    // agent의 상태,
    private volatile AgentStatus agentStatus;
    private HeartBitChecker heartBitChecker;

    static {
        // rpc쪽 preload
        ClassPreLoader.preload();
    }


    public DefaultAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
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

        this.profilerConfig = profilerConfig;
        this.serverInfo = new ServerInfo();

        final ApplicationServerTypeResolver typeResolver = new ApplicationServerTypeResolver(profilerConfig.getApplicationServerType());
        if (!typeResolver.resolve()) {
            throw new PinpointException("ApplicationServerType not found.");
        }
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(typeResolver.getServerLibPath(), this);
        if (logger.isInfoEnabled()) {
            logger.info("DefaultAgent classLoader:{}", this.getClass().getClassLoader());
        }
        this.classFileTransformer = new ClassFileTransformerDispatcher(this, byteCodeInstrumentor);
        instrumentation.addTransformer(this.classFileTransformer);

        final AgentInformationFactory agentInformationFactory = new AgentInformationFactory();
        this.agentInformation = agentInformationFactory.createAgentInformation(typeResolver.getServerType());
        logger.info("agentInformation:{}", agentInformation);

        this.tAgentInfo = createTAgentInfo();
        
        this.factory = createPinpointSocketFactory();
        this.socket = createPinpointSocket(this.profilerConfig.getCollectorServerIp(), this.profilerConfig.getCollectorTcpServerPort(), factory, this.profilerConfig.isTcpDataSenderCommandAcceptEnable());
        
        this.tcpDataSender = createTcpDataSender(socket);
        
        this.spanDataSender = createUdpDataSender(this.profilerConfig.getCollectorUdpSpanServerPort(), "Pinpoint-UdpSpanDataExecutor",
                this.profilerConfig.getSpanDataSenderWriteQueueSize(), this.profilerConfig.getSpanDataSenderSocketTimeout(), this.profilerConfig.getSpanDataSenderSocketSendBufferSize());
        this.statDataSender = createUdpDataSender(this.profilerConfig.getCollectorUdpServerPort(), "Pinpoint-UdpStatDataExecutor",
                this.profilerConfig.getStatDataSenderWriteQueueSize(), this.profilerConfig.getStatDataSenderSocketTimeout(), this.profilerConfig.getStatDataSenderSocketSendBufferSize());


        this.traceContext = createTraceContext(agentInformation.getServerType());

        this.heartBitChecker = new HeartBitChecker(tcpDataSender, profilerConfig.getHeartbeatInterval(), tAgentInfo);

        // JVM 통계 등을 주기적으로 수집하여 collector에 전송하는 monitor를 초기화한다.
        this.agentStatMonitor = new AgentStatMonitor(this.statDataSender, this.agentInformation.getAgentId(), this.agentInformation.getStartTime());

        preLoadClass();
        
        /**
         * FIXME
         * tomcat의 경우에는 com.nhn.pinpoint.profiler.modifier.tomcat.interceptor.CatalinaAwaitInterceptor가
         * org/apache/catalina/startup/Catalina/await함수가 실행되기 전에 실행해주나.
         * stand alone application은 그렇지 않으므로..
         */
        if (typeResolver.isManuallyStartupRequired()) {
        	started();
        }
    }

    private void preLoadClass() {
        logger.debug("preLoadClass:{}", new ArcusMethodFilter().getClass().getName());
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


    private TAgentInfo createTAgentInfo() {
        final ServerInfo serverInfo = this.serverInfo;
        String ip = serverInfo.getHostip();
        final StringBuilder ports = new StringBuilder();
        for (Entry<Integer, String> entry : serverInfo.getConnectors().entrySet()) {
            ports.append(" ");
            ports.append(entry.getKey());
        }

        final TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setIp(ip);
        agentInfo.setHostname(this.agentInformation.getMachineName());
        agentInfo.setPorts(ports.toString());
        agentInfo.setAgentId(agentInformation.getAgentId());
        agentInfo.setApplicationName(agentInformation.getApplicationName());
        agentInfo.setPid(agentInformation.getPid());
        agentInfo.setStartTimestamp(agentInformation.getStartTime());
		agentInfo.setServiceType(agentInformation.getServerType());
        agentInfo.setVersion(Version.VERSION);

//        agentInfo.setIsAlive(true);

        return agentInfo;
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
        final DefaultTraceContext traceContext = new DefaultTraceContext(jdbcSqlCacheSize, serverType, storageFactory, sampler);
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

    protected PinpointSocketFactory createPinpointSocketFactory() {
    	Map<String, Object> properties =  this.agentInformation.toMap();
    	properties.put(AgentPropertiesType.IP.getName(), serverInfo.getHostip());

    	PinpointSocketFactory pinpointSocketFactory = new PinpointSocketFactory();
        pinpointSocketFactory.setTimeoutMillis(1000 * 5);
        pinpointSocketFactory.setProperties(properties);

        return pinpointSocketFactory;
	}
    
    protected PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory) {
    	return createPinpointSocket(host, port, factory, false);
    }
    
    protected PinpointSocket createPinpointSocket(String host, int port, PinpointSocketFactory factory, boolean useMessageListener) {
    	// 1.2 버전이 Tcp Data Command 허용하는 버전이 아니기 떄문에 true이던 false이던 무조건 SimpleLoggingMessageListener를 이용하게 함
    	// SimpleLoggingMessageListener.LISTENER 는 서로 통신을 하지 않게 설정되어 있음 (테스트코드는 pinpoint-rpc에 존재)
    	// 1.3 버전으로 할 경우 아래 분기에서 MessageListener 변경 필요
    	MessageListener messageListener = null;
    	if (useMessageListener) {
    		messageListener = SimpleLoggingMessageListener.LISTENER;
    	} else {
    		messageListener = SimpleLoggingMessageListener.LISTENER;
    	}
    	
    	PinpointSocket socket = null;
    	for (int i = 0; i < 3; i++) {
            try {
                socket = factory.connect(host, port, messageListener);
                logger.info("tcp connect success:{}/{}", host, port);
                return socket;
            } catch (PinpointSocketException e) {
                logger.warn("tcp connect fail:{}/{} try reconnect, retryCount:{}", host, port, i);
            }
        }
        logger.warn("change background tcp connect mode  {}/{} ", host, port);
        socket = factory.scheduledConnect(host, port, messageListener);
    	
        return socket;
    }

    protected EnhancedDataSender createTcpDataSender(PinpointSocket socket) {
        return new TcpDataSender(socket);
    }
    
    protected DataSender createUdpDataSender(int port, String threadName, int writeQueueSize, int timeout, int sendBufferSize) {
        return new UdpDataSender(this.profilerConfig.getCollectorServerIp(), port, threadName, writeQueueSize, timeout, sendBufferSize);
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

    public void addConnector(String protocol, int port){
        this.serverInfo.addConnector(protocol, port);
    }


    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }

    public AgentInformation getAgentInformation() {
        return agentInformation;
    }

    public boolean isRunning() {
        return agentStatus == AgentStatus.RUNNING;
    }

    // TODO 필요없을것 같음 started를 start로 바꿔도 될 듯...
    @Override
    public void start() {
        logger.info("Starting {} Agent.", ProductInfo.CAMEL_NAME);
    }

    /**
     * org/apache/catalina/startup/Catalina/await함수가 호출되기 전에 실행된다.
     * Tomcat이 구동되고 context가 모두 로드 된 다음 사용자의 요청을 처리할 수 있게 되었을 때 실행됨.
     */
    public void started() {
        changeStatus(AgentStatus.RUNNING);
        this.heartBitChecker.start();
        this.agentStatMonitor.start();
    }

    @Override
    public void stop() {
        logger.info("Stopping {} Agent.", ProductInfo.CAMEL_NAME);

        this.heartBitChecker.stop();

        tAgentInfo.setEndStatus(0);
        tAgentInfo.setEndTimestamp(System.currentTimeMillis());
        this.tcpDataSender.send(tAgentInfo);
        // TODO send tAgentInfo alive false후 send 메시지의 처리가 정확하지 않음

        changeStatus(AgentStatus.STOPPING);

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
        
        changeStatus(AgentStatus.STOPPED);
    }

}
