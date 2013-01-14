package com.profiler;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.common.util.SpanUtils;
import com.profiler.config.ProfilerConfig;
import com.profiler.context.BypassStorageFactory;
import com.profiler.context.TimeBaseStorageFactory;
import com.profiler.context.TraceContext;
import com.profiler.sender.DataSender;
import com.profiler.sender.UdpDataSender;
import com.profiler.util.Assert;
import com.profiler.util.NetworkUtils;

public class Agent {

    private static final Logger logger = Logger.getLogger(Agent.class.getName());

    private volatile boolean alive = false;

    private final ProfilerConfig profilerConfig;
    private final ServerInfo serverInfo;
    private final SystemMonitor systemMonitor;

    private TraceContext traceContext;
    private DataSender dataSender;

    private final String agentId;
    private final String nodeName;
    private final String applicationName;
    private final long startTime;

    public Agent(ProfilerConfig profilerConfig) {
        Assert.notNull(profilerConfig, "profilerConfig must not be null");

        this.profilerConfig = profilerConfig;
        this.serverInfo = new ServerInfo();

//        this.agentId = getId("hippo.agentId", "UnkonwnAgentId");
//        일단 임시로 호환성을 위해 agentid에 머신name을넣도록 하자
        String machineName = NetworkUtils.getMachineName();
        this.agentId = getId("hippo.agentId", machineName);
        // TODO node name의 string limit 제한을 해결해야 된다.
        this.nodeName = getId("hippo.nodeName", machineName);
        this.applicationName = getId("hippo.applicationName", "UnknownApplicationName");

        this.dataSender = createDataSender();
        this.startTime = System.currentTimeMillis();

        initializeTraceContext();

        this.systemMonitor = new SystemMonitor(this.traceContext, this.profilerConfig);
        this.systemMonitor.setDataSender(dataSender);

        // 매핑 테이블 초기화를 위해 엑세스
        ApiMappingTable.findApiId("test", null, null);

        SingletonHolder.INSTANCE = this;
    }

    private void initializeTraceContext() {

        this.traceContext = TraceContext.getTraceContext();
//        this.traceContext.setDataSender(this.dataSender);

        this.traceContext.setAgentId(this.agentId);
        this.traceContext.setApplicationId(this.applicationName);

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

    private String getId(String key, String defaultValue) {
        String value = System.getProperty(key, defaultValue);
        validateId(value, key);
        return value;
    }

    private void validateId(String id, String idName) {
        try {
            byte[] bytes = id.getBytes("UTF-8");
            if (bytes.length > SpanUtils.AGENT_NAME_LIMIT) {
                logger.warning(idName + " is too long(1~24). value=" + id);
            }
            // validate = false;
            // TODO 이제 그냥 exception을 던지면 됨 agent 생성 타이밍이 최초 vm스타트와 동일하다.
        } catch (UnsupportedEncodingException e) {
            logger.log(Level.WARNING, "invalid agentId. Cause:" + e.getMessage(), e);
        }

    }

    private static class SingletonHolder {
        public static Agent INSTANCE;
    }

    @Deprecated
    public static Agent getInstance() {
        return SingletonHolder.INSTANCE;
    }

    public boolean isAlive() {
        return alive;
    }

    public void setIsAlive(boolean alive) {
        this.alive = alive;
    }

    public ServerInfo getServerInfo() {
        return this.serverInfo;
    }

    public String getAgentId() {
        return agentId;
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

    /**
     * HIPPO 서버로 WAS정보를 전송한다.
     */
    // TODO: life cycle을 체크하는 방법으로 바꿀까.. DEAD, STARTING, STARTED, STOPPING,
    // STOPPED
    public void sendStartupInfo() {
        logger.info("Send startup information to HIPPO server.");

        String ip = getServerInfo().getHostip();
        String ports = "";
        for (Entry<Integer, String> entry : getServerInfo().getConnectors().entrySet()) {
            ports += " " + entry.getKey();
        }

        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setHostname(ip);
        agentInfo.setPorts(ports);
        agentInfo.setAgentId(getAgentId());
        agentInfo.setApplicationName(getApplicationName());

        agentInfo.setIsAlive(true);
        agentInfo.setTimestamp(this.startTime);

        send3(agentInfo);
    }

    private void send3(AgentInfo agentInfo) {
        // 특정 collector가 죽더라도 나머지 collector가 받을수 있도록 일부러 중복해서 3번 보낸다.
        this.dataSender.send(agentInfo);
        this.dataSender.send(agentInfo);
        this.dataSender.send(agentInfo);
    }

    public void start() {
        logger.info("Starting HIPPO Agent.");
    }

    public void stop() {
        logger.info("Stopping HIPPO Agent.");
        systemMonitor.stop();

        String ip = getServerInfo().getHostip();
        String ports = "";
        for (Entry<Integer, String> entry : getServerInfo().getConnectors().entrySet()) {
            ports += " " + entry.getKey();
        }

        AgentInfo agentInfo = new AgentInfo();

        agentInfo.setHostname(ip);
        agentInfo.setPorts(ports);
        agentInfo.setAgentId(getAgentId());
        agentInfo.setApplicationName(getApplicationName());

        agentInfo.setIsAlive(false);
        agentInfo.setTimestamp(this.startTime);

        send3(agentInfo);
        // 종료 처리 필요.
        this.dataSender.stop();
    }

}
