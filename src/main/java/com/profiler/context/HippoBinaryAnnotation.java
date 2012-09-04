package com.profiler.context;

public class HippoBinaryAnnotation {

	private final long time;
	private final String key;
	private final Object value;
	private final Long duration;
	private final String threadname; // TODO: remove, just for debug.

	public HippoBinaryAnnotation(long time, String key, Object value, Long duration) {
		this.time = time;
		this.key = key;
		this.value = value;
		this.duration = duration;
		this.threadname = Thread.currentThread().getName();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append("@={");
		sb.append("time=").append(time);
		sb.append(", key=").append(key);
		sb.append(", value=").append(value);
		sb.append(", duration=").append(duration);
		sb.append(", threadname=").append(threadname);
		sb.append("}");

		return sb.toString();
	}

}
