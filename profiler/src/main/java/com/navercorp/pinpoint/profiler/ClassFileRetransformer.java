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

import com.navercorp.pinpoint.profiler.modifier.Modifier;

public class ClassFileRetransformer implements ClassFileTransformer {
    private final Instrumentation instrumentation;
    private final ConcurrentHashMap<Class<?>, Modifier> targets = new ConcurrentHashMap<Class<?>, Modifier>();
    
    public ClassFileRetransformer(Instrumentation instrumentation) {
        this.instrumentation = instrumentation;
    }

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        Modifier modifier = targets.remove(classBeingRedefined);
        
        if (modifier == null) {
            return null;
        }
        
        return modifier.modify(loader, className.replace('/', '.'), protectionDomain, classfileBuffer);
    }
    
    public void retransform(Class<?> target, Modifier modifier) {
        Modifier removed = targets.put(target, modifier);

        if (removed != null) {
            // TODO log
        }
        
        try {
            instrumentation.retransformClasses(target);
        } catch (UnmodifiableClassException e) {
            throw new ProfilerException(e);
        }
    }
}
