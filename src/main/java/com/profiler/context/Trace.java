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
	private static final ThreadLocal<TraceIDStack> traceIdLocal = new NamedThreadLocal<TraceIDStack>("TraceId");
	private static volatile boolean tracingEnabled = true;

	private Trace() {
	}

	public static void handle(TraceHandler handler) {
		TraceIDStack traceIDStack = traceIdLocal.get();
		if (traceIDStack == null) {
			traceIDStack = new TraceIDStack();
			traceIdLocal.set(traceIDStack);
		}

		try {
			TraceID nextId = getNextTraceId();
			traceIDStack.incr();

			if (traceIDStack.getTraceId() == null) {
				System.out.println(getCurrentTraceId());
				traceIDStack.setTraceId(nextId);
			}

			handler.handle();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			traceIDStack.decr();
		}
	}

	public static void traceBlockBegin() {
		TraceIDStack traceIDStack = traceIdLocal.get();
		if (traceIDStack == null) {
			traceIDStack = new TraceIDStack();
			traceIdLocal.set(traceIDStack);
		}

		try {
			TraceID nextId = getNextTraceId();
			traceIDStack.incr();

			if (traceIDStack.getTraceId() == null) {
				traceIDStack.setTraceId(nextId);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void traceBlockEnd() {
		TraceIDStack traceIDStack = traceIdLocal.get();
		traceIDStack.decr();
	}

	/**
	 * Get current TraceID or if's not exists create new one and return it.
	 * 
	 * @return
	 */
	public static TraceID getTraceIdOrCreateNew() {
		// TraceID id = traceIdLocal.get();
		TraceIDStack stack = traceIdLocal.get();
		TraceID id = null;
		if (stack != null) {
			id = stack.getTraceId();
		}

		if (id == null) {
			System.out.println("create new traceid");

			id = TraceID.newTraceId();
			// traceIdLocal.set(id);

			if (stack == null) {
				traceIdLocal.set(new TraceIDStack());
			}

			traceIdLocal.get().setTraceId(id);
			return id;
		}

		return id;
	}

	public static boolean removeCurrentTraceIdFromStack() {
		TraceIDStack stack = traceIdLocal.get();
		TraceID traceId = null;

		if (stack != null) {
			traceId = stack.getTraceId();
		} else {
			// TODO : remove this log.
			System.out.println("#############################################################");
			System.out.println("# Something's going wrong. Stack is not exists.             #");
			System.out.println("#############################################################");

			stack = new TraceIDStack();
			traceIdLocal.set(stack);
		}

		if (traceId != null) {
			stack.clear();
			spanMap.remove(traceId);
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
		// return traceIdLocal.get();
		TraceIDStack stack = traceIdLocal.get();

		if (stack == null) {
			return null;
		}

		return stack.getTraceId();
	}

	public static void enable() {
		tracingEnabled = true;
	}

	public static void disable() {
		tracingEnabled = false;
	}

	public static TraceID getNextTraceId() {
		TraceID current = getTraceIdOrCreateNew();
		return current.getNextTraceId();
	}

	public static void setTraceId(TraceID traceId) {
		// TODO: remove this, just for debugging.
		if (getCurrentTraceId() != null) {
			System.out.println("###############################################################################################################");
			System.out.println("# [DEBUG MSG] TraceID is overwritten.");
			System.out.println("#   Before : " + getCurrentTraceId());
			System.out.println("#   After  : " + traceId);
			System.out.println("###############################################################################################################");
			new RuntimeException("TraceID overwritten.").printStackTrace();
		}

		TraceIDStack stack = traceIdLocal.get();

		if (stack == null)
			traceIdLocal.set(new TraceIDStack());

		Trace.traceIdLocal.get().setTraceId(traceId);
		// Trace.traceIdLocal.set(traceId);
	}

	private static void mutate(TraceID traceId, SpanUpdater spanUpdater) {
		Span span = spanMap.update(traceId, spanUpdater);

		if (span.isExistsAnnotationType(Annotation.ClientRecv.getCode()) || span.isExistsAnnotationType(Annotation.ServerSend.getCode())) {
			// remove current context threadId from stack
			removeCurrentTraceIdFromStack();
			logSpan(span);
		}
	}

	static void logSpan(Span span) {
		try {
			// TODO: send span to the server.
			System.out.println("\n\n[WRITE SPAN] hashCode=" + span.hashCode() + ",\n\t " + span + ",\n\t SpanMap.size=" + spanMap.size() + ",\n\t CurrentThreadID=" + Thread.currentThread().getId() + ",\n\t CurrentThreadName=" + Thread.currentThread().getName() + "\n\n");

			// TODO: remove this, just for debugging
			// if (spanMap.size() > 0) {
			// System.out.println("##################################################################");
			// System.out.println("# [DEBUG MSG] WARNING SpanMap size > 0 check spanMap.            #");
			// System.out.println("##################################################################");
			// System.out.println("current spamMap=" + spanMap);
			// }

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