package com.profiler.context;

import com.profiler.util.NamedThreadLocal;

public class RequestContext {

	private static final ThreadLocal<Span> span = new NamedThreadLocal<Span>("Span");

	public static Span getSpan(String traceID, int parentSpanID, String name, boolean debug) {
		Span ctx = span.get();
		if (ctx == null) {
			ctx = new Span(traceID, parentSpanID, name, debug);
			span.set(ctx);
		}
		return ctx;
	}

	/**
	 * Calling from Span.flush()
	 */
	public static void removeCurrentContext() {
		span.remove();
	}
}
