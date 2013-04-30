package com.profiler.interceptor.bci;

import javassist.CtMethod;

import com.profiler.interceptor.Interceptor;

public interface InstrumentClass {

	boolean insertCodeBeforeMethod(String methodName, String[] args, String code);

	boolean insertCodeAfterMethod(String methodName, String[] args, String code);

    int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addConstructorInterceptor(String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException;


	int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int addInterceptorCallByContextClassLoader(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

	int addInterceptorCallByContextClassLoader(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

	boolean addDebugLogBeforeAfterMethod();

	boolean addDebugLogBeforeAfterConstructor();

	byte[] toBytecode() throws InstrumentException ;

	Class<?> toClass() throws InstrumentException;

    void addTraceVariable(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException;

	void addTraceVariable(String variableName, String setterName, String getterName, String variableType) throws InstrumentException;

	boolean insertCodeAfterConstructor(String[] args, String code);

	boolean insertCodeBeforeConstructor(String[] args, String code);
	
	public CtMethod[] getDeclaredMethods();
	
	public boolean isInterceptable();
}
