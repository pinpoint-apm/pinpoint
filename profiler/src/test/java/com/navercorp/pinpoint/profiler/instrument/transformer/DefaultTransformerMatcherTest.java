/*
 * Copyright 2017 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.instrument.transformer;

import com.navercorp.pinpoint.bootstrap.config.InstrumentMatcherCacheConfig;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.AnnotationInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.ClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.InterfaceInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.MatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.PackageInternalNameMatcherOperand;
import com.navercorp.pinpoint.bootstrap.instrument.matcher.operand.SuperClassInternalNameMatcherOperand;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadata;
import com.navercorp.pinpoint.profiler.instrument.classreading.InternalClassMetadataReader;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Test;

import javax.annotation.Resource;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */
public class DefaultTransformerMatcherTest {

    @Test
    public void matchClass() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        InstrumentMatcherCacheConfig config = new InstrumentMatcherCacheConfig();
        TransformerMatcher matcher = new DefaultTransformerMatcher(config);
        InternalClassMetadata stringClassMetadata = readClassMetadata(classLoader, String.class.getName());
        InternalClassMetadata threadClassMetadata = readClassMetadata(classLoader, Thread.class.getName());
        InternalClassMetadata inputStreamClassMetadata = readClassMetadata(classLoader, InputStream.class.getName());

        MatcherOperand operand = null;

        // single operand.
        operand = new ClassInternalNameMatcherOperand("java/lang/String");
        boolean result = matcher.match(classLoader, operand, stringClassMetadata);
        assertTrue(result);

        // not matched.
        operand = new ClassInternalNameMatcherOperand("java/io/InputStream");
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertFalse(result);

        // and operator
        // package AND interface
        operand = new PackageInternalNameMatcherOperand("java/lang").and(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        // java.lang.Thread
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertTrue(result);
        // java.lang.String
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertFalse(result);

        // or operator
        // package OR interface
        operand = new PackageInternalNameMatcherOperand("java/lang").or(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        // java.lang.Thread
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertTrue(result);
        // java.lang.String
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertTrue(result);

        // not operator
        // NOT interface.
        operand = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false);
        operand = operand.not();
        // java.lang.Thread
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertFalse(result);
        // java.lang.String
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertTrue(result);

        // complex operator
        // (class or interface) AND (class or interface) ==> class, interface
        operand = new ClassInternalNameMatcherOperand("java/lang/String").or(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        operand = operand.and(new ClassInternalNameMatcherOperand("java/lang/Thread").or(new InterfaceInternalNameMatcherOperand("java/lang/Comparable", false)));
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertTrue(result);


        // (class AND interface) OR (class AND interface) ==> class, class
        operand = new ClassInternalNameMatcherOperand("java/lang/String").and(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false));
        operand = operand.or(new ClassInternalNameMatcherOperand("java/lang/Thread").and(new InterfaceInternalNameMatcherOperand("java/lang/Comparable", false)));
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertFalse(result);
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertFalse(result);

        // package AND (interface OR annotation) ==> package
        operand = new PackageInternalNameMatcherOperand("java/lang");
        operand = operand.and(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false).or(new AnnotationInternalNameMatcherOperand("java/lang/Override", false)));
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertFalse(result);
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertTrue(result);

        // class OR (interface AND NOT annotation)
        operand = new ClassInternalNameMatcherOperand("java/lang/String");
        operand = operand.or(new InterfaceInternalNameMatcherOperand("java/lang/Runnable", false).and(new AnnotationInternalNameMatcherOperand("java/lang/Override", false).not()));
        result = matcher.match(classLoader, operand, stringClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, operand, threadClassMetadata);
        assertTrue(result);
    }

    @Test
    public void considerHierarchy() throws Exception {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

        InstrumentMatcherCacheConfig config = new InstrumentMatcherCacheConfig();
        TransformerMatcher matcher = new DefaultTransformerMatcher(config);
        boolean result = false;

        InternalClassMetadata stringClassMetadata = readClassMetadata(classLoader, String.class.getName());
        InternalClassMetadata threadClassMetadata = readClassMetadata(classLoader, Thread.class.getName());
        InternalClassMetadata extendsThreadClassMetadata = readClassMetadata(classLoader, ExtendsThread.class.getName());
        InternalClassMetadata extendsExtendsThreadClassMetadata = readClassMetadata(classLoader, ExtendsExtendsThread.class.getName());
        InternalClassMetadata hasMetaAnnotationClassMetadata = readClassMetadata(classLoader, HasMetaAnnotation.class.getName());
        InternalClassMetadata hasMetaMetaAnnotationClassMetadata = readClassMetadata(classLoader, HasMetaMetaAnnotation.class.getName());

        // interface
        InterfaceInternalNameMatcherOperand interfaceMatcherOperand = new InterfaceInternalNameMatcherOperand("java/lang/Runnable", true);
        result = matcher.match(classLoader, interfaceMatcherOperand, extendsThreadClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, interfaceMatcherOperand, threadClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, interfaceMatcherOperand, stringClassMetadata);
        assertFalse(result);
        result = matcher.match(classLoader, interfaceMatcherOperand, extendsExtendsThreadClassMetadata);
        assertTrue(result);

        // super
        SuperClassInternalNameMatcherOperand superMatcherOperand = new SuperClassInternalNameMatcherOperand("java/lang/Thread", true);
        result = matcher.match(classLoader, superMatcherOperand, extendsExtendsThreadClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, superMatcherOperand, extendsThreadClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, superMatcherOperand, threadClassMetadata);
        assertFalse(result);
        result = matcher.match(classLoader, superMatcherOperand, stringClassMetadata);
        assertFalse(result);

        // annotation
        AnnotationInternalNameMatcherOperand annotationMatcherOperand = new AnnotationInternalNameMatcherOperand("javax/annotation/Resource", true);
        result = matcher.match(classLoader, annotationMatcherOperand, hasMetaAnnotationClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, annotationMatcherOperand, hasMetaMetaAnnotationClassMetadata);
        assertTrue(result);
        result = matcher.match(classLoader, annotationMatcherOperand, stringClassMetadata);
        assertFalse(result);
        result = matcher.match(classLoader, annotationMatcherOperand, threadClassMetadata);
        assertFalse(result);
    }

    class ExtendsThread extends Thread {
    }

    class ExtendsExtendsThread extends ExtendsThread {
    }

    @Resource
    @interface ResourceMetaAnnotation {
    }

    @ResourceMetaAnnotation
    @interface ResourceMetaMetaAnnotation {
    }

    @ResourceMetaAnnotation
    class HasMetaAnnotation {
    }

    @ResourceMetaMetaAnnotation
    class HasMetaMetaAnnotation {
    }

    private InternalClassMetadata readClassMetadata(final ClassLoader classLoader, final String className) throws IOException {
        return InternalClassMetadataReader.readInternalClassMetadata(classLoader, JavaAssistUtils.javaNameToJvmName(className));
    }
}