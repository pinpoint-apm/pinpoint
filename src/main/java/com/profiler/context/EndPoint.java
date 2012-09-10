package com.profiler.context;

public class EndPoint {

	private final String ip;
	private final int port;

	public EndPoint(String ip, int port) {
		this.ip = ip;
		this.port = port;
	}

	public String toString() {
		return "{ip=" + ip + ", port=" + port + "}";
	}

	public com.profiler.common.dto.thrift.Endpoint toThrift() {
		return new com.profiler.common.dto.thrift.Endpoint(ip, (short) port);
	}
}
