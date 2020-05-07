/*
 * Copyright 2017 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.config.Filter;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;

import java.lang.instrument.ClassFileTransformer;

/**
 * @author Woonduk Kang(emeroad)
 */
public class DebugTransformerRegistry implements TransformerRegistry {

    private final Filter<String> debugTargetFilter;
    private final DebugTransformer debugTransformer;

    public DebugTransformerRegistry(Filter<String> debugTargetFilter, DebugTransformer debugTransformer) {
        this.debugTargetFilter = Assert.requireNonNull(debugTargetFilter, "debugTargetFilter");
        this.debugTransformer = Assert.requireNonNull(debugTransformer, "debugTransformer");
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer) {
        return findTransformer(classLoader, classInternalName, classFileBuffer, null);
    }

    @Override
    public ClassFileTransformer findTransformer(ClassLoader classLoader, String classInternalName, byte[] classFileBuffer, InternalClassMetadata classMetadata) {
        if (classInternalName == null) {
            throw new NullPointerException("classInternalName");
        }
        if (this.debugTargetFilter.filter(classInternalName)) {
            // Added to see if call stack view is OK on a test machine.
            return debugTransformer;
        }
        return null;
    }
}