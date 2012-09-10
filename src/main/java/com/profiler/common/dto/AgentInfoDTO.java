package com.profiler.common.dto;

import java.io.Serializable;
import java.util.Map.Entry;

import com.profiler.Agent;
import com.profiler.config.TomcatProfilerConfig;

public class AgentInfoDTO implements Serializable {
	private static final long serialVersionUID = 1465266151876398515L;

	public AgentInfoDTO() {
		// TODO: 코드 정리가 필요하다.
		// 새로 만든 Agent클래스가 제대로 동작하는지 알아보기 위해.
		// 임시로 이렇게 조치함.
		Agent agent = Agent.getInstance();
		hostIP = agent.getServerInfo().getHostip();
		portNumbers = "";
		for (Entry<Integer, String> entry : agent.getServerInfo().getConnectors().entrySet()) {
			portNumbers += " " + entry.getKey();
		}
		portNumbers = portNumbers.trim();
		hostHashCode = (hostIP + portNumbers).hashCode();

		agentTCPPortNumber = TomcatProfilerConfig.AGENT_TCP_LISTEN_PORT;
		timestamp = System.currentTimeMillis();
	}

	private String hostIP, portNumbers;
	private int hostHashCode, agentTCPPortNumber;
	private boolean isAlive = true;
	private long timestamp;

	public void setIsDead() {
		isAlive = false;
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
