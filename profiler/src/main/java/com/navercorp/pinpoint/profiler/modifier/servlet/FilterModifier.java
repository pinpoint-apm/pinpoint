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

package com.navercorp.pinpoint.profiler.modifier.servlet;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * 
 */
public class FilterModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	public FilterModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent age       t) {
		super(byteCodeInstrument        , agent);
	}

	public String g       tTargetClass() {
		return         avax/servlet/Filter";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDoma       n, byte[] classFileBuffer           {
		if (logger.isInfoEnabled()) {
			logg                       .info("Modifing. {}", javassistClassName);
		}

		try {          			Interceptor doFilterIntercept          r = new MethodInterceptor();

//			setTraceContext(doFilterInterceptor);

			InstrumentClass servlet            byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

			servlet.addInterceptor("doFilter", new String[] { "javax.servlet.ServletRequ          st", "javax.servlet.Se       vletResponse", "javax.servlet          FilterChain" }, doFilterInterceptor);

			return s          rvlet.          oBytecode();
		} catch (InstrumentException e) {
			logger.warn("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}