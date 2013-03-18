package com.nhn.hippo.web.calltree.server;

import java.util.HashSet;
import java.util.Set;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.SpanBo;
import com.profiler.common.bo.SpanEventBo;

/**
 * @author netspider
 */
public class Server implements Comparable<Server> {
	protected int sequence;
	protected final String id;
	protected final Set<String> hosts = new HashSet<String>();
	protected final String applicationName;
//	protected final String endPoint;
	protected final ServiceType serviceType;

	protected int recursiveCallCount;

	public Server(SpanEventBo spanEventBo, NodeSelector nodeSelector) {
		if (spanEventBo.getServiceType().isTerminal()) {
			this.hosts.add(spanEventBo.getAgentId());
		} else {
			this.hosts.add(spanEventBo.getEndPoint());
		}

		if (spanEventBo.getServiceType().isRpcClient()) {
			// this is unknown cloud, there is not exists the child spanEvent.
			this.id = spanEventBo.getEndPoint();
			this.applicationName = spanEventBo.getEndPoint();
			this.serviceType = ServiceType.UNKNOWN_CLOUD;
		} else {
			this.id = nodeSelector.getServerId(spanEventBo);
			this.applicationName = spanEventBo.getDestinationId();
			this.serviceType = spanEventBo.getServiceType();
		}

		this.recursiveCallCount = 0;
	}

	public Server(SpanBo span, NodeSelector nodeSelector) {

		this.id = nodeSelector.getServerId(span);

		if (span.getServiceType().isTerminal()) {
			// TODO 이 함수는 terminal span이 들어올리가 없음.
			this.hosts.add(span.getAgentId());
		} else {
			
			// this.hosts.add(span.getEndPoint());
		}

		this.applicationName = span.getApplicationId();
		this.recursiveCallCount = span.getRecursiveCallCount();
		this.serviceType = span.getServiceType();
	}

	/**
	 * makes server from terminal statistics
	 * 
	 * @param id
	 * @param applicationName
	 * @param endPoint
	 * @param serviceType
	 */
	public Server(String id, String applicationName, Set<String> hosts, /* String endPoint,*/ ServiceType serviceType) {
		this.id = id;
		this.applicationName = applicationName;
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
//		this.endPoint = endPoint;
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

	public Set<String> getHosts() {
		return hosts;
	}

	public void setHosts(Set<String> hosts) {
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
	}
	
//	public String getEndPoint() {
//		return endPoint;
//	}

	public String getApplicationName() {
		return applicationName;
	}

	public int getRecursiveCallCount() {
		return recursiveCallCount;
	}

	public void mergeWith(Server server) {
		this.recursiveCallCount += server.recursiveCallCount;
		this.hosts.addAll(server.getHosts());
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
