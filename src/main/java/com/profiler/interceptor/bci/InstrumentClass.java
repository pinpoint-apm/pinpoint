package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;

public interface InstrumentClass {
    void addInterceptor(String methodName, String[] args, Interceptor interceptor);

    byte[] toBytecode();

    Class toClass();
}
