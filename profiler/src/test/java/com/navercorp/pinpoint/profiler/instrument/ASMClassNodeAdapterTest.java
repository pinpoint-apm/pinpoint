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

import com.navercorp.pinpoint.bootstrap.context.AsyncContext;
import com.navercorp.pinpoint.bootstrap.instrument.ClassInputStreamProvider;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.profiler.instrument.mock.JavaSerializableUtils;
import com.navercorp.pinpoint.profiler.instrument.mock.accessor.MockAsyncContext;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.junit.Assert;
import org.junit.Test;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

public class ASMClassNodeAdapterTest {

    private final ClassInputStreamProvider pluginClassInputStreamProvider = new SimpleClassInputStreamProvider();
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Test
    public void get() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ASMClassNodeAdapter adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(), "com/navercorp/pinpoint/profiler/instrument/mock/BaseClass");
        assertNotNull(adapter);

        adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(),"com/navercorp/pinpoint/profiler/instrument/mock/NotExistClass");
        assertNull(adapter);

        // skip code
        adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(), "com/navercorp/pinpoint/profiler/instrument/mock/BaseClass", true);
        try {
            adapter.getDeclaredMethods();
            fail("can't throw IllegalStateException");
        } catch(Exception ignored) {
        }

        try {
            adapter.getDeclaredMethod("base", "()");
            fail("can't throw IllegalStateException");
        } catch(Exception ignored) {
        }
    }

    @Test
    public void getter() {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        ASMClassNodeAdapter adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(), "com/navercorp/pinpoint/profiler/instrument/mock/ExtendedClass");
        // name
        assertEquals("com/navercorp/pinpoint/profiler/instrument/mock/ExtendedClass", adapter.getInternalName());
        assertEquals("com.navercorp.pinpoint.profiler.instrument.mock.ExtendedClass", adapter.getName());
        assertEquals("com/navercorp/pinpoint/profiler/instrument/mock/BaseClass", adapter.getSuperClassInternalName());
        assertEquals("com.navercorp.pinpoint.profiler.instrument.mock.BaseClass", adapter.getSuperClassName());
        assertEquals(false, adapter.isInterface());
        assertEquals(false, adapter.isAnnotation());
        assertEquals(0, adapter.getInterfaceNames().length);

        // method
        List<ASMMethodNodeAdapter> methods = adapter.getDeclaredMethods();
        assertEquals(1, methods.size());

        ASMMethodNodeAdapter method = adapter.getDeclaredMethod("extended", "()");
        assertEquals("extended", method.getName());

        method = adapter.getDeclaredMethod("notExist", "()");
        assertNull(method);

        // field
        ASMFieldNodeAdapter field = adapter.getField("e", null);
        assertEquals("e", field.getName());

        field = adapter.getField("e", "Ljava/lang/String;");
        assertEquals("e", field.getName());
        assertEquals("Ljava/lang/String;", field.getDesc());

        field = adapter.getField("notExist", null);
        assertNull(field);

        // interface
        adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(),"com/navercorp/pinpoint/profiler/instrument/mock/BaseInterface");
        assertEquals(true, adapter.isInterface());

        // implement
        adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(),"com/navercorp/pinpoint/profiler/instrument/mock/BaseImplementClass");
        String[] interfaceNames = adapter.getInterfaceNames();
        assertEquals(1, interfaceNames.length);
        assertEquals("com.navercorp.pinpoint.profiler.instrument.mock.BaseInterface", interfaceNames[0]);

        // annotation
        adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, classLoader, getClass().getProtectionDomain(),"com/navercorp/pinpoint/bootstrap/instrument/aspect/Aspect");
        assertEquals(true, adapter.isAnnotation());


    }

    @Test
    public void addGetter() throws Exception {
        final String targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.BaseClass";
        final String getterMethodName = "_$PINPOINT$_getValue";
        ASMClassNodeLoader.TestClassLoader classLoader = ASMClassNodeLoader.getClassLoader();
        classLoader.setTargetClassName(targetClassName);
        classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                ASMClassNodeAdapter adapter = new ASMClassNodeAdapter(pluginClassInputStreamProvider, null, getClass().getProtectionDomain(), classNode);
                ASMFieldNodeAdapter field = adapter.getField("i", null);
                adapter.addGetterMethod(getterMethodName, field);
            }
        });
        Class<?> clazz = classLoader.loadClass(targetClassName);
        Method method = clazz.getDeclaredMethod(getterMethodName);
        assertEquals(0, method.invoke(clazz.newInstance()));
    }


    @Test
    public void addSetter() throws Exception {
        final String targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.BaseClass";
        final String setterMethodName = "_$PINPOINT$_setValue";
        ASMClassNodeLoader.TestClassLoader classLoader = ASMClassNodeLoader.getClassLoader();
        classLoader.setTargetClassName(targetClassName);
        classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                ASMClassNodeAdapter adapter = new ASMClassNodeAdapter(pluginClassInputStreamProvider, null, getClass().getProtectionDomain(), classNode);
                ASMFieldNodeAdapter field = adapter.getField("i", null);
                adapter.addSetterMethod(setterMethodName, field);
            }
        });
        Class<?> clazz = classLoader.loadClass(targetClassName);
        Method method = clazz.getDeclaredMethod(setterMethodName, int.class);
        method.invoke(clazz.newInstance(), 10);
    }

    @Test
    public void addField() throws Exception {
        final String targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.BaseClass";
        final String accessorClassName = "com.navercorp.pinpoint.profiler.instrument.mock.BaseAccessor";
        final ASMClassNodeLoader.TestClassLoader classLoader = ASMClassNodeLoader.getClassLoader();
        classLoader.setTargetClassName(targetClassName);
        classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                ASMClassNodeAdapter classNodeAdapter = new ASMClassNodeAdapter(pluginClassInputStreamProvider, null, getClass().getProtectionDomain(), classNode);
                classNodeAdapter.addField("_$PINPOINT$_" + JavaAssistUtils.javaClassNameToVariableName(accessorClassName), Type.getDescriptor(int.class));
                classNodeAdapter.addInterface(accessorClassName);
                ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField("_$PINPOINT$_" + JavaAssistUtils.javaClassNameToVariableName(accessorClassName), null);
                classNodeAdapter.addGetterMethod("_$PINPOINT$_getTraceInt", fieldNode);
                classNodeAdapter.addSetterMethod("_$PINPOINT$_setTraceInt", fieldNode);
            }
        });
        Class<?> clazz = classLoader.loadClass(targetClassName);
        Object instance = clazz.newInstance();

        Method setMethod = clazz.getDeclaredMethod("_$PINPOINT$_setTraceInt", int.class);
        setMethod.invoke(instance, 10);

        Method getMethod = clazz.getDeclaredMethod("_$PINPOINT$_getTraceInt");
        int result = (Integer) getMethod.invoke(instance);
        logger.debug("{}", result);
    }

    @Test
    public void addField_transient() throws Exception {
        final String baseClassName = "com.navercorp.pinpoint.profiler.instrument.mock.SerializableClass";
        final Class<AsyncContext> asyncContextClass = AsyncContext.class;
        final String accessorClassName = asyncContextClass.getName();
        final ASMClassNodeLoader.TestClassLoader classLoader = ASMClassNodeLoader.getClassLoader();
        classLoader.setTargetClassName(baseClassName);
        classLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                ASMClassNodeAdapter classNodeAdapter = new ASMClassNodeAdapter(pluginClassInputStreamProvider, null, getClass().getProtectionDomain(), classNode);
                classNodeAdapter.addField("_$PINPOINT$_" + JavaAssistUtils.javaClassNameToVariableName(accessorClassName), Type.getDescriptor(asyncContextClass));
                classNodeAdapter.addInterface(accessorClassName);
                ASMFieldNodeAdapter fieldNode = classNodeAdapter.getField("_$PINPOINT$_" + JavaAssistUtils.javaClassNameToVariableName(accessorClassName), null);
                classNodeAdapter.addGetterMethod("_$PINPOINT$_getAsyncContext", fieldNode);
                classNodeAdapter.addSetterMethod("_$PINPOINT$_setAsyncContext", fieldNode);
            }
        });
        Class<?> baseClazz = classLoader.loadClass(baseClassName);
        Object instance = baseClazz.newInstance();
        AsyncContext asyncContext = new MockAsyncContext();
        Method setMethod = baseClazz.getDeclaredMethod("_$PINPOINT$_setAsyncContext", asyncContextClass);
        setMethod.invoke(instance, asyncContext);

        Method getMethod = baseClazz.getDeclaredMethod("_$PINPOINT$_getAsyncContext");
        AsyncContext result = (AsyncContext) getMethod.invoke(instance);

        JavaSerializableUtils.serialize(instance);
        logger.debug("{}", result);
    }


    @Test
    public void hasAnnotation() throws Exception {
        ASMClassNodeAdapter classNodeAdapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, ASMClassNodeLoader.getClassLoader(), getClass().getProtectionDomain(), "com/navercorp/pinpoint/profiler/instrument/mock/AnnotationClass");
        Assert.assertTrue(classNodeAdapter.hasAnnotation(Aspect.class));
        Assert.assertFalse(classNodeAdapter.hasAnnotation(Override.class));
    }

    @Test
    public void addMethod() throws Exception {
        final MethodNode methodNode = ASMClassNodeLoader.get("com.navercorp.pinpoint.profiler.instrument.mock.ArgsClass", "arg");
        final ASMMethodNodeAdapter adapter = new ASMMethodNodeAdapter("com/navercorp/pinpoint/profiler/instrument/mock/ArgsClass", methodNode);

        final ASMClassNodeLoader.TestClassLoader testClassLoader = ASMClassNodeLoader.getClassLoader();
        final String targetClassName = "com.navercorp.pinpoint.profiler.instrument.mock.BaseClass";
        testClassLoader.setTargetClassName(targetClassName);
        testClassLoader.setCallbackHandler(new ASMClassNodeLoader.CallbackHandler() {
            @Override
            public void handle(ClassNode classNode) {
                ASMClassNodeAdapter classNodeAdapter = new ASMClassNodeAdapter(pluginClassInputStreamProvider, null, getClass().getProtectionDomain(), classNode);
                classNodeAdapter.copyMethod(adapter);
            }
        });
        Class<?> clazz = testClassLoader.loadClass(targetClassName);
        Method method = clazz.getDeclaredMethod("arg");
        method.invoke(clazz.newInstance());
    }

    @Test
    public void getDeclaredMethod() {
        ASMClassNodeAdapter adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, ASMClassNodeLoader.getClassLoader(),
                getClass().getProtectionDomain(),"com/navercorp/pinpoint/profiler/instrument/mock/ArgsClass");
        List<ASMMethodNodeAdapter> constructors = adapter.getDeclaredConstructors();
        Assert.assertEquals(2, constructors.size());
        assertEquals("ArgsClass", constructors.get(0).getName());

        assertEquals("ArgsClass", constructors.get(1).getName());
        assertArrayEquals(new String[] {"int"}, constructors.get(1).getParameterTypes());
        ASMMethodNodeAdapter m1 = adapter.getDeclaredMethod("<init>", null);
        logger.debug("{}", m1.getName());
    }

    @Test
    public void subclassOf() {
        ASMClassNodeAdapter adapter = ASMClassNodeAdapter.get(pluginClassInputStreamProvider, ASMClassNodeLoader.getClassLoader(), getClass().getProtectionDomain(),"com/navercorp/pinpoint/profiler/instrument/mock/ExtendedClass");
        // self
        assertEquals(true, adapter.subclassOf("com/navercorp/pinpoint/profiler/instrument/mock/ExtendedClass"));

        // super
        assertEquals(true, adapter.subclassOf("com/navercorp/pinpoint/profiler/instrument/mock/BaseClass"));
        assertEquals(true, adapter.subclassOf("java/lang/Object"));

        // not
        assertEquals(false, adapter.subclassOf("com/navercorp/pinpoint/profiler/instrument/mock/NormalClass"));
    }
}