package com.nhn.pinpoint.web.applicationmap;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import com.profiler.common.ServiceType;
import com.profiler.common.bo.AgentInfoBo;

/**
 * application map에서 application을 나타낸다.
 * 
 * @author netspider
 */
public class Application implements Comparable<Application> {
	protected int sequence;
	protected final String id;

	protected final Set<String> hosts = new HashSet<String>();
	// TODO 여기에서 agentinfobo를 사용하는게 옳은건가??
	protected final Set<AgentInfoBo> agents = new HashSet<AgentInfoBo>();
	protected final String applicationName;
	protected final ServiceType serviceType;

	public Application(String id, String applicationName, ServiceType serviceType, Set<String> hosts, Set<AgentInfoBo> agents) {
		this.id = id;
		if (serviceType == ServiceType.CLIENT) {
			this.applicationName = "CLIENT";
		} else {
			this.applicationName = applicationName;
		}
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
		if (agents != null) {
			this.agents.addAll(agents);
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
	
	public Set<AgentInfoBo> getAgents() {
		return agents;
	}

	public void setHosts(Set<String> hosts) {
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
	}

	public String getApplicationName() {
		return applicationName;
	}

	public void mergeWith(Application application) {
		this.hosts.addAll(application.getHosts());
		this.agents.addAll(application.getAgents());
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
		sb.append("\"serviceTypeCode\" : \"").append(serviceType.getCode()).append("\",");
		sb.append("\"agents\" : [ ");
		Iterator<AgentInfoBo> iterator = agents.iterator();
		while (iterator.hasNext()) {
			sb.append(iterator.next().getJson());
			if (iterator.hasNext()) {
				sb.append(",");
			}
		}
		sb.append(" ]");
		sb.append(" }");

		return sb.toString();
	}
	
	@Override
	public int compareTo(Application server) {
		return id.compareTo(server.id);
	}

	@Override
	public String toString() {
		return "Application [applicationName=" + applicationName + ", serviceType=" + serviceType + ", hosts=" + hosts + "]";
	}
}
