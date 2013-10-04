package com.nhn.pinpoint.profiler;

import com.nhn.pinpoint.ProductInfo;
import com.nhn.pinpoint.common.PinpointConstants;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.util.BytesUtils;
import com.nhn.pinpoint.profiler.logging.PLogger;
import com.nhn.pinpoint.profiler.logging.PLoggerBinder;
import com.nhn.pinpoint.profiler.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.util.RuntimeMXBeanUtils;
import com.nhn.pinpoint.thrift.dto.AgentInfo;
import com.nhn.pinpoint.profiler.config.ProfilerConfig;
import com.nhn.pinpoint.profiler.context.BypassStorageFactory;
import com.nhn.pinpoint.profiler.context.DefaultTraceContext;
import com.nhn.pinpoint.profiler.context.TimeBaseStorageFactory;
import com.nhn.pinpoint.profiler.context.TraceContext;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.monitor.AgentStatMonitor;
import com.nhn.pinpoint.profiler.sampler.Sampler;
import com.nhn.pinpoint.profiler.sampler.SamplerFactory;
import com.nhn.pinpoint.profiler.sender.DataSender;
import com.nhn.pinpoint.profiler.sender.TcpDataSender;
import com.nhn.pinpoint.profiler.sender.UdpDataSender;
import com.nhn.pinpoint.profiler.util.NetworkUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.instrument.Instrumentation;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;

public class DefaultAgent implements Agent {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final PLoggerBinder binder;

    private final ByteCodeInstrumentor byteCodeInstrumentor;

    private final ProfilerConfig profilerConfig;

    private final ServerInfo serverInfo;
    private final AgentStatMonitor agentStatMonitor;

    private final TraceContext traceContext;

    private final DataSender tcpDataSender;
    private final DataSender statDataSender;
    private final DataSender spanDataSender;

    private final AgentInformation agentInformation;

    // agent info는 heartbeat에서 매번 사용한다.
    private AgentInfo agentInfo;

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

        String[] paths = getTomcatlibPath();
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(paths, this);

        ClassFileTransformerDispatcher classFileTransformerDispatcher = new ClassFileTransformerDispatcher(this);
        instrumentation.addTransformer(classFileTransformerDispatcher);

        // TODO 일단 임시로 호환성을 위해 agentid에 machinename을 넣도록 하자
        // TODO 박스 하나에 서버 인스턴스를 여러개 실행할 때에 문제가 될 수 있음.
        this.agentInformation = createAgentInformation();
        logger.info("agentInformation:{}", agentInformation);

        this.tcpDataSender = createTcpDataSender();
        this.spanDataSender = createUdpDataSender(this.profilerConfig.getCollectorUdpSpanServerPort(), "Pinpoint-UdpSpanDataExecutor");
        this.statDataSender = createUdpDataSender(this.profilerConfig.getCollectorUdpServerPort(), "Pinpoint-UdpStatDataExecutor");

        this.traceContext = createTraceContext();

        // 매핑 테이블 초기화를 위해 엑세스
//        ApiMappingTable.findApiId("test", null, null);

        this.agentInfo = createAgentInfo();
        this.heartBitChecker = new HeartBitChecker(tcpDataSender, profilerConfig.getHeartbeatInterval(), agentInfo);

        // JVM 통계 등을 주기적으로 수집하여 collector에 전송하는 monitor를 초기화한다.
        this.agentStatMonitor = new AgentStatMonitor(this.statDataSender, this.agentInformation.getAgentId());

        preloadClass();
        SingletonHolder.INSTANCE = this;
    }

    private void preloadClass() {
        //To change body of created methods use File | Settings | File Templates.
    }

    private AgentInformation createAgentInformation() {
        final String machineName = NetworkUtils.getHostName();
        final String agentId = getId("pinpoint.agentId", machineName, PinpointConstants.AGENT_NAME_MAX_LEN);
        final String applicationName = getId("pinpoint.applicationName", "UnknownApplicationName", PinpointConstants.APPLICATION_NAME_MAX_LEN);
        final long startTime = RuntimeMXBeanUtils.getVmStartTime();
        final int pid = RuntimeMXBeanUtils.getPid();
        return new AgentInformation(agentId, applicationName, startTime, pid, machineName);
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

    public ByteCodeInstrumentor getByteCodeInstrumentor() {
        return byteCodeInstrumentor;
    }

    private String[] getTomcatlibPath() {
        String catalinaHome = System.getProperty("catalina.home");

        if (catalinaHome == null) {
            logger.info("CATALINA_HOME is null");
            return new String[0];
        }

        if (logger.isInfoEnabled()) {
            logger.info("CATALINA_HOME={}", catalinaHome);
        }

        File f1 = new File(catalinaHome + "/server/lib/catalina.jar");
        File f2 = new File(catalinaHome + "/common/lib/servlet-api.jar");
        
        // TODO 이 방법이 최선인가?? 모르겠음.
		if (f1.exists() && f2.exists() && f1.isFile() && f2.isFile()) {
			// BLOC
			return new String[] { catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar" };
		} else {
			if (profilerConfig.getServiceType() == ServiceType.BLOC) {
				// BLOC
				return new String[] { catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar" };
			} else {
				// TOMCAT
				return new String[] { catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar" };
			}
		}
    }

    private AgentInfo createAgentInfo() {
        final ServerInfo serverInfo = this.serverInfo;
        String ip = serverInfo.getHostip();
        String ports = "";
        for (Entry<Integer, String> entry : serverInfo.getConnectors().entrySet()) {
            ports += " " + entry.getKey();
        }

        final AgentInfo agentInfo = new AgentInfo();
        agentInfo.setIp(ip);
        agentInfo.setHostname(this.agentInformation.getMachineName());
        agentInfo.setPorts(ports);
        agentInfo.setAgentId(agentInformation.getAgentId());
        agentInfo.setApplicationName(agentInformation.getApplicationName());
        agentInfo.setPid(agentInformation.getPid());
        agentInfo.setTimestamp(agentInformation.getStartTime());
		agentInfo.setServiceType(profilerConfig.getServiceType().getCode());

        agentInfo.setIsAlive(true);

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
        DefaultTraceContext traceContext = new DefaultTraceContext();
        traceContext.setAgentInformation(this.agentInformation);
        traceContext.setPriorityDataSender(this.tcpDataSender);

        Sampler sampler = createSampler();
        logger.info("SamplerType:{}", sampler.getClass());

        traceContext.setSampler(sampler);

        if (profilerConfig.isSamplingElapsedTimeBaseEnable()) {
            TimeBaseStorageFactory timeBaseStorageFactory = new TimeBaseStorageFactory(this.spanDataSender, this.profilerConfig);
            traceContext.setStorageFactory(timeBaseStorageFactory);
        } else {
            traceContext.setStorageFactory(new BypassStorageFactory(spanDataSender));
        }
        return traceContext;
    }

    private Sampler createSampler() {

        boolean samplingEnable = this.profilerConfig.isSamplingEnable();
        int samplingRate = this.profilerConfig.getSamplingRate();

        SamplerFactory samplerFactory = new SamplerFactory();
        return samplerFactory.createSampler(samplingEnable, samplingRate);
    }

    private DataSender createTcpDataSender() {
        return new TcpDataSender(this.profilerConfig.getCollectorServerIp(), this.profilerConfig.getCollectorTcpServerPort());
    }

    private DataSender createUdpDataSender(int port, String threadName) {
        return new UdpDataSender(this.profilerConfig.getCollectorServerIp(), port, threadName);
    }

    private String getId(String key, String defaultValue, int maxlen) {
        String value = System.getProperty(key, defaultValue);
        validateId(value, key, maxlen);
        return value;
    }

    private void validateId(String id, String idName, int maxlen) {
        // 에러 체크 로직을 bootclass 앞단으로 이동시킴.
        byte[] bytes = BytesUtils.getBytes(id);
        if (bytes.length > maxlen) {
            logger.warn("{} is too long(1~24). value={}", idName, id);
        }
    }


    private static class SingletonHolder {
        public static DefaultAgent INSTANCE;
    }


    @Deprecated
    public static DefaultAgent getInstance() {
        return SingletonHolder.INSTANCE;
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

        agentInfo.setIsAlive(false);
        this.tcpDataSender.send(agentInfo);
        // TODO send agentInfo alive false후 send 메시지의 처리가 정확하지 않음

        changeStatus(AgentStatus.STOPPING);
        this.heartBitChecker.stop();
        this.agentStatMonitor.stop();

        // 종료 처리 필요.
        this.spanDataSender.stop();
        this.statDataSender.stop();
        this.tcpDataSender.stop();

        changeStatus(AgentStatus.STOPPED);

    }

}
