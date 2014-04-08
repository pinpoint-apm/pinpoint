package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.context.TraceContext;
import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.Version;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.exception.PinpointException;
import com.nhn.pinpoint.profiler.context.*;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerBinder;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.modifier.arcus.ArcusMethodFilter;
import com.nhn.pinpoint.profiler.sender.EnhancedDataSender;
import com.nhn.pinpoint.profiler.util.ApplicationServerTypeResolver;
import com.nhn.pinpoint.profiler.util.PreparedStatementUtils;
import com.nhn.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.nhn.pinpoint.rpc.ClassPreLoader;
import com.nhn.pinpoint.thrift.dto.TAgentInfo;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.monitor.AgentStatMonitor;
import com.nhn.pinpoint.bootstrap.sampler.Sampler;
import com.nhn.pinpoint.profiler.sampler.SamplerFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;
import com.nhn.pinpoint.bootstrap.util.NetworkUtils;
import com.nhn.pinpoint.thrift.dto.TResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

/**
 * @author emeroad
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


        // TODO 일단 임시로 호환성을 위해 agentid에 machinename을 넣도록 하자
        // TODO 박스 하나에 서버 인스턴스를 여러개 실행할 때에 문제가 될 수 있음.
        this.agentInformation = createAgentInformation(typeResolver.getServerType());
        logger.info("agentInformation:{}", agentInformation);

        this.tAgentInfo = createTAgentInfo();
        this.tcpDataSender = createTcpDataSender();
        this.spanDataSender = createUdpDataSender(this.profilerConfig.getCollectorUdpSpanServerPort(), "Pinpoint-UdpSpanDataExecutor", this.profilerConfig.getSpanDataSenderWriteQueueSize());
        this.statDataSender = createUdpDataSender(this.profilerConfig.getCollectorUdpServerPort(), "Pinpoint-UdpStatDataExecutor", this.profilerConfig.getStatDataSenderWriteQueueSize());


        this.traceContext = createTraceContext();

        this.heartBitChecker = new HeartBitChecker(tcpDataSender, profilerConfig.getHeartbeatInterval(), tAgentInfo);

        // JVM 통계 등을 주기적으로 수집하여 collector에 전송하는 monitor를 초기화한다.
        this.agentStatMonitor = new AgentStatMonitor(this.statDataSender, this.agentInformation.getAgentId(), this.agentInformation.getStartTime());

        preLoadClass();
    }

    private void preLoadClass() {
        // rpc쪽 preload
        ClassPreLoader.preload();
        logger.debug("preLoadClass:{}", new ArcusMethodFilter().getClass().getName());
        logger.debug("preLoadClass:{}", PreparedStatementUtils.class.getName(), PreparedStatementUtils.findBindVariableSetMethod());
    }


    private AgentInformation createAgentInformation(ServiceType serverType) {
        if (serverType == null) {
            throw new NullPointerException("serverType must not be null");
        }
        final String machineName = NetworkUtils.getHostName();
        final String agentId = getId("pinpoint.agentId", machineName, PinpointConstants.AGENT_NAME_MAX_LEN);
        final String applicationName = getId("pinpoint.applicationName", "UnknownApplicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN);
        final long startTime = RuntimeMXBeanUtils.getVmStartTime();
        final int pid = RuntimeMXBeanUtils.getPid();
        return new AgentInformation(agentId, applicationName, startTime, pid, machineName, serverType.getCode(), Version.VERSION);
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
        String ports = "";
        for (Entry<Integer, String> entry : serverInfo.getConnectors().entrySet()) {
            ports += " " + entry.getKey();
        }

        final TAgentInfo agentInfo = new TAgentInfo();
        agentInfo.setIp(ip);
        agentInfo.setHostname(this.agentInformation.getMachineName());
        agentInfo.setPorts(ports);
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


    private TraceContext createTraceContext() {
        final int jdbcSqlCacheSize = profilerConfig.getJdbcSqlCacheSize();
        final DefaultTraceContext traceContext = new DefaultTraceContext(jdbcSqlCacheSize);
        traceContext.setAgentInformation(this.agentInformation);
        traceContext.setPriorityDataSender(this.tcpDataSender);

        final Sampler sampler = createSampler();
        logger.info("SamplerType:{}", sampler.getClass());

        traceContext.setSampler(sampler);
        traceContext.setProfilerConfig(profilerConfig);


        final StorageFactory storageFactory = createStorageFactory();
        logger.info("StorageFactoryType:{}", storageFactory.getClass());
        traceContext.setStorageFactory(storageFactory);
        return traceContext;
    }

    private StorageFactory createStorageFactory() {
        if (profilerConfig.isSamplingElapsedTimeBaseEnable()) {
            return new TimeBaseStorageFactory(this.spanDataSender, this.profilerConfig, this.agentInformation);
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

    private EnhancedDataSender createTcpDataSender() {
        return new TcpDataSender(this.profilerConfig.getCollectorServerIp(), this.profilerConfig.getCollectorTcpServerPort());
    }

    private DataSender createUdpDataSender(int port, String threadName, int writeQueueSize) {
        return new UdpDataSender(this.profilerConfig.getCollectorServerIp(), port, threadName, writeQueueSize);
    }

    private String getId(String key, String defaultValue, int maxlen) {
        String value = System.getProperty(key, defaultValue);
        validateId(value, key, maxlen);
        return value;
    }

    private void validateId(String id, String idName, int maxlen) {
        // 에러 체크 로직을 bootclass 앞단으로 이동시킴.
        byte[] bytes = BytesUtils.toBytes(id);
        if (bytes.length > maxlen) {
            logger.warn("{} is too long(1~24). value={}", idName, id);
        }
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

        changeStatus(AgentStatus.STOPPED);

    }

}
