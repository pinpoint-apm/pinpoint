package com.profiler.context;

import com.profiler.util.NamedThreadLocal;

/**
 * 
 * @author netspider
 * 
 */
public class Trace {

	private static final DeadlineSpanMap spanMap = new DeadlineSpanMap();

	private static final ThreadLocal<TraceID> traceId = new NamedThreadLocal<TraceID>("TraceId");

	private static volatile boolean tracingEnabled = true;

	private Trace() {

	}

	/**
	 * Get current TraceID or if's not exists create new one and return it.
	 * 
	 * @return
	 */
	public static TraceID getTraceId() {
		TraceID id = traceId.get();

		if (id == null) {
			id = TraceID.newTraceId();
			traceId.set(id);
			return id;
		}

		return id;
	}

	/**
	 * Get current TraceID. If it was not set this will return null.
	 * 
	 * @return
	 */
	public static TraceID getCurrentTraceId() {
		return traceId.get();
	}

	public static void enable() {
		tracingEnabled = true;
	}

	public static void disable() {
		tracingEnabled = false;
	}

	public static TraceID getNextId() {
		TraceID current = getTraceId();
		return new TraceID(current.getTraceId(), current.getSpanId(), SpanID.newSpanID(), current.isSampled(), current.getFlags());
	}

	public static void setTraceId(TraceID traceId) {
		Trace.traceId.set(traceId);
	}

	private static void mutate(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = spanMap.update(traceId, spanUpdater);

		if (span.isExistsAnnotationType("CR") || span.isExistsAnnotationType("SS")) {
			spanMap.remove(traceId);
			logSpan(span);
		}
	}

	private static void logSpan(Span span) {
		// TODO: send span to server
		System.out.println("\n\nWrite span hash=" + span.hashCode() + ", value=" + span + ", spanMap.size=" + spanMap.size() + ", threadid=" + Thread.currentThread().getId() + "\n\n");
	}

	public static void record(Annotation annotation) {
		if (!tracingEnabled)
			return;

		annotate(annotation.getCode(), null);
	}

	public static void record(Annotation annotation, long duration) {
		if (!tracingEnabled)
			return;

		annotate(annotation.getCode(), duration);
	}

	public static void recordAttibute(final String key, final Object value) {
		if (!tracingEnabled)
			return;

		mutate(getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				span.addAnnotation(new HippoBinaryAnnotation(System.currentTimeMillis(), key, value, span.getEndPoint(), null));
				return span;
			}
		});
	}

	public static void recordMessage(String message) {
		if (!tracingEnabled)
			return;

		annotate(message, null);
	}

	public static void recordRpcName(final String service, final String rpc) {
		if (!tracingEnabled)
			return;

		mutate(getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				span.setServiceName(service);
				span.setName(rpc);
				return span;
			}
		});
	}

	public static void recordEndPoint(final String ip, final int port) {
		if (!tracingEnabled)
			return;

		mutate(getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				// set endpoint to both span and annotations
				span.setEndPoint(new EndPoint(ip, port));
				return span;
			}
		});
	}

	private static void annotate(final String value, final Long duration) {
		if (!tracingEnabled)
			return;

		mutate(getTraceId(), new SpanUpdater() {
			@Override
			public Span updateSpan(Span span) {
				span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), value, span.getEndPoint(), duration));
				return span;
			}
		});
	}
}