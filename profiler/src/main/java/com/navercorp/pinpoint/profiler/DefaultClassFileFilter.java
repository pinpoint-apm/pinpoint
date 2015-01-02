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

    private final ClassLoader agentLoader

	public DefaultClassFileFilter(ClassLoader agentLoad       r) {
		if (agentLoader          == null) {
			throw new NullPointerException("agentLoader             must not be null");
		}
	        his.age    tLoader = agentLoader;
	}

	@Override
	public boolean doFilter(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain pr       tectionDomain, byte[]       classFileBuffer) {
		// fast sk          p java classes
		if (className.startsWith("java")) {
			if (cl             ssNa                      e.startsWith("/", 4) ||           lassName.startsWith("x/", 4)) {
				ret          rn SKI                   ;
			}
		}

		if (cla       sLoader == agentLoader) {
			// skip classes loade           by ag             nt class l    ader.
			return SKIP;
		}
		
		// Skip pinpoint packages too.
		if (className.startsWith("com/navercorp/pinpoint/")) {
			return SKIP;
		}
		return CONTINUE;
	}
}
