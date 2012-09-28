package com.nhn.hippo.web.calltree;

/**
 * 
 * 
 * @author netspider
 * 
 */
public class Server implements Comparable<Server> {
	private int sequence;
	private final String id;
	private final String agentId;

	public Server(String agentId, String endPoint) {
		this.id = agentId + ":" + endPoint;
		this.agentId = agentId;
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

	@Override
	public int compareTo(Server server) {
		return id.compareTo(server.id);
	}

	@Override
	public String toString() {
		return id;
	}
}
