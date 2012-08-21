package com.profiler.context;

public class EndPoint {

	public static final EndPoint NONE = null;

	private final String protocol;
	private final String ip;
	private final int port;
	private final String name;

	public EndPoint(String protocol, String ip, int port, String name) {
		this.protocol = protocol;
		this.ip = ip;
		this.port = port;
		this.name = name;
	}

	public String toString() {
		return "EndPoint{Protocol=" + protocol + ", IP=" + ip + ", Port=" + port + ", Name=" + name + "]";
	}
}
