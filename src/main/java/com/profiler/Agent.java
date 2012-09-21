package com.profiler;

import java.util.Map.Entry;
import java.util.logging.Logger;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.sender.DataSender;

public class Agent {

	public static final String FQCN = Agent.class.getName();

	private static final Logger logger = Logger.getLogger(Agent.class.getName());

	private volatile boolean alive = false;

	private final ServerInfo serverInfo;
	private final SystemMonitor systemMonitor;

	private Agent() {
		this.serverInfo = new ServerInfo();
		this.systemMonitor = new SystemMonitor();
	}

	private static class SingletonHolder {
		public static final Agent INSTANCE = new Agent();
	}

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

	/**
	 * Agent hash code.
	 * 
	 * @return
	 */
	public String getAgentId() {
		// TODO: agent id 생성 방법 변경이 필요함.
		logger.warning("Generating agent id is not implementd. use default 'TEST_AGENT_ID");

		return "TEST_AGENT_ID";
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
		agentInfo.setIsAlive(true);
		agentInfo.setTimestamp(System.currentTimeMillis());
		agentInfo.setAgentId(getAgentId());

		DataSender.getInstance().addDataToSend(agentInfo);
	}

	public void start() {
		logger.info("Starting HIPPO Agent.");
		systemMonitor.start();
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
		agentInfo.setIsAlive(false);
		agentInfo.setTimestamp(System.currentTimeMillis());
		agentInfo.setAgentId(getAgentId());

		DataSender.getInstance().addDataToSend(agentInfo);
	}

	public static void startAgent() {
		Agent.getInstance().start();
	}

	public static void stopAgent() throws Exception {
		Agent.getInstance().stop();
	}
}
