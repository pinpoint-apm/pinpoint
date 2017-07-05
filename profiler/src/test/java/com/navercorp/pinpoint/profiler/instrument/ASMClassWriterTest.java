/*
 * Copyright 2016 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.InputStream;
import java.io.PrintWriter;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * @author jaehong.kim
 */
public class ASMClassWriterTest {

    private final InstrumentContext pluginContext = mock(InstrumentContext.class);

    @Before
    public void setUp() {
        when(pluginContext.injectClass(ArgumentMatchers.<ClassLoader>isNull(), anyString())).thenAnswer(new Answer<Class<?>>() {

            @Override
            public Class<?> answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader loader = (ClassLoader) invocation.getArguments()[0];
                String name = (String) invocation.getArguments()[1];

                return loader.loadClass(name);
            }

        });
        when(pluginContext.getResourceAsStream(ArgumentMatchers.<ClassLoader>isNull(), anyString())).thenAnswer(new Answer<InputStream>() {

            @Override
            public InputStream answer(InvocationOnMock invocation) throws Throwable {
                ClassLoader loader = (ClassLoader) invocation.getArguments()[0];
                String name = (String) invocation.getArguments()[1];
                if (loader == null) {
                    loader = ClassLoader.getSystemClassLoader();
                }

                return loader.getResourceAsStream(name);
            }
        });
    }


    @Test
    public void accept() throws Exception {
        final String className = "com.navercorp.pinpoint.profiler.instrument.mock.SampleClass";
        ClassNode classNode = ASMClassNodeLoader.get(JavaAssistUtils.javaNameToJvmName(className));

        ASMClassWriter cw = new ASMClassWriter(pluginContext, 0, null);
        TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
        classNode.accept(tcv);
    }

    @Test
    public void getCommonSuperClass() throws Exception {
        ASMClassWriter cw = new ASMClassWriter(pluginContext, 0, null);
        // java/lang/object.
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/util/Iterator", "java/lang/Object"));
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/lang/Object", "java/lang/String"));
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/lang/Object", "java/util/List"));

        // interface
        assertEquals("java/util/List", cw.getCommonSuperClass("java/util/ArrayList", "java/util/List"));
        assertEquals("java/util/Map", cw.getCommonSuperClass("java/util/HashMap", "java/util/Map"));

        // extends
        assertEquals("com/navercorp/pinpoint/profiler/instrument/mock/ConstructorParentClass", cw.getCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/mock/ConstructorParentClass", "com/navercorp/pinpoint/profiler/instrument/mock/ConstructorChildClass"));
        assertEquals("java/lang/Exception", cw.getCommonSuperClass("java/io/IOException", "java/lang/Exception"));
        assertEquals("java/lang/Throwable", cw.getCommonSuperClass("java/lang/Throwable", "java/lang/Exception"));
        assertEquals("org/springframework/beans/PropertyValues", cw.getCommonSuperClass("org/springframework/beans/PropertyValues", "org/springframework/beans/MutablePropertyValues"));
        assertEquals("java/net/URLConnection", cw.getCommonSuperClass("java/net/HttpURLConnection", "java/net/URLConnection"));

        // others
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/lang/Exception", "java/lang/Class"));
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/lang/String", "java/lang/Class"));
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/lang/IllegalArgumentException", "javax/servlet/http/Cookie"));
        assertEquals("java/lang/Object", cw.getCommonSuperClass("java/net/MalformedURLException", "java/net/URL"));
    }

    @Test
    public void getCommonSuperClassByClass() throws Exception {
        // class, class
        // Object
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$B");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$C", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$B");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$D", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$B");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$E", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$B");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$F", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$B");

        // A
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$C");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$D");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$E");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$F");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$C", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$D");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$C", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$F");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$E", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$D");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$E", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$F");

        // C
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$C", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$C", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$E");

        // D
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$D", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$D", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$F");
    }

    @Test
    public void getCommonSuperClassByInterface() throws Exception {
        // interface, class
        // Object
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$A");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AJ");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AJ");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$B");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$E");

        // I
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AI");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AK");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AL");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AM");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AL");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AM");

        // J
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AJ");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AL");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AM");

        // K
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AK");

        // L
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AL");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AM");

        // M
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$M", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$M", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$AM");

        // interface, interface
        // Object
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J");
        assertCommonSuperClass("java/lang/Object", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J");

        // I
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$M");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$I", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$K", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$M");

        // J
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L");
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$J", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$M");

        // L
        assertCommonSuperClass("com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$L", "com/navercorp/pinpoint/profiler/instrument/ASMClassWriterTest$M");
    }

    void assertCommonSuperClass(final String superClassInternalName, final String classInternalName1, final String classInternalName2) {
        ASMClassWriter cw = new ASMClassWriter(pluginContext, 0, null);
        assertEquals(superClassInternalName, cw.getCommonSuperClass(classInternalName1, classInternalName2));
        // reverse
        assertEquals(superClassInternalName, cw.getCommonSuperClass(classInternalName2, classInternalName1));

        AL al = new AL();
        I i = al;
        AK ak = new AK();
        i = ak;
    }

    //         Object
    //       A        B
    //   C      D
    //   E      F

    class A {
    }

    class B {
    }

    class C extends A {
    }

    class D extends A {
    }

    class E extends C {
    }

    class F extends D {
    }

    //     Object
    //   I        J
    // K    L
    //      M

    interface I {
    }

    interface J {
    }

    interface K extends I {
    }

    interface L extends I, J {
    }

    interface M extends L {
    }

    class AI implements I {
    }

    class BI implements I {
    }

    class AJ implements J {
    }

    class AK implements K {
    }

    class AL implements L {
    }

    class AM implements M {
    }
}