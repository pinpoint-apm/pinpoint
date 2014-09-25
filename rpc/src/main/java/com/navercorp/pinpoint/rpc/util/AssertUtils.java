package com.nhn.pinpoint.rpc.util;

/**
 * @author koo.taejin
 */
public final class AssertUtils {

	private AssertUtils() {
	}

	public static void assertNotNull(Object object) {
		assertNotNull(object, "Object may not be null.");
	}
	
	public static void assertNotNull(Object object, String message) {
		if (object == null) {
			throw new NullPointerException(message);
		}
	}
	
	public static <T extends Throwable> void assertNotNull(Object object, T throwable) throws T {
		if (object == null) {
			throw throwable;
		}
	}
	
}
