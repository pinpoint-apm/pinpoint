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

import com.navercorp.pinpoint.common.util.ClassLoaderUtils;
import com.navercorp.pinpoint.profiler.util.BytecodeUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.lang.annotation.Target;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class ClassReaderWrapperTest {

    @Test
    public void base() throws Exception {
        final Class<?> clazz = String.class;
        final byte[] classBinary = BytecodeUtils.getClassFile(ClassLoaderUtils.getDefaultClassLoader(), clazz.getName());

        assertClassReader(new ClassReaderWrapper(classBinary));
        assertClassReader(new ClassReaderWrapper(ClassLoaderUtils.getDefaultClassLoader(), JavaAssistUtils.javaNameToJvmName(clazz.getName())));
    }

    @Test
    public void annotation() throws Exception {
        ClassReaderWrapper classReader = new ClassReaderWrapper(ClassLoaderUtils.getDefaultClassLoader(), JavaAssistUtils.javaNameToJvmName(AnnotationMock.class.getName()), true);
        assertTrue(classReader.getAnnotationInternalNames().contains("java/lang/Deprecated"));
        assertTrue(classReader.getAnnotationInternalNames().contains("javax/annotation/Resource"));
    }

    @Test
    public void outerClass() throws Exception {
        ClassReaderWrapper classReader = new ClassReaderWrapper(ClassLoaderUtils.getDefaultClassLoader(), JavaAssistUtils.javaNameToJvmName(InnterClassMock.class.getName()), true);
        assertFalse(classReader.isInnerClass());

        classReader = new ClassReaderWrapper(ClassLoaderUtils.getDefaultClassLoader(), JavaAssistUtils.javaNameToJvmName(InnterClassMock.class.getName() + "$1"), true);
        assertTrue(classReader.isInnerClass());

        classReader = new ClassReaderWrapper(ClassLoaderUtils.getDefaultClassLoader(), JavaAssistUtils.javaNameToJvmName(InnterClassMock.class.getName() + "$2"), true);
        assertTrue(classReader.isInnerClass());

        classReader = new ClassReaderWrapper(ClassLoaderUtils.getDefaultClassLoader(), JavaAssistUtils.javaNameToJvmName(InnterClassMock.class.getName() + "$3"), true);
        assertTrue(classReader.isInnerClass());
    }

    private void assertClassReader(ClassReaderWrapper classReader) {
        assertEquals("java/lang/String", classReader.getClassInternalName());
        assertEquals("java/lang/Object", classReader.getSuperClassInternalName());
        assertTrue(classReader.getInterfaceInternalNames().contains("java/lang/Comparable"));
        assertTrue(classReader.getInterfaceInternalNames().contains("java/io/Serializable"));
        classReader.getVersion();
        classReader.getAccess();
        assertNotNull(classReader.getClassBinary());
    }


    @Deprecated
    @Resource
    class AnnotationMock {
    }

    class InnterClassMock {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
            }
        };

        public InnterClassMock() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                }
            };
        }

        public void foo() {
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                }
            };
        }
    }


}