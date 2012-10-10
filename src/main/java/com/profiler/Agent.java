package com.profiler;

import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Map.Entry;
import java.util.logging.Logger;

import com.profiler.common.dto.thrift.AgentInfo;
import com.profiler.context.TraceContext;
import com.profiler.sender.DataSender;

public class Agent {

	public static final String FQCN = Agent.class.getName();

	private static final Logger logger = Logger.getLogger(Agent.class.getName());

	private volatile boolean alive = false;

	private final ServerInfo serverInfo;
	private final SystemMonitor systemMonitor;

	private final String agentId;
	private final String applicationName;
	
	private Agent() {
		this.serverInfo = new ServerInfo();
		this.systemMonitor = new SystemMonitor();
		
		this.agentId = System.getProperty("hippo.agentId", getMachineName());
		this.applicationName = System.getProperty("hippo.applicationName", "TOMCAT");
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

	public String getAgentId() {
		return agentId;
	}

	public String getApplicationName() {
		return applicationName;
	}

	private String getMachineName() {
		try {
			String name = null;
			Enumeration<NetworkInterface> enet = NetworkInterface.getNetworkInterfaces();

			while (enet.hasMoreElements() && (name == null)) {
				NetworkInterface net = enet.nextElement();

				if (net.isLoopback())
					continue;

				Enumeration<InetAddress> eaddr = net.getInetAddresses();

				while (eaddr.hasMoreElements()) {
					InetAddress inet = eaddr.nextElement();

					if (inet.getCanonicalHostName().equalsIgnoreCase(inet.getHostAddress()) == false) {
						name = inet.getCanonicalHostName();
						break;
					}
				}
			}
			return name;
		} catch (Exception e) {
			logger.warning(e.getMessage());
			return "UNKNOWN-HOST";
		}
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
        // trace context 새롭게 생성.
        TraceContext.initialize();
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
