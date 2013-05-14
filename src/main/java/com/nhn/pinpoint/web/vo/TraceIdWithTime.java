package com.nhn.pinpoint.web.vo;

public class TraceIdWithTime extends TraceId {

	private final long acceptedTime;

	public TraceIdWithTime(long most, long least, long acceptedTime) {
		super(most, least);
		this.acceptedTime = acceptedTime;
	}

	public long getAcceptedTime() {
		return acceptedTime;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + (int) (acceptedTime ^ (acceptedTime >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (!super.equals(obj))
			return false;
		if (getClass() != obj.getClass())
			return false;
		TraceIdWithTime other = (TraceIdWithTime) obj;
		if (acceptedTime != other.acceptedTime)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "TraceIdWithTime [acceptedTime=" + acceptedTime + ", most=" + most + ", least=" + least + "]";
	}
}
