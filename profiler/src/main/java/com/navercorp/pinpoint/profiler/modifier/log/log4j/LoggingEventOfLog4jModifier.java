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
package com.navercorp.pinpoint.profiler.modifier.log.log4j;

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
 * @author minwoo.jung
 */
public class LoggingEventOfLog4jModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public LoggingEventOfLog4jModifier(ByteCodeInstrumentor byteCodeInstrumentor,Agent agent) {
        super(byteCodeInstrumentor, agent);
    }

    
    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        if (logger.isInfoEnabled()) {
            logger.info("Modifing. {}", javassistClassName);
        }


        try {
            InstrumentClass loggingEvent = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            
            Interceptor interceptor1 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.log.log4j.interceptor.LoggingEventInterceptor");
            loggingEvent.addConstructorInterceptor(new String[]{"java.lang.String", "org.apache.log4j.Category", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"}, interceptor1);
            
            Interceptor interceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.log.log4j.interceptor.LoggingEventInterceptor");
            loggingEvent.addConstructorInterceptor(new String[]{"java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Priority", "java.lang.Object", "java.lang.Throwable"}, interceptor2);
            
            Interceptor interceptor3 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.navercorp.pinpoint.profiler.modifier.log.log4j.interceptor.LoggingEventInterceptor");
            loggingEvent.addConstructorInterceptor(new String[]{"java.lang.String", "org.apache.log4j.Category", "long", "org.apache.log4j.Level", "java.lang.Object", "java.lang.String", "org.apache.log4j.spi.ThrowableInformation", "java.lang.String", "org.apache.log4j.spi.LocationInfo", "java.util.Map"}, interceptor3);
            
            return loggingEvent.toBytecode();
        } catch (InstrumentException e) {
            logger.warn("modify fail. Cause:" + e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getTargetClass() {
        return "org/apache/log4j/spi/LoggingEvent";
    }

}
