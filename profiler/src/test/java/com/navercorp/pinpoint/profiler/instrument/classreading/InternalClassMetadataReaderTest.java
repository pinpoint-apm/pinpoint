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

import com.google.common.base.Function;
import com.google.common.collect.Lists;
import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Test;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class InternalClassMetadataReaderTest {

    @Test
    public void getInternalClassMetadata() throws Exception {
        final Class<?> clazz = String.class;
        final byte[] classBinary = BytecodeUtils.getClassFile(ClassLoaderUtils.getDefaultClassLoader(), clazz.getName());

        InternalClassMetadata classMetadata = InternalClassMetadataReader.readInternalClassMetadata(classBinary);
        // name
        assertEquals(JavaAssistUtils.javaNameToJvmName(clazz.getName()), classMetadata.getClassInternalName());

        // interfaces
        for(Class interfacez : clazz.getInterfaces()) {
            final String interfaceInternalName = JavaAssistUtils.javaNameToJvmName(interfacez.getName());
            classMetadata.getInterfaceInternalNames().contains(interfaceInternalName);
        }

        // super
        assertEquals("java/lang/Object", classMetadata.getSuperClassInternalName());

        // annotations
        for (Annotation annotation : clazz.getAnnotations()) {
            final String annotationInternalName = JavaAssistUtils.javaNameToJvmName(annotation.annotationType().getName());
            classMetadata.getAnnotationInternalNames().contains(annotationInternalName);
        }
    }

    @Test
    public void SuperIsNull() throws Exception {
        final Class<?> clazz = Object.class;
        final byte[] classBinary = BytecodeUtils.getClassFile(ClassLoaderUtils.getDefaultClassLoader(), clazz.getName());
        InternalClassMetadata classMetadata = InternalClassMetadataReader.readInternalClassMetadata(classBinary);
        // name
        assertEquals(JavaAssistUtils.javaNameToJvmName(clazz.getName()), classMetadata.getClassInternalName());
        // super
        assertNull(classMetadata.getSuperClassInternalName());
    }

}