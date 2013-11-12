package com.nhn.pinpoint.web.util;

import java.util.HashMap;
import java.util.Map;


/**
 * 
 * @author netspider
 * 
 * @param <K>
 * @param <V>
 */
public class TemporaryMergeableCache<K, V extends Mergeable<K, V>> {
	private final Map<K, V> cache = new HashMap<K, V>();

	public V get(K key) {
		return cache.get(key);
	}

	public void add(K key, V value) {
		if (cache.containsKey(key)) {
			V v = cache.get(key);
			if (v != null) {
				replace(key, v.mergeWith(value));
			} else {
				replace(key, value);
			}
		} else {
			replace(key, value);
		}
	}

	public void replace(K key, V value) {
		cache.put(key, value);
	}
}
