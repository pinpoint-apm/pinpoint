package com.nhn.hippo.web.vo;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.profiler.common.bo.SpanBo;

public class BusinessTransaction {
	private final List<Trace> traces = new ArrayList<Trace>();
	private final String rpc;

	private int calls = 0;
	private long totalTime = 0;
	private long maxTime = 0;
	private long minTime = 0;

	public BusinessTransaction(SpanBo span) {
		this.rpc = span.getRpc();

		long elapsed = span.getElapsed();
		totalTime = maxTime = minTime = elapsed;

		this.traces.add(new Trace(new UUID(span.getMostTraceId(), span.getLeastTraceId()).toString(), elapsed, span.getStartTime(), span.getException()));
		calls++;
	}

	public void add(SpanBo span) {
		long elapsed = span.getElapsed();

		totalTime += elapsed;
		if (maxTime < elapsed)
			maxTime = elapsed;
		if (minTime > elapsed)
			minTime = elapsed;

		this.traces.add(new Trace(new UUID(span.getMostTraceId(), span.getLeastTraceId()).toString(), elapsed, span.getStartTime(), span.getException()));

		//if (span.getParentSpanId() == -1) {
			calls++;
		//}
	}

	public String getRpc() {
		return rpc;
	}

	public List<Trace> getTraces() {
		return traces;
	}

	public int getCalls() {
		return calls;
	}

	public long getTotalTime() {
		return totalTime;
	}

	public long getMaxTime() {
		return maxTime;
	}

	public long getMinTime() {
		return minTime;
	}
}
