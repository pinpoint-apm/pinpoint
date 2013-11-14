package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.service.ComplexNodeId;
import com.nhn.pinpoint.web.service.Node;
import com.nhn.pinpoint.web.service.NodeId;
import com.nhn.pinpoint.web.service.SimpleNodeId;
import com.nhn.pinpoint.web.util.Mergeable;

/**
 * DB에서 조회한 application호출 관계 정보.
 * 
 * @author netspider
 * @author emeroad
 */
public class TransactionFlowStatistics implements Mergeable<NodeId, TransactionFlowStatistics> {

//    private String from;
//    private ServiceType fromServiceType;
//    private String to;
//    private ServiceType toServiceType;
    private ComplexNodeId nodeId;

	/**
	 * key = hostname
	 */
    private HostList toHostList;

    private Set<AgentInfoBo> toAgentSet;

	public TransactionFlowStatistics(String from, short fromServiceType, String to, short toServiceType) {
//		this.from = from;
//		this.fromServiceType = ServiceType.findServiceType(fromServiceType);
//		this.to = to;
//		this.toServiceType = ServiceType.findServiceType(toServiceType);
        final Node fromNode = new Node(from, ServiceType.findServiceType(fromServiceType));
        final Node toNode = new Node(to, ServiceType.findServiceType(toServiceType));
        this.nodeId = new ComplexNodeId(fromNode, toNode);
        this.toHostList = new HostList();
	}

	public TransactionFlowStatistics(String from, ServiceType fromServiceType, String to, ServiceType toServiceType) {
		this(from, fromServiceType.getCode(), to, toServiceType.getCode());
	}

    public TransactionFlowStatistics(ComplexNodeId nodeId) {
        if (nodeId == null) {
            throw new NullPointerException("nodeId must not be null");
        }
        this.nodeId = nodeId;
        this.toHostList = new HostList();
    }
	
	public NodeId getFromApplicationId() {
//		return from + fromServiceType;
//        return new ComplexNodeId(nodeId.getSrc(), Node.EMPTY);
        return new SimpleNodeId(nodeId.getSrc());
	}
	
	public NodeId getToApplicationId() {
//		return to + toServiceType;
//        return nodeId.getDest();
//        return new ComplexNodeId(Node.EMPTY, nodeId.getDest());
//        return new ComplexNodeId(Node.EMPTY, nodeId.getDest());
        return new SimpleNodeId(nodeId.getDest());
	}
	
	/**
	 * 
	 * @param hostname
	 *            host이름 또는 endpoint
	 * @param slot
	 * @param value
	 */
	public void addSample(String hostname, short serviceTypeCode, short slot, long value) {
		// TODO 임시코드
		if (hostname == null || hostname.length() == 0) {
			hostname = "UNKNOWNHOST";
		}
        this.toHostList.addHost(hostname, serviceTypeCode, slot, value);
	}


	public NodeId getId() {
		return nodeId;
	}

	public String getFrom() {
        return nodeId.getSrc().getName();
	}

	public String getTo() {
		return nodeId.getDest().getName();
	}

	public ServiceType getFromServiceType() {
		return nodeId.getSrc().getServiceType();
	}

	public ServiceType getToServiceType() {
        return nodeId.getDest().getServiceType();
	}

	public void setFrom(String from) {
        nodeId.getSrc().setName(from);
	}

	public void setTo(String to) {
        nodeId.getDest().setName(to);
	}

	public void setFromServiceType(ServiceType fromServiceType) {
        nodeId.getSrc().setServiceType(fromServiceType);
	}

	public void setToServiceType(ServiceType toServiceType) {
        nodeId.getDest().setServiceType(toServiceType);
	}

	public HostList getToHostList() {
		return toHostList;
	}

	public Set<AgentInfoBo> getToAgentSet() {
		return toAgentSet;
	}

	public void addToAgentSet(Set<AgentInfoBo> agentSet) {
		if (this.toAgentSet != null) {
			this.toAgentSet.addAll(agentSet);
		} else {
			this.toAgentSet = agentSet;
		}
	}

	public TransactionFlowStatistics mergeWith(TransactionFlowStatistics applicationStatistics) {
		if (this.equals(applicationStatistics)) {
            final HostList target = applicationStatistics.getToHostList();
            this.toHostList.addHostList(target);
			return this;
		} else {
			throw new IllegalArgumentException("Can't merge with different link.");
		}
	}

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TransactionFlowStatistics{");
        sb.append("nodeId=").append(nodeId);
        sb.append(", toHostList=").append(toHostList);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionFlowStatistics that = (TransactionFlowStatistics) o;

        if (nodeId != null ? !nodeId.equals(that.nodeId) : that.nodeId != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return nodeId != null ? nodeId.hashCode() : 0;
    }
}
