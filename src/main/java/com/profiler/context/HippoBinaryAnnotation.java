package com.profiler.context;

public class HippoBinaryAnnotation {

	private final long time;
	private final String key;
	private final Object value;
	private final Long duration;
	private final String threadname; // TODO: remove, just for debug.

	private EndPoint endPoint;

	public HippoBinaryAnnotation(long time, String key, Object value, EndPoint endPoint, Long duration) {
		this.time = time;
		this.key = key;
		this.value = value;
		this.endPoint = endPoint;
		this.duration = duration;
		this.threadname = Thread.currentThread().getName();
	}

	public void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("@={");
		sb.append("time=").append(time);
		sb.append(", value=").append(value);
		sb.append(", duration=").append(duration);
		sb.append(", endpoint=").append(endPoint);
		sb.append(", threadname=").append(threadname);
		sb.append("}");

		return sb.toString();
	}

}
