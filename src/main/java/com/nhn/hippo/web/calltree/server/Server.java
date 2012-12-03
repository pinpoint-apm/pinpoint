package com.nhn.hippo.web.calltree.server;

import java.util.HashSet;
import java.util.Set;

import com.nhn.hippo.web.vo.TerminalRequest;
import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;

/**
 * @author netspider
 */
public class Server implements Comparable<Server> {
	private int sequence;
	private final String id;
	private final Set<String> agentIds = new HashSet<String>();
	private final String applicationName;
	private final String endPoint;
	private final ServiceType serviceType;

	private int recursiveCallCount;

	public Server(SpanBo span) {
		this.id = span.getServiceName();

		if (span.getServiceType().isTerminal()) {
			this.agentIds.add(span.getAgentId());
		} else {
			this.agentIds.add(span.getEndPoint());
		}

		this.applicationName = span.getServiceName();
		this.endPoint = span.getEndPoint();
		this.recursiveCallCount = span.getRecursiveCallCount();
		this.serviceType = span.getServiceType();
	}

	public Server(String id, String applicationName, String endPoint, ServiceType serviceType) {
		this.id = id;
		this.applicationName = applicationName;
		this.endPoint = endPoint;
		this.serviceType = serviceType;
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

	public ServiceType getServiceType() {
		return serviceType;
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
