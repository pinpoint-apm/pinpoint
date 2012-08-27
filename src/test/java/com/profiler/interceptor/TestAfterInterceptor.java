package com.profiler.interceptor;

import java.util.Arrays;
import java.util.logging.Logger;

public class TestAfterInterceptor implements StaticAfterInterceptor {
    private Logger logger = Logger.getLogger(this.getClass().getName());

    public int call = 0;
    public Object target;
    public String className;
    public String methodName;
    public Object[] args;
    public Object result;

    @Override
    public void after(Object target, String className, String methodName, Object[] args, Object result) {
        logger.info("after target:" + target  + " className:" + className + " methodName:" + methodName + " args:" + Arrays.toString(args));
        this.target = target;
        this.className = className;
        this.methodName = methodName;
        this.args = args;
        call++;
        this.result = result;
    }
}
