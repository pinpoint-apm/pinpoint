package com.profiler.interceptor;

import com.profiler.interceptor.StaticBeforeInterceptor;

import java.util.Arrays;
import java.util.logging.Logger;

public class TestBeforeInterceptor implements StaticBeforeInterceptor {
    private Logger logger = Logger.getLogger(this.getClass().getName());

        public int call = 0;
        public Object target;
        public String className;
        public String methodName;
        public Object[] args;

        @Override
        public void before(Object target, String className, String methodName, Object[] args) {
            logger.info("before target:" + target  + " className:" + className + " methodName:" + methodName + " args:" + Arrays.toString(args));
            this.target = target;
            this.className = className;
            this.methodName = methodName;
            this.args = args;
            call++;
        }
}
