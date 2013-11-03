package com.nhn.pinpoint.web.calltree.server;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.SpanBo;
import com.nhn.pinpoint.common.bo.SpanEventBo;

/**
 * @author netspider
 */
@Deprecated
public class Server implements Comparable<Server> {
	protected int sequence;
	protected final String id;
	protected final Set<String> hosts = new HashSet<String>();
	protected final String applicationName;
	protected final ServiceType serviceType;

	public Server(SpanEventBo spanEventBo, NodeSelector nodeSelector) {
		if (spanEventBo.getServiceType().isTerminal()) {
			this.hosts.add(spanEventBo.getAgentId());
		} else {
			this.hosts.add(spanEventBo.getEndPoint());
		}

        this.id = nodeSelector.getServerId(spanEventBo);
        this.applicationName = spanEventBo.getDestinationId();
		if (spanEventBo.getServiceType().isRpcClient()) {
			this.serviceType = ServiceType.UNKNOWN_CLOUD;
		} else {
			this.serviceType = spanEventBo.getServiceType();
		}
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
	
	public String getApplicationName() {
		return applicationName;
	}

	public void mergeWith(Server server) {
		this.hosts.addAll(server.getHosts());
	}

	public ServiceType getServiceType() {
		return serviceType;
	}
	
	public String getJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{ ");
		sb.append("\"sequence\" : ").append(sequence).append(",");
		sb.append("\"applicationName\" : \"").append(applicationName).append("\",");
		sb.append("\"serviceType\" : \"").append(serviceType).append("\",");
		sb.append("\"agents\" : [ ");
		Iterator<String> iterator = hosts.iterator();
		while (iterator.hasNext()) {
			sb.append("\"").append(iterator.next()).append("\"");
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(" ]");
		sb.append(" }");

		return sb.toString();
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
