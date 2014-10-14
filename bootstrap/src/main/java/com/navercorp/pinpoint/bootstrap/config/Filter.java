package com.nhn.pinpoint.bootstrap.config;

/**
 * @author emeroad
 */
public interface Filter<T> {
	public static final boolean FILTERED = true;

	boolean filter(T value);
}
