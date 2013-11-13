package com.nhn.pinpoint.web.util;

import java.util.HashMap;

/**
 * 
 * @author netspider
 * 
 * @param <K>
 * @param <V>
 */
public class MergeableHashMap<K, V extends Mergeable<K, V>> extends HashMap<K, V> implements MergeableMap<K, V> {

	private static final long serialVersionUID = -8474027588052874209L;

	public V putOrMerge(K key, V value) {
		final V find = get(key);
		if (find == null) {
			put(key, value);
		} else {
            find.mergeWith(value);
		}
		return find;
	}
}
