package com.profiler.context;

import java.util.UUID;

public class SpanID {
	public static final String ROOT_SPAN_ID = null;

	public static String newSpanID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
