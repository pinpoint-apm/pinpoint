package com.profiler;

import java.io.UnsupportedEncodingException;
import java.util.Map.Entry;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.common.hbase.HBaseTables;
import com.profiler.common.mapping.ApiMappingTable;
import com.profiler.config.ProfilerConfig;
import com.profiler.context.BypassStorageFactory;
import com.profiler.context.TimeBaseStorageFactory;
import com.profiler.context.TraceContext;
import com.profiler.sender.DataSender;
import com.profiler.sender.UdpDataSender;
import com.profiler.util.Assert;
import com.profiler.util.NetworkUtils;

public class Agent implements Runnable {

	private static final Logger logger = Logger.getLogger(Agent.class.getName());
	private static final Random IDENTIFIER_KEY = new Random();

	private final ProfilerConfig profilerConfig;
	private final ServerInfo serverInfo;
	private final SystemMonitor systemMonitor;

	private TraceContext traceContext;

	private DataSender priorityDataSender;
	private DataSender dataSender;

	private final String machineName;
	private final String agentId;
	private final String nodeName;
	private final String applicationName;
	private final long startTime;
	private final short identifier;

	// agent info는 heartbeat에서 매번 사용한다.
	private AgentInfo agentInfo;
	
	// agent의 상태, 
	private AgentStatus agentStatus;
	private Thread heartbeatThread;

	/**
	 * collector로 heartbeat을 보낸다. heartbeat의 내용은 agent info.
	 */
	@Override
	public void run() {
		try {
			logger.info("Send startup information to HIPPO server via " + this.priorityDataSender.getClass().getSimpleName() + ". agentInfo=" + agentInfo);
			this.priorityDataSender.send(agentInfo);
			this.priorityDataSender.send(agentInfo);
			this.priorityDataSender.send(agentInfo);

			logger.info("Starting agent heartbeat.");
			while (true) {
				if (agentStatus == AgentStatus.RUNNING) {
					logger.fine("Send heartbeat");
					this.priorityDataSender.send(agentInfo);
				} else if (agentStatus == AgentStatus.STOPPING || agentStatus == AgentStatus.STOPPED) {
					break;
				}
				// TODO 정밀한 시간계산 없이 일단 그냥 interval 단위로 보냄.
				Thread.sleep(profilerConfig.getHeartbeatInterval());
			}
		} catch (InterruptedException e) {
			logger.warning(e.getMessage());
		}
		logger.info(Thread.currentThread().getName() + " stopped.");
	}

	public Agent(ProfilerConfig profilerConfig) {
		changeStatus(AgentStatus.INITIALIZING);
		
		Assert.notNull(profilerConfig, "profilerConfig must not be null");

		this.profilerConfig = profilerConfig;
		this.serverInfo = new ServerInfo();

		// TODO 일단 임시로 호환성을 위해 agentid에 machinename을 넣도록 하자
		// TODO 박스 하나에 서버 인스턴스를 여러개 실행할 때에 문제가 될 수 있음.
		this.machineName = NetworkUtils.getMachineName();
		this.agentId = getId("hippo.agentId", machineName, HBaseTables.AGENT_NAME_MAX_LEN);
		this.nodeName = System.getProperty("hippo.nodeName", machineName);
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
		this.heartbeatThread = createHeartbeatThread();

		SingletonHolder.INSTANCE = this;
	}

	private Thread createHeartbeatThread() {
		Thread thread = new Thread(this);
		thread.setName("HIPPO-Agent-Heartbeat-Thread");
		thread.setDaemon(true);
		return thread;
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

		agentInfo.setIsAlive(true);
		agentInfo.setTimestamp(this.startTime);

		return agentInfo;
	}

	private void changeStatus(AgentStatus status) {
		this.agentStatus = status;
		logger.severe("Agent status is changed. " + status);
	}
	
	private short getShortIdentifier() {
		return (short) (IDENTIFIER_KEY.nextInt(65536) - 32768);
	}

	private void initializeTraceContext() {
		this.traceContext = TraceContext.getTraceContext();
		// this.traceContext.setDataSender(this.dataSender);

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

	public static Agent getInstance() {
		return SingletonHolder.INSTANCE;
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

	public String getNodeName() {
		return nodeName;
	}

	public boolean isRunning() {
		return agentStatus == AgentStatus.RUNNING;
	}

	// TODO 필요없을것 같음 started를 start로 바꿔도 될 듯...
	public void start() {
		logger.info("Starting HIPPO Agent.");
	}

	/**
	 * org/apache/catalina/startup/Catalina/await함수가 호출되기 전에 실행된다.
	 * Tomcat이 구동되고 context가 모두 로드 된 다음 사용자의 요청을 처리할 수 있게 되었을 때 실행됨.
	 */
	public void started() {
		changeStatus(AgentStatus.RUNNING);
		this.heartbeatThread.start();
	}

	public void stop() {
		logger.info("Stopping HIPPO Agent.");

		changeStatus(AgentStatus.STOPPING);
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
