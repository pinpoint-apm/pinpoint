package com.nhn.pinpoint.web.applicationmap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

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

	protected final Map<String, SortedMap<String, ServerInstance>> serverInstanceList = new TreeMap<String, SortedMap<String, ServerInstance>>();

	protected final Map<String, List<AgentInfoBo>> agentList = new TreeMap<String, List<AgentInfoBo>>();

	public Application(String id, String applicationName, ServiceType serviceType, Map<String, Host> serverList, Set<AgentInfoBo> agentSet) {
		this.id = id;
		if (serviceType == ServiceType.CLIENT) {
			this.applicationName = "CLIENT";
		} else {
			this.applicationName = applicationName;
		}

		this.hostList = (serverList == null) ? new HashMap<String, Host>() : serverList;
		this.serviceType = serviceType;

		fillAgentList(agentSet);
		fillServerInstanceList(agentSet, hostList);
		
//		System.out.println("");
//		System.out.println("----------------------------------");
//		System.out.println("applicationName=" + applicationName);
//		System.out.println("SERVERLIST=" + serverList);
//		System.out.println("AGENTSET=" + agentSet);
//		System.out.println("HOSTLIST=" + hostList);
//		System.out.println("serverInstanceList=" + serverInstanceList);
//		System.out.println("");
	}

	public Map<String, SortedMap<String, ServerInstance>> getServerInstanceList() {
		return serverInstanceList;
	}

	private void fillAgentList(final Set<AgentInfoBo> agentSet) {
		if (agentSet == null) {
			return;
		}
		for (AgentInfoBo agent : agentSet) {
			String key = agent.getHostname();
			List<AgentInfoBo> list = agentList.get(key);

			if (list == null) {
				List<AgentInfoBo> value = new ArrayList<AgentInfoBo>();
				value.add(agent);
				agentList.put(key, value);
			} else {
				agentList.get(key).add(agent);
			}
		}
	}

	private void fillServerInstanceList(final Set<AgentInfoBo> agentSet, final Map<String, Host> agentHistogram) {
		if (agentSet == null) {
			return;
		}
		for (AgentInfoBo agent : agentSet) {
			String key = agent.getHostname();
			SortedMap<String, ServerInstance> serverInstanceMap = serverInstanceList.get(key);

			ResponseHistogram histogram = null;
			if (agentHistogram != null) {
				Host host = agentHistogram.get(agent.getAgentId());
				if (host != null) {
					histogram = host.getHistogram();
				}
			}
			
			ServerInstance serverInstance = new ServerInstance(agent, histogram);

			if (serverInstanceMap == null) {
				SortedMap<String, ServerInstance> value = new TreeMap<String, ServerInstance>();
				value.put(serverInstance.getId(), serverInstance);
				serverInstanceList.put(key, value);
			} else {
				SortedMap<String, ServerInstance> map = serverInstanceList.get(key);
				if (map.containsKey(serverInstance.getId())) {
					map.get(serverInstance.getId()).mergeWith(serverInstance);
				} else {
					map.put(key, serverInstance);
				}
			}
		}
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

	public Map<String, List<AgentInfoBo>> getAgentList() {
		return agentList;
	}

	public void mergeWith(Application application) {
		// merge host list
		for (Entry<String, Host> entry : application.getHostList().entrySet()) {
			Host host = hostList.get(entry.getKey());
			if (host != null) {
				host.mergeWith(entry.getValue());
			} else {
				hostList.put(entry.getKey(), entry.getValue());
			}
		}

		// merge server instance list
		for (Entry<String, SortedMap<String, ServerInstance>> entry : application.getServerInstanceList().entrySet()) {
			SortedMap<String, ServerInstance> exists = serverInstanceList.get(entry.getKey());

			if (exists == null) {
				serverInstanceList.put(entry.getKey(), entry.getValue());
			} else {
				SortedMap<String, ServerInstance> srcValueMap = entry.getValue();
				for (Entry<String, ServerInstance> valueEntry : srcValueMap.entrySet()) {
					if (exists.containsKey(valueEntry.getKey())) {
						exists.get(valueEntry.getKey()).mergeWith(valueEntry.getValue());
					} else {
						exists.put(valueEntry.getKey(), valueEntry.getValue());
					}
				}
			}
		}
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
		sb.append("\"serviceTypeCode\" : \"").append(serviceType.getCode()).append("\"");
		// sb.append("\"agents\" : [ ");
		// if (agentList != null) {
		// Iterator<Entry<String, List<AgentInfoBo>>> iterator =
		// agentList.entrySet().iterator();
		// while (iterator.hasNext()) {
		// Entry<String, List<AgentInfoBo>> entry = iterator.next();
		// sb.append("{ \"host\":\"").append(entry.getKey()).append("\", \"agentList\":[");
		//
		// Iterator<AgentInfoBo> agentIterator = entry.getValue().iterator();
		// while(agentIterator.hasNext()) {
		// sb.append(agentIterator.next().getJson());
		// if (agentIterator.hasNext()) {
		// sb.append(",");
		// }
		// }
		//
		// sb.append("]}");
		// if (iterator.hasNext()) {
		// sb.append(",");
		// }
		// }
		// }
		// sb.append(" ]");
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
