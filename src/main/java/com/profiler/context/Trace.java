package com.profiler.context;

import java.util.ArrayList;
import java.util.List;

import com.profiler.context.tracer.Tracer;
import com.profiler.util.NamedThreadLocal;

/**
 * use by interceptors
 * 
 * @author netspider
 * 
 */
public class Trace {

	private static final ThreadLocal<TraceID> traceId = new NamedThreadLocal<TraceID>("TraceID");

	private static final List<Tracer> tracers = new ArrayList<Tracer>(1);

	private Trace() {

	}

	public static void addTracer(Tracer tracer) {
		tracers.add(tracer);
	}

	private static TraceID getTraceID() {
		TraceID id = traceId.get();
		if (id == null) {
			id = new TraceID(null, null, SpanID.newSpanID(), false, 0);
			traceId.set(id);
		}
		return traceId.get();
	}

	private static void record(Record record) {
		for (Tracer t : tracers) {
			t.record(record);
		}
	}

	public static void setTraceId(TraceID traceId) {
		Trace.traceId.set(traceId);
	}

	public static void record(Annotation annotation) {
		record(new Record(getTraceID(), System.currentTimeMillis(), annotation, null));
	}

	public static void record(Annotation annotation, long duration) {
		record(new Record(getTraceID(), System.currentTimeMillis(), annotation, duration));
	}

	public static void recordBinary(String key, Object value) {
		record(new Record(getTraceID(), System.currentTimeMillis(), new Annotation.BinaryAnnotation(key, value), null));
	}

	public static void record(String message) {
		record(new Record(getTraceID(), System.currentTimeMillis(), new Annotation.Message(message), null));
	}

	public static void recordRpcName(String service, String rpc) {
		record(new Record(getTraceID(), System.currentTimeMillis(), new Annotation.RpcName(service, rpc), null));
	}

	public static void recordClientAddr(String ip, int port) {
		record(new Record(getTraceID(), System.currentTimeMillis(), new Annotation.ClientAddr(ip, port), null));
	}

	public static void recordServerAddr(String ip, int port) {
		record(new Record(getTraceID(), System.currentTimeMillis(), new Annotation.ServerAddr(ip, port), null));
	}
}