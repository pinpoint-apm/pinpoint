package com.profiler.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class DeadlineSpanMap {

	private final ConcurrentMap<TraceID.TraceKey, Span> map = new ConcurrentHashMap<TraceID.TraceKey, Span>();

	public Span update(TraceID traceId, SpanUpdater spanUpdater) {
        TraceID.TraceKey traceIdKey = traceId.getTraceKey();
        Span span = map.get(traceIdKey);

		if (span == null) {
			span = new Span(traceId, null, null);

			map.put(traceIdKey, span);
		}

		return spanUpdater.updateSpan(span);
	}

	public Span remove(TraceID traceId) {
		return map.remove(traceId.getTraceKey());
	}

	public int size() {
		return map.size();
	}
}
