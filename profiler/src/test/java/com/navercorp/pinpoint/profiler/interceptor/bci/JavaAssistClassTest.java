package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.bootstrap.context.DatabaseInfo;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.BindValueTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.DatabaseInfoTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.IntTraceValue;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.ObjectTraceValue;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.DefaultAgent;
import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.profiler.logging.Slf4jLoggerBinder;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.UnKnownDatabaseInfo;
import com.nhn.pinpoint.profiler.util.MockAgent;
import com.nhn.pinpoint.profiler.util.TestClassLoader;
import com.nhn.pinpoint.profiler.util.TestModifier;
import javassist.bytecode.Descriptor;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.ProtectionDomain;
import java.util.Collections;
import java.util.Map;

/**
 * @author emeroad
 */
public class JavaAssistClassTest {
    private Logger logger = LoggerFactory.getLogger(JavaAssistByteCodeInstrumentor.class.getName());

    @Test
    public void addTraceValue() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String javassistClassName = "com.nhn.pinpoint.profiler.interceptor.bci.TestObject";
        final TestModifier testModifier = new TestModifier(loader.getInstrumentor(), loader.getAgent()) {

            @Override
            public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
                try {
                    logger.info("modify cl:{}", classLoader);

                    InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

                    Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.interceptor.TestBeforeInterceptor");
                    addInterceptor(interceptor);
                    aClass.addTraceValue(ObjectTraceValue.class);
                    aClass.addTraceValue(IntTraceValue.class);
                    aClass.addTraceValue(DatabaseInfoTraceValue.class);
                    aClass.addTraceValue(BindValueTraceValue.class);

                    logger.info(interceptor.getClass().getClassLoader().toString());
                    String methodName = "callA";
                    aClass.addInterceptor(methodName, null, interceptor);
                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
        testModifier.setTargetClass(javassistClassName);
        loader.addModifier(testModifier);
        loader.initialize();

        Class testObjectClazz = loader.loadClass(javassistClassName);
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        callA.invoke(testObject);


        if (testObject instanceof ObjectTraceValue) {
            ObjectTraceValue objectTraceValue = (ObjectTraceValue) testObject;
            objectTraceValue.__setTraceObject("a");
            Object get = objectTraceValue.__getTraceObject();
            Assert.assertEquals("a", get);
        } else {
            Assert.fail("ObjectTraceValue implements fail");
        }

        if (testObject instanceof IntTraceValue) {
            IntTraceValue intTraceValue = (IntTraceValue) testObject;
            intTraceValue.__setTraceInt(1);
            int a = intTraceValue.__getTraceInt();
            Assert.assertEquals(1, a);
        } else {
            Assert.fail("IntTraceValue implements fail");
        }

        if (testObject instanceof DatabaseInfoTraceValue) {
            DatabaseInfoTraceValue databaseInfoTraceValue = (DatabaseInfoTraceValue) testObject;
            databaseInfoTraceValue.__setTraceDatabaseInfo(UnKnownDatabaseInfo.INSTANCE);
            DatabaseInfo databaseInfo = databaseInfoTraceValue.__getTraceDatabaseInfo();
            Assert.assertSame(UnKnownDatabaseInfo.INSTANCE, databaseInfo);
        } else {
            Assert.fail("DatabaseInfoTraceValue implements fail");
        }

        if (testObject instanceof BindValueTraceValue) {
            BindValueTraceValue bindValueTraceValue = (BindValueTraceValue) testObject;
            Map<Integer, String> integerStringMap = Collections.emptyMap();
            bindValueTraceValue.__setTraceBindValue(integerStringMap);
            Map<Integer, String> bindValueMap = bindValueTraceValue.__getTraceBindValue();
            Assert.assertSame(integerStringMap, bindValueMap);
        } else {
            Assert.fail("BindValueTraceValue implements fail");
        }

    }

    @Test
    public void testBeforeAddInterceptor() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String javassistClassName = "com.nhn.pinpoint.profiler.interceptor.bci.TestObject";

        final TestModifier testModifier = new TestModifier(loader.getInstrumentor(), loader.getAgent()) {

            @Override
            public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
                try {
                    logger.info("modify cl:{}", classLoader);

                    InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

                    Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.interceptor.TestBeforeInterceptor");
                    addInterceptor(interceptor);
                    logger.info(interceptor.getClass().getClassLoader().toString());
                    String methodName = "callA";
                    aClass.addInterceptor(methodName, null, interceptor);
                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    e.printStackTrace();
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
        testModifier.setTargetClass(javassistClassName);
        loader.addModifier(testModifier);
        loader.initialize();



        Class testObjectClazz = loader.loadClass(javassistClassName);
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        callA.invoke(testObject);


        Interceptor interceptor = testModifier.getInterceptor(0);
        assertEqualsIntField(interceptor, "call", 1);
        assertEqualsObjectField(interceptor, "className", "com.nhn.pinpoint.profiler.interceptor.bci.TestObject");
        assertEqualsObjectField(interceptor, "methodName", methodName);
        assertEqualsObjectField(interceptor, "args", null);

        assertEqualsObjectField(interceptor, "target", testObject);

    }

    private TestClassLoader getTestClassLoader() {
        PLoggerFactory.initialize(new Slf4jLoggerBinder());


        ProfilerConfig profilerConfig = new ProfilerConfig();
        profilerConfig.setApplicationServerType(ServiceType.TEST_STAND_ALONE);
        DefaultAgent agent = new MockAgent("", profilerConfig);

        return new TestClassLoader(agent);
    }

    public void assertEqualsIntField(Object target, String fieldName, int value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getField(fieldName);
        int anInt = field.getInt(target);
        Assert.assertEquals(anInt, value);
    }

    public void assertEqualsObjectField(Object target, String fieldName, Object value) throws NoSuchFieldException, IllegalAccessException {
        Field field = target.getClass().getField(fieldName);
        Object obj = field.get(target);
        Assert.assertEquals(obj, value);
    }


    @Test
    public void testBeforeAddInterceptorFormContextClassLoader() throws Exception {
        final TestClassLoader loader = getTestClassLoader();
        final String testClassObject = "com.nhn.pinpoint.profiler.interceptor.bci.TestObjectContextClassLoader";
        final TestModifier testModifier = new TestModifier(loader.getInstrumentor(), loader.getAgent()) {

            @Override
            public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
                try {
                    logger.info("modify cl:{}", classLoader);
                    InstrumentClass aClass = byteCodeInstrumentor.getClass(testClassObject);

                    Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.interceptor.TestBeforeInterceptor");
                    addInterceptor(interceptor);
                    logger.info(interceptor.getClass().getClassLoader().toString());
                    String methodName = "callA";
                    aClass.addInterceptor(methodName, null, interceptor);
                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }
        };
        testModifier.setTargetClass(testClassObject);
        loader.addModifier(testModifier);
        loader.initialize();



        Class testObjectClazz = loader.loadClass("com.nhn.pinpoint.profiler.interceptor.bci.TestObjectContextClassLoader");
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        callA.invoke(testObject);


        final Interceptor interceptor = testModifier.getInterceptor(0);
        assertEqualsIntField(interceptor, "call", 1);
        assertEqualsObjectField(interceptor, "className", "com.nhn.pinpoint.profiler.interceptor.bci.TestObjectContextClassLoader");
        assertEqualsObjectField(interceptor, "methodName", methodName);
        assertEqualsObjectField(interceptor, "args", null);

        assertEqualsObjectField(interceptor, "target", testObject);


    }

    @Test
    public void testAddAfterInterceptor() throws Exception {
        // TODO aClass.addInterceptorCallByContextClassLoader 코드의 테스트 케이스도 추가해야함.


        final TestClassLoader loader = getTestClassLoader();
        final String testClassObject = "com.nhn.pinpoint.profiler.interceptor.bci.TestObject2";
        final TestModifier testModifier = new TestModifier(loader.getInstrumentor(), loader.getAgent()) {

            @Override
            public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
                try {
                    logger.info("modify cl:{}", classLoader);
                    InstrumentClass aClass = byteCodeInstrumentor.getClass(testClassObject);

                    Interceptor interceptor = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.interceptor.TestAfterInterceptor");
                    addInterceptor(interceptor);
                    logger.info(interceptor.getClass().getClassLoader().toString());
                    String methodName = "callA";
                    aClass.addInterceptor(methodName, null, interceptor);

                    Interceptor interceptor2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.interceptor.TestAfterInterceptor");
                    addInterceptor(interceptor2);
                    String methodName2 = "callB";
                    aClass.addInterceptor(methodName2, null, interceptor2);

                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

        };
        testModifier.setTargetClass(testClassObject);
        loader.addModifier(testModifier);
        loader.initialize();



        Class testObjectClazz = loader.loadClass(testClassObject);
        final String methodName = "callA";
        logger.info("class:{}", testObjectClazz.toString());
        final Object testObject = testObjectClazz.newInstance();
        Method callA = testObjectClazz.getMethod(methodName);
        Object result = callA.invoke(testObject);


        Interceptor interceptor = testModifier.getInterceptor(0);
        assertEqualsIntField(interceptor, "call", 1);
        assertEqualsObjectField(interceptor, "className", testClassObject);
        assertEqualsObjectField(interceptor, "methodName", methodName);
        assertEqualsObjectField(interceptor, "args", null);

        assertEqualsObjectField(interceptor, "target", testObject);
        assertEqualsObjectField(interceptor, "result", result);


        final String methodName2 = "callB";
        Method callBMethod = testObject.getClass().getMethod(methodName2);
        callBMethod.invoke(testObject);

        Interceptor interceptor2 = testModifier.getInterceptor(1);
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
        logger.info("Descript null:{}", nullDescriptor);
    }

    @Test
    public void testLog() throws Exception {

        final TestClassLoader loader = getTestClassLoader();
        final String testClassObject = "com.nhn.pinpoint.profiler.interceptor.bci.TestLog";
        final TestModifier testModifier = new TestModifier(loader.getInstrumentor(), loader.getAgent()) {

            @Override
            public byte[] modify(ClassLoader classLoader, String className, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
                try {
                    logger.info("modify cl:{}", classLoader);
                    InstrumentClass aClass = byteCodeInstrumentor.getClass(testClassObject);

                    aClass.addDebugLogBeforeAfterMethod();
                    aClass.addDebugLogBeforeAfterConstructor();

                    return aClass.toBytecode();
                } catch (InstrumentException e) {
                    throw new RuntimeException(e.getMessage(), e);
                }
            }

        };
        testModifier.setTargetClass(testClassObject);
        loader.addModifier(testModifier);
        loader.initialize();



        Object testObject = loader.loadClass(testClassObject).newInstance();

        Method test = testObject.getClass().getMethod("test", null);
        test.invoke(testObject);

        Method testString = testObject.getClass().getMethod("test", new Class[]{String.class});
        testString.invoke(testObject, "method");

        Constructor<?> constructor = testObject.getClass().getConstructor(null);
        Object o = constructor.newInstance();

    }

    private Object createInstance(InstrumentClass aClass) throws InstrumentException {
        // ci서버에서 test가 2번이상 돌아갈 경우 이미 define된 class를 다시 define하려고 하여 문제가 발생할수 있음.
        // 일단 임시 방편으로 다시 define하려고 할 경우. 실패후 그냥 현재 cl에서 class를 찾는 코드로 변경함.
        // 좀더 장기적으로는 define할 class를 좀더 정확하게 지정하고 testclass도 지정할수 있도록 해야 될것으로 보임.
        try {
            Class<?> aClass1 = aClass.toClass();
            return aClass1.newInstance();
        } catch (InstantiationException e) {
            throw new InstrumentException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new InstrumentException(e.getMessage(), e);
        } catch (InstrumentException linkageError) {
            ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
            try {
                return contextClassLoader.loadClass(aClass.getName()).newInstance();
            } catch (Throwable e) {
                throw new InstrumentException(e.getMessage(), e);
            }
        }
    }

}
