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

package com.navercorp.pinpoint.profiler;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import com.navercorp.pinpoint.bootstrap.instrument.RequestHandle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformRequestListener;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;
import com.navercorp.pinpoint.common.util.Assert;

/**
 * @author emeroad
 */
public class DynamicTransformService implements DynamicTransformTrigger {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Instrumentation instrumentation;

    private DynamicTransformRequestListener dynamicTransformRequestListener;

    public DynamicTransformService(Instrumentation instrumentation, DynamicTransformRequestListener listener) {
        Assert.requireNonNull(instrumentation, "instrumentation must not be null");
        Assert.requireNonNull(listener, "listener must not be null");

        this.instrumentation = instrumentation;
        this.dynamicTransformRequestListener = listener;
    }

    @Override
    public void retransform(Class<?> target, ClassFileTransformer transformer) {
        if (this.logger.isDebugEnabled()) {
            logger.debug("retransform request class:{}", target.getName());
        }
        assertClass(target);

        final RequestHandle requestHandle = this.dynamicTransformRequestListener.onRetransformRequest(target, transformer);
        boolean success = false;
        try {
            triggerRetransform(target);
            success = true;
        } finally {
            if (!success) {
                requestHandle.cancel();
            }
        }
    }
    
    @Override
    public void addClassFileTransformer(ClassLoader classLoader, String targetClassName, ClassFileTransformer transformer) {
        if (this.logger.isDebugEnabled()) {
            logger.debug("Add dynamic transform. classLoader={}, class={}", classLoader, targetClassName);
        }
        
        this.dynamicTransformRequestListener.onTransformRequest(classLoader, targetClassName, transformer);
    }

    private void assertClass(Class<?> target) {
        if (!instrumentation.isModifiableClass(target)) {
            throw new ProfilerException("Target class " + target + " is not modifiable");
        }
    }

    private void triggerRetransform(Class<?> target) {
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new ProfilerException(e);
        }
    }

    public void setTransformRequestEventListener(DynamicTransformRequestListener retransformEventListener) {
        if (retransformEventListener == null) {
            throw new NullPointerException("dynamicTransformRequestListener must not be null");
        }
        this.dynamicTransformRequestListener = retransformEventListener;
    }

}
