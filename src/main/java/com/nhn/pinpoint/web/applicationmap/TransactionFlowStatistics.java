package com.nhn.pinpoint.web.applicationmap;

import java.util.HashSet;
import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.common.bo.AgentInfoBo;

/**
 * DB에서 조회한 application호출 관계 정보.
 * 
 * @author netspider
 * 
 */
public class TransactionFlowStatistics {

	private String id;
	private String from;
	private ServiceType fromServiceType;
	private String to;
	private ServiceType toServiceType;
	private final ResponseHistogram histogram;
	private final Set<String> toHosts;
	private final Set<AgentInfoBo> toAgents;

	public TransactionFlowStatistics(String from, short fromServiceType, String to, short toServiceType) {
		this.from = from;
		this.fromServiceType = ServiceType.findServiceType(fromServiceType);
		this.to = to;
		this.toServiceType = ServiceType.findServiceType(toServiceType);
		this.histogram = new ResponseHistogram(ServiceType.findServiceType(toServiceType));
		this.toHosts = new HashSet<String>();
		this.toAgents = new HashSet<AgentInfoBo>();
		makeId();
	}

	public void makeId() {
		this.id = from + fromServiceType + to + toServiceType;
	}

	public String getId() {
		return id;
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

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	public void setFrom(String from) {
		this.from = from;
		makeId();
	}

	public void setTo(String to) {
		this.to = to;
		makeId();
	}

	public void setFromServiceType(ServiceType fromServiceType) {
		this.fromServiceType = fromServiceType;
		makeId();
	}

	public void setToServiceType(ServiceType toServiceType) {
		this.toServiceType = toServiceType;
		makeId();
	}
	
	public void clearHosts() {
		this.toHosts.clear();
	}

	public TransactionFlowStatistics mergeWith(TransactionFlowStatistics applicationStatistics) {
		if (this.equals(applicationStatistics)) {
			histogram.mergeWith(applicationStatistics.getHistogram());
			return this;
		} else {
			throw new IllegalArgumentException("Can't merge with different link.");
		}
	}

	public void addToHosts(Set<String> hosts) {
		if (hosts != null) {
			this.toHosts.addAll(hosts);
		}
	}
	
	public void addToHost(String host) {
		if (host != null) {
			this.toHosts.add(host);
		}
	}

	public Set<String> getToHosts() {
		return toHosts;
	}

	public void addToAgents(Set<AgentInfoBo> agentInfo) {
		if (agentInfo != null) {
			this.toAgents.addAll(agentInfo);
		}
	}
	
	public Set<AgentInfoBo> getToAgents() {
		return toAgents;
	}
	
	@Override
	public String toString() {
		return "ApplicationStatistics [id=" + id + ", from=" + from + ", fromServiceType=" + fromServiceType + ", to=" + to + ", toServiceType=" + toServiceType + ", histogram=" + histogram + ", toHosts=" + toHosts + "]";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TransactionFlowStatistics other = (TransactionFlowStatistics) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}
}
