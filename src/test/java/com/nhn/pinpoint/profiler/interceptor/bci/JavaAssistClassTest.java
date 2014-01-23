package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.profiler.interceptor.TestAfterInterceptor;
import com.nhn.pinpoint.profiler.interceptor.TestBeforeInterceptor;
import javassist.bytecode.Descriptor;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * @author emeroad
 */
public class JavaAssistClassTest {
    private Logger logger = LoggerFactory.getLogger(JavaAssistByteCodeInstrumentor.class.getName());

    @Test
    public void testBeforeAddInterceptor() throws Exception {

        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.nhn.pinpoint.profiler.interceptor.bci.TestObject");

        TestBeforeInterceptor interceptor = new TestBeforeInterceptor();
        String methodName = "callA";

        aClass.addInterceptor(methodName, null, interceptor);

        Object testObject = createInstance(aClass);
        Method callA = testObject.getClass().getMethod(methodName);
        callA.invoke(testObject);

        Assert.assertEquals(interceptor.call, 1);
        Assert.assertEquals(interceptor.className, "com.nhn.pinpoint.profiler.interceptor.bci.TestObject");
        Assert.assertEquals(interceptor.methodName, methodName);
        Assert.assertEquals(interceptor.args, null);
        Assert.assertEquals(interceptor.target, testObject);
        
    }

    @Test
    public void testBeforeAddInterceptorFormContextClassLoader() throws Exception {

        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.nhn.pinpoint.profiler.interceptor.bci.TestObjectContextClassLoader");

        TestBeforeInterceptor interceptor = new TestBeforeInterceptor();
        String methodName = "callA";

        aClass.addInterceptorCallByContextClassLoader(methodName, null, interceptor);

        Object testObject = createInstance(aClass);
        Method callA = testObject.getClass().getMethod(methodName);
        callA.invoke(testObject);

        Assert.assertEquals(interceptor.call, 1);
        Assert.assertEquals(interceptor.className, "com.nhn.pinpoint.profiler.interceptor.bci.TestObjectContextClassLoader");
        Assert.assertEquals(interceptor.methodName, methodName);
        Assert.assertEquals(interceptor.args, null);
        Assert.assertEquals(interceptor.target, testObject);

    }

    @Test
    public void testAddAfterInterceptor() throws Exception {
        // TODO aClass.addInterceptorCallByContextClassLoader 코드의 테스트 케이스도 추가해야함.

        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.nhn.pinpoint.profiler.interceptor.bci.TestObject2");

        TestAfterInterceptor callaInterceptor = new TestAfterInterceptor();
        String callA = "callA";
        aClass.addInterceptor(callA, null, callaInterceptor);

        // return type void test
        TestAfterInterceptor callbInterceptor = new TestAfterInterceptor();
        String callB = "callB";
        aClass.addInterceptor(callB, null, callbInterceptor);


        Object testObject = createInstance(aClass);
        Method callAMethod = testObject.getClass().getMethod(callA);
        Object result = callAMethod.invoke(testObject);

        Assert.assertEquals(callaInterceptor.call, 1);
        Assert.assertEquals(callaInterceptor.className, "com.nhn.pinpoint.profiler.interceptor.bci.TestObject2");
        Assert.assertEquals(callaInterceptor.methodName, callA);
        Assert.assertNull(callaInterceptor.args);
        Assert.assertEquals(callaInterceptor.target, testObject);
        Assert.assertEquals(callaInterceptor.result, result);

        Method callBMethod = testObject.getClass().getMethod(callB);
        callBMethod.invoke(testObject);

        Assert.assertEquals(callbInterceptor.call, 1);
        Assert.assertEquals(callbInterceptor.className, "com.nhn.pinpoint.profiler.interceptor.bci.TestObject2");
        Assert.assertEquals(callbInterceptor.methodName, callB);
        Assert.assertNull(callbInterceptor.args);
        Assert.assertEquals(callbInterceptor.target, testObject);
        Assert.assertNull(callbInterceptor.result);

    }

    @Test
    public void nullDescriptor() {
        String nullDescriptor = Descriptor.ofParameters(null);
        logger.info("Descript null:{}", nullDescriptor);
    }

    @Test
    public void testLog() throws Exception {
        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.nhn.pinpoint.profiler.interceptor.bci.TestLog");

        aClass.addDebugLogBeforeAfterMethod();
        aClass.addDebugLogBeforeAfterConstructor();


        Object testObject = createInstance(aClass);

        Method test = testObject.getClass().getMethod("test", null);
        test.invoke(testObject);

        Method testString = testObject.getClass().getMethod("test", new Class[]{String.class});
        testString.invoke(testObject, "method");

        Constructor<? extends Object> constructor = testObject.getClass().getConstructor(null);
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
