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
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.profiler.modifier.Modifier;

public class ClassFileRetransformer implements ClassFileTransformer {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    
    private final Instrumentation instrumentation;
    
    private final ConcurrentHashMap<Class<?>, Modifier> targets = new ConcurrentHashMap<Class<?>, Modifier>();
    private final ConcurrentHashMap<Class<?>, ClassFileTransformer> transformerMap = new ConcurrentHashMap<Class<?>, ClassFileTransformer>();
    
    public ClassFileRetransformer(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (classBeingRedefined == null) {
            return null;
        }

        final Modifier modifier = targets.remove(classBeingRedefined);
        if (modifier != null) {
            try {
                return modifier.modify(loader, className.replace('/', '.'), protectionDomain, classfileBuffer);
            } catch (Throwable t) {
                logger.warn("Failed to retransform " + className + " with " + modifier);
                return null;
            }
        }

        final ClassFileTransformer transformer = transformerMap.remove(classBeingRedefined);
        if (transformer != null) {
            try {
                return transformer.transform(loader, className, classBeingRedefined, protectionDomain, classfileBuffer);
            } catch (Throwable t) {
                logger.warn("Failed to retransform " + className + " with " + transformer);
                return null;
            }
        }
        
        logger.warn("Unexpected retransform request for " + className);
        return null;
    }
    
    @Deprecated
    public void retransform(Class<?> target, Modifier modifier) {
        if (!instrumentation.isModifiableClass(target)) {
            throw new ProfilerException("Target class " + target + " is not modifiable");
        }

        Modifier prev = targets.putIfAbsent(target, modifier);

        if (prev != null) {
            throw new ProfilerException("Retransform already requested. target: " + target + ", modifier: " + prev);
        }
        
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new ProfilerException(e);
        }
    }
    
    public void retransform(Class<?> target, ClassFileTransformer transformer) {
        if (!instrumentation.isModifiableClass(target)) {
            throw new ProfilerException("Target class " + target + " is not modifiable");
        }

        ClassFileTransformer prev = transformerMap.putIfAbsent(target, transformer);

        if (prev != null) {
            throw new ProfilerException("Retransform already requested. target: " + target + ", transformer: " + prev);
        }
        
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new ProfilerException(e);
        }
    }
}
