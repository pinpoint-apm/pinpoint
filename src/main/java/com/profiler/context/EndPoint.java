package com.profiler.context;

@Deprecated
public class EndPoint {

	private final String protocol;
	private final String ip;
	private final int port;

	public EndPoint(String protocol, String ip, int port) {
		this.protocol = protocol;
		this.ip = ip;
		this.port = port;
	}
	
	public String toString() {
		return protocol + "://" + ip + ":" + port;
	}

	public com.profiler.common.dto.thrift.Endpoint toThrift() {
		return new com.profiler.common.dto.thrift.Endpoint(protocol, ip, (short) port);
	}
}
