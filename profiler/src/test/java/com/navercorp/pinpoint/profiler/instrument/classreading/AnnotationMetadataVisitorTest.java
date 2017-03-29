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

import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;

import java.io.InputStream;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author jaehong.kim
 */
public class AnnotationMetadataVisitorTest {

    @Test
    public void visitAnnotation() throws Exception {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream(JavaAssistUtils.javaNameToJvmName(HasAnnotation.class.getName()) + ".class");

        AnnotationMetadataVisitor visitor = new AnnotationMetadataVisitor();
        ClassReader reader = new ClassReader(in);
        in.close();
        reader.accept(visitor, 0);

        assertTrue(visitor.getAnnotationInternalNames().contains(JavaAssistUtils.javaNameToJvmName(SimpleClassAnnotation.class.getName())));
        assertTrue(visitor.getAnnotationInternalNames().contains(JavaAssistUtils.javaNameToJvmName(RetentionPolicyClassAnnotation.class.getName())));
        assertTrue(visitor.getAnnotationInternalNames().contains(JavaAssistUtils.javaNameToJvmName(RetentionPolicyRuntimeAnnotation.class.getName())));
        // skip source
        assertFalse(visitor.getAnnotationInternalNames().contains(JavaAssistUtils.javaNameToJvmName(RetentionPolicySourceAnnotation.class.getName())));
    }


    @Test
    public void visitMetaAnnotation() throws Exception {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream(JavaAssistUtils.javaNameToJvmName(HasMetaAnnotation.class.getName()) + ".class");

        AnnotationMetadataVisitor visitor = new AnnotationMetadataVisitor();
        ClassReader reader = new ClassReader(in);
        in.close();
        reader.accept(visitor, 0);

        assertTrue(visitor.getAnnotationInternalNames().contains(JavaAssistUtils.javaNameToJvmName(NestedMetaAnnotation.class.getName())));
        // no consider hierarchy.
        assertFalse(visitor.getAnnotationInternalNames().contains(JavaAssistUtils.javaNameToJvmName(RetentionPolicyClassAnnotation.class.getName())));
    }


    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @interface SimpleClassAnnotation {
    }

    @Documented
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @interface RetentionPolicyClassAnnotation {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    @interface RetentionPolicyRuntimeAnnotation {
    }

    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.SOURCE)
    @interface RetentionPolicySourceAnnotation {
    }

    @RetentionPolicyClassAnnotation
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.CLASS)
    @interface NestedMetaAnnotation {
    }

    @SimpleClassAnnotation
    @RetentionPolicyClassAnnotation
    @RetentionPolicyRuntimeAnnotation
    @RetentionPolicySourceAnnotation
    class HasAnnotation {
    }

    @NestedMetaAnnotation
    class HasMetaAnnotation {
    }
}