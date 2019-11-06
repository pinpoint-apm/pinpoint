package com.navercorp.pinpoint.profiler.instrument.mock;

import com.navercorp.pinpoint.bootstrap.interceptor.AroundInterceptor;

import java.util.Arrays;

public class ExceptionInterceptor implements AroundInterceptor {

    public static boolean before;
    public static boolean after;
    public static Object beforeTarget;
    public static Object[] beforeArgs;
    public static Object afterTarget;
    public static Object[] afterArgs;
    public static Object result;
    public static Throwable throwable;

    public static void clear() {
        before = false;
        after = false;
        beforeTarget = null;
        beforeArgs = null;
        afterTarget = null;
        afterArgs = null;
        result = null;
        throwable = null;
    }


    @Override
    public void before(Object target, Object[] args) {
        this.before = true;
        this.beforeTarget = target;
        this.beforeArgs = args;

        throw new RuntimeException("before exception");
    }

    @Override
    public void after(Object target, Object[] args, Object result, Throwable throwable) {
        this.after = true;
        this.afterTarget = target;
        this.afterArgs = args;
        this.result = result;
        this.throwable = throwable;

        throw new RuntimeException("after exception");
    }

    private String toArgs(Object[] args) {
        if (args == null) {
            return "null";
        }

        return Arrays.asList(args).toString();
    }

}
