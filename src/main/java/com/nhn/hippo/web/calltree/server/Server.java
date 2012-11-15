package com.nhn.hippo.web.calltree.server;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author netspider
 */
public class Server implements Comparable<Server> {
    private int sequence;
    private final String id;
    private final Set<String> agentIds = new HashSet<String>();
    private final String applicationName;
    private final String endPoint;
    private final boolean terminal;
    private int recursiveCallCount;

	public Server(String agentId, String applicationName, String endPoint, boolean terminal, int recursiveCallCount) {
		// this.id = agentId + ":" + endPoint;
		// this.id = endPoint;
		this.id = applicationName;
		this.agentIds.add(agentId);
		this.applicationName = applicationName;
		this.endPoint = endPoint;
		this.terminal = terminal;
		this.recursiveCallCount = recursiveCallCount;
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

    public Set<String> getAgentIds() {
        return agentIds;
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

	public int getRecursiveCallCount() {
		return recursiveCallCount;
	}
    
	public void mergeWith(Server server) {
		this.recursiveCallCount += server.recursiveCallCount;
		this.agentIds.addAll(server.getAgentIds());
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
