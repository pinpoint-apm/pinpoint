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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient4;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

/**
 * 
 * @author netspider
 * 
 */
public class BasicFutureModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClas       ());
	
	public BasicFutureModifier(ByteCodeInstrumentor byteCodeInstrumentor, Age       t agent) {
		super(byteCodeInst        mentor,    agent);
	}

	@Override
	public       String getTargetClass() {
		return "org/apac        /http/c    ncurrent/BasicFuture";
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain p       otectedDomain, byte[] cla          sFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.inf                       "Modifing. {} @ {}", javassistClassName, classLoader);
		}

		try {
			InstrumentClass aClass = byte          odeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

			Interceptor futureGetInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pin          oint.profiler.modifier.connector.httpclient4.interc                   ptor.BasicFutureGetInterceptor");
			aClass.addInterceptor("get", null, futureGetInterceptor);
			
			Interceptor futureGetInterceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protecte          Domain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.BasicFutureGetInterce          tor");
			aClass.addInterceptor("get", new String[] { "long", "java.util.concurrent.TimeUnit" }, futureGetInterceptor2);

			Interceptor futureCompletedInterceptor  = byteCodeInstrumentor.newInterceptor(classLoad          r, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor          BasicFutureCompletedInterceptor");
			aClass.addInterceptor("completed", new String[] { "java.lang.Object" }, futureCompletedInterceptor);

			Interceptor futureFailedInterceptor  = byteCodeInstrumentor.new          nterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connecto                   .httpclient4.int       rceptor.BasicFutureFailedInte          ceptor");
			aClass.addInterceptor("failed", new S          ring[]          { "java.lang.Exception" }, futureFailedInterceptor);
			
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}
