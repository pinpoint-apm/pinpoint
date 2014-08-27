package com.nhn.pinpoint.profiler.util;

/**
 * @author koo.taejin
 */
public final class ClassUtils {

	private ClassUtils() {
	}

	public static boolean isAssignableValue(Class<?> type, Object value) {
		if (type == null) {
			return false;
		}
		
		if (type.isPrimitive()) {
			return false;
		}
	
		if (value == null) {
			return false;
		}

		return type.isInstance(value);
	}

}
