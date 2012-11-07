package com.nhn.hippo.web.calltree.server;

/**
 * @author netspider
 */
public class Server implements Comparable<Server> {
    private int sequence;
    private final String id;
    private final String agentId;
    private final String applicationName;
    private final String endPoint;
    private final boolean terminal;

	public Server(String agentId, String applicationName, String endPoint, boolean terminal) {
		// this.id = agentId + ":" + endPoint;
		this.id = endPoint;
		this.agentId = agentId;
		this.applicationName = applicationName;
		this.endPoint = endPoint;
		this.terminal = terminal;
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

    public String getEndPoint() {
        return endPoint;
    }

    public boolean isTerminal() {
        return terminal;
    }

    public String getApplicationName() {
		return applicationName;
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
