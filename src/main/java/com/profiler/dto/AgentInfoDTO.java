package com.profiler.dto;

import java.io.Serializable;

import com.profiler.config.TomcatProfilerConfig;

public class AgentInfoDTO implements Serializable {
	private static final long serialVersionUID = 1465266151876398515L;

	public AgentInfoDTO() {
		hostHashCode = staticHostHashCode;
		hostIP = staticHostIP;
		portNumbers = staticPortNumber;
		agentTCPPortNumber = TomcatProfilerConfig.AGENT_TCP_LISTEN_PORT;
		timestamp = System.currentTimeMillis();
	}

	public transient static final StringBuffer portNumberBuffer = new StringBuffer();
	// static variable is only used in the agent.
	public static int staticHostHashCode;
	public static String staticHostIP, staticPortNumber;
	// instance variable is commonly used.
	private String hostIP, portNumbers;
	private int hostHashCode, agentTCPPortNumber;
	private boolean isAlive = true;
	private long timestamp;

	public void setIsDead() {
		isAlive = false;
	}

	public static String getPortNumberString() {
		staticPortNumber = portNumberBuffer.toString();
		staticPortNumber.trim();
		return staticPortNumber;
	}

	public String toString() {
		return hostHashCode + " " + hostIP + " " + portNumbers + " " + agentTCPPortNumber + " isAlive=" + isAlive;
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public int getHostHashCode() {
		return hostHashCode;
	}

	public void setHostHashCode(int hostHashCode) {
		this.hostHashCode = hostHashCode;
	}

	public String getPortNumbers() {
		return portNumbers;
	}

	public void setPortNumbers(String portNumber) {
		this.portNumbers = portNumber;
	}

	public int getAgentTCPPortNumber() {
		return agentTCPPortNumber;
	}

	public void setAgentTCPPortNumber(int agentTCPPortNumber) {
		this.agentTCPPortNumber = agentTCPPortNumber;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public boolean isAlive() {
		return isAlive;
	}
}
