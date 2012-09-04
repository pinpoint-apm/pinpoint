package com.profiler.context;

public class EndPoint {

	private final String ip;
	private final int port;

	public EndPoint(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String toString() {
		return "EndPoint{ip=" + ip + ", port=" + port + "}";
	}

	public com.profiler.context.gen.Endpoint toThrift() {
		return new com.profiler.context.gen.Endpoint(ip, (short) port);
	}
}
