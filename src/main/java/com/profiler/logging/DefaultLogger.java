package com.profiler.logging;

import java.util.Date;

import com.profiler.config.TomcatProfilerConfig;

public class DefaultLogger extends Logger {

	public DefaultLogger(String name) {
		super(name);
	}

	@Override
	public void info(String message, Object... args) {
		if (TomcatProfilerConfig.LOG_LEVEL.priority >= LogLevel.INFO.priority) {
			System.out.printf("[HIPPO] %s [INFO] [%s] %s \n", df.format(new Date()), name, String.format(message, args));
		}
	}

	@Override
	public void debug(String message, Object... args) {
		if (TomcatProfilerConfig.LOG_LEVEL.priority >= LogLevel.DEBUG.priority) {
			System.out.printf("[HIPPO] %s [DEBUG] [%s] %s \n", df.format(new Date()), name, String.format(message, args));
		}
	}

	@Override
	public void warn(String message, Object... args) {
		if (TomcatProfilerConfig.LOG_LEVEL.priority >= LogLevel.WARN.priority) {
			System.out.printf("[HIPPO] %s [WARN] [%s] %s \n", df.format(new Date()), name, String.format(message, args));
		}
	}

	@Override
	public void error(String message, Object... args) {
		if (TomcatProfilerConfig.LOG_LEVEL.priority >= LogLevel.ERROR.priority) {
			System.out.printf("[HIPPO] %s [ERROR] [%s] %s \n", df.format(new Date()), name, String.format(message, args));
		}
	}

	@Override
	public void fatal(String message, Object... args) {
		if (TomcatProfilerConfig.LOG_LEVEL.priority >= LogLevel.FATAL.priority) {
			System.out.printf("[HIPPO] %s [FATAL] [%s] %s \n", df.format(new Date()), name, String.format(message, args));
		}
	}

	@Override
	public boolean isDebugEnabled() {
		return TomcatProfilerConfig.LOG_LEVEL.priority >= LogLevel.DEBUG.priority;
	}
}
