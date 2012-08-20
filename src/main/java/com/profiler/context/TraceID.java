package com.profiler.context;

import java.util.UUID;

public class TraceID {

	public static final String EMPTY = null;

	public static String newTraceID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
