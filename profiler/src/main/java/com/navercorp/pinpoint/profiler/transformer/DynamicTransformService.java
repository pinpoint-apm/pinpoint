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

package com.navercorp.pinpoint.profiler.transformer;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

import com.navercorp.pinpoint.bootstrap.instrument.RequestHandle;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.profiler.ProfilerException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformRequestListener;
import com.navercorp.pinpoint.bootstrap.instrument.DynamicTransformTrigger;

import com.navercorp.pinpoint.common.util.Assert;

import static com.navercorp.pinpoint.common.util.JvmVersion.JAVA_8;

/**
 * @author emeroad
 */
public class DynamicTransformService implements DynamicTransformTrigger {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Instrumentation instrumentation;

    private DynamicTransformRequestListener dynamicTransformRequestListener;

    public DynamicTransformService(Instrumentation instrumentation, DynamicTransformRequestListener listener) {
        this.instrumentation = Assert.requireNonNull(instrumentation, "instrumentation");
        this.dynamicTransformRequestListener = Assert.requireNonNull(listener, "listener");
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
        final JvmVersion version = JvmUtils.getVersion();
        if (JAVA_8.compareTo(version) == 0) {
            // If the version is java 8
            // Java 8 bug - NoClassDefFound error in transforming lambdas(https://bugs.openjdk.java.net/browse/JDK-8145964)
            final String className = target.getName();
            if (className != null && className.contains("$$Lambda$")) {
                throw new ProfilerException("Target class " + target + " is lambda class, Causes NoClassDefFound error in java 8.");
            }
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
            throw new NullPointerException("dynamicTransformRequestListener");
        }
        this.dynamicTransformRequestListener = retransformEventListener;
    }

}
