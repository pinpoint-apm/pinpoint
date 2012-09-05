package com.profiler.interceptor.bci;

import com.profiler.interceptor.Interceptor;

public interface InstrumentClass {

	boolean insertCodeBeforeMethod(String methodName, String[] args, String code);

	boolean insertCodeAfterMethod(String methodName, String[] args, String code);

	boolean addInterceptor(String methodName, String[] args, Interceptor interceptor);

	boolean addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type);

	boolean addDebugLogBeforeAfterMethod();

	boolean addDebugLogBeforeAfterConstructor();

	byte[] toBytecode();

	Class<?> toClass();

	boolean addTraceVariable(String variableName, String setterName, String getterName, String variableType);

	boolean insertCodeAfterConstructor(String[] args, String code);

	boolean insertCodeBeforeConstructor(String[] args, String code);
}
