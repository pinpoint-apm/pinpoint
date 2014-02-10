package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.HostList;
import com.nhn.pinpoint.web.service.NodeId;
import com.nhn.pinpoint.web.vo.Application;
import com.nhn.pinpoint.web.vo.ResponseHistogramSummary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.util.JsonSerializable;

/**
 * node map에서 application을 나타낸다.
 * 
 * @author netspider
 * @author emeroad
 */
public class Node implements JsonSerializable {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private int sequence;
    private final NodeId id;
    private final Application application;

    private final ServerInstanceList serverInstanceList = new ServerInstanceList();

	private final HostList hostList;
	private final Set<AgentInfoBo> agentSet;

    private ResponseHistogramSummary responseHistogramSummary;
	

	public Node(NodeId id, Application application, Set<AgentInfoBo> agentSet) {
        this(id, application, null, agentSet);
	}

    public Node(NodeId id, Application application, HostList hostList) {
        this(id, application, hostList, null);
    }

    Node(NodeId id, Application application, HostList hostList, Set<AgentInfoBo> agentSet) {
        if (id == null) {
            throw new NullPointerException("id must not be null");
        }
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }

        logger.debug("create node id={}, applicationName={}, serviceType={}, agentSet={}", id, application, agentSet);
        this.id = id;
        this.application = application;
        this.hostList = new HostList();
        this.agentSet = new HashSet<AgentInfoBo>();

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

    public Node(Node copyNode) {
        if (copyNode == null) {
            throw new NullPointerException("copyNode must not be null");
        }
        this.id = copyNode.id;
        this.application = copyNode.application;
        this.hostList = new HostList(copyNode.hostList);
        this.agentSet = new HashSet<AgentInfoBo>(copyNode.agentSet);
    }

    private String getApplicationName(Application application) {
        if (application.getServiceType().isUser()) {
            return "USER";
        } else {
            return application.getName();
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
		return getApplicationName(application);
	}

	public Node add(Node node) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
        logger.debug("merge node a={}, b={}", this.id, node.id);
		
        // 리얼 application을 실제빌드할때 copy하여 만들기 때문에. add할때 데이터를 hostList를 add해도 된다.
        this.hostList.addHostList(node.hostList);
//        this.hostList.put(node.hostList);

		if (node.agentSet != null) {
			this.agentSet.addAll(node.agentSet);
		}
		
		// FIXME 여기를 주석처리하면 filter map에서 데이터가 제대로 보이지 않고.
		// 주석 해제하면 통계 map에서 데이터가 맞지 않음.
		// merge server instance list
//		for (Entry<String, MergeableMap<String, ServerInstance>> entry : node.getServerInstanceList().entrySet()) {
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

    public Node deepCopy() {
        HostList copyHostList = new HostList(hostList);
        return new Node(this.id, this.application, copyHostList, agentSet);
    }

	public ServiceType getServiceType() {
		return application.getServiceType();
	}

    public ResponseHistogramSummary getResponseHistogramSummary() {
        return responseHistogramSummary;
    }

    public void setResponseHistogramSummary(ResponseHistogramSummary responseHistogramSummary) {
        this.responseHistogramSummary = responseHistogramSummary;
    }

    @Override
	public String getJson() {
		StringBuilder sb = new StringBuilder();
		sb.append("{ ");
		sb.append("\"sequence\" : ").append(sequence).append(",");
		sb.append("\"applicationName\" : \"").append(application.getName()).append("\",");
		sb.append("\"serviceType\" : \"").append(application.getServiceType()).append("\",");
		sb.append("\"serviceTypeCode\" : \"").append(application.getServiceTypeCode()).append("\"");
		sb.append(" }");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Node [sequence=" + sequence + ", id=" + id + ", application=" + application + ", serverInstanceList=" + serverInstanceList + "]";
	}
}
