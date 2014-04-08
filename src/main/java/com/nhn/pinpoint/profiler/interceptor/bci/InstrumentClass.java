package com.nhn.pinpoint.profiler.interceptor.bci;

import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.util.DepthScope;

import java.util.List;

/**
 * @author emeroad
 */
public interface InstrumentClass {

    boolean isInterface();

    String getName();

	boolean insertCodeBeforeMethod(String methodName, String[] args, String code);

	boolean insertCodeAfterMethod(String methodName, String[] args, String code);

    int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addConstructorInterceptor(String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException;


	int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addScopeInterceptor(String methodName, String[] args, Interceptor interceptor, DepthScope scope) throws InstrumentException, NotFoundInstrumentException;

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

    List<Method> getDeclaredMethods() throws NotFoundInstrumentException;
	
	List<Method> getDeclaredMethods(MethodFilter methodFilter) throws NotFoundInstrumentException;
	
	public boolean isInterceptable();
	
	boolean hasDeclaredMethod(String methodName, String[] args);
}
