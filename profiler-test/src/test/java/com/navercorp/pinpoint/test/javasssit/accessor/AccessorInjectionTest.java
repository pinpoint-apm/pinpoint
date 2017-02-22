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
import com.navercorp.pinpoint.common.trace.ServiceType;
import com.navercorp.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.navercorp.pinpoint.test.MockApplicationContext;
import com.navercorp.pinpoint.test.classloader.TestClassLoader;
import com.navercorp.pinpoint.test.javasssit.JavassistClassTest;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.security.ProtectionDomain;

/**
 * @author Woonduk Kang(emeroad)
 */
@Deprecated
public class AccessorInjectionTest {

    private Logger logger = LoggerFactory.getLogger(JavassistClassTest.class.getName());

    private TestClassLoader getTestClassLoader() {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());

        DefaultProfilerConfig profilerConfig = new DefaultProfilerConfig();
        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE.getName());
        MockApplicationContext applicationContext = MockApplicationContext.of(profilerConfig);

        TestClassLoader testClassLoader = new TestClassLoader(applicationContext);
        testClassLoader.initialize();
        return testClassLoader;
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

                    aClass.addField(ObjectTraceValue.class.getName());
                    aClass.addField(IntTraceValue.class.getName());
                    aClass.addField(IntArrayTraceValue.class.getName());
                    aClass.addField(IntegerArrayTraceValue.class.getName());
                    aClass.addField(DatabaseInfoTraceValue.class.getName());
                    aClass.addField(BindValueTraceValue.class.getName());

                    String methodName = "callA";
                    aClass.getDeclaredMethod(methodName).addInterceptor("com.navercorp.pinpoint.test.javasssit.TestBeforeInterceptor");
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
        Assert.assertTrue("ObjectTraceValue implements fail", objectTraceValue.isInstance(testObject));
        objectTraceValue.getMethod("_$PINPOINT$_setTraceObject", Object.class).invoke(testObject, "a");
        Object get = objectTraceValue.getMethod("_$PINPOINT$_getTraceObject").invoke(testObject);
        Assert.assertEquals("a", get);

        Class<?> intTraceValue = loader.loadClass(IntTraceValue.class.getName());
        Assert.assertTrue("IntTraceValue implements fail", intTraceValue.isInstance(testObject));
        intTraceValue.getMethod("_$PINPOINT$_setTraceInt", int.class).invoke(testObject, 1);
        int a = (Integer) intTraceValue.getMethod("_$PINPOINT$_getTraceInt").invoke(testObject);
        Assert.assertEquals(1, a);

        Class<?> intArrayTraceValue = loader.loadClass(IntArrayTraceValue.class.getName());
        Assert.assertTrue("IntArrayTraceValue implements fail", intArrayTraceValue.isInstance(testObject));
        int[] expectedInts = {1, 2, 3};
        intArrayTraceValue.getMethod("_$PINPOINT$_setTraceIntArray", int[].class).invoke(testObject, expectedInts);
        int[] ints = (int[]) intArrayTraceValue.getMethod("_$PINPOINT$_getTraceIntArray").invoke(testObject);
        Assert.assertEquals(expectedInts, ints);

        Class<?> integerArrayTraceValue = loader.loadClass(IntegerArrayTraceValue.class.getName());
        Assert.assertTrue("IntegerArrayTraceValue implements fail", integerArrayTraceValue.isInstance(testObject));
        Integer[] expectedIntegers = {1, 2};
        // wrap due to vararg expansion
        Object[] wrappedExpectedIntegers = new Object[]{expectedIntegers};
        integerArrayTraceValue.getMethod("_$PINPOINT$_setTraceIntegerArray", Integer[].class).invoke(testObject, wrappedExpectedIntegers);
        Integer[] integers = (Integer[]) integerArrayTraceValue.getMethod("_$PINPOINT$_getTraceIntegerArray").invoke(testObject);
        Assert.assertArrayEquals(expectedIntegers, integers);

        Class<?> databaseTraceValue = loader.loadClass(DatabaseInfoTraceValue.class.getName());
        Assert.assertTrue("DatabaseInfoTraceValue implements fail", databaseTraceValue.isInstance(testObject));
        databaseTraceValue.getMethod("_$PINPOINT$_setTraceDatabaseInfo", DatabaseInfo.class).invoke(testObject, UnKnownDatabaseInfo.INSTANCE);
        Object databaseInfo = databaseTraceValue.getMethod("_$PINPOINT$_getTraceDatabaseInfo").invoke(testObject);
        Assert.assertSame(UnKnownDatabaseInfo.INSTANCE, databaseInfo);
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

                    aClass.addGetter(StringGetter.class.getName(), "value");
                    aClass.addGetter(IntGetter.class.getName(), "intValue");
                    aClass.addGetter(IntArrayGetter.class.getName(), "intValues");
                    aClass.addGetter(IntegerArrayGetter.class.getName(), "integerValues");

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

        Assert.assertTrue(stringGetter.isInstance(testObject));
        Assert.assertTrue(intGetter.isInstance(testObject));
        Assert.assertTrue(intsGetter.isInstance(testObject));
        Assert.assertTrue(integersGetter.isInstance(testObject));

        String value = "hehe";
        int intValue = 99;
        int[] intValues = {99, 100};
        Integer[] integerValues = {99, 100};

        Method method = testObject.getClass().getMethod("setValue", String.class);
        method.invoke(testObject, value);

        Method getString = stringGetter.getMethod("_$PINPOINT$_getString");
        Assert.assertEquals(value, getString.invoke(testObject));

        Method setIntValue = testObject.getClass().getMethod("setIntValue", int.class);
        setIntValue.invoke(testObject, intValue);

        Method getInt = intGetter.getMethod("_$PINPOINT$_getInt");
        Assert.assertEquals(intValue, getInt.invoke(testObject));

        Method setIntValues = testObject.getClass().getMethod("setIntValues", int[].class);
        setIntValues.invoke(testObject, intValues);

        Method getIntValues = intsGetter.getMethod("_$PINPOINT$_getIntArray");
        Assert.assertEquals(intValues, getIntValues.invoke(testObject));

        Method setIntegerValues = testObject.getClass().getMethod("setIntegerValues", Integer[].class);
        // wrap due to vararg expansion
        Object[] wrappedIntegerValues = new Object[]{integerValues};
        setIntegerValues.invoke(testObject, wrappedIntegerValues);

        Method getIntegerValues = integersGetter.getMethod("_$PINPOINT$_getIntegerArray");
        Assert.assertEquals(integerValues, getIntegerValues.invoke(testObject));

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

                    testClass.addSetter(IntSetter.class.getName(), "intValue");
                    testClass.addSetter(IntArraySetter.class.getName(), "intValues");
                    testClass.addSetter(IntegerArraySetter.class.getName(), "integerValues");

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

        Assert.assertTrue(intSetter.isInstance(testObject));
        Assert.assertTrue(intsSetter.isInstance(testObject));
        Assert.assertTrue(integersSetter.isInstance(testObject));

        int intValue = 99;
        int[] intValues = {99, 100};
        Integer[] integerValues = {99, 100};

        Method setInt = intSetter.getMethod("_$PINPOINT$_setInt", int.class);
        setInt.invoke(testObject, intValue);
        Method getInt = testObject.getClass().getMethod("getIntValue");
        Assert.assertEquals(intValue, getInt.invoke(testObject));

        Method setInts = intsSetter.getMethod("_$PINPOINT$_setIntArray", int[].class);
        setInts.invoke(testObject, intValues);
        Method getInts = testObject.getClass().getMethod("getIntValues");
        Assert.assertEquals(intValues, getInts.invoke(testObject));

        Method setIntegers = integersSetter.getMethod("_$PINPOINT$_setIntegerArray", Integer[].class);
        // wrap due to vararg expansion
        Object[] wrappedIntegerValues = new Object[]{integerValues};
        setIntegers.invoke(testObject, wrappedIntegerValues);
        Method getIntegers = testObject.getClass().getMethod("getIntegerValues");
        Assert.assertEquals(integerValues, getIntegers.invoke(testObject));
    }

}
