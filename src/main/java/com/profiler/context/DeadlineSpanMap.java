package com.profiler.context;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.profiler.context.tracer.Tracer;

public class DeadlineSpanMap {

	private final Map<String, Span> map = new ConcurrentHashMap<String, Span>();

	private final Tracer tracer;

	public DeadlineSpanMap(Tracer tracer) {
		this.tracer = tracer;
	}

	public Span update(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = map.get(traceId.toString());

		if (span == null) {
			span = new Span(traceId, null, null);
			map.put(traceId.toString(), span);
		}

		return spanUpdater.updateSpan(span);
	}

	public Span remove(TraceID traceId) {
		return map.remove(traceId.toString());
	}
}
