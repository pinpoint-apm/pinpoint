/**
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
package com.navercorp.pinpoint.plugin.spring.beans;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.Arrays;
import java.util.List;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.PinpointInstrument;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.PinpointClassFileTransformer;
import com.navercorp.pinpoint.bootstrap.interceptor.BasicMethodInterceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

/**
 * @author Jongho Moon
 *
 */
public class BeanMethodTransformer implements PinpointClassFileTransformer, SpringBeansConstants {
    private static final int REQUIRED_ACCESS_FLAG = Modifier.PUBLIC;
    private static final int REJECTED_ACCESS_FLAG = Modifier.ABSTRACT |  Modifier.NATIVE | Modifier.STATIC;
    private static final MethodFilter METHOD_FILTER = MethodFilters.modifier(REQUIRED_ACCESS_FLAG, REJECTED_ACCESS_FLAG);

    private final PLogger logger = PLoggerFactory.getLogger(getClass());
    
    
    /* (non-Javadoc)
     * @see com.navercorp.pinpoint.bootstrap.plugin.transformer.PinpointClassFileTransformer#transform(com.navercorp.pinpoint.bootstrap.plugin.PinpointInstrument, java.lang.ClassLoader, java.lang.String, java.lang.Class, java.security.ProtectionDomain, byte[])
     */
    @Override
    public byte[] transform(PinpointInstrument instrumentContext, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        if (logger.isInfoEnabled()) {
            logger.info("Modify {}", className);
        }

        try {
            InstrumentClass target = instrumentContext.getInstrumentClass(loader, className, classfileBuffer);

            if (!target.isInterceptable()) {
                return null;
            }

            List<InstrumentMethod> methodList = target.getDeclaredMethods(METHOD_FILTER);
            for (InstrumentMethod method : methodList) {
                if (logger.isTraceEnabled()) {
                    logger.trace("### c={}, m={}, params={}", new Object[] {className, method.getName(), Arrays.toString(method.getParameterTypes())});
                }

                method.addInterceptor(BasicMethodInterceptor.class.getName(), SERVICE_TYPE);
            }

            return target.toBytecode();
        } catch (Exception e) {
            logger.warn("modify fail. Cause:{}", e.getMessage(), e);
            return null;
        }
    }
}
