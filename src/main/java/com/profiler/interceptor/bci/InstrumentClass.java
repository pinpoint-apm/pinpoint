package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;

public interface InstrumentClass {
	boolean addInterceptor(String methodName, String[] args, Interceptor interceptor);

	boolean addDebugLogBeforeAfterMethod();

	boolean addDebugLogBeforeAfterConstructor();

	byte[] toBytecode();

	Class<?> toClass();
}
