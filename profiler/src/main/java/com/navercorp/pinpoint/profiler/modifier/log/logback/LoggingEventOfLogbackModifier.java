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
package com.navercorp.pinpoint.profiler.modifier.log.logback;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

/**
 * This modifier support slf4j 1.4.1 version and logback 0.9.8 version, or greater.
 * Because package name of MDC class is different on under those version 
 * and under those version is too old.
 * By the way slf4j 1.4.0 version release on May 2007.
 * Refer to url http://mvnrepository.com/artifact/org.slf4j/slf4j-api for detail.
 * 
 * @author minwoo.jung
 */
public class LoggingEventOfLogbackModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public LoggingEventOfLogbackModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    
    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifying. {}", javassistClassName);
        }
        
        try {
            InstrumentableClass mdcClass = byteCodeInstrumentor.getClass(classLoader, "org.slf4j.MDC", classFileBuffer);
            
            if (!mdcClass.hasMethod("put", new String[]{"java.lang.String", "java.lang.String"}, "void")) {
                logger.warn("modify fail. Because put method does not existed org.slf4j.MDC class.");
                return null;
            }
            if (!mdcClass.hasMethod("remove", new String[]{"java.lang.String"}, "void")) {
                logger.warn("modify fail. Because remove method does not existed org.slf4j.MDC class.");
                return null;
            }
        } catch (InstrumentException e) {
            logger.warn("modify fail. Because org.slf4j.MDC does not existed. Cause:" + e.getMessage(), e);
            return null;
        }
        
        try {
            InstrumentableClass loggingEvent = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            
            Interceptor interceptor1 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.log.logback.interceptor.LoggingEventOfLogbackInterceptor");
            loggingEvent.addConstructorInterceptor(new String[]{"java.lang.String", "ch.qos.logback.classic.Logger", "ch.qos.logback.classic.Level", "java.lang.String", "java.lang.Throwable", "java.lang.Object[]"}, interceptor1);

            Interceptor interceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.log.logback.interceptor.LoggingEventOfLogbackInterceptor");
            loggingEvent.addConstructorInterceptor(new String[]{}, interceptor2);
            
            return loggingEvent.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher("ch/qos/logback/classic/spi/LoggingEvent");
    }

}
