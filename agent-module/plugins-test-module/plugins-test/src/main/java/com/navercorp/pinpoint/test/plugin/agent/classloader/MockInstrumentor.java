/*
 * Copyright 2016 NAVER Corp.
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
 *
 */

package com.navercorp.pinpoint.test.plugin.agent.classloader;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.lang.instrument.ClassFileTransformer;
import java.util.Objects;

/**
 * @author Woonduk Kang(emeroad)
 */
public class MockInstrumentor {
    private final Logger logger = LogManager.getLogger(this.getClass());
    private final ClassLoader loader;
    private final ClassFileTransformer dispatcher;

    public MockInstrumentor(ClassLoader loader, ClassFileTransformer defaultTransformer) {
        this.loader = loader;
        this.dispatcher = Objects.requireNonNull(defaultTransformer, "defaultTransformer");
    }

    public byte[] transform(ClassLoader classLoader, String className, byte[] classfileBuffer) {
        final String classInternalName = JavaAssistUtils.javaNameToJvmName(className);
        try {
            final byte[] transformBytes = dispatcher.transform(classLoader, classInternalName, null, null, classfileBuffer);
            if (transformBytes != null) {
                return transformBytes;
            }
            return null;
        } catch (Throwable th) {
            throw new RuntimeException(className + " transform fail", th);
        }
    }
}
