package com.profiler.data.vo;

public class AgentVO {
	String hostIP;
	String portNumber;
	int agentTCPPortNumber;
	public AgentVO(String hostIP,String hostPortNumber,int agentTCPPortNumber) {
		this.hostIP=hostIP;
		this.portNumber=hostPortNumber;
		this.agentTCPPortNumber=agentTCPPortNumber;
	}
	
	@Override
	public String toString() {
		return "AgentVO [hostIP=" + hostIP + ", portNumber=" + portNumber + ", agentTCPPortNumber="+agentTCPPortNumber+"]";
	}

	public String getHostIP() {
		return hostIP;
	}

	public void setHostIP(String hostIP) {
		this.hostIP = hostIP;
	}

	public String getPortNumber() {
		return portNumber;
	}

	public void setPortNumber(String portNumber) {
		this.portNumber = portNumber;
	}

	public int getAgentTCPPortNumber() {
		return agentTCPPortNumber;
	}

	public void setAgentTCPPortNumber(int agentTCPPortNumber) {
		this.agentTCPPortNumber = agentTCPPortNumber;
	}
	
}