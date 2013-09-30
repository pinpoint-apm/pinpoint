package com.nhn.pinpoint.web.applicationmap;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.Host;
import com.nhn.pinpoint.web.applicationmap.rawdata.ResponseHistogram;
import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.Mergeable;
import com.nhn.pinpoint.web.util.MergeableMap;
import com.nhn.pinpoint.web.util.MergeableTreeMap;

/**
 * application map에서 application을 나타낸다.
 * 
 * @author netspider
 */
public class Application implements Comparable<Application>, Mergeable<Application>, JsonSerializable {
	protected int sequence;
	protected final String id;
	protected final String applicationName;
	protected final ServiceType serviceType;
	protected final Map<String, MergeableMap<String, ServerInstance>> serverInstanceList = new TreeMap<String, MergeableMap<String, ServerInstance>>();

	public Application(String id, String applicationName, ServiceType serviceType, Map<String, Host> serverList, Set<AgentInfoBo> agentSet) {
		this.id = id;
		this.applicationName = (serviceType == ServiceType.CLIENT) ? "CLIENT" : applicationName;
		this.serviceType = serviceType;
		if (agentSet != null) {
			fillServerInstanceList(agentSet);
		} else {
			fillServerInstanceList(serverList);
		}
	}

	public Map<String, MergeableMap<String, ServerInstance>> getServerInstanceList() {
		return serverInstanceList;
	}

	public void mapHistogram(Map<String, ResponseHistogram> histogramMap) {
		for (Entry<String, MergeableMap<String, ServerInstance>> mapEntry : this.serverInstanceList.entrySet()) {
			for (Entry<String, ServerInstance> entry : mapEntry.getValue().entrySet()) {
				ServerInstance instance = entry.getValue();
				ResponseHistogram histogram = histogramMap.get(instance.getId());
				if (histogram != null) {
					instance.setHistogram(histogram);
				}
			}
		}
	}

	private void fillServerInstanceList(final Map<String, Host> hostHistogram) {
		if (hostHistogram == null) {
			return;
		}
		
		for (Entry<String, Host> entry : hostHistogram.entrySet()) {
			String key = entry.getKey();
			MergeableMap<String, ServerInstance> serverInstanceMap = serverInstanceList.get(key);

//			ResponseHistogram histogram = null;
//			if (agentHistogram != null) {
//				Host host = agentHistogram.get(agent.getAgentId());
//				if (host != null) {
//					histogram = host.getHistogram();
//				}
//			}

			ServerInstance serverInstance = new ServerInstance(key, entry.getValue().getHistogram());

			if (serverInstanceMap == null) {
				MergeableMap<String, ServerInstance> value = new MergeableTreeMap<String, ServerInstance>();
				value.put(serverInstance.getId(), serverInstance);
				serverInstanceList.put(key, value);
			} else {
				MergeableMap<String, ServerInstance> map = serverInstanceList.get(key);
				map.putOrMerge(serverInstance.getId(), serverInstance);
			}
		}
	}
	
	private void fillServerInstanceList(final Set<AgentInfoBo> agentSet) {
		if (agentSet == null) {
			return;
		}
		for (AgentInfoBo agent : agentSet) {
			String key = agent.getHostname();
			MergeableMap<String, ServerInstance> serverInstanceMap = serverInstanceList.get(key);
			ServerInstance serverInstance = new ServerInstance(agent, null);
			if (serverInstanceMap == null) {
				MergeableMap<String, ServerInstance> value = new MergeableTreeMap<String, ServerInstance>();
				value.put(serverInstance.getId(), serverInstance);
				serverInstanceList.put(key, value);
			} else {
				MergeableMap<String, ServerInstance> map = serverInstanceList.get(key);
				map.putOrMerge(serverInstance.getId(), serverInstance);
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

	@Override
	public Application mergeWith(Application application) {
		// FIXME 서버맵에서 callcount가 맞지않아 주석처리함. 수정중.
		// merge host list
//		for (Entry<String, Host> entry : application.getHostList().entrySet()) {
//			hostList.putOrMerge(entry.getKey(), entry.getValue());
//		}

		// merge server instance list
//		for (Entry<String, MergeableMap<String, ServerInstance>> entry : application.getServerInstanceList().entrySet()) {
//			MergeableMap<String, ServerInstance> exists = serverInstanceList.get(entry.getKey());
//			if (exists == null) {
//				serverInstanceList.put(entry.getKey(), entry.getValue());
//			} else {
//				MergeableMap<String, ServerInstance> srcValueMap = entry.getValue();
//				for (Entry<String, ServerInstance> valueEntry : srcValueMap.entrySet()) {
//					exists.putOrMerge(valueEntry.getKey(), valueEntry.getValue());
//				}
//			}
//		}
		return this;
	}

	public ServiceType getServiceType() {
		return serviceType;
	}

	@Override
	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"sequence\" : ").append(sequence).append(",");
		sb.append("\"applicationName\" : \"").append(applicationName).append("\",");
		sb.append("\"serviceType\" : \"").append(serviceType).append("\",");
		sb.append("\"serviceTypeCode\" : \"").append(serviceType.getCode()).append("\"");
		sb.append(" }");
		return sb.toString();
	}

	@Override
	public int compareTo(Application server) {
		return id.compareTo(server.id);
	}

	@Override
	public String toString() {
		return "Application [sequence=" + sequence + ", id=" + id + ", applicationName=" + applicationName + ", serviceType=" + serviceType + ", serverInstanceList=" + serverInstanceList + "]";
	}
}
