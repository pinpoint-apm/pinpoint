package com.profiler.context;

import java.util.UUID;

public class IDFactory {

	public static String newTraceID() {
		UUID uuid = UUID.randomUUID();
		return uuid.toString();
	}
}
