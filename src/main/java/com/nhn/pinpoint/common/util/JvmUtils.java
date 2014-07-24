package com.nhn.pinpoint.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

import com.nhn.pinpoint.common.SystemPropertyKey;
import com.nhn.pinpoint.common.JvmVersion;

/**
 * @author hyungil.jeong
 */
public class JvmUtils {
	private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean();
	private static final Map<String, String> SYSTEM_PROPERTIES = RUNTIME_MX_BEAN.getSystemProperties();
	
	private JvmUtils() {
		throw new IllegalAccessError();
	}
	
	public static JvmVersion getVersion() {
		String javaVersion = getSystemProperty(SystemPropertyKey.JAVA_SPECIFICATION_VERSION);
		return JvmVersion.getFromVersion(javaVersion);
	}
	
	public static boolean supportsVersion(JvmVersion other) {
		String javaVersion = getSystemProperty(SystemPropertyKey.JAVA_SPECIFICATION_VERSION);
		return JvmVersion.getFromVersion(javaVersion).onOrAfter(other);
	}
	
	public static String getSystemProperty(SystemPropertyKey systemPropertyKey) {
		String key = systemPropertyKey.getKey();
		if (SYSTEM_PROPERTIES.containsKey(key)) {
			return SYSTEM_PROPERTIES.get(key);
		}
		return "";
	}
}
