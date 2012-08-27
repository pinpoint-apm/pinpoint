package com.profiler.context.tracer;

import com.profiler.context.EndPoint;

public class HippoAnnotation {

	private final long time;
	private final String value;
	private final Long duration;

	private EndPoint endPoint;

	public HippoAnnotation(long time, String value, EndPoint endPoint, Long duration) {
		this.time = time;
		this.value = value;
		this.endPoint = endPoint;
		this.duration = duration;
	}

	public String getValue() {
		return this.value;
	}

	public void setEndPoint(EndPoint endPoint) {
		this.endPoint = endPoint;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("HippoAnnotation={");
		sb.append("time=").append(time);
		sb.append("value=").append(value);
		sb.append("duration=").append(duration);
		sb.append("endpoint=").append(endPoint);
		sb.append("}");

		return sb.toString();
	}

}
