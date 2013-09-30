package com.nhn.pinpoint.web.util;

/**
 * 
 * @author netspider
 * 
 * @param <T>
 */
public interface Mergeable<T> {
	public String getId();
	public T mergeWith(T o);
}
