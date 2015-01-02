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
 * For HTTP Client 4.3 or later.
 * 
 * @author netspider
 * 
 */
public class ClosableHttpAsyncClientModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	public ClosableHttpAsyncClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent age       t) {
		super(byteCodeInstrument        , agent    ;
	}

	@Override
	public Strin        getTargetClass() {
		return "org/apache/http/impl/nio/client/        oseable    ttpAsyncClient";
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protect       dDomain, byte[] classFile          uffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Mod             f          ng. {} @ {}", javassistClassName, classLoader);
		}


		try {
			InstrumentClass aClass = byteCodeIns                   rumentor.getClass(classLoader, javassistClassName, classFileBuffer);
 
			/**
	                   	 * Below two methods are overloaded, but they don't call each other. No                 cope r                quired.
			 */
			Interceptor executeInterceptor = byteCodeInstrumentor.newInterceptor(classLoader
					protectedDomain,
					"                om.navercorp.pinpo                nt.profiler.modifier.                onnector.httpclient4.intercept                r.AsyncClientExecuteInterceptor")
			
			String[] executeParams = new String[] { 
					"                                              rg.apache.http.HttpHost",
					"org.apache.http.HttpRequest", 
					"or                .apach                .http.protocol.HttpContext",
					"org.apache.http.concurrent.FutureCallback"
					};
			aClass.addInterc                   ptor("execute", executeParams, execut                Interceptor);
			
			/**
			 * 
			 */
			Inter                eptor internalExecuteInterceptor = byteCodeInstr                mentor.newInterceptor(classLoader
					protectedDomain,
					"com.navercorp.pinpoint.profiler.modifier.c                   nnector.httpclie       t4.interceptor.AsyncInternalC          ientExecuteInterceptor");
			
			String[] internal          xecute          arams = new String[] {
					"org.apache.http.nio.protocol.HttpAsyncRequestProducer", 
					"org.apache.http.nio.protocol.HttpAsyncResponseConsumer", 
					"org.apache.http.concurrent.FutureCallback"
					};
			aClass.addInterceptor("execute", internalExecuteParams, internalExecuteInterceptor);
			
			return aClass.toBytecode();
		} catch (InstrumentException e) {
			logger.info("modify fail. Cause:{}", e.getMessage(), e);
			return null;
		}
	}
}
