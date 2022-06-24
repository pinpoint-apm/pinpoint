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

package com.navercorp.pinpoint.test.javasssit.accessor;

import com.navercorp.pinpoint.bootstrap.config.DefaultProfilerConfig;
import com.navercorp.pinpoint.bootstrap.context.DatabaseInfo;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.transformer.TransformCallback;
import com.navercorp.pinpoint.bootstrap.logging.PLoggerFactory;
import com.navercorp.pinpoint.bootstrap.plugin.jdbc.UnKnownDatabaseInfo;
import com.navercorp.pinpoint.profiler.context.module.DefaultApplicationContext;
import com.navercorp.pinpoint.profiler.logging.Log4j2Binder;
import com.navercorp.pinpoint.test.MockApplicationContextFactory;
import com.navercorp.pinpoint.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.test.javasssit.TestBeforeInterceptor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.spi.LoggerContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
@Deprecated
public class AccessorInjectionTest {

    private final Logger logger = LogManager.getLogger(this.getClass());

    private DefaultApplicationContext applicationContext;

    private TestClassLoader getTestClassLoader() {
        LoggerContext context = LogManager.getContext();
        PLoggerFactory.initialize(new Log4j2Binder(context));

        DefaultProfilerConfig profilerConfig = new DefaultProfilerConfig();

        MockApplicationContextFactory factory = new MockApplicationContextFactory();
        this.applicationContext = factory.build(profilerConfig);
        this.applicationContext.start();

        TestClassLoader testClassLoader = new TestClassLoader(applicationContext);
        testClassLoader.initialize();
        return testClassLoader;
    }

    @AfterEach
    public void tearDown() {
        if (applicationContext != null) {
            applicationContext.close();
        }
    }

    @Test
    public void addTraceValue() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String javassistClassName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject";

        loader.addTransformer(javassistClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                try {
                    logger.info("modify cl:{}", loader);

                    InstrumentClass aClass = instrumentor.getInstrumentClass(loader, javassistClassName, classfileBuffer);

                    aClass.addField(ObjectTraceValue.class);
                    aClass.addField(IntTraceValue.class);
                    aClass.addField(IntArrayTraceValue.class);
                    aClass.addField(IntegerArrayTraceValue.class);
                    aClass.addField(DatabaseInfoTraceValue.class);
                    aClass.addField(BindValueTraceValue.class);

                    String methodName = "callA";
                    aClass.getDeclaredMethod(methodName).addInterceptor(TestBeforeInterceptor.class);
                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });

        Class<?> testObjectClazz = loader.loadClass(javassistClassName);
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        callA.invoke(testObject);

        Class<?> objectTraceValue = loader.loadClass(ObjectTraceValue.class.getName());
        Assertions.assertTrue(objectTraceValue.isInstance(testObject), "ObjectTraceValue implements fail");
        objectTraceValue.getMethod("_$PINPOINT$_setTraceObject", Object.class).invoke(testObject, "a");
        Object get = objectTraceValue.getMethod("_$PINPOINT$_getTraceObject").invoke(testObject);
        Assertions.assertEquals("a", get);

        Class<?> intTraceValue = loader.loadClass(IntTraceValue.class.getName());
        Assertions.assertTrue(intTraceValue.isInstance(testObject), "IntTraceValue implements fail");
        intTraceValue.getMethod("_$PINPOINT$_setTraceInt", int.class).invoke(testObject, 1);
        int a = (Integer) intTraceValue.getMethod("_$PINPOINT$_getTraceInt").invoke(testObject);
        Assertions.assertEquals(1, a);

        Class<?> intArrayTraceValue = loader.loadClass(IntArrayTraceValue.class.getName());
        Assertions.assertTrue(intArrayTraceValue.isInstance(testObject), "IntArrayTraceValue implements fail");
        int[] expectedInts = {1, 2, 3};
        intArrayTraceValue.getMethod("_$PINPOINT$_setTraceIntArray", int[].class).invoke(testObject, expectedInts);
        int[] ints = (int[]) intArrayTraceValue.getMethod("_$PINPOINT$_getTraceIntArray").invoke(testObject);
        Assertions.assertEquals(expectedInts, ints);

        Class<?> integerArrayTraceValue = loader.loadClass(IntegerArrayTraceValue.class.getName());
        Assertions.assertTrue(integerArrayTraceValue.isInstance(testObject), "IntegerArrayTraceValue implements fail");
        Integer[] expectedIntegers = {1, 2};
        // wrap due to vararg expansion
        Object[] wrappedExpectedIntegers = new Object[]{expectedIntegers};
        integerArrayTraceValue.getMethod("_$PINPOINT$_setTraceIntegerArray", Integer[].class).invoke(testObject, wrappedExpectedIntegers);
        Integer[] integers = (Integer[]) integerArrayTraceValue.getMethod("_$PINPOINT$_getTraceIntegerArray").invoke(testObject);
        Assertions.assertArrayEquals(expectedIntegers, integers);

        Class<?> databaseTraceValue = loader.loadClass(DatabaseInfoTraceValue.class.getName());
        Assertions.assertTrue(databaseTraceValue.isInstance(testObject), "DatabaseInfoTraceValue implements fail");
        databaseTraceValue.getMethod("_$PINPOINT$_setTraceDatabaseInfo", DatabaseInfo.class).invoke(testObject, UnKnownDatabaseInfo.INSTANCE);
        Object databaseInfo = databaseTraceValue.getMethod("_$PINPOINT$_getTraceDatabaseInfo").invoke(testObject);
        Assertions.assertSame(UnKnownDatabaseInfo.INSTANCE, databaseInfo);
    }

    @Test
    public void testAddGetter() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String targetClassName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject3";


        loader.addTransformer(targetClassName, new TransformCallback() {

            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                try {
                    logger.info("modify cl:{}", classLoader);
                    InstrumentClass aClass = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    aClass.addGetter(StringGetter.class, "value");
                    aClass.addGetter(IntGetter.class, "intValue");
                    aClass.addGetter(IntArrayGetter.class, "intValues");
                    aClass.addGetter(IntegerArrayGetter.class, "integerValues");

                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });


        Object testObject = loader.loadClass(targetClassName).newInstance();

        Class<?> stringGetter = loader.loadClass(StringGetter.class.getName());
        Class<?> intGetter = loader.loadClass(IntGetter.class.getName());
        Class<?> intsGetter = loader.loadClass(IntArrayGetter.class.getName());
        Class<?> integersGetter = loader.loadClass(IntegerArrayGetter.class.getName());

        Assertions.assertTrue(stringGetter.isInstance(testObject));
        Assertions.assertTrue(intGetter.isInstance(testObject));
        Assertions.assertTrue(intsGetter.isInstance(testObject));
        Assertions.assertTrue(integersGetter.isInstance(testObject));

        String value = "hehe";
        int intValue = 99;
        int[] intValues = {99, 100};
        Integer[] integerValues = {99, 100};

        Method method = testObject.getClass().getMethod("setValue", String.class);
        method.invoke(testObject, value);

        Method getString = stringGetter.getMethod("_$PINPOINT$_getString");
        Assertions.assertEquals(value, getString.invoke(testObject));

        Method setIntValue = testObject.getClass().getMethod("setIntValue", int.class);
        setIntValue.invoke(testObject, intValue);

        Method getInt = intGetter.getMethod("_$PINPOINT$_getInt");
        Assertions.assertEquals(intValue, getInt.invoke(testObject));

        Method setIntValues = testObject.getClass().getMethod("setIntValues", int[].class);
        setIntValues.invoke(testObject, intValues);

        Method getIntValues = intsGetter.getMethod("_$PINPOINT$_getIntArray");
        Assertions.assertEquals(intValues, getIntValues.invoke(testObject));

        Method setIntegerValues = testObject.getClass().getMethod("setIntegerValues", Integer[].class);
        // wrap due to vararg expansion
        Object[] wrappedIntegerValues = new Object[]{integerValues};
        setIntegerValues.invoke(testObject, wrappedIntegerValues);

        Method getIntegerValues = integersGetter.getMethod("_$PINPOINT$_getIntegerArray");
        Assertions.assertEquals(integerValues, getIntegerValues.invoke(testObject));

    }

    @Test
    public void testAddSetter() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String targetClassName = "com.navercorp.pinpoint.test.javasssit.mock.TestObject4";

        loader.addTransformer(targetClassName, new TransformCallback() {
            @Override
            public byte[] doInTransform(Instrumentor instrumentor, ClassLoader classLoader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws InstrumentException {
                try {
                    logger.info("modify cl:{}", classLoader);
                    InstrumentClass testClass = instrumentor.getInstrumentClass(classLoader, className, classfileBuffer);

                    testClass.addSetter(IntSetter.class, "intValue");
                    testClass.addSetter(IntArraySetter.class, "intValues");
                    testClass.addSetter(IntegerArraySetter.class, "integerValues");

                    return testClass.toBytecode();
                } catch (InstrumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        });


        Object testObject = loader.loadClass(targetClassName).newInstance();

        Class<?> intSetter = loader.loadClass(IntSetter.class.getName());
        Class<?> intsSetter = loader.loadClass(IntArraySetter.class.getName());
        Class<?> integersSetter = loader.loadClass(IntegerArraySetter.class.getName());

        Assertions.assertTrue(intSetter.isInstance(testObject));
        Assertions.assertTrue(intsSetter.isInstance(testObject));
        Assertions.assertTrue(integersSetter.isInstance(testObject));

        int intValue = 99;
        int[] intValues = {99, 100};
        Integer[] integerValues = {99, 100};

        Method setInt = intSetter.getMethod("_$PINPOINT$_setInt", int.class);
        setInt.invoke(testObject, intValue);
        Method getInt = testObject.getClass().getMethod("getIntValue");
        Assertions.assertEquals(intValue, getInt.invoke(testObject));

        Method setInts = intsSetter.getMethod("_$PINPOINT$_setIntArray", int[].class);
        setInts.invoke(testObject, intValues);
        Method getInts = testObject.getClass().getMethod("getIntValues");
        Assertions.assertEquals(intValues, getInts.invoke(testObject));

        Method setIntegers = integersSetter.getMethod("_$PINPOINT$_setIntegerArray", Integer[].class);
        // wrap due to vararg expansion
        Object[] wrappedIntegerValues = new Object[]{integerValues};
        setIntegers.invoke(testObject, wrappedIntegerValues);
        Method getIntegers = testObject.getClass().getMethod("getIntegerValues");
        Assertions.assertEquals(integerValues, getIntegers.invoke(testObject));
    }

}
