package com.nhn.pinpoint.web.applicationmap;

import java.util.*;

import com.nhn.pinpoint.web.applicationmap.rawdata.CallHistogramList;
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

    public static final String NODE_DELIMITER = "^";
    private final Application application;

    private final ServerInstanceList serverInstanceList = new ServerInstanceList();

	private final CallHistogramList callHistogramList;
	private final Set<AgentInfoBo> agentSet;

    private ResponseHistogramSummary responseHistogramSummary;
	

	public Node(Application application, Set<AgentInfoBo> agentSet) {
        this(application, null, agentSet);
	}

    public Node(Application application, CallHistogramList callHistogramList) {
        this(application, callHistogramList, null);
    }

    Node(Application application, CallHistogramList callHistogramList, Set<AgentInfoBo> agentSet) {
        if (application == null) {
            throw new NullPointerException("application must not be null");
        }

        logger.debug("create node application={}, agentSet={}", application, agentSet);
        this.application = application;
        this.callHistogramList = new CallHistogramList();
        this.agentSet = new HashSet<AgentInfoBo>();

        if (callHistogramList != null) {
            // 이 put은 정확하지 않음.
            //		this.callHistogramList.addCallHistogram(callHistogramList);
            this.callHistogramList.put(callHistogramList);
        }

        if (agentSet != null) {
            this.agentSet.addAll(agentSet);
        }
    }

    public Node(Node copyNode) {
        if (copyNode == null) {
            throw new NullPointerException("copyNode must not be null");
        }
        this.application = copyNode.application;
        this.callHistogramList = new CallHistogramList(copyNode.callHistogramList);
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
            serverInstanceList.fillServerInstanceList(callHistogramList);
		}
	}
	
	public Map<String, List<ServerInstance>> getServerInstanceList() {
		return serverInstanceList.getServerInstanceList();
	}

    public Application getApplication() {
        return application;
    }



	public String getNodeName() {
		return application.getName() + NODE_DELIMITER + application.getServiceType();
	}

	public String getApplicationName() {
		return getApplicationName(application);
	}

	public void add(Node node) {
        if (node == null) {
            throw new NullPointerException("node must not be null");
        }
        logger.trace("merge node this={}, node={}", this.application, node.application);
		
        // 리얼 application을 실제빌드할때 copy하여 만들기 때문에. add할때 데이터를 hostList를 add해도 된다.
        this.callHistogramList.addCallHistogram(node.callHistogramList);

		if (node.agentSet != null) {
			this.agentSet.addAll(node.agentSet);
		}
		
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
		sb.append("\"applicationName\" : \"").append(application.getName()).append("\",");
		sb.append("\"serviceType\" : \"").append(application.getServiceType()).append("\",");
		sb.append("\"serviceTypeCode\" : \"").append(application.getServiceTypeCode()).append("\"");
		sb.append(" }");
		return sb.toString();
	}

	@Override
	public String toString() {
		return "Node [application=" + application + ", serverInstanceList=" + serverInstanceList + "]";
	}
}
