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

package com.navercorp.pinpoint.profiler.plugin.xml.transformer;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.plugin.MatchableClassFileTransformer;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class DedicatedClassFileTransformer implements MatchableClassFileTransformer {
    private final InstrumentContext context;

    private final String targetClassName;
    private final ClassRecipe recipe;
    
    
    public DedicatedClassFileTransformer(InstrumentContext context, String targetClassName, ClassRecipe recipe) {
        this.context = context;
        this.targetClassName = targetClassName;
        this.recipe = recipe;
    }
    
    @Override
    public byte[] transform(ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        try {
            InstrumentClass target = context.getInstrumentClass(classLoader, className, classfileBuffer);
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
