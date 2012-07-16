package com.profiler.logging;

public abstract class Logger {

	protected final String name;

    protected final ThreadSafeSimpleDateFormat df = new ThreadSafeSimpleDateFormat();

	public Logger(String name) {
		this.name = name;
	}

	public static Logger getLogger(Class<?> clazz) {
		return new DefaultLogger(clazz.getName());
	}

	public abstract void info(String message, Object... args);

	public abstract void debug(String message, Object... args);

	public abstract void warn(String message, Object... args);

	public abstract void error(String message, Object... args);

	public abstract void fatal(String message, Object... args);

	public abstract boolean isDebugEnabled();
}
