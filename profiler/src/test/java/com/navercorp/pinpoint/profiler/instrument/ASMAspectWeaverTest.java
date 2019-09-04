/*
 * Copyright 2018 NAVER Corp.
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
package com.navercorp.pinpoint.profiler.instrument;

import com.navercorp.pinpoint.bootstrap.instrument.ClassInputStreamProvider;
import com.navercorp.pinpoint.common.util.IOUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.ClassNode;

import java.io.InputStream;
import java.lang.reflect.Method;

/**
 * @author jaehong.kim
 */
public class ASMAspectWeaverTest {

    private final String ORIGINAL = "com.navercorp.pinpoint.profiler.instrument.mock.AspectOriginalClass";
    private final String ORIGINAL_SUB = "com.navercorp.pinpoint.profiler.instrument.mock.AspectOriginalSubClass";

    private final String ASPECT = "com.navercorp.pinpoint.profiler.instrument.mock.AspectInterceptorClass";
    private final String ASPECT_NO_EXTENTS = "com.navercorp.pinpoint.profiler.instrument.mock.AspectInterceptorNoExtendClass";
    private final String ASPECT_EXTENTS_SUB = "com.navercorp.pinpoint.profiler.instrument.mock.AspectInterceptorExtendSubClass";

    private final String ERROR_ASPECT1 = "com.navercorp.pinpoint.profiler.instrument.mock.AspectInterceptorErrorClass";
    private final String ERROR_ASPECT2 = "com.navercorp.pinpoint.profiler.instrument.mock.AspectInterceptorError2Class";

    private final String ERROR_ASPECT_INVALID_EXTENTS = "com.navercorp.pinpoint.profiler.instrument.mock.AspectInterceptorInvalidExtendClass";

    private final ClassInputStreamProvider pluginClassInputStreamProvider = new SimpleClassInputStreamProvider();

    @Test
    public void weaving() throws Exception {
        weaving(ORIGINAL, ASPECT);
        weaving(ORIGINAL, ASPECT_NO_EXTENTS);
        weaving(ORIGINAL, ASPECT_EXTENTS_SUB);

        weaving(ORIGINAL_SUB, ASPECT_EXTENTS_SUB);
        weaving(ORIGINAL_SUB, ASPECT_NO_EXTENTS);
    }

    private void weaving(final String originalClass, final String adviceClass) throws Exception {
        final Object instance = getInstnace(originalClass, adviceClass);
        invoke(instance, "testVoid");
        invoke(instance, "testInt");
        invoke(instance, "testString");
        invoke(instance, "testUtilMethod");
        invoke(instance, "testNoTouch");
        invoke(instance, "testInternalMethod");
        invoke(instance, "testMethodCall");
    }

    @Test(expected = Exception.class)
    public void invalidHierarchy() throws Exception {
        weaving(ORIGINAL_SUB, ASPECT);
    }

    @Test(expected = Exception.class)
    public void signatureMiss() throws Exception {
        // not found method
        getInstnace(ORIGINAL, ERROR_ASPECT1);
    }

    @Test(expected = Exception.class)
    public void internalTypeMiss() throws Exception {
        getInstnace(ORIGINAL, ERROR_ASPECT2);
    }

    @Test(expected = Exception.class)
    public void invalidExtend() throws Exception {
        // invalid class hierarchy.
        getInstnace(ORIGINAL, ERROR_ASPECT_INVALID_EXTENTS);
    }

    private Object getInstnace(final String originalName, final String aspectName) throws Exception {
        final ClassLoader defaultClassLoader = Thread.currentThread().getContextClassLoader();
        final ClassLoader classLoader = new ClassLoader() {
            @Override
            public Class<?> loadClass(String name) throws ClassNotFoundException {
                if (name.equals(originalName)) {
                    try {
                        final String jvmClassName = "/" + JavaAssistUtils.javaClassNameToJvmResourceName(name);
                        final InputStream stream = getClass().getResourceAsStream(jvmClassName);
                        byte[] classBytes = IOUtils.toByteArray(stream);
                        final ClassReader cr = new ClassReader(classBytes);
                        final ClassNode classNode = new ClassNode();
                        cr.accept(classNode, 0);

                        final ASMClassNodeAdapter sourceClassNode = new ASMClassNodeAdapter(pluginClassInputStreamProvider, defaultClassLoader, null, classNode);
                        final ASMClassNodeAdapter adviceClassNode = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, defaultClassLoader, null, JavaAssistUtils.javaNameToJvmName(aspectName));

                        final ASMAspectWeaver aspectWeaver = new ASMAspectWeaver();
                        aspectWeaver.weaving(sourceClassNode, adviceClassNode);

                        final ClassWriter cw = new ClassWriter(0);
                        classNode.accept(cw);
                        final byte[] bytecode = cw.toByteArray();
//                        CheckClassAdapter.verify(new ClassReader(bytecode), false, new PrintWriter(System.out));

                        return super.defineClass(name, bytecode, 0, bytecode.length);
                    } catch (Exception ex) {
                        throw new ClassNotFoundException("Load error: " + ex.toString(), ex);
                    }
                } else {
                    return super.loadClass(name);
                }
            }
        };
        Class clazz = classLoader.loadClass(originalName);
        return clazz.newInstance();
    }

    private Object invoke(Object o, String methodName, Object... args) {
        try {
            Class<?> clazz = o.getClass();
            Method method = clazz.getMethod(methodName);
            return method.invoke(o, args);
        } catch (Exception e) {
            throw new RuntimeException(e.getMessage(), e);
        }
    }
}