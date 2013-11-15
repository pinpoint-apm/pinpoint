package com.nhn.pinpoint.web.vo;

import java.util.HashSet;
import java.util.Set;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * @author emeroad
 */
@Deprecated
public class TerminalStatistics {

	private final String id;
	private final String from;
	private final String to;
	private short toServiceType;
	private final ResponseHistogram histogram;
	private final Set<String> hosts;

	public TerminalStatistics(String from, String to, short toServiceType) {
		this.id = from + to + toServiceType;
		this.from = from;
		this.to = to;
		this.toServiceType = toServiceType;
		this.histogram = new ResponseHistogram(ServiceType.findServiceType(toServiceType));
		this.hosts = new HashSet<String>();
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

	public short getToServiceType() {
		return toServiceType;
	}

	public void setToServiceType(short toServiceType) {
		this.toServiceType = toServiceType;
	}

	public ResponseHistogram getHistogram() {
		return histogram;
	}

	public TerminalStatistics mergeWith(TerminalStatistics terminalRequest) {
		if (this.equals(terminalRequest)) {
			histogram.mergeWith(terminalRequest.getHistogram());
			return this;
		} else {
			throw new IllegalArgumentException("Can't merge with different link.");
		}
	}

	public void addHosts(Set<String> hosts) {
		if (hosts != null) {
			this.hosts.addAll(hosts);
		}
	}

	public Set<String> getHosts() {
		return hosts;
	}
	
	@Override
	public String toString() {
		return "{ From=" + from + ", To=" + to + ", ToSvcType=" + toServiceType + ", Histogram=" + histogram + ", Hosts=" + hosts + " }";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((from == null) ? 0 : from.hashCode());
		result = prime * result + ((to == null) ? 0 : to.hashCode());
		result = prime * result + toServiceType;
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
		TerminalStatistics other = (TerminalStatistics) obj;
		if (from == null) {
			if (other.from != null)
				return false;
		} else if (!from.equals(other.from))
			return false;
		if (to == null) {
			if (other.to != null)
				return false;
		} else if (!to.equals(other.to))
			return false;
		if (toServiceType != other.toServiceType)
			return false;
		return true;
	}
}
