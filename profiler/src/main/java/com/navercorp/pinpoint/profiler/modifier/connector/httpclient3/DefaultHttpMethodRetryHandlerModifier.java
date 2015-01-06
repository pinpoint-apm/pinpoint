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

package com.navercorp.pinpoint.profiler.modifier.connector.httpclient3;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

/**
 * @author Minwoo Jung
 */
public class DefaultHttpMethodRetryHandlerModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    public DefaultHttpMethodRetryHandlerModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }

        try {
            Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.connector.httpclient3.interceptor.RetryMethodInterceptor");

            InstrumentClass retryHandler = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            retryHandler.addInterceptor("retryMethod", new String[]{"org.apache.commons.httpclient.HttpMethod", "java.io.IOException", "int"}, interceptor);
            return retryHandler.toBytecode();
        } catch (Throwable e) {
            logger.warn("org.apache.commons.httpclient.DefaultHttpMethodRetryHandler modifier error. Caused:{}", e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getTargetClass() {
        return "org/apache/commons/httpclient/DefaultHttpMethodRetryHandler";
    }
}
