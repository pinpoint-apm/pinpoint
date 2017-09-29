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
 *
 */

package com.navercorp.pinpoint.test.javasssit;

import com.google.inject.Provider;
import com.google.inject.util.Providers;
import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;

import com.navercorp.pinpoint.bootstrap.instrument.ClassFilters;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.common.util.ArrayUtils;
import com.navercorp.pinpoint.profiler.instrument.InstrumentEngine;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.instrument.JavassistEngine;
import com.navercorp.pinpoint.profiler.interceptor.registry.GlobalInterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.test.MockApplicationContext;
import com.navercorp.pinpoint.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.test.util.BytecodeUtils;
import javassist.bytecode.Descriptor;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

/**
 * @author emeroad
 */
@Deprecated
public class JavassistClassTest {
    private Logger logger = LoggerFactory.getLogger(JavassistClassTest.class.getName());

    @Before
    public void clear() {
        TestInterceptors.clear();
    }

    private byte[] readByteCode(String className) {
        final ClassLoader classLoader = ClassLoader.getSystemClassLoader();
        return BytecodeUtils.getClassFile(classLoader, className);
    }

    @Test
    public void testClassHierarchy() throws InstrumentException {
        InstrumentEngine engine = newJavassistEngine();

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        String testObjectName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject";

        byte[] testObjectByteCode = readByteCode(testObjectName);
        InstrumentClass testObject = engine.getClass(instrumentContext, null, testObjectName, testObjectByteCode);

        Assert.assertEquals(testObject.getName(), testObjectName);

        String testObjectSuperClass = testObject.getSuperClass();
        Assert.assertEquals("java.lang.Object", testObjectSuperClass);

        String[] testObjectSuperClassInterfaces = testObject.getInterfaces();
        Assert.assertEquals(testObjectSuperClassInterfaces.length, 0);

        final String classHierarchyTestMockName = "com.navercorp.pinpoint.test.javasssit.mock.ClassHierarchyTestMock";
        byte[] classHierarchyTestMockByteCode = readByteCode(classHierarchyTestMockName);
        InstrumentClass classHierarchyObject = engine.getClass(instrumentContext, null, classHierarchyTestMockName, classHierarchyTestMockByteCode);
        String hierarchySuperClass = classHierarchyObject.getSuperClass();
        Assert.assertEquals("java.util.HashMap", hierarchySuperClass);

        String[] hierarchyInterfaces = classHierarchyObject.getInterfaces();
        Assert.assertEquals(hierarchyInterfaces.length, 2);
        Assert.assertEquals(hierarchyInterfaces[0], "java.lang.Runnable");
        Assert.assertEquals(hierarchyInterfaces[1], "java.lang.Comparable");
    }

    private InstrumentEngine newJavassistEngine() {
        Instrumentation instrumentation = mock(Instrumentation.class);
        ObjectBinderFactory objectBinderFactory = mock(ObjectBinderFactory.class);
        Provider<ApiMetaDataService> apiMetaDataService = Providers.of(mock(ApiMetaDataService.class));

        return new JavassistEngine(instrumentation, objectBinderFactory, new GlobalInterceptorRegistryBinder(), apiMetaDataService, null);
    }

    @Test
    public void testDeclaredMethod() throws InstrumentException {

        InstrumentEngine engine = newJavassistEngine();

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        String testObjectName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject";
        byte[] testObjectByteCode = readByteCode(testObjectName);
        InstrumentClass testObject = engine.getClass(instrumentContext, null, testObjectName, testObjectByteCode);

        Assert.assertEquals(testObject.getName(), testObjectName);

        InstrumentMethod declaredMethod = testObject.getDeclaredMethod("callA");
        Assert.assertNotNull(declaredMethod);

    }

    @Test
    public void testDeclaredMethods() throws InstrumentException {
        InstrumentEngine engine = newJavassistEngine();

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        String testObjectName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject";
        byte[] testObjectByteCode = readByteCode(testObjectName);
        InstrumentClass testObject = engine.getClass(instrumentContext, null, testObjectName, testObjectByteCode);
        Assert.assertEquals(testObject.getName(), testObjectName);

        int findMethodCount = 0;
        for (InstrumentMethod methodInfo : testObject.getDeclaredMethods()) {
            if (!methodInfo.getName().equals("callA")) {
                continue;
            }
            String[] parameterTypes = methodInfo.getParameterTypes();
            if (ArrayUtils.isEmpty(parameterTypes)) {
                findMethodCount++;
            }
        }
        Assert.assertEquals(findMethodCount, 1);
    }

    @Test
    public void testBeforeAddInterceptor() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String javassistClassName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject";

        loader.addTransformer(javassistClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                try {
                    logger.info("modify className:{} cl:{}", className, classLoader);

                    InstrumentClass aClass = instrumentor.getInstrumentClass(classLoader, javassistClassName, classfileBuffer);

                    String methodName = "callA";
                    InstrumentMethod callaMethod = aClass.getDeclaredMethod(methodName);
                    callaMethod.addInterceptor("com.navercorp.pinpoint.test.javasssit.TestBeforeInterceptor");

                    byte[] bytes = aClass.toBytecode();
                    return bytes;
                } catch (Throwable e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Class<?> testObjectClazz = loader.loadClass(javassistClassName);
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        logger.info("class cl:{}", testObjectClazz.getClassLoader());

        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        callA.invoke(testObject);
        Interceptor interceptor = getInterceptor(loader, 0);
        assertEqualsIntField(interceptor, "call", 1);
        assertEqualsObjectField(interceptor, "className", "com.navercorp.pinpoint.test.javasssit.mock.TestObject");
        assertEqualsObjectField(interceptor, "methodName", methodName);
        assertEqualsObjectField(interceptor, "args", null);

        assertEqualsObjectField(interceptor, "target", testObject);
    }

    private Interceptor getInterceptor(final TestClassLoader loader, int index) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException, ClassNotFoundException {
        Class<?> interceptorClazz = loader.loadClass("com.navercorp.pinpoint.test.javasssit.TestInterceptors");
        Method getMethod = interceptorClazz.getMethod("get", int.class);
        Interceptor interceptor = (Interceptor) getMethod.invoke(null, index);
        return interceptor;
    }

    private TestClassLoader getTestClassLoader() {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        DefaultProfilerConfig profilerConfig = new DefaultProfilerConfig();
        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE.getName());
        MockApplicationContext applicationContext = MockApplicationContext.of(profilerConfig);

        TestClassLoader testClassLoader = new TestClassLoader(applicationContext);
        testClassLoader.initialize();
        return testClassLoader;
    }

    public void assertEqualsIntField(Object target, String fieldName, int value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getField(fieldName);
        int anInt = field.getInt(target);
        Assert.assertEquals(anInt, value);
    }

    public void assertEqualsObjectField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getField(fieldName);
        Object obj = field.get(target);
        Assert.assertEquals(value, obj);
    }

    @Ignore
    @Test
    public void testAddAfterInterceptor() throws Exception {

        final TestClassLoader loader = getTestClassLoader();
        final String testClassObject = "com.navercorp.pinpoint.test.javasssit.mock.TestObject2";

        loader.addTransformer(testClassObject, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                try {
                    logger.info("modify cl:{}", classLoader);
                    InstrumentClass aClass = instrumentor.getInstrumentClass(classLoader, testClassObject, classfileBuffer);

                    String methodName = "callA";
                    aClass.getDeclaredMethod(methodName).addInterceptor("com.navercorp.pinpoint.profiler.interceptor.TestAfterInterceptor");

                    String methodName2 = "callB";
                    aClass.getDeclaredMethod(methodName2).addInterceptor("com.navercorp.pinpoint.profiler.interceptor.TestAfterInterceptor");

                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });


        Class<?> testObjectClazz = loader.loadClass(testClassObject);
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        Object result = callA.invoke(testObject);

        Interceptor interceptor = getInterceptor(loader, 0);
        assertEqualsIntField(interceptor, "call", 1);
        assertEqualsObjectField(interceptor, "className", testClassObject);
        assertEqualsObjectField(interceptor, "methodName", methodName);
        assertEqualsObjectField(interceptor, "args", null);

        assertEqualsObjectField(interceptor, "target", testObject);
        assertEqualsObjectField(interceptor, "result", result);

        final String methodName2 = "callB";
        Method callBMethod = testObject.getClass().getMethod(methodName2);
        callBMethod.invoke(testObject);

        Interceptor interceptor2 = getInterceptor(loader, 1);
        assertEqualsIntField(interceptor2, "call", 1);
        assertEqualsObjectField(interceptor2, "className", testClassObject);
        assertEqualsObjectField(interceptor2, "methodName", methodName2);
        assertEqualsObjectField(interceptor2, "args", null);

        assertEqualsObjectField(interceptor2, "target", testObject);
        assertEqualsObjectField(interceptor2, "result", null);

    }

    @Test
    public void nullDescriptor() {
        String nullDescriptor = Descriptor.ofParameters(null);
        logger.debug("Descriptor null:{}", nullDescriptor);
    }



    @Test
    public void getNestedClasses() throws Exception {

        InstrumentEngine engine = newJavassistEngine();

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        String testObjectName = "com.navercorp.pinpoint.test.javasssit.mock.TestObjectNestedClass";

        byte[] testObjectByteCode = readByteCode(testObjectName);
        InstrumentClass testObject = engine.getClass(instrumentContext, null, testObjectName, testObjectByteCode);
        Assert.assertEquals(testObject.getName(), testObjectName);

        // find class name condition.
        final String targetClassName = "com.navercorp.pinpoint.profiler.interceptor.bci.TestObjectNestedClass$InstanceInner";
        for (InstrumentClass c : testObject.getNestedClasses(ClassFilters.name(targetClassName))) {
            assertEquals(targetClassName, c.getName());
        }

        // find enclosing method condition.
        assertEquals(2, testObject.getNestedClasses(ClassFilters.enclosingMethod("annonymousInnerClass")).size());

        // find interface condition.
        assertEquals(2, testObject.getNestedClasses(ClassFilters.interfaze("java.util.concurrent.Callable")).size());

        // find enclosing method & interface condition.
        assertEquals(1, testObject.getNestedClasses(ClassFilters.chain(ClassFilters.enclosingMethod("annonymousInnerClass"), ClassFilters.interfaze("java.util.concurrent.Callable"))).size());
    }

    @Test
    public void hasEnclodingMethod() throws Exception {

        InstrumentEngine engine = newJavassistEngine();

        InstrumentContext instrumentContext = mock(InstrumentContext.class);
        String testObjectName = "com.navercorp.pinpoint.test.javasssit.mock.TestObjectNestedClass";

        byte[] testObjectByteCode = readByteCode(testObjectName);
        InstrumentClass testObject = engine.getClass(instrumentContext, null, testObjectName, testObjectByteCode);
        Assert.assertEquals(testObject.getName(), testObjectName);

        assertEquals(1, testObject.getNestedClasses(ClassFilters.enclosingMethod("enclosingMethod", "java.lang.String", "int")).size());
        assertEquals(0, testObject.getNestedClasses(ClassFilters.enclosingMethod("enclosingMethod", "int")).size());
    }
}
