package com.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.profiler.context.tracer.DefaultTracer;
import com.profiler.context.tracer.Tracer;
import com.profiler.util.NamedThreadLocal;

/**
 * 
 * @author netspider
 * 
 */
public class Trace {

	private static final ThreadLocal<TraceID> traceId = new NamedThreadLocal<TraceID>("TraceId");
	private static final List<Tracer> tracers = new ArrayList<Tracer>();
	private static volatile boolean tracingEnabled = true;

	static {
		tracers.add(new DefaultTracer());
	}

	private Trace() {

	}

	public static TraceID getTraceId() {
		TraceID id = traceId.get();

		if (id == null) {
			id = TraceID.newTraceId();
			traceId.set(id);
			return id;
		}

		return id;
	}

	public static List<Tracer> getTracers() {
		return tracers;
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
		tracers.add(tracer);
	}

	public static void setTraceId(TraceID traceId) {
		Trace.traceId.set(traceId);
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