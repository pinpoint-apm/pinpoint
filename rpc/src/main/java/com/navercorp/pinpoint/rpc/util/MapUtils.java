package com.nhn.pinpoint.rpc.util;

import java.util.Map;


/**
 * @author koo.taejin
 */
public class MapUtils {

	private MapUtils() {
	}

	public static String getString(Map<Object, Object> map, String key) {
		return getString(map, key, null);
	}

	public static String getString(Map<Object, Object> map, String key, String defaultValue) {
		if (map == null) {
			return defaultValue;
		}
		
		final Object value = map.get(key);
        if (value instanceof String) {
            return (String) value;
        }

		return null;	
	}


    public static Integer getInteger(Map<Object, Object> map, String key) {
        return getInteger(map, key, null);
    }

    public static Integer getInteger(Map<Object, Object> map, String key, Integer defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        final Object value = map.get(key);
        if (value instanceof Integer) {
            return (Integer) value;
        }

        return null;
    }
    
    public static Long getLong(Map<Object, Object> map, String key) {
    	return getLong(map, key, null);
    }

    public static Long getLong(Map<Object, Object> map, String key, Long defaultValue) {
        if (map == null) {
            return defaultValue;
        }

        final Object value = map.get(key);
        if (value instanceof Long) {
            return (Long) value;
        }

        return null;
    }

}
