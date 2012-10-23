package com.nhn.hippo.web.vo;

public class Trace {

	private final String traceId;
	private final long time;

	public Trace(String traceId, long time) {
		this.traceId = traceId;
		this.time = time;
	}

	public String getTraceId() {
		return traceId;
	}

	public long getTime() {
		return time;
	}
}
