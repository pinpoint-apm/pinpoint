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

package com.navercorp.pinpoint.profiler.plugin.transformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.MatchableClassFileTransformer;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class DedicatedClassFileTransformer implements MatchableClassFileTransformer {
    private final ByteCodeInstrumentor instrumentor;

    private final String targetClassName;
    private final ClassRecipe recipe;
    
    
    public DedicatedClassFileTransformer(ByteCodeInstrumentor instrumentor, String targetClassName, ClassRecipe recipe) {
        this.instrumentor = instrumentor;
        this.targetClassName = targetClassName;
        this.recipe = recipe;
    }
    
    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            InstrumentableClass target = instrumentor.getClass(classLoader, className, classfileBuffer);
            recipe.edit(classLoader, target);
            return target.toBytecode();
        } catch (PinpointException e) {
            throw e;
        } catch (Throwable e) {
            String msg = "Fail to invoke plugin class recipe: " + toString();
            throw new PinpointException(msg, e);
        }
    }

    @Override
    public Matcher getMatcher() {
        return Matchers.newClassNameMatcher(JavaAssistUtils.javaNameToJvmName(targetClassName));
    }

    @Override
    public String toString() {
        return "ClassEditor[target=" + targetClassName + ", subEditors=" + recipe + "]";
    }
}
