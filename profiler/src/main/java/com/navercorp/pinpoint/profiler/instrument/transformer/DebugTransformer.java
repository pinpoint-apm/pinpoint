/*
 * Copyright 2014 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Arrays;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;

/**
 * @author Jongho Moon
 *
 */
public class DebugTransformer implements ClassFileTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final InstrumentContext instrumentContext;
    private final InstrumentEngine instrumentEngine;

    public DebugTransformer(InstrumentEngine instrumentEngine, InstrumentContext instrumentContext) {
        if (instrumentEngine == null) {
            throw new NullPointerException("instrumentEngine must not be null");
        }
        if (instrumentContext == null) {
            throw new NullPointerException("instrumentContext must not be null");
        }
        this.instrumentEngine = instrumentEngine;
        this.instrumentContext = instrumentContext;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            final InstrumentClass target = instrumentEngine.getClass(instrumentContext, loader, className, classfileBuffer);
            if (target == null) {
                if (logger.isWarnEnabled()) {
                    logger.warn("targetClass not found. className:{}, classBeingRedefined:{} :{} ", className, classBeingRedefined, loader);
                }
                // throw exception ?
                return null;
            }
            
            if (!target.isInterceptable()) {
                return null;
            }
    
            for (InstrumentMethod method : target.getDeclaredMethods(MethodFilters.ACCEPT_ALL)) {
                if (logger.isTraceEnabled()) {
                    logger.trace("### c={}, m={}, params={}", className, method.getName(), Arrays.toString(method.getParameterTypes()));
                }
                
                method.addInterceptor(BasicMethodInterceptor.class.getName());
            }
    
            return target.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("Failed to instrument " + className, e);
            return null;
        }
    }
    
    
}
