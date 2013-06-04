package com.nhn.pinpoint.profiler;

import java.net.InetAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServerInfo {

	private volatile String hostip;
	private final Map<Integer, String> connectors = new ConcurrentHashMap<Integer, String>();
	private volatile boolean isAlive;
	private volatile long uptime;
	private volatile String agentHashCode;
	
	public ServerInfo() {
		try {
			InetAddress thisIp = InetAddress.getLocalHost();
			hostip = thisIp.getHostAddress();
			uptime = System.currentTimeMillis();
		} catch (Exception e) {
			e.printStackTrace();
			hostip = "127.0.0.1";
		}
	}

	public void addConnector(String protocol, int port) {
		connectors.put(port, protocol);
	}

	@Override
	public String toString() {
		return String.format("agentHash=%s, ip=%s, connectors=%s, uptime=%s, isAlive=%s", agentHashCode, hostip, connectors, uptime, isAlive);
	}

	public String getHostip() {
		return hostip;
	}

	public Map<Integer, String> getConnectors() {
		return connectors;
	}

	public boolean isAlive() {
		return isAlive;
	}

	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

	public long getUptime() {
		return uptime;
	}
}
