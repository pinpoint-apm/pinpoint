package com.nhn.pinpoint.web.applicationmap.rawdata;

import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;
import com.nhn.pinpoint.web.service.Node;
import com.nhn.pinpoint.web.service.NodeId;
import com.nhn.pinpoint.web.service.SimpleNodeId;

/**
 * DB에서 조회한 application호출 관계 정보.
 * 
 * @author netspider
 * @author emeroad
 */
public class TransactionFlowStatistics {

    private String from;
    private ServiceType fromServiceType;
    private String to;
    private ServiceType toServiceType;

	/**
	 * key = hostname
	 */
    private HostList toHostList;

    private Set<AgentInfoBo> toAgentSet;

	public TransactionFlowStatistics(String from, short fromServiceType, String to, short toServiceType) {
        if (from == null) {
            throw new NullPointerException("from must not be null");
        }
        if (to == null) {
            throw new NullPointerException("to must not be null");
        }
        this.from = from;
		this.fromServiceType = ServiceType.findServiceType(fromServiceType);
		this.to = to;
		this.toServiceType = ServiceType.findServiceType(toServiceType);
        this.toHostList = new HostList();
	}

	public TransactionFlowStatistics(String from, ServiceType fromServiceType, String to, ServiceType toServiceType) {
		this(from, fromServiceType.getCode(), to, toServiceType.getCode());
	}


	public NodeId getFromApplicationId() {
        return new SimpleNodeId(this.from, this.fromServiceType);
	}
	
	public NodeId getToApplicationId() {
        return new SimpleNodeId(this.to, this.toServiceType);
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



	public String getFrom() {
        return from;
	}

	public String getTo() {
		return to;
	}

	public ServiceType getFromServiceType() {
		return fromServiceType;
	}

	public ServiceType getToServiceType() {
        return toServiceType;
	}

	public void setFrom(String from) {
        this.from = from;
	}

	public void setTo(String to) {
        this.to = to;
	}

	public void setFromServiceType(ServiceType fromServiceType) {
        this.fromServiceType = fromServiceType;
	}

	public void setToServiceType(ServiceType toServiceType) {
        this.toServiceType = toServiceType;
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

	public TransactionFlowStatistics add(TransactionFlowStatistics applicationStatistics) {
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
        sb.append("from='").append(from).append('\'');
        sb.append(", fromServiceType=").append(fromServiceType);
        sb.append(", to='").append(to).append('\'');
        sb.append(", toServiceType=").append(toServiceType);
        sb.append(", toHostList=").append(toHostList);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TransactionFlowStatistics that = (TransactionFlowStatistics) o;

        if (from != null ? !from.equals(that.from) : that.from != null) return false;
        if (fromServiceType != that.fromServiceType) return false;
        if (to != null ? !to.equals(that.to) : that.to != null) return false;
        if (toServiceType != that.toServiceType) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = from != null ? from.hashCode() : 0;
        result = 31 * result + (fromServiceType != null ? fromServiceType.hashCode() : 0);
        result = 31 * result + (to != null ? to.hashCode() : 0);
        result = 31 * result + (toServiceType != null ? toServiceType.hashCode() : 0);
        return result;
    }
}
