/*
 * Copyright 2018 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;

import java.lang.instrument.ClassFileTransformer;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Woonduk Kang(emeroad)
 */
public class PredefinedTransformerRegistry implements TransformerRegistry {

    private volatile boolean fastSkipPath = true;

    private final Map<String, ClassFileTransformer> registry = new ConcurrentHashMap<String, ClassFileTransformer>();

    public PredefinedTransformerRegistry() {

    }

    public void addRegistry(String className, ClassFileTransformer classFileTransformer) {
        registry.put(className, classFileTransformer);
        fastSkipPath = false;
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        return findTransformer(classLoader, classInternalName, classFileBuffer, null);
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer, InternalClassMetadata classMetadata) {
        if (classInternalName == null) {
            return null;
        }
        if (fastSkipPath) {
            return null;
        }

        final ClassFileTransformer classFileTransformer = this.registry.remove(classInternalName);
        if (classFileBuffer != null) {
            if (this.registry.isEmpty()) {
                fastSkipPath = true;
            }
        }
        return classFileTransformer;
    }

}
