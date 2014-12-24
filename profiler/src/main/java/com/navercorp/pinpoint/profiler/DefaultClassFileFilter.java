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

package com.navercorp.pinpoint.profiler;

import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
public class DefaultClassFileFilter implements ClassFileFilter {

	private final ClassLoader agentLoader;

	public DefaultClassFileFilter(ClassLoader agentLoader) {
		if (agentLoader == null) {
			throw new NullPointerException("agentLoader must not be null");
		}
		this.agentLoader = agentLoader;
	}

	@Override
	public boolean doFilter(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classFileBuffer) {
		// fast java class skip
		if (className.startsWith("java")) {
			if (className.startsWith("/", 4) || className.startsWith("x/", 4)) {
				return SKIP;
			}
		}

		if (classLoader == agentLoader) {
			// agent의 clssLoader에 로드된 클래스는 스킵한다.
			return SKIP;
		}
		// 자기 자신의 패키지도 제외
		// 향후 패키지명 변경에 의해 코드 변경이 필요함.
		if (className.startsWith("com/navercorp/pinpoint/")) {
			return SKIP;
		}
		return CONTINUE;
	}
}
