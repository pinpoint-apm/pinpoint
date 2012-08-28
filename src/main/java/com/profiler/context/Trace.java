package com.profiler.context;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.profiler.context.tracer.Tracer;
import com.profiler.util.NamedThreadLocal;

/**
 * 
 * @author netspider
 * 
 */
public class Trace {

	private static final TraceID defaultId = new TraceID(null, null, SpanID.newSpanID(), true, 0);
	private static final ThreadLocal<State> local = new NamedThreadLocal<State>("State");
	// private static final TraceStack local = new TraceStack();
	private static volatile boolean tracingEnabled = true;

	private Trace() {
	}

	public static TraceID getTraceId() {
		State state = local.get();

		if (state == null || state.getId() == null) {
			return defaultId;
		}

		return state.getId();
	}

	public static boolean isTerminal() {
		State state = local.get();
		if (state == null)
			return false;
		return state.isTerminal();
	}

	public static List<Tracer> getTracers() {
		State state = local.get();
		if (state == null)
			return new ArrayList<Tracer>();
		return state.getTracers();
	}

	public static interface UnwindCallback {
		void callback();
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

	public static void addTracer(final Tracer tracer) {
		State state = local.get();

		if (state == null) {
			ArrayList<Tracer> tracers = new ArrayList<Tracer>();
			tracers.add(tracer);
			state = new State(null, false, tracers);

			local.set(state);
		} else {
			state.addTracer(tracer);
		}
	}

	public static TraceID setTraceId(TraceID traceId) {
		State state = local.get();

		if (state == null) {
			local.set(new State(traceId, false, getTracers()));
		} else {
			state.setId(traceId);
			state.setTerminal(false);
		}

		return traceId;
	}

	private static void record(Record record) {
		if (!tracingEnabled)
			return;

		List<Tracer> tracers = getTracers();

		for (Tracer t : tracers) {
			t.record(record);
		}
	}

	public static void record(Annotation annotation) {
		record(new Record(getTraceId(), System.currentTimeMillis(), annotation, null));
	}

	public static void record(Annotation annotation, long duration) {
		record(new Record(getTraceId(), System.currentTimeMillis(), annotation, duration));
	}

	public static void recordBinary(String key, Object value) {
		record(new Record(getTraceId(), System.currentTimeMillis(), new Annotation.BinaryAnnotation(key, value), null));
	}

	public static void record(String message) {
		record(new Record(getTraceId(), System.currentTimeMillis(), new Annotation.Message(message), null));
	}

	public static void recordRpcName(String service, String rpc) {
		record(new Record(getTraceId(), System.currentTimeMillis(), new Annotation.RpcName(service, rpc), null));
	}

	public static void recordClientAddr(String ip, int port) {
		record(new Record(getTraceId(), System.currentTimeMillis(), new Annotation.ClientAddr(ip, port), null));
	}

	public static void recordServerAddr(String ip, int port) {
		record(new Record(getTraceId(), System.currentTimeMillis(), new Annotation.ServerAddr(ip, port), null));
	}
}