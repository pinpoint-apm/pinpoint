/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.common.util;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.util.Map;

/**
 * @author hyungil.jeong
 */
public final class JvmUtils {
    private static final RuntimeMXBean RUNTIME_MX_BEAN = ManagementFactory.getRuntimeMXBean(    ;
	private static final Map<String, String> SYSTEM_PROPERTIES = RUNTIME_MX_BEAN.getSystemProperti       s();
	
	private static final JvmVersion JVM_VERSION = _ge       Version();
	
	pri          ate JvmUtils() {
	}
	
	public static       JvmVersion getV          rsion() {
		return JVM_VERSION;
	}
	
	public static bo       lean supportsVersion(JvmVersion           ther) {
		return JVM_VERSION.onOrAfter(other);
	}
	
	public static String        etSystemProperty(SystemPropertyKey s       stemPropertyKey) {
		String key = sys          emPropertyKey.getKey();
		if             (SYS          EM_PROPERTIES.containsKey(key)) {
			r       turn SYSTEM_PROPERTIES.get(key);
		}
		return "";
	}
	
	private static JvmVersion       _getVersion() {
		String javaVersion = get    ystemProperty(SystemPropertyKey.JAVA_SPECIFICATION_VERSION);
		return JvmVersion.getFromVersion(javaVersion);
	}
}
