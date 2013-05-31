package com.nhn.pinpoint.context;

public enum Header {

	HTTP_TRACE_ID("X-TRACER-TraceID"),
	HTTP_SPAN_ID("X-TRACER-SpanID"),
	HTTP_PARENT_SPAN_ID("X-TRACER-ParentSpanID"),
	HTTP_SAMPLED("X-TRACER-Sampled"),
	HTTP_FLAGS("X-TRACER-Flags"),
	HTTP_PARENT_APPLICATION_NAME("X-TRACER-ParentAppName"),
	HTTP_PARENT_APPLICATION_TYPE("X-TRACER-ParentAppType");

	private String token;

	Header(String token) {
		this.token = token;
	}

	public String toString() {
		return token;
	}
}
