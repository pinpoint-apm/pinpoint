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

import org.junit.Test;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.util.TraceClassVisitor;

import java.io.PrintWriter;

import static org.junit.Assert.*;

/**
 * @author jaehong.kim
 */

public class ASMClassWriterTest {

    @Test
    public void accept() throws Exception {
        final String className = "com.navercorp.pinpoint.profiler.instrument.mock.SampleClass";
        ClassNode classNode = ASMClassNodeLoader.get(className.replace('.', '/'));

        ASMClassWriter cw = new ASMClassWriter(classNode.name, classNode.superName, 0, null);
        TraceClassVisitor tcv = new TraceClassVisitor(cw, new PrintWriter(System.out));
        classNode.accept(tcv);
    }

    @Test
    public void getCommonSuperClass() throws Exception {
        ASMClassWriter cw = new ASMClassWriter("", "", 0, null);
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
}