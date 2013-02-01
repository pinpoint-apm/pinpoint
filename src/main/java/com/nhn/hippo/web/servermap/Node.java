package com.nhn.hippo.web.servermap;

import java.util.HashSet;
import java.util.Set;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SubSpanBo;

/**
 * 
 * @author netspider
 * 
 */
public class Node implements Comparable<Node> {

	private int sequence;
	private final String id;
	private final Set<String> agentIds = new HashSet<String>();
	private final String applicationName;
	private final String endPoint;
	private final ServiceType serviceType;

	private int recursiveCallCount;

	public Node(SubSpanBo span) {
		if (span.getServiceType().isTerminal()) {
			this.agentIds.add(span.getAgentId());
		} else {
			this.agentIds.add(span.getEndPoint());
		}

		if (span.getServiceType().isRpcClient()) {
			// this is unknown cloud, there is not exists the child span.
			this.id = span.getEndPoint();
			this.applicationName = span.getEndPoint();
			this.serviceType = ServiceType.UNKNOWN_CLOUD;
		} else {
			this.id = span.getServiceName();
			this.applicationName = span.getServiceName();
			this.serviceType = span.getServiceType();
		}

		this.endPoint = span.getEndPoint();
		this.recursiveCallCount = 0;
	}

	public Node(SpanBo span) {
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

	/**
	 * makes node from terminal statistics
	 * 
	 * @param id
	 * @param applicationName
	 * @param endPoint
	 * @param serviceType
	 */
	public Node(String id, String applicationName, String endPoint, ServiceType serviceType) {
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

	public void mergeWith(Node node) {
		this.recursiveCallCount += node.recursiveCallCount;
		this.agentIds.addAll(node.getAgentIds());
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public int compareTo(Node node) {
		return id.compareTo(node.id);
	}

	@Override
	public String toString() {
		return id;
	}
}
