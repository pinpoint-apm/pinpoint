/*
 * Copyright 2017 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument.classreading;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import java.util.ArrayList;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class AnnotationMetadataVisitor extends ClassVisitor {
    private final List<String> annotationInternalNames = new ArrayList<String>();

    public AnnotationMetadataVisitor() {
        super(Opcodes.ASM5);
    }

    @Override
    public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
        final Type type = Type.getType(desc);
        if (type.getSort() == Type.OBJECT) {
            final String internalName = type.getInternalName();
            if (internalName != null) {
                this.annotationInternalNames.add(internalName);
            }
        }

        return null;
    }

    public List<String> getAnnotationInternalNames() {
        return this.annotationInternalNames;
    }
}