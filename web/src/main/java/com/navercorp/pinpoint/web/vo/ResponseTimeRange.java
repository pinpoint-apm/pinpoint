package com.nhn.pinpoint.web.vo;

/**
 * FIXME 그냥 Range를 활용해도 될 듯..
 * 
 * @author netspider
 * 
 */
public final class ResponseTimeRange {
	private final int from;
	private final int to;

	public ResponseTimeRange(int from, int to) {
		this.from = from;
		this.to = to;
		validate();
	}

	public ResponseTimeRange(int from, int to, boolean check) {
		this.from = from;
		this.to = to;
		if (check) {
			validate();
		}
	}

	public static ResponseTimeRange createUncheckedRange(int from, int to) {
		return new ResponseTimeRange(from, to, false);
	}

	public int getFrom() {
		return from;
	}

	public int getTo() {
		return to;
	}

	public int getRange() {
		return to - from;
	}

	public void validate() {
		if (this.to < this.from) {
			throw new IllegalArgumentException("invalid range:" + this);
		}
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + from;
		result = prime * result + to;
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
		ResponseTimeRange other = (ResponseTimeRange) obj;
		if (from != other.from)
			return false;
		if (to != other.to)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "ResponseTimeRange [from=" + from + ", to=" + to + "]";
	}
}
