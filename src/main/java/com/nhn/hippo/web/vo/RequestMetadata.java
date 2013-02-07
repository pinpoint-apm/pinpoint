package com.nhn.hippo.web.vo;

import java.util.UUID;

/**
 * 
 * @author netspider
 * 
 */
public class RequestMetadata {

	private final String traceId;
	private final long startTime;
	private final int elapsed;
	private final String application;

	public RequestMetadata(long mostTraceId, long leastTraceId, long startTime, int elapsed, String application) {
		this.traceId = new UUID(mostTraceId, leastTraceId).toString();
		this.startTime = startTime;
		this.elapsed = elapsed;
		this.application = application;
	}

	public String getTraceId() {
		return traceId;
	}

	public long getStartTime() {
		return startTime;
	}

	public int getElapsed() {
		return elapsed;
	}

	public String getApplication() {
		return application;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(traceId);
		sb.append(startTime);
		sb.append(elapsed);
		sb.append(application);
		return sb.toString();
	}
}
