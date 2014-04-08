package com.nhn.pinpoint.bootstrap.context;

/**
 * @author emeroad
 */
public enum Header {

	HTTP_TRACE_ID("Pinpoint-TraceID"),
	HTTP_SPAN_ID("Pinpoint-SpanID"),
	HTTP_PARENT_SPAN_ID("Pinpoint-pSpanID"),
	HTTP_SAMPLED("Pinpoint-Sampled"),
	HTTP_FLAGS("Pinpoint-Flags"),
	HTTP_PARENT_APPLICATION_NAME("Pinpoint-pAppName"),
	HTTP_PARENT_APPLICATION_TYPE("Pinpoint-pAppType");

	private String token;

	Header(String token) {
		this.token = token;
	}

	public String toString() {
		return token;
	}
}
