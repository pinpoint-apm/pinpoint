package com.profiler;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.profiler.config.TomcatProfilerConfig;

public abstract class Logger {

	protected final String name;

	protected final DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	public Logger(String name) {
		this.name = name;
	}

	public enum LogLevel {
		INFO(0), DEBUG(1), WARN(2), ERROR(3), FATAL(4);

		private int priority;

		LogLevel(int priority) {
			this.priority = priority;
		}
	}

	public static Logger getLogger(Class<?> clazz) {
		return new Logger(clazz.getName()) {
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
		};
	}

	public abstract void info(String message, Object... args);

	public abstract void debug(String message, Object... args);

	public abstract void warn(String message, Object... args);

	public abstract void error(String message, Object... args);

	public abstract void fatal(String message, Object... args);

}
