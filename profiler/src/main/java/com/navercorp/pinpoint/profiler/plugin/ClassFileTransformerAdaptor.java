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

package com.navercorp.pinpoint.profiler.plugin;

import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.ClassNameMatcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matcher;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.Matchers;
import com.navercorp.pinpoint.bootstrap.plugin.transformer.DedicatedClassFileTransformer;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.modifier.AbstractModifier;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class ClassFileTransformerAdaptor extends AbstractModifier {
    private final DedicatedClassFileTransformer transformer;

    
    public ClassFileTransformerAdaptor(ByteCodeInstrumentor byteCodeInstrumentor, DedicatedClassFileTransformer transformer) {
        super(byteCodeInstrumentor);
        this.transformer = transformer;
    }

    @Override
    public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectionDomain, byte[] classfileBuffer) {
        try {
            return transformer.transform(classLoader, className, null, protectionDomain, classfileBuffer);
        } catch (IllegalClassFormatException e) {
            throw new PinpointException("Fail to transform class: " + className, e);
        }
    }

    @Override
    public Matcher getMatcher() {
        final Matcher matcher = transformer.getMatcher();

        if (matcher instanceof ClassNameMatcher) {
            ClassNameMatcher classNameMatcher = (ClassNameMatcher)matcher;
            final String className = classNameMatcher.getClassName();

            String jvmClassName = JavaAssistUtils.javaNameToJvmName(className);
            return Matchers.newClassNameMatcher(jvmClassName);
        }
        throw new IllegalArgumentException("unsupported matcher :" + matcher);
    }
}
