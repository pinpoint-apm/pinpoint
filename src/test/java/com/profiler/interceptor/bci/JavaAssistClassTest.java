package com.profiler.interceptor.bci;

import com.profiler.interceptor.StaticAfterInterceptor;
import com.profiler.interceptor.StaticBeforeInterceptor;
import org.junit.Test;

import java.lang.reflect.Method;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaAssistClassTest {
    private Logger logger = Logger .getLogger(JavaAssistByteCodeInstrumentor.class.getName());
    @Test
    public void testAddInterceptor() throws Exception {
        Logger.getLogger(JavaAssistByteCodeInstrumentor.class.getName()).setLevel(Level.FINE);
        Logger.getLogger(JavaAssistClass.class.getName()).setLevel(Level.FINE);


        JavaAssistByteCodeInstrumentor javaAssistByteCodeInstrumentor = new JavaAssistByteCodeInstrumentor();
        InstrumentClass aClass = javaAssistByteCodeInstrumentor.getClass("com.profiler.interceptor.bci.TestObject");


        StaticBeforeInterceptor staticBeforeInterceptor = new StaticBeforeInterceptor() {
            private Logger logger = Logger.getLogger(StaticBeforeInterceptor.class.getName());
            private int call = 0;
            @Override
            public void before(Object target, String className, String methodName, Object[] args) {
                logger.info("target:" + target);
                logger.info("className:" + className);
                logger.info("methodName:" + methodName);
                logger.info("args:" + Arrays.toString(args));
                call++;
            }
            public int getCall() {
                return call;
            }
        };

        aClass.addInterceptor("callA", null, staticBeforeInterceptor);

        Class aClass1 = aClass.toClass();

        Object o = aClass1.newInstance();
        Method aClass1Method = aClass1.getMethod("callA");
        aClass1Method.invoke(o, null);

         StaticAfterInterceptor staticAfterInterceptor = new StaticAfterInterceptor() {
            private Logger logger = Logger.getLogger(StaticBeforeInterceptor.class.getName());
            private int call = 0;
            @Override
            public void after(Object target, String className, String methodName, Object[] args, Object result) {
                logger.info("target:" + target);
                logger.info("className:" + className);
                logger.info("methodName:" + methodName);
                logger.info("args:" + Arrays.toString(args));
                call++;
            }
            public int getCall() {
                return call;
            }
        };
        aClass.addInterceptor("callA", null, staticAfterInterceptor);
        Object o2 = aClass1.newInstance();

        aClass1Method.invoke(o2, null);
    }


}
