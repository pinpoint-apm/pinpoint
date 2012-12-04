package com.nhn.hippo.web.vo;

public class TerminalRequest {

	private final String id;
	private final String from;
	private final String to;
	private short toServiceType;
	private long requestCount;

	public TerminalRequest(String from, String to, short toServiceType, long requestCount) {
		this.id = from + to + toServiceType;
		this.from = from;
		this.to = to;
		this.toServiceType = toServiceType;
		this.requestCount = requestCount;
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

	public long getRequestCount() {
		return requestCount;
	}

	public short getToServiceType() {
		return toServiceType;
	}

	public void setToServiceType(short toServiceType) {
		this.toServiceType = toServiceType;
	}

	public TerminalRequest mergeWith(TerminalRequest terminalRequest) {
		if (this.equals(terminalRequest)) {
			this.requestCount += terminalRequest.requestCount;
			return this;
		} else {
			throw new IllegalArgumentException("Can't merge with different link.");
		}
	}

	@Override
	public String toString() {
		return "{From=" + from + ", To=" + to + ", ReqCount=" + requestCount + "}";
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
		TerminalRequest other = (TerminalRequest) obj;
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
