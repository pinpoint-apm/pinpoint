package com.profiler;

import com.profiler.common.ServiceType;
import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.config.ProfilerConfig;
import com.profiler.context.BypassStorageFactory;
import com.profiler.context.DefaultTraceContext;
import com.profiler.context.TimeBaseStorageFactory;
import com.profiler.context.TraceContext;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.JavaAssistByteCodeInstrumentor;
import com.profiler.logger.Slf4jLoggerBinder;
import com.profiler.logging.Logger;
import com.profiler.logging.LoggerBinder;
import com.profiler.logging.LoggerFactory;
import com.profiler.sender.DataSender;
import com.profiler.sender.UdpDataSender;
import com.profiler.util.NetworkUtils;

import java.io.UnsupportedEncodingException;
import java.lang.instrument.Instrumentation;
import java.util.Map.Entry;
import java.util.Random;

public class DefaultAgent implements Agent {

    private Logger logger;
    private static final Random IDENTIFIER_KEY = new Random();


    private final ByteCodeInstrumentor byteCodeInstrumentor;

    private final ProfilerConfig profilerConfig;

    private final ServerInfo serverInfo;
    private final SystemMonitor systemMonitor;

    private DefaultTraceContext traceContext;

    private DataSender priorityDataSender;
    private DataSender dataSender;

    private final String machineName;
    private final String agentId;
    private final String applicationName;
    private final long startTime;
    private final short identifier;

    // agent info는 heartbeat에서 매번 사용한다.
    private AgentInfo agentInfo;

    // agent의 상태,
    private volatile AgentStatus agentStatus;
    private HeartBitChecker heartBitChecker;


    public DefaultAgent(String agentArgs, Instrumentation instrumentation, ProfilerConfig profilerConfig) {
        if (profilerConfig == null) {
            throw new NullPointerException("profilerConfig must not be null");
        }

        initializeLogger();
        changeStatus(AgentStatus.INITIALIZING);

        this.profilerConfig = profilerConfig;
        this.serverInfo = new ServerInfo();

        String[] paths = getTomcatlibPath();
        this.byteCodeInstrumentor = new JavaAssistByteCodeInstrumentor(paths, this);

        ClassFileTransformerDispatcher classFileTransformerDispatcher = new ClassFileTransformerDispatcher(this);
        instrumentation.addTransformer(classFileTransformerDispatcher);

        // TODO 일단 임시로 호환성을 위해 agentid에 machinename을 넣도록 하자
        // TODO 박스 하나에 서버 인스턴스를 여러개 실행할 때에 문제가 될 수 있음.
        this.machineName = NetworkUtils.getMachineName();
        this.agentId = getId("hippo.agentId", machineName, HBaseTables.AGENT_NAME_MAX_LEN);
        this.applicationName = getId("hippo.applicationName", "UnknownApplicationName", HBaseTables.APPLICATION_NAME_MAX_LEN);

        this.priorityDataSender = createDataSender();
        this.dataSender = createDataSender();
        this.startTime = System.currentTimeMillis();

        this.identifier = getShortIdentifier();

        initializeTraceContext();

        this.systemMonitor = new SystemMonitor(this.traceContext, this.profilerConfig);
        this.systemMonitor.setDataSender(dataSender);

        // 매핑 테이블 초기화를 위해 엑세스
        ApiMappingTable.findApiId("test", null, null);

        this.agentInfo = createAgentInfo();
        this.heartBitChecker = new HeartBitChecker(priorityDataSender, profilerConfig.getHeartbeatInterval(), agentInfo);


        SingletonHolder.INSTANCE = this;
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

        if (profilerConfig.getServiceType() == ServiceType.BLOC) {
            return new String[] { catalinaHome + "/server/lib/catalina.jar", catalinaHome + "/common/lib/servlet-api.jar" };
        } else {
            return new String[] { catalinaHome + "/lib/servlet-api.jar", catalinaHome + "/lib/catalina.jar" };
        }
    }


    private AgentInfo createAgentInfo() {
        String ip = getServerInfo().getHostip();
        String ports = "";
        for (Entry<Integer, String> entry : getServerInfo().getConnectors().entrySet()) {
            ports += " " + entry.getKey();
        }

        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setIp(ip);
        agentInfo.setHostname(this.machineName);
        agentInfo.setPorts(ports);

        agentInfo.setAgentId(getAgentId());
        agentInfo.setIdentifier(this.identifier);
        agentInfo.setApplicationName(getApplicationName());
		agentInfo.setServiceType(profilerConfig.getServiceType().getCode());

        agentInfo.setIsAlive(true);
        agentInfo.setTimestamp(this.startTime);

        return agentInfo;
    }

    private void changeStatus(AgentStatus status) {
        this.agentStatus = status;
        if (logger.isDebugEnabled()) {
            logger.debug("Agent status is changed. {}", status);
        }
    }

    public LoggerBinder initializeLogger() {
        Slf4jLoggerBinder binder = new Slf4jLoggerBinder();
        logger = binder.getLogger(DefaultAgent.class.getName());
        com.profiler.logging.Logger logger = binder.getLogger(Slf4jLoggerBinder.class.getName());
        logger.info("slf4jLoggerBinder initialized");

        // static LoggerFactory에 binder를 붙임.
        LoggerFactory.initialize(binder);


        return binder;
    }

    private short getShortIdentifier() {
        return (short) (IDENTIFIER_KEY.nextInt(65536) - 32768);
    }

    private void initializeTraceContext() {
        this.traceContext = new DefaultTraceContext();

        this.traceContext.setAgentId(this.agentId);
        this.traceContext.setApplicationId(this.applicationName);
        this.traceContext.setPriorityDataSender(this.priorityDataSender);

        if (profilerConfig.isSamplingElapsedTimeBaseEnable()) {
            TimeBaseStorageFactory timeBaseStorageFactory = new TimeBaseStorageFactory(this.dataSender, this.profilerConfig);
            this.traceContext.setStorageFactory(timeBaseStorageFactory);
        } else {
            this.traceContext.setStorageFactory(new BypassStorageFactory(dataSender));
        }
    }

    private UdpDataSender createDataSender() {
        return new UdpDataSender(this.profilerConfig.getCollectorServerIp(), this.profilerConfig.getCollectorServerPort());
    }

    private String getId(String key, String defaultValue, int maxlen) {
        String value = System.getProperty(key, defaultValue);
        validateId(value, key, maxlen);
        return value;
    }

    private void validateId(String id, String idName, int maxlen) {
        try {
            byte[] bytes = id.getBytes("UTF-8");
            if (bytes.length > maxlen) {
                logger.warn("{} is too long(1~24). value={}", idName, id);
            }
            // validate = false;
            // TODO 이제 그냥 exception을 던지면 됨 agent 생성 타이밍이 최초 vm스타트와 동일하다.
        } catch (UnsupportedEncodingException e) {
            logger.warn("invalid agentId. Cause:" + e.getMessage(), e);
        }
    }

    private static class SingletonHolder {
        public static DefaultAgent INSTANCE;
    }

    public static DefaultAgent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public void addConnector(String protocol, int port){
        this.getServerInfo().addConnector(protocol, port);
    }


    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    public String getAgentId() {
        return agentId;
    }

    public short getIdentifier() {
        return identifier;
    }

    public long getStartTime() {
        return startTime;
    }

    public String getApplicationName() {
        return applicationName;
    }

    public TraceContext getTraceContext() {
        return traceContext;
    }


    public boolean isRunning() {
        return agentStatus == AgentStatus.RUNNING;
    }

    // TODO 필요없을것 같음 started를 start로 바꿔도 될 듯...
    @Override
    public void start() {
        logger.info("Starting HIPPO Agent.");
    }

    /**
     * org/apache/catalina/startup/Catalina/await함수가 호출되기 전에 실행된다.
     * Tomcat이 구동되고 context가 모두 로드 된 다음 사용자의 요청을 처리할 수 있게 되었을 때 실행됨.
     */
    public void started() {
        changeStatus(AgentStatus.RUNNING);
        this.heartBitChecker.start();
    }

    @Override
    public void stop() {
        logger.info("Stopping HIPPO Agent.");

        changeStatus(AgentStatus.STOPPING);
        this.heartBitChecker.close();


        systemMonitor.stop();

        agentInfo.setIsAlive(false);

        // TODO 개선필요. 특정 collector가 죽더라도 나머지 collector가 받을수 있도록 일부러 중복해서 3번 보낸다.
        this.priorityDataSender.send(agentInfo);
        this.priorityDataSender.send(agentInfo);
        this.priorityDataSender.send(agentInfo);

        // 종료 처리 필요.
        this.dataSender.stop();
        this.priorityDataSender.stop();

        changeStatus(AgentStatus.STOPPED);
    }
}
