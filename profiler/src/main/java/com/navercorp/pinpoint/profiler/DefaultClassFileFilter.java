package com.nhn.pinpoint.profiler;

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
		if (className.startsWith("com/nhn/pinpoint/")) {
			return SKIP;
		}
		return CONTINUE;
	}
}
