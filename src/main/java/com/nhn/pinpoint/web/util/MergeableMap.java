package com.nhn.pinpoint.web.util;

import java.util.Map;

/**
 * 
 * @author netspider
 * 
 * @param <K>
 * @param <V>
 */
public interface MergeableMap<K, V extends Mergeable<K, V>> extends Map<K, V> {
	public V putOrMerge(K key, V value);
}
