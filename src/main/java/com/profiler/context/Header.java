package com.profiler.context;

public enum Header {

	HTTP_TRACE_ID("X-TRACER-TraceID"),
	HTTP_SPAN_ID("X-TRACER-SpanID"),
	HTTP_PARENT_SPAN_ID("X-TRACER-ParentSpanID"),
	HTTP_SAMPLED("X-TRACER-Sampled"),
	HTTP_FLAGS("X-TRACER-Flags");

	private String token;

	Header(String token) {
		this.token = token;
	}

	public String toString() {
		return token;
	}
}
