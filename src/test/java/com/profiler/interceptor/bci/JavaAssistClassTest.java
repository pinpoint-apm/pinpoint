package com.profiler.interceptor.bci;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.util.logging.Logger;

import javassist.bytecode.Descriptor;

import org.junit.Assert;
import org.junit.Test;

import com.profiler.interceptor.TestAfterInterceptor;
import com.profiler.interceptor.TestBeforeInterceptor;


public class JavaAssistClassTest {
    private Logger logger = Logger.getLogger(JavaAssistByteCodeInstrumentor.class.getName());

    @Test
    public void testBeforeAddInterceptor() throws Exception {

        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.profiler.interceptor.bci.TestObject");

        TestBeforeInterceptor interceptor = new TestBeforeInterceptor();
        String methodName = "callA";

        aClass.addInterceptor(methodName, null, interceptor);

        Object testObject = aClass.toClass().newInstance();
        Method callA = testObject.getClass().getMethod(methodName);
        callA.invoke(testObject);

        Assert.assertEquals(interceptor.call, 1);
        Assert.assertEquals(interceptor.className, "com.profiler.interceptor.bci.TestObject");
        Assert.assertEquals(interceptor.methodName, methodName);
        Assert.assertEquals(interceptor.args.length, 0);
        Assert.assertEquals(interceptor.target, testObject);
        
    }

    @Test
    public void testAddAfterInterceptor() throws Exception {

        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.profiler.interceptor.bci.TestObject2");

        TestAfterInterceptor callaInterceptor = new TestAfterInterceptor();
        String callA = "callA";
        aClass.addInterceptor(callA, null, callaInterceptor);

        // return type void test
        TestAfterInterceptor callbInterceptor = new TestAfterInterceptor();
        String callB = "callB";
        aClass.addInterceptor(callB, null, callbInterceptor);


        Object testObject = aClass.toClass().newInstance();
        Method callAMethod = testObject.getClass().getMethod(callA);
        Object result = callAMethod.invoke(testObject);

        Assert.assertEquals(callaInterceptor.call, 1);
        Assert.assertEquals(callaInterceptor.className, "com.profiler.interceptor.bci.TestObject2");
        Assert.assertEquals(callaInterceptor.methodName, callA);
        Assert.assertEquals(callaInterceptor.args.length, 0);
        Assert.assertEquals(callaInterceptor.target, testObject);
        Assert.assertEquals(callaInterceptor.result, result);

        Method callBMethod = testObject.getClass().getMethod(callB);
        callBMethod.invoke(testObject);

        Assert.assertEquals(callbInterceptor.call, 1);
        Assert.assertEquals(callbInterceptor.className, "com.profiler.interceptor.bci.TestObject2");
        Assert.assertEquals(callbInterceptor.methodName, callB);
        Assert.assertEquals(callbInterceptor.args.length, 0);
        Assert.assertEquals(callbInterceptor.target, testObject);
        Assert.assertNull(callbInterceptor.result);

    }

    @Test
    public void nullDescriptor() {
        String nullDescriptor = Descriptor.ofParameters(null);
        logger.info("Descript null:" + nullDescriptor);
    }

    @Test
    public void testLog() throws Exception {
        ByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.profiler.interceptor.bci.TestLog");

        aClass.addDebugLogBeforeAfterMethod();
        aClass.addDebugLogBeforeAfterConstructor();

        Object testObject = aClass.toClass().newInstance();

        Method test = testObject.getClass().getMethod("test", null);
        test.invoke(testObject);

        Method testString = testObject.getClass().getMethod("test", new Class[]{String.class});
        testString.invoke(testObject, "method");

        Constructor<? extends Object> constructor = testObject.getClass().getConstructor(null);
        Object o = constructor.newInstance();

    }

}
