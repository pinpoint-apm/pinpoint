package com.nhn.pinpoint.web.applicationmap;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * application map에서 application을 나타낸다.
 * 
 * @author netspider
 */
public class Application implements Comparable<Application> {
	protected int sequence;
	protected final String id;
	protected final String applicationName;
	protected final ServiceType serviceType;
	protected final Map<String, Host> hostList;
	protected final Set<AgentInfoBo> agentSet;

	public Application(String id, String applicationName, ServiceType serviceType, Map<String, Host> hostList, Set<AgentInfoBo> agentSet) {
		this.id = id;
		if (serviceType == ServiceType.CLIENT) {
			this.applicationName = "CLIENT";
		} else {
			this.applicationName = applicationName;
		}

		this.hostList = (hostList == null) ? new HashMap<String, Host>() : hostList;
		this.agentSet = agentSet;
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

	public String getApplicationName() {
		return applicationName;
	}

	public Map<String, Host> getHostList() {
		if (hostList == null) {
			// CLIENT span의 경우 host가 없다.
			return Collections.emptyMap();
		}
		return hostList;
	}

	public void mergeWith(Application application) {
		for (Entry<String, Host> entry : application.getHostList().entrySet()) {
			Host host = hostList.get(entry.getKey());
			if (host != null) {
				host.mergeWith(entry.getValue());
			} else {
				hostList.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public ServiceType getServiceType() {
		return serviceType;
	}
	
	public Set<AgentInfoBo> getAgentSet() {
		return agentSet;
	}

	public String getJson() {
		StringBuilder sb = new StringBuilder();

		sb.append("{ ");
		sb.append("\"sequence\" : ").append(sequence).append(",");
		sb.append("\"applicationName\" : \"").append(applicationName).append("\",");
		sb.append("\"serviceType\" : \"").append(serviceType).append("\",");
		sb.append("\"serviceTypeCode\" : \"").append(serviceType.getCode()).append("\",");
		sb.append("\"agents\" : [ ");
		if (agentSet != null) {
			Iterator<AgentInfoBo> iterator = agentSet.iterator();
			while (iterator.hasNext()) {
				sb.append(iterator.next().getJson());
				if (iterator.hasNext()) {
					sb.append(",");
				}
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
		return "Application [sequence=" + sequence + ", id=" + id + ", applicationName=" + applicationName + ", serviceType=" + serviceType + ", hostList=" + hostList + "]";
	}
}
