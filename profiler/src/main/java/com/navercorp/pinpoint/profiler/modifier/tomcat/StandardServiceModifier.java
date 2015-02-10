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

package com.navercorp.pinpoint.profiler.modifier.tomcat;

import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.LifeCycleEventListener;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author cowboy93, netspider
 * @author hyungil.jeong
 */
public class StandardServiceModifier extends AbstractModifier {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Agent agent;

    public StandardServiceModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
        super(byteCodeInstrumentor, agent.getProfilerConfig());
        this.agent = agent;
    }

    public String getTargetClass() {
        return "org/apache/catalina/core/StandardService";
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
        logger.info("Modifying. {}", javassistClassName);
        
        try {
            InstrumentClass standardService = byteCodeInstrumentor.getClass(classLoader, javassistClassName, classFileBuffer);
            LifeCycleEventListener lifeCycleEventListener = new LifeCycleEventListener(agent);
            
            Interceptor standardServiceStartInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                    "com.navercorp.pinpoint.profiler.modifier.tomcat.interceptor.StandardServiceStartInterceptor",
                    new Object[] { lifeCycleEventListener }, new Class[] { LifeCycleEventListener.class });
            Interceptor standardServiceStopInterceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain,
                    "com.navercorp.pinpoint.profiler.modifier.tomcat.interceptor.StandardServiceStopInterceptor",
                    new Object[] { lifeCycleEventListener }, new Class[] { LifeCycleEventListener.class });

            boolean isHooked = false;
            // Tomcat 6 - org.apache.catalina.core.StandardService.start(), stop()
            if (standardService.hasDeclaredMethod("start", null) && standardService.hasDeclaredMethod("stop", null)) {
                standardService.addInterceptor("start", null, standardServiceStartInterceptor);
                standardService.addInterceptor("stop", null, standardServiceStopInterceptor);
                isHooked = true;
            }
            // Tomcat 7, 8 - org.apache.catalina.core.StandardService.startInternal(), stopInternal()
            else if (standardService.hasDeclaredMethod("startInternal", null) && standardService.hasDeclaredMethod("stopInternal", null)) {
                standardService.addInterceptor("startInternal", null, standardServiceStartInterceptor);
                standardService.addInterceptor("stopInternal", null, standardServiceStopInterceptor);
                isHooked = true;
            }
            
            if (isHooked) {
                logger.info("{} class is converted.", javassistClassName);
            } else {
                logger.warn("{} class not converted - start() or startInternal() method not found.", javassistClassName);
            }
            return standardService.toBytecode();
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn("modify fail. Cause:" + e.getMessage(), e);
            }
        }
        return null;
    }
}
