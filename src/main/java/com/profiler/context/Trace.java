package com.profiler.context;

import com.profiler.context.gen.Annotation;
import com.profiler.context.gen.BinaryAnnotation;
import com.profiler.context.gen.Span;

/**
 * 
 * @author netspider
 * 
 */
public class Trace {

	private static final int NO_PARENT_SPAN_ID = -1;

	private final String traceID;

	private final Span span;

	Trace(String traceID, String parentSpanID, String name, boolean debug) {
		this.traceID = (traceID == null) ? TraceID.newTraceID() : traceID;

		this.span = new Span();
		this.span.setTraceID(this.traceID);
		this.span.setSpanID(SpanID.newSpanID());
		this.span.setParentSpanId(parentSpanID);
		this.span.setName(name);
		this.span.setDebug(debug);
	}

	public void record(Annotation annotation) {
		span.addToAnnotations(annotation);
	}

	public void recordBinary(BinaryAnnotation annotation) {
		span.addToBinaryAnnotations(annotation);
	}
}
