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

import com.navercorp.pinpoint.bootstrap.instrument.RetransformEventTrigger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * @author emeroad
 */
public class RetransformService implements RetransformEventTrigger {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final Instrumentation instrumentation;

    private ClassFileTransformerDispatcher classFileRetransformer;

    public RetransformService(Instrumentation instrumentation) {
        if (instrumentation == null) {
            throw new NullPointerException("instrumentation must not be null");
        }
        this.instrumentation = instrumentation;
    }

    @Override
    public void retransform(Class<?> target, ClassFileTransformer transformer) {
        if (this.logger.isDebugEnabled()) {
            logger.debug("retranform request class:{}", target.getName());
        }
        assertClass(target);

        this.classFileRetransformer.addRetransformEvent(target, transformer);

        triggerRetransform(target);

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

    public void bindRetransformEvent(ClassFileTransformerDispatcher classFileTransformerDispatcher) {
        if (classFileTransformerDispatcher == null) {
            throw new NullPointerException("classFileTransformerDispatcher must not be null");
        }
        this.classFileRetransformer = classFileTransformerDispatcher;
    }

}
