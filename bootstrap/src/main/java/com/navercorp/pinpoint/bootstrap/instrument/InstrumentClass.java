package com.nhn.pinpoint.bootstrap.instrument;

import java.util.List;

import com.nhn.pinpoint.bootstrap.interceptor.Interceptor;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;

/**
 * @author emeroad
 * @author netspider
 */
public interface InstrumentClass {

    boolean isInterface();

    String getName();

    String getSuperClass();

    String[] getInterfaces();

    @Deprecated
    boolean insertCodeBeforeMethod(String methodName, String[] args, String code);

    @Deprecated
    boolean insertCodeAfterMethod(String methodName, String[] args, String code);

    @Deprecated
    int addAllConstructorInterceptor(Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    @Deprecated
    int addAllConstructorInterceptor(Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addConstructorInterceptor(String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException;

    int reuseInterceptor(String methodName, String[] args, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException;


	int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

    int addScopeInterceptor(String methodName, String[] args, Interceptor interceptor, String scopeName) throws InstrumentException, NotFoundInstrumentException;

    int addScopeInterceptor(String methodName, String[] args, Interceptor interceptor, Scope scope) throws InstrumentException, NotFoundInstrumentException;

    int addScopeInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, String scopeName) throws InstrumentException;
    /**
     * methodName, args가 일치하는 메소드가 클래스에 구현되어있는 경우에만 scope interceptor를 적용합니다.
     * 
     * @param methodName
     * @param args
     * @param interceptor
     * @param scope
     * @return
     * @throws InstrumentException
     */
    int addScopeInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, Scope scope) throws InstrumentException;

    int addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

    int addInterceptorCallByContextClassLoader(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException;

	int addInterceptorCallByContextClassLoader(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException;

	void weaving(String adviceClassName) throws InstrumentException;

	boolean addDebugLogBeforeAfterMethod();

	boolean addDebugLogBeforeAfterConstructor();

	byte[] toBytecode() throws InstrumentException ;

	Class<?> toClass() throws InstrumentException;

    /**
     * 대신 addTraceValue 를 사용하라.
     */
    @Deprecated
    void addTraceVariable(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException;

    /**
     * 대신 addTraceValue 를 사용하라.
     */
    @Deprecated
	void addTraceVariable(String variableName, String setterName, String getterName, String variableType) throws InstrumentException;

    void addTraceValue(Class<? extends TraceValue> traceValue, String initValue) throws InstrumentException;

    void addTraceValue(Class<? extends TraceValue> traceValue) throws InstrumentException;
    
	boolean insertCodeAfterConstructor(String[] args, String code);

	boolean insertCodeBeforeConstructor(String[] args, String code);

    List<MethodInfo> getDeclaredMethods();
	
	List<MethodInfo> getDeclaredMethods(MethodFilter methodFilter);
	
	MethodInfo getDeclaredMethod(String name, String[] parameterTypes);
	
	MethodInfo getConstructor(String[] parameterTypes);
	
	public boolean isInterceptable();
	
	boolean hasDeclaredMethod(String methodName, String[] args);

    boolean hasMethod(String methodName, String[] parameterTypeArray, String returnType);

    InstrumentClass getNestedClass(String className);

    void addGetter(String getterName, String fieldName, String fieldType) throws InstrumentException;
}
