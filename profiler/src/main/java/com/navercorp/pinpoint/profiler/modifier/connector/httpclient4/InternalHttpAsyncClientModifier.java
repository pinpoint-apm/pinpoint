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

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
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

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public InternalHttpAsyncClientModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher("org/apache/http/impl/nio/client/InternalHttpAsyncClient");
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {} @ {}", javassistClassName, classLoader);
        }

        try {
            InstrumentClass aClass = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            Interceptor internalExecuteInterceptor = byteCodeInstrumentor.newInterceptor(classLoader,
                    protectedDomain,
                    "com.navercorp.pinpoint.profiler.modifier.connector.httpclient4.interceptor.AsyncInternalClientExecuteInterceptor");

            String[] internalExecuteParams = new String[] {
                    "org.apache.http.nio.protocol.HttpAsyncRequestProducer",
                    "org.apache.http.nio.protocol.HttpAsyncResponseConsumer",
                    "org.apache.http.protocol.HttpContext",
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
