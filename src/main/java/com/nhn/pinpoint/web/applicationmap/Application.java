package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.HostList;
import com.nhn.pinpoint.web.service.NodeId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * application map에서 application을 나타낸다.
 * 
 * @author netspider
 * @author emeroad
 */
public class Application implements JsonSerializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private int sequence;
    private final NodeId id;
    private final String applicationName;
    private final ServiceType serviceType;

    private final ServerInstanceList serverInstanceList = new ServerInstanceList();

	private final HostList hostList = new HostList();
	private final Set<AgentInfoBo> agentSet = new HashSet<AgentInfoBo>();
	

	public Application(NodeId id, String applicationName, ServiceType serviceType, Set<AgentInfoBo> agentSet) {
        this(id, applicationName, serviceType, null, agentSet);
	}

    public Application(NodeId id, String applicationName, ServiceType serviceType, HostList hostList) {
        this(id, applicationName, serviceType, hostList, null);
    }

    Application(NodeId id, String applicationName, ServiceType serviceType, HostList hostList, Set<AgentInfoBo> agentSet) {
        logger.debug("create application id={}, applicationName={}, serviceType={}, agentSet={}", id, applicationName, serviceType, agentSet);
        this.id = id;
        this.applicationName = getApplicationName(applicationName, serviceType);
        this.serviceType = serviceType;

        if (hostList != null) {
            // 이 put은 정확하지 않음.
            //		this.hostList.addHostList(hostList);
            logger.debug("createApplication");
            this.hostList.put(hostList);
        }

        if (agentSet != null) {
            this.agentSet.addAll(agentSet);
        }
    }

    private String getApplicationName(String applicationName, ServiceType serviceType) {
        if (serviceType == ServiceType.CLIENT) {
            return "CLIENT";
        } else {
            return applicationName;
        }
    }

    void build() {
		if (!agentSet.isEmpty()) {
			serverInstanceList.fillServerInstanceList(agentSet);
		} else {
            serverInstanceList.fillServerInstanceList(hostList);
		}
	}
	
	public Map<String, List<ServerInstance>> getServerInstanceList() {
		return serverInstanceList.getServerInstanceList();
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

	public Application add(Application application) {
		logger.debug("merge application a={}, b={}", this.id, application.id);
		

        logger.debug("addApplication");
        // 리얼 application을 실제빌드할때 copy하여 만들기 때문에. add할때 데이터를 hostList를 add해도 된다.
        this.hostList.addHostList(application.hostList);
//        this.hostList.put(application.hostList);

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

    public Application deepCopy() {
        HostList copyHostList = hostList.deepCopy();
        return new Application(this.id, this.applicationName, this.serviceType, copyHostList, agentSet);
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
