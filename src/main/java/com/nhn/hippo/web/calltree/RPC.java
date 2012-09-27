package com.nhn.hippo.web.calltree;

/**
 * 
 * @author netspider
 * 
 */
public class RPC implements Comparable<RPC> {
	private int sequence;
	private final String id;
	private final String agentId;
	private final String serviceName;
	private final String rpc;

	public RPC(String agentId, String serviceName, String rpc) {
		this.id = agentId + ":" + serviceName + ":" + rpc;
		this.agentId = agentId;
		this.serviceName = serviceName;
		this.rpc = rpc;
	}

	public String getId() {
		return this.id;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}

	public int getSequence() {
		return sequence;
	}

	public String getAgentId() {
		return agentId;
	}

	public String getServiceName() {
		return serviceName;
	}

	public String getRpc() {
		return rpc;
	}

	@Override
	public int compareTo(RPC rpc) {
		return id.compareTo(rpc.id);
	}

	@Override
	public String toString() {
		return id;
	}
}
