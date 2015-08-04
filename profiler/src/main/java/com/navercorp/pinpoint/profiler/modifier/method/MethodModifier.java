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

package com.navercorp.pinpoint.profiler.modifier.method;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author netspider
 * @author emeroad
 * 
 */
public class MethodModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public MethodModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher("*");
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }

        try {
            InstrumentClass clazz = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            if (!clazz.isInterceptable()) {
                return null;
            }

            List<InstrumentMethod> methodList = clazz.getDeclaredMethods(EmptyMethodFilter.FILTER);
            for (InstrumentMethod method : methodList) {
                final Interceptor interceptor = new MethodInterceptor();
                if (logger.isTraceEnabled()) {
                    logger.trace("### c={}, m={}, params={}", javassistClassName, method.getName(), Arrays.toString(method.getParameterTypes()));
                }
                clazz.addInterceptor(method.getName(), method.getParameterTypes(), interceptor);
            }

            return clazz.toBytecode();
        } catch (Exception e) {
            logger.warn("modify fail. Cause:{}", e.getMessage(), e);
            return null;
        }
    }
}