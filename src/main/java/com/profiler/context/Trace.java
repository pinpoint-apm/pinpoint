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

    public static boolean removeTraceId() {
        TraceID traceID = traceId.get();
        if(traceID != null) {
            traceId.remove();
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

	static void logSpan(Span span) {
		try {
			// TODO: send span to server
			System.out.println("\n\nWrite span hash=" + span.hashCode() + ", value=" + span + ", spanMap.size=" + spanMap.size() + ", threadid=" + Thread.currentThread().getId() + "\n\n");

			DataSender.getInstance().addDataToSend(span.toThrift());
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
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

	public static void recordAttibute(final String key, final Object value) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceId(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					span.addAnnotation(new HippoBinaryAnnotation(System.currentTimeMillis(), key, value));
					return span;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
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
			mutate(getTraceId(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					span.setServiceName(service);
					span.setName(rpc);
					return span;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	public static void recordEndPoint(final String ip, final int port) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceId(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					// set endpoint to both span and annotations
					span.setEndPoint(new EndPoint(ip, port));
					return span;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
		}
	}

	private static void annotate(final String value, final Long duration) {
		if (!tracingEnabled)
			return;

		try {
			mutate(getTraceId(), new SpanUpdater() {
				@Override
				public Span updateSpan(Span span) {
					span.addAnnotation(new HippoAnnotation(System.currentTimeMillis(), value, duration));
					return span;
				}
			});
		} catch (Exception e) {
			e.printStackTrace();
			logger.log(Level.SEVERE, e.getMessage());
		}
	}
}