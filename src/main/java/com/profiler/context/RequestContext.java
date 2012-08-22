package com.profiler.context;

import com.profiler.util.NamedThreadLocal;

public class RequestContext {

	private static final ThreadLocal<Trace> trace = new NamedThreadLocal<Trace>("Trace");

	public static Trace getTrace(String traceID, String parentSpanID, String name, boolean debug) {
		Trace t = trace.get();
		if (t == null) {
			t = new Trace(traceID, parentSpanID, name, debug);
			trace.set(t);
		}
		return t;
	}

	/**
	 * Calling from Span.flush()
	 */
	public static void removeCurrentContext() {
		trace.remove();
	}
}
