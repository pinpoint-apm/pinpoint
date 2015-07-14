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

package com.navercorp.pinpoint.profiler.modifier.spring.beans;

import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import javassist.bytecode.AccessFlag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.modifier.Modifier;
import com.navercorp.pinpoint.profiler.modifier.method.interceptor.MethodInterceptor;

/**
 * 
 * @author netspider
 * @author emeroad
 * @author Jongho Moon <jongho.moon@navercorp.com>
 */
public class BeanMethodModifier implements Modifier {
    static final MethodFilter METHOD_FILTER = new MethodFilter() {
        private static final int REQUIRED_ACCESS_FLAG = AccessFlag.PUBLIC;
        private static final int REJECTED_ACCESS_FLAG = AccessFlag.ABSTRACT | AccessFlag.BRIDGE | AccessFlag.NATIVE | AccessFlag.PRIVATE |
                AccessFlag.PROTECTED | AccessFlag.SYNTHETIC | AccessFlag.STATIC;

        @Override
        public boolean accept(MethodInfo ctMethod) {
            if (ctMethod.isConstructor()) {
                return REJECT;
            }

            int access = ctMethod.getModifiers();

            return ((access & REQUIRED_ACCESS_FLAG) == REQUIRED_ACCESS_FLAG) && ((access & REJECTED_ACCESS_FLAG) == 0);
        }
    };

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final ByteCodeInstrumentor byteCodeInstrumentor;

    public BeanMethodModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
        this.byteCodeInstrumentor = byteCodeInstrumentor;
    }

    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modify {}", javassistClassName);
        }


        try {
            InstrumentClass clazz = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);

            if (!clazz.isInterceptable()) {
                return null;
            }

            List<MethodInfo> methodList = clazz.getDeclaredMethods(METHOD_FILTER);
            for (MethodInfo method : methodList) {
                if (logger.isTraceEnabled()) {
                    logger.trace("### c={}, m={}, params={}", javassistClassName, method.getName(), Arrays.toString(method.getParameterTypes()));
                }

                MethodInterceptor interceptor = new MethodInterceptor();
                interceptor.setServiceType(ServiceType.SPRING_BEAN);
                
                clazz.addInterceptor(method.getName(), method.getParameterTypes(), interceptor);
            }

            return clazz.toBytecode();
        } catch (Exception e) {
            logger.warn("modify fail. Cause:{}", e.getMessage(), e);
            return null;
        }
    }
}