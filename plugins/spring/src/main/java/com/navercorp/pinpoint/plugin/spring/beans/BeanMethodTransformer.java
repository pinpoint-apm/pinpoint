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
package com.navercorp.pinpoint.plugin.spring.beans;

import java.lang.reflect.Modifier;
import java.security.ProtectionDomain;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLogger;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;

import static com.navercorp.pinpoint.common.util.VarArgs.va;

/**
 * @author Jongho Moon
 *
 */
public class BeanMethodTransformer implements TransformCallback {
    private static final int REQUIRED_ACCESS_FLAG = Modifier.PUBLIC;
    private static final int REJECTED_ACCESS_FLAG = Modifier.ABSTRACT | Modifier.NATIVE | Modifier.STATIC;
    private static final MethodFilter METHOD_FILTER = MethodFilters.modifier(REQUIRED_ACCESS_FLAG, REJECTED_ACCESS_FLAG);

    private final PLogger logger = PLoggerFactory.getLogger(getClass());

    private final Object lock = new Object();
    private final AtomicInteger interceptorId = new AtomicInteger(-1);

    private final boolean markError;

    public BeanMethodTransformer(boolean markError) {
        this.markError = markError;
    }

    @Override
    public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
        try {
            final InstrumentClass target = instrumentor.getInstrumentClass(loader, className, classfileBuffer);
            if (!target.isInterceptable()) {
                return null;
            }

            final List<InstrumentMethod> methodList = target.getDeclaredMethods(METHOD_FILTER);
            for (InstrumentMethod method : methodList) {
                addInterceptor(method);
            }

            return target.toBytecode();
        } catch (Exception e) {
            if(logger.isWarnEnabled()) {
                logger.warn("Failed to spring beans modify. Cause:{}", e.getMessage(), e);
            }

            return null;
        }
    }

    private void addInterceptor(InstrumentMethod targetMethod) throws InstrumentException {
        int id = interceptorId.get();

        if (id != -1) {
            targetMethod.addInterceptor(id);
            return;
        }

        synchronized (lock) {
            id = interceptorId.get();
            if (id != -1) {
                targetMethod.addInterceptor(id);
                return;
            }

            id = targetMethod.addInterceptor("com.navercorp.pinpoint.plugin.spring.beans.interceptor.BeanMethodInterceptor", va(markError));
            interceptorId.set(id);
        }
    }
}