package com.profiler.context;

public class HippoAnnotation {

	protected final long time;
	protected final String value;
	protected final Long duration;
	protected final String threadname; // TODO: remove, just for debug.

	/**
	 * 
	 * @param time
	 * @param value
	 * @param duration duration in nano second.
	 */
	public HippoAnnotation(long time, String value, Long duration) {
		this.time = time;
		this.value = value;
		this.duration = duration;
		this.threadname = Thread.currentThread().getName();
	}

	public String getValue() {
		return this.value;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("@={");
		sb.append("time=").append(time);
		sb.append(", value=").append(value);
		sb.append(", duration=").append(duration);
		sb.append(", threadname=").append(threadname);
		sb.append("}");

		return sb.toString();
	}

	public com.profiler.common.dto.thrift.Annotation toThrift() {
		com.profiler.common.dto.thrift.Annotation ann = new com.profiler.common.dto.thrift.Annotation();

		ann.setTimestamp(time);
		ann.setValue(value);

		if (duration != null) {
			ann.setDuration(duration);
		}

		return ann;
	}
}
