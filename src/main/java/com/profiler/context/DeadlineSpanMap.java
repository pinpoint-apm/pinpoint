package com.profiler.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class DeadlineSpanMap {

	private final Map<String, Span> map = new ConcurrentHashMap<String, Span>();

	public Span update(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = map.get(traceId.getTraceId());

		if (span == null) {
			span = new Span(traceId, null, null);
			map.put(traceId.getTraceId(), span);
		}

		return spanUpdater.updateSpan(span);
	}

	public Span remove(TraceID traceId) {
		return map.remove(traceId.getTraceId());
	}

	public int size() {
		return map.size();
	}
}
