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

package com.navercorp.pinpoint.profiler.modifier;

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @author emeroad
 */
@Deprecated
public class ModifierTransformAdaptor implements ClassFileTransformer {

    private final Modifier modifier;

    public ModifierTransformAdaptor(Modifier modifier) {
        if (modifier == null) {
            throw new NullPointerException("modifier must not be null");
        }
        this.modifier = modifier;
    }


    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        final String jvmClassName = JavaAssistUtils.jvmNameToJavaName(className);
        return modifier.modify(loader, jvmClassName, protectionDomain, classfileBuffer);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ModifierTransformAdaptor{");
        sb.append("modifier=").append(modifier);
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ModifierTransformAdaptor that = (ModifierTransformAdaptor) o;

        return !(modifier != null ? !modifier.equals(that.modifier) : that.modifier != null);

    }

    @Override
    public int hashCode() {
        return modifier != null ? modifier.hashCode() : 0;
    }
}
