package com.profiler.logging;

public enum LogLevel {
	INFO(0), DEBUG(1), WARN(2), ERROR(3), FATAL(4);

	int priority;

	LogLevel(int priority) {
		this.priority = priority;
	}
}
