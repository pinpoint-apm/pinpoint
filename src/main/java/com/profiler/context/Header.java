package com.profiler.context;

public enum Header {

	HTTP_TRACE_ID("x-tracer-trace_id"),
	HTTP_TRACE_DEBUG("x-tracer-debug"),
	HTTP_TRACE_PARENT_SPAN_ID("x-tracer-parent_span_id");

	private String token;

	Header(String token) {
		this.token = token;
	}

	public String toString() {
		return token;
	}
}
