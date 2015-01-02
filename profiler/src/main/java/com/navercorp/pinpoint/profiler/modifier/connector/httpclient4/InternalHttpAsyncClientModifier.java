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
@Deprecated
public class InternalHttpAsyncClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	public InternalHttpAsyncClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent age       t) {
		super(byteCodeInstrument        , agent    ;
	}

	@Override
	public Strin        getTargetClass() {
		return "org/apache/http/impl/nio/client        nternal    ttpAsyncClient";
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protect       dDomain, byte[] classFile          uffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Mod                       ing. {} @ {}", javassistClassName, classLoader);
		}

		try {
			InstrumentClass aClass = byteCodeIn          trumentor.getClass(classLoader, javassistClassName, classFileBuffer);

			Intercept                r inte                nalExecuteInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,
					protectedDomain,
					"com.n                   vercorp.pinpoint.profiler.modifier.co                nector.httpclient4.interceptor.AsyncInternalCli                ntExecuteInterceptor");
			
			String[] internal                xecuteParams = new String[] {                 					"org.apache.http.nio.protoco                         .HttpAsyncRequestProducer",
					"org.apache.http.nio.protocol.HttpAsy                   cResponseConsume       ",
					"org.apache.http.pro          ocol.HttpContext",
					"org.apache.http.concurre          t.Futu          eCallback"
					};
			aClass.addInterceptor("execute", internalExecuteParams, internalExecuteInterceptor);
			
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}
