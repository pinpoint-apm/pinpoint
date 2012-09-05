package com.profiler.context;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.profiler.sender.DataSender;
import com.profiler.util.NamedThreadLocal;

/**
 * 
 * @author netspider
 * 
 */
public final class Trace {

	private static final Logger logger = Logger.getLogger(Trace.class.getName());

	private static final DeadlineSpanMap spanMap = new DeadlineSpanMap();

	private static final ThreadLocal<TraceID> traceIdLocal = new NamedThreadLocal<TraceID>("TraceId");

	private static volatile boolean tracingEnabled = true;

	private Trace() {

	}

	/**
	 * Get current TraceID or if's not exists create new one and return it.
	 * 
	 * @return
	 */
	public static TraceID getTraceIdOrCreateNew() {
		TraceID id = traceIdLocal.get();

		if (id == null) {
			id = TraceID.newTraceId();
			traceIdLocal.set(id);
			return id;
		}

		return id;
	}

	public static boolean removeTraceId() {
		TraceID traceID = traceIdLocal.get();
		if (traceID != null) {
			traceIdLocal.remove();
			spanMap.remove(traceID);
			return true;
		}
		return false;
	}

	/**
	 * Get current TraceID. If it was not set this will return null.
	 * 
	 * @return
	 */
	public static TraceID getCurrentTraceId() {
		return traceIdLocal.get();
	}

	public static void enable() {
		tracingEnabled = true;
	}

	public static void disable() {
		tracingEnabled = false;
	}

	public static TraceID getNextTraceId() {
		TraceID current = getTraceIdOrCreateNew();
		return new TraceID(current.getId(), current.getSpanId(), SpanID.newSpanID(), current.isSampled(), current.getFlags());
	}

	public static void setTraceId(TraceID traceId) {
		if (getCurrentTraceId() != null) {
			logger.log(Level.WARNING, "TraceID is already exists. But overwritten.");
		}
		Trace.traceIdLocal.set(traceId);
	}

	private static void mutate(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = spanMap.update(traceId, spanUpdater);

		if (span.isExistsAnnotationType("CR") || span.isExistsAnnotationType("SS")) {
			spanMap.remove(traceId);
			logSpan(span);
		}
	}

	static void logSpan(Span span) {
		try {
			// TODO: send span to the server.
			System.out.println("\n\n[WRITE SPAN] hashCode=" + span.hashCode() + ", Value=" + span + ", SpanMap.size=" + spanMap.size() + ", CurrentThreadID=" + Thread.currentThread().getId() + "\n\n");

			// TODO: remove this, just for debugging
			if(spanMap.size() > 0) {
				System.out.println("###############################################################");
				System.out.println("#          WARNING SpanMap size > 0 check spanMap.            #");
				System.out.println("###############################################################");
			}
			
			DataSender.getInstance().addDataToSend(span.toThrift());

			span.cancelTimer();
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
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

	public static void recordAttribute(final String key, final String value) {
		recordAttibute(key, (Object) value);
	}

	public static void recordAttibute(final String key, final Object value) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					span.addAnnotation(new HippoBinaryAnnotation(System.currentTimeMillis(), key, value));
					return span;
				}
			});
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void recordMessage(String message) {
		if (!tracingEnabled)
			return;

		annotate(message, null);
	}

	public static void recordRpcName(final String service, final String rpc) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					span.setServiceName(service);
					span.setName(rpc);
					return span;
				}
			});
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	public static void recordEndPoint(final String ip, final int port) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					// set endpoint to both span and annotations
					span.setEndPoint(new EndPoint(ip, port));
					return span;
				}
			});
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}

	private static void annotate(final String value, final Long duration) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceIdOrCreateNew(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), value, duration));
					return span;
				}
			});
		} catch (Exception e) {
			logger.log(Level.SEVERE, e.getMessage(), e);
		}
	}
}