package com.navercorp.pinpoint.profiler.instrument.mock;

import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;

/**
 * @author jaehong.kim
 */
public class SampleClass {

    public SampleClass() {
        this("foo");
    }

    public SampleClass(String s) {
        Interceptor interceptor = new ArgsArrayInterceptor();
        Object[] args = null;
        Object result = null;
        Throwable throwable = null;

        try {
            ((ArgsArrayInterceptor) interceptor).before(this, args);

            ((ArgsArrayInterceptor) interceptor).after(this, args, result, throwable);
        } catch (Throwable t) {
            throwable = t;
            ((ArgsArrayInterceptor) interceptor).after(this, args, result, t);
        }
    }

    public void voidMethod() {
        Interceptor interceptor = new ArgsArrayInterceptor();
        Object[] args = null;
        Object result = null;
        Throwable throwable = null;

        try {
            ((ArgsArrayInterceptor) interceptor).before(this, args);

            ((ArgsArrayInterceptor) interceptor).after(this, args, result, throwable);
        } catch (Throwable t) {
            throwable = t;
            ((ArgsArrayInterceptor) interceptor).after(this, args, result, t);
        }
    }
}
