package com.nhn.pinpoint.web.applicationmap;

import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import com.nhn.pinpoint.web.service.ComplexNodeId;
import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.applicationmap.rawdata.Host;
import com.nhn.pinpoint.web.util.JsonSerializable;
import com.nhn.pinpoint.web.util.Mergeable;
import com.nhn.pinpoint.web.util.MergeableMap;
import com.nhn.pinpoint.web.util.MergeableTreeMap;

/**
 * application map에서 application을 나타낸다.
 * 
 * @author netspider
 */
public class Application implements Mergeable<NodeId, Application>, JsonSerializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	protected int sequence;
	protected final NodeId id;
	protected final String applicationName;
	protected final ServiceType serviceType;
	protected final Map<String, MergeableMap<String, ServerInstance>> serverInstanceList = new TreeMap<String, MergeableMap<String, ServerInstance>>();

	private final Map<String, Host> serverList = new TreeMap<String, Host>();
	private final Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
	
	private boolean isBuilt = false;
	
	public Application(NodeId id, String applicationName, ServiceType serviceType, Map<String, Host> serverList, Set<AgentInfoBo> agentSet) {
		logger.debug("create application id={}, applicationName={}, serviceType={}, serverList={}, agentSet={}", id, applicationName, serviceType, serverList, agentSet);
		this.id = id;
		this.applicationName = (serviceType == ServiceType.CLIENT) ? "CLIENT" : applicationName;
		this.serviceType = serviceType;
		if (serverList != null) {
			this.serverList.putAll(serverList);
		}
		if (agentSet != null) {
			this.agentSet.addAll(agentSet);
		}
	}

	public Application build() {
		if (isBuilt) {
			return this;
		}
		if (!agentSet.isEmpty()) {
			fillServerInstanceList(agentSet);
		} else {
			fillServerInstanceList(serverList);
		}
		isBuilt = true;
		return this;
	}
	
	public Map<String, MergeableMap<String, ServerInstance>> getServerInstanceList() {
		return serverInstanceList;
	}

	/**
	 * 어플리케이션에 속한 물리서버와 서버 인스턴스 정보를 채운다.
	 * 
	 * @param hostHistogram
	 */
	private void fillServerInstanceList(final Map<String, Host> hostHistogram) {
		if (hostHistogram == null) {
			return;
		}
		
		for (Entry<String, Host> entry : hostHistogram.entrySet()) {
			String instanceName = entry.getKey();
			int pos = instanceName.indexOf(':');
			String hostName = (pos > 0) ? instanceName.substring(0, pos) : instanceName;
			ServiceType serviceType = entry.getValue().getServiceType();
			ServerInstance serverInstance = new ServerInstance(instanceName, serviceType, null);

			MergeableMap<String, ServerInstance> serverInstanceMap = serverInstanceList.get(hostName);
			if (serverInstanceMap == null) {
				MergeableMap<String, ServerInstance> value = new MergeableTreeMap<String, ServerInstance>();
				value.put(serverInstance.getId(), serverInstance);
				serverInstanceList.put(hostName, value);
			} else {
				MergeableMap<String, ServerInstance> map = serverInstanceList.get(hostName);
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

	public NodeId getId() {
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
		logger.debug("merge application a={}, b={}", this.id, application.id);
		
		if (application.serverList != null) {
			this.serverList.putAll(application.serverList);
		}
		if (application.agentSet != null) {
			this.agentSet.addAll(application.agentSet);
		}
		
		// FIXME 여기를 주석처리하면 filter map에서 데이터가 제대로 보이지 않고.
		// 주석 해제하면 통계 map에서 데이터가 맞지 않음.
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
	public String toString() {
		return "Application [sequence=" + sequence + ", id=" + id + ", applicationName=" + applicationName + ", serviceType=" + serviceType + ", serverInstanceList=" + serverInstanceList + "]";
	}
}
