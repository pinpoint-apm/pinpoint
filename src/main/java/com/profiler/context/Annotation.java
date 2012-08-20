package com.profiler.context;

public class Annotation {

	protected final long timestamp;
	protected final String value;
	protected final EndPoint endPoint;

	protected long processStart;
	protected long processEnd;

	/**
	 * @param timestamp
	 *            when was this annotation created? microseconds from epoch
	 * @param value
	 *            description of what happened at the timestamp could for
	 *            example be "cache miss for key: x"
	 * @param endPoint
	 *            host this annotation was created on
	 */
	public Annotation(long timestamp, String value, EndPoint endPoint) {
		this.timestamp = timestamp;
		this.value = value;
		this.endPoint = endPoint;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void processStart() {
		processStart = System.nanoTime();
	}

	public void processEnd() {
		processEnd = System.nanoTime();
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("Annotation[");
		sb.append("Timestamp=").append(timestamp);
		sb.append("Value=").append(value);
		sb.append("EndPoint=").append(endPoint);
		sb.append("ProcessStart=").append(processStart);
		sb.append("ProcessEnd=").append(processEnd);
		sb.append("]");

		return sb.toString();
	}
}
