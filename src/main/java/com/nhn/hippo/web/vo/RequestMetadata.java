package com.nhn.hippo.web.vo;

import com.profiler.common.util.TraceIdUtils;

/**
 * UI로 이 객체 대신 SpanBO를 던진다.
 * 
 * @author netspider
 * 
 */
@Deprecated
public class RequestMetadata {

	private final String traceId;
	private final long startTime;
	private final int elapsed;
	private final String application;

	public RequestMetadata(long mostTraceId, long leastTraceId, long startTime, int elapsed, String application) {
		this.traceId = TraceIdUtils.formatString(mostTraceId, leastTraceId);
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
