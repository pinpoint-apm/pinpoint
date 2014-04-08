package com.nhn.pinpoint.profiler.interceptor;

import com.nhn.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.nhn.pinpoint.bootstrap.interceptor.TargetClassLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;


/**
 * @author emeroad
 */
public class TestBeforeInterceptor implements StaticAroundInterceptor, TargetClassLoader {
    static{
        System.out.println("load TestBeforeInterceptor cl:" + TestBeforeInterceptor.class.getClassLoader());
    }
        private Logger logger = LoggerFactory.getLogger(this.getClass().getName());

        public int call = 0;
        public Object target;
        public String className;
        public String methodName;
        public Object[] args;

        @Override
        public void before(Object target, String className, String methodName, String parameterDescription, Object[] args) {
            logger.info("before target:" + target  + " " + className + "." + methodName + parameterDescription + " args:" + Arrays.toString(args));
            this.target = target;
            this.className = className;
            this.methodName = methodName;
            this.args = args;
            call++;
        }

    @Override
    public void after(Object target, String className, String methodName, String parameterDescription, Object[] args, Object result) {

    }
}
