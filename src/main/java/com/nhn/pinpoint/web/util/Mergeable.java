package com.nhn.pinpoint.web.util;

/**
 * 
 * @author netspider
 * @param <K>
 * @param <V>
 */
public interface Mergeable<K, V> {
	public K getId();
	public V mergeWith(V o);
}
