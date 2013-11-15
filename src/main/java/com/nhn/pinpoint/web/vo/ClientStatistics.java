package com.nhn.pinpoint.web.vo;

import com.nhn.pinpoint.common.ServiceType;

/**
 * 
 * @author netspider
 * 
 */
@Deprecated
public class ClientStatistics {

	private final String id;
	private final String to;
	private short toServiceType;
	private final ResponseHistogram histogram;

	public ClientStatistics(String to, short toServiceType) {
//		this.id = "CLIENT-" + to + toServiceType;
		this.id = "CLIENT-" + to;
		this.to = to;
		this.toServiceType = toServiceType;
		this.histogram = new ResponseHistogram(ServiceType.findServiceType(toServiceType));
	}

	public String getId() {
		return id;
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

	public ClientStatistics mergeWith(ClientStatistics terminalRequest) {
		if (this.equals(terminalRequest)) {
			histogram.mergeWith(terminalRequest.getHistogram());
			return this;
		} else {
			throw new IllegalArgumentException("Can't merge with different link.");
		}
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
		ClientStatistics other = (ClientStatistics) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ClientStatistics [id=" + id + ", to=" + to + ", toServiceType=" + toServiceType + ", histogram=" + histogram + "]";
	}
}
