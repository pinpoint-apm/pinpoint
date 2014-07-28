package com.nhn.pinpoint.rpc.util;

import java.util.Map;


/**
 * @author koo.taejin
 */
public class MapUtils {

	private MapUtils() {
	}

	public static <T> T get(Map map, Object key, Class<T> clazz) {
		return get(map, key, clazz, null);
	}

	public static <T> T get(Map map, Object key, Class<T> clazz, T defaultValue) {
		if (!map.containsKey(key)) {
			return defaultValue;
		}

		Object value = map.get(key);
		if (ClassUtils.isAssignable(value.getClass(), clazz)) {
			return (T) value;
		}

		return null;
		
	}
	
}
