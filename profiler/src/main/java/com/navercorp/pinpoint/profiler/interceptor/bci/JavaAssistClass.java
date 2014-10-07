package com.nhn.pinpoint.profiler.interceptor.bci;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.nhn.pinpoint.bootstrap.interceptor.*;
import com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue;
import com.nhn.pinpoint.profiler.util.ApiUtils;
import com.nhn.pinpoint.profiler.interceptor.*;
import com.nhn.pinpoint.profiler.util.JavaAssistUtils;

import com.nhn.pinpoint.profiler.util.Scope;
import javassist.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author emeroad
 * @author netspider
 */
public class JavaAssistClass implements InstrumentClass {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final JavaAssistByteCodeInstrumentor instrumentor;
    private final CtClass ctClass;
    private static final int STATIC_INTERCEPTOR = 0;
    private static final int SIMPLE_INTERCEPTOR = 1;

    private static final int NOT_DEFINE_INTERCEPTOR_ID = -1;

    private static final String FIELD_PREFIX = "__p";
    private static final String SETTER_PREFIX = "__set";
    private static final String GETTER_PREFIX = "__get";
    private static final String MARKER_CLASS_NAME = "com.nhn.pinpoint.bootstrap.interceptor.tracevalue.TraceValue";


    public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass) {
        this.instrumentor = instrumentor;
        this.ctClass = ctClass;
    }

    public boolean isInterface() {
        return this.ctClass.isInterface();
    }

    public String getName() {
        return this.ctClass.getName();
    }

    @Override
    public boolean insertCodeBeforeConstructor(String[] args, String code) {
        try {
            CtConstructor constructor = getConstructor(args);
            constructor.insertBefore(code);
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public boolean insertCodeAfterConstructor(String[] args, String code) {
        try {
            CtConstructor constructor = getConstructor(args);
            constructor.insertAfter(code);
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public boolean insertCodeBeforeMethod(String methodName, String[] args, String code) {
        try {
            CtMethod method = getMethod(methodName, args);
            method.insertBefore(code);
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return false;
        }
    }

    @Override
    public boolean insertCodeAfterMethod(String methodName, String[] args, String code) {
        try {
            CtMethod method = getMethod(methodName, args);
            method.insertAfter(code);
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
            return false;
        }
    }

    @Deprecated
    public void addTraceVariable(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException {
        addTraceVariable0(variableName, setterName, getterName, variableType, initValue);
    }

    @Deprecated
    public void addTraceVariable(String variableName, String setterName, String getterName, String variableType) throws InstrumentException {
        addTraceVariable0(variableName, setterName, getterName, variableType, null);
    }

    @Deprecated
    private void addTraceVariable0(String variableName, String setterName, String getterName, String variableType, String initValue) throws InstrumentException {
        try {
            CtClass type = instrumentor.getClassPool().get(variableType);
            CtField traceVariable = new CtField(type, variableName, ctClass);
            if (initValue == null) {
                ctClass.addField(traceVariable);
            } else {
                ctClass.addField(traceVariable, initValue);
            }
            if (setterName != null) {
                CtMethod setterMethod = CtNewMethod.setter(setterName, traceVariable);
                ctClass.addMethod(setterMethod);
            }
            if (getterName != null) {
                CtMethod getterMethod = CtNewMethod.getter(getterName, traceVariable);
                ctClass.addMethod(getterMethod);
            }
        } catch (NotFoundException e) {
            throw new InstrumentException(variableName + " addTraceVariable fail. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(variableName + " addTraceVariable fail. Cause:" + e.getMessage(), e);
        }
    }

    public void addTraceValue(Class<? extends TraceValue> traceValue) throws InstrumentException {
        addTraceValue0(traceValue, null);
    }

    public void addTraceValue(Class<? extends TraceValue> traceValue, String initValue) throws InstrumentException {
        addTraceValue0(traceValue, initValue);
    }

    public void addTraceValue0(Class<? extends TraceValue> traceValue, String initValue) throws InstrumentException {
        if (traceValue == null) {
            throw new NullPointerException("traceValue must not be null");
        }
        // testcase에서 classLoader가 다를수 있어서 isAssignableFrom으로 안함.
        // 추가로 수정하긴해야 될듯함.
        final boolean marker = checkTraceValueMarker(traceValue);
        if (!marker) {
            throw new InstrumentException(traceValue + " marker interface  not implements" );
        }

        try {
            final CtClass ctValueHandler = instrumentor.getClassPool().get(traceValue.getName());

            final java.lang.reflect.Method[] declaredMethods = traceValue.getDeclaredMethods();
            final String variableName = FIELD_PREFIX + ctValueHandler.getSimpleName();

            final CtField traceVariableType = determineTraceValueType(variableName, declaredMethods);

            boolean requiredField = false;
            for (java.lang.reflect.Method method : declaredMethods) {
                // 2개 이상의 중복일때를 체크하지 않았음.
                if (isSetter(method)) {
                    // setter
                    CtMethod setterMethod = CtNewMethod.setter(method.getName(), traceVariableType);
                    ctClass.addMethod(setterMethod);
                    requiredField = true;
                } else if(isGetter(method)) {
                    // getter
                    CtMethod getterMethod = CtNewMethod.getter(method.getName(), traceVariableType);
                    ctClass.addMethod(getterMethod);
                    requiredField = true;
                }
            }
            if (requiredField) {
                ctClass.addInterface(ctValueHandler);
                if (initValue == null) {
                    ctClass.addField(traceVariableType);
                } else {
                    ctClass.addField(traceVariableType, initValue);
                }
            }

        } catch (NotFoundException e) {
            throw new InstrumentException(traceValue + " implements fail. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(traceValue + " implements fail. Cause:" + e.getMessage(), e);
        }
    }



    private boolean checkTraceValueMarker(Class<?> traceValue) {
        for (Class<?> anInterface : traceValue.getInterfaces()) {
            if (MARKER_CLASS_NAME.equals(anInterface.getName())) {
                return true;
            }
        }
        return false;
    }

    private boolean isGetter(java.lang.reflect.Method method) {
        return method.getName().startsWith(GETTER_PREFIX);
    }

    private boolean isSetter(java.lang.reflect.Method method) {
        return method.getName().startsWith(SETTER_PREFIX);
    }

    private CtField determineTraceValueType(String variableName, java.lang.reflect.Method[] declaredMethods) throws NotFoundException, CannotCompileException, InstrumentException {
        Class<?> getterReturnType = null;
        Class<?> setterType = null;
        for (java.lang.reflect.Method method : declaredMethods) {
            if (isGetter(method)) {
                getterReturnType = method.getReturnType();
            } else if (isSetter(method)) {
                Class<?>[] parameterTypes = method.getParameterTypes();
                if (parameterTypes.length != 1) {
                    throw new InstrumentException("invalid setterParameter. parameterTypes:" + Arrays.toString(parameterTypes));
                }
                setterType = parameterTypes[0];
            }
        }
        if (getterReturnType == null && setterType == null) {
            throw new InstrumentException("getter or setter not found");
        }
        if (getterReturnType != null && setterType != null) {
            if(!getterReturnType.equals(setterType)) {
                throw new InstrumentException("invalid setter or getter parameter");
            }
        }
        Class<?> resolveType;
        if (getterReturnType != null) {
            resolveType = getterReturnType;
        } else {
            resolveType = setterType;
        }
        CtClass type = instrumentor.getClassPool().get(resolveType.getName());
        return new CtField(type, variableName, ctClass);
    }

    public int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getBehavior(null, args, interceptor, NOT_DEFINE_INTERCEPTOR_ID);
        return addInterceptor0(behavior, null, interceptor, NOT_DEFINE_INTERCEPTOR_ID, Type.around, false);
    }

    public int addConstructorInterceptor(String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getBehavior(null, args, interceptor, NOT_DEFINE_INTERCEPTOR_ID);
        return addInterceptor0(behavior, null, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type, false);
    }

    public int addAllConstructorInterceptor(Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException{
        return addAllConstructorInterceptor(interceptor, Type.around);
    }

    public int addAllConstructorInterceptor(Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtConstructor[] constructorList = ctClass.getConstructors();
        final int length = constructorList.length;
        if (length == 0) {
            throw new NotFoundInstrumentException("Constructor not found.");
        }
        int interceptorId = 0;
        if (length > 0) {
            interceptorId = addInterceptor0(constructorList[0], null, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type, false);
        }
        if (length > 1) {
            for (int i = 1; i< length; i++) {
                addInterceptor0(constructorList[i], null, null, interceptorId, Type.around, false);
            }
        }
        return interceptorId;
    }


    @Override
    public int addInterceptorCallByContextClassLoader(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getBehavior(methodName, args, interceptor, NOT_DEFINE_INTERCEPTOR_ID);
        return addInterceptor0(behavior, methodName, interceptor, NOT_DEFINE_INTERCEPTOR_ID, Type.around, true);
    }

    @Override
    public int addInterceptorCallByContextClassLoader(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getBehavior(methodName, args, interceptor, NOT_DEFINE_INTERCEPTOR_ID);
        return addInterceptor0(behavior, methodName, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type, true);
    }

    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getBehavior(methodName, args, interceptor, NOT_DEFINE_INTERCEPTOR_ID);
        return addInterceptor0(behavior, methodName, interceptor, NOT_DEFINE_INTERCEPTOR_ID, Type.around, false);
    }

    @Override
    public int addScopeInterceptor(String methodName, String[] args, Interceptor interceptor, String scopeName) throws InstrumentException, NotFoundInstrumentException {
        final Scope scope = this.instrumentor.getScope(scopeName);
        return addScopeInterceptor(methodName, args, interceptor, scope);
    }


    @Override
    public int addScopeInterceptor(String methodName, String[] args, Interceptor interceptor, Scope scope) throws InstrumentException, NotFoundInstrumentException {
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        if (scope == null) {
            throw new NullPointerException("scope must not be null");
        }
        interceptor = wrapScopeInterceptor(interceptor, scope);
        return addInterceptor(methodName, args, interceptor);
    }
    
	/*
	 * (non-Javadoc)
	 * @see com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass#addScopeInterceptorIfDeclared(java.lang.String, java.lang.String[], com.nhn.pinpoint.bootstrap.interceptor.Interceptor, com.nhn.pinpoint.profiler.util.DepthScope)
	 */
    @Override
    public int addScopeInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, Scope scope) throws InstrumentException {
    	if (methodName == null) {
    		throw new NullPointerException("methodName must not be null");
    	}
    	if (interceptor == null) {
    		throw new IllegalArgumentException("interceptor is null");
    	}
    	if (scope == null) {
    		throw new NullPointerException("scope must not be null");
    	}
    	if (hasDeclaredMethod(methodName, args)) {
    		interceptor = wrapScopeInterceptor(interceptor, scope);
    		return addInterceptor(methodName, args, interceptor);
    	} else {
			if (logger.isWarnEnabled()) {
				logger.warn("Method is not declared. class={}, methodName={}, args={}", ctClass.getName(), methodName, Arrays.toString(args));
			}
    		return -1;
    	}
    }

    @Override
    public int addScopeInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, String scopeName) throws InstrumentException {
        final Scope scope = this.instrumentor.getScope(scopeName);
        return addScopeInterceptorIfDeclared(methodName, args, interceptor, scope);
    }

    private Interceptor wrapScopeInterceptor(Interceptor interceptor, Scope scope) {
        final Logger interceptorLogger = LoggerFactory.getLogger(interceptor.getClass());

        if (interceptor instanceof  SimpleAroundInterceptor) {
            if (interceptorLogger.isDebugEnabled()) {
                return new DebugScopeDelegateSimpleInterceptor((SimpleAroundInterceptor)interceptor, scope);
            } else {
                return new ScopeDelegateSimpleInterceptor((SimpleAroundInterceptor)interceptor, scope);
            }
        }
        else if (interceptor instanceof StaticAroundInterceptor) {
            if (interceptorLogger.isDebugEnabled()) {
                return new DebugScopeDelegateStaticInterceptor((StaticAroundInterceptor)interceptor, scope);
            } else {
                return new ScopeDelegateStaticInterceptor((StaticAroundInterceptor)interceptor, scope);
            }
        }
        throw new IllegalArgumentException("unknown Interceptor Type:" + interceptor.getClass());

    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException {
        final CtBehavior behavior = getBehavior(methodName, args, null, interceptorId);
        return addInterceptor0(behavior, methodName, null, interceptorId, Type.around, false);
    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException {
        final CtBehavior behavior = getBehavior(methodName, args, null, interceptorId);
        return addInterceptor0(behavior, methodName, null, interceptorId, type, false);
    }

    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getBehavior(methodName, args, interceptor, NOT_DEFINE_INTERCEPTOR_ID);
        return addInterceptor0(behavior, methodName, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type, false);
    }

    private CtBehavior getBehavior(String methodName, String[] args) throws NotFoundException {
        if (methodName == null) {
            return getConstructor(args);
        }
        return getMethod(methodName, args);
    }

    private CtBehavior getBehavior(String methodName, String[] args, Interceptor interceptor, int interceptorId) throws NotFoundInstrumentException {
        try {
            return getBehavior(methodName, args);
        } catch (NotFoundException e) {
            // target method나 constructor를 차지 못했을 경우는 NotFoundInstrumentException을 던진다.
            if (interceptor == null) {
                throw new NotFoundInstrumentException(interceptorId + " add fail. Cause:" + e.getMessage(), e);
            } else {
                throw new NotFoundInstrumentException(interceptor.getClass().getSimpleName() + " add fail. Cause:" + e.getMessage(), e);
            }
        }
    }

    private int addInterceptor0(CtBehavior behavior, String methodName, Interceptor interceptor, int interceptorId, Type type, boolean useContextClassLoader) throws InstrumentException, NotFoundInstrumentException {
        try {
            if (interceptor != null) {
                if(interceptor instanceof StaticAroundInterceptor) {
                    StaticAroundInterceptor staticAroundInterceptor = (StaticAroundInterceptor) interceptor;
                    interceptorId = InterceptorRegistry.addInterceptor(staticAroundInterceptor);
                } else if(interceptor instanceof SimpleAroundInterceptor) {
                    SimpleAroundInterceptor simpleAroundInterceptor = (SimpleAroundInterceptor) interceptor;
                    interceptorId = InterceptorRegistry.addSimpleInterceptor(simpleAroundInterceptor);
                } else {
                    throw new InstrumentException("unsupported Interceptor Type:" + interceptor);
                }
                // traceContext는 가장먼제 inject되어야 한다.
                if (interceptor instanceof TraceContextSupport) {
                    ((TraceContextSupport)interceptor).setTraceContext(instrumentor.getAgent().getTraceContext());
                }
                if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
                    setMethodDescriptor(behavior, (ByteCodeMethodDescriptorSupport) interceptor);
                }
            } else {
                interceptor = InterceptorRegistry.findInterceptor(interceptorId);
            }
            // 이제는 aroundType 인터셉터만 받고 코드 인젝션을 별도 type으로 받아야 함.
            if (interceptor instanceof StaticAroundInterceptor) {
                switch (type) {
                    case around:
                        addStaticAroundInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
                        break;
                    case before:
                        addStaticBeforeInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
                        break;
                    case after:
                        addStaticAfterInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
                        break;
                    default:
                        throw new UnsupportedOperationException("unsupport type");
                }
            } else if(interceptor instanceof SimpleAroundInterceptor) {
                switch (type) {
                    case around:
                        addSimpleAroundInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
                        break;
                    case before:
                        addSimpleBeforeInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
                        break;
                    case after:
                        addSimpleAfterInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
                        break;
                    default:
                        throw new UnsupportedOperationException("unsupport type");
                }
            } else {
                throw new IllegalArgumentException("unsupported");
            }
            return interceptorId;
        } catch (NotFoundException e) {
            throw new InstrumentException(interceptor.getClass().getSimpleName() + " add fail. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(interceptor.getClass().getSimpleName() + "add fail. Cause:" + e.getMessage(), e);
        }
    }

    private void setMethodDescriptor(CtBehavior behavior, ByteCodeMethodDescriptorSupport interceptor) throws NotFoundException {
        DefaultMethodDescriptor methodDescriptor = new DefaultMethodDescriptor();

        String methodName = behavior.getName();
        methodDescriptor.setMethodName(methodName);

        methodDescriptor.setClassName(ctClass.getName());

        CtClass[] ctParameterTypes = behavior.getParameterTypes();
        String[] parameterTypes = JavaAssistUtils.getParameterType(ctParameterTypes);
        methodDescriptor.setParameterTypes(parameterTypes);

        String[] parameterVariableName = JavaAssistUtils.getParameterVariableName(behavior);
        methodDescriptor.setParameterVariableName(parameterVariableName);

        int lineNumber = JavaAssistUtils.getLineNumber(behavior);
        methodDescriptor.setLineNumber(lineNumber);

        String parameterDescriptor = ApiUtils.mergeParameterVariableNameDescription(parameterTypes, parameterVariableName);
        methodDescriptor.setParameterDescriptor(parameterDescriptor);

        String apiDescriptor = ApiUtils.mergeApiDescriptor(ctClass.getName(), methodName, parameterDescriptor);
        methodDescriptor.setApiDescriptor(apiDescriptor);

        interceptor.setMethodDescriptor(methodDescriptor);
    }

    private void addStaticAroundInterceptor(String methodName, int id, CtBehavior method, boolean useContextClassLoader) throws NotFoundException, CannotCompileException {
        addStaticBeforeInterceptor(methodName, id, method, useContextClassLoader);
        addStaticAfterInterceptor(methodName, id, method, useContextClassLoader);
    }

    private void addStaticBeforeInterceptor(String methodName, int id, CtBehavior behavior, boolean useContextClassLoader) throws CannotCompileException, NotFoundException {
        addBeforeInterceptor(methodName, id, behavior, useContextClassLoader, STATIC_INTERCEPTOR);
    }

    private void addStaticAfterInterceptor(String methodName, int interceptorId, CtBehavior behavior, boolean useContextClassLoader) throws NotFoundException, CannotCompileException {
        addAfterInterceptor(methodName, interceptorId, behavior, useContextClassLoader, STATIC_INTERCEPTOR);
    }

    private void addSimpleAroundInterceptor(String methodName, int interceptorId, CtBehavior behavior, boolean useContextClassLoader) throws NotFoundException, CannotCompileException {
        addSimpleBeforeInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
        addSimpleAfterInterceptor(methodName, interceptorId, behavior, useContextClassLoader);
    }

    private void addSimpleBeforeInterceptor(String methodName, int interceptorId, CtBehavior behavior, boolean useContextClassLoader) throws NotFoundException, CannotCompileException {
        addBeforeInterceptor(methodName, interceptorId, behavior, useContextClassLoader, SIMPLE_INTERCEPTOR);
    }

    private void addSimpleAfterInterceptor(String methodName, int interceptorId, CtBehavior behavior, boolean useContextClassLoader) throws NotFoundException, CannotCompileException {
        addAfterInterceptor(methodName, interceptorId, behavior, useContextClassLoader, SIMPLE_INTERCEPTOR);
    }

	@Override
	public void weaving(String adviceClassName) throws InstrumentException {
		CtClass adviceClass;
		try {
			adviceClass = this.instrumentor.getClassPool().get(adviceClassName);
		} catch (NotFoundException e) {
			throw new NotFoundInstrumentException(adviceClassName + " not found. Caused:" + e.getMessage(), e);
		}
		try {
			AspectWeaverClass weaverClass = new AspectWeaverClass();
			weaverClass.weaving(ctClass, adviceClass);
		}  catch (CannotCompileException e) {
			throw new InstrumentException("weaving fail. sourceClassName:" + ctClass.getName() + " adviceClassName:" + adviceClassName + " Caused:" + e.getMessage(), e);
		} catch (NotFoundException e) {
			throw new InstrumentException("weaving fail. sourceClassName:" + ctClass.getName() + " adviceClassName:" + adviceClassName + " Caused:" + e.getMessage(), e);
		}
	}




    private void addAfterInterceptor(String methodName, int id, CtBehavior behavior, boolean useContextClassLoader, int interceptorType) throws NotFoundException, CannotCompileException {
        String returnType = getReturnType(behavior);
        String target = getTarget(behavior);

        String parameterTypeString = null;
        if (interceptorType == STATIC_INTERCEPTOR) {
            parameterTypeString = JavaAssistUtils.getParameterDescription(behavior.getParameterTypes());
        }
        final String parameter = getParameter(behavior);

        final CodeBuilder after = new CodeBuilder();
        if (useContextClassLoader) {
            after.begin();
            beginAddFindInterceptorCode(id, after, interceptorType);
            // TODO getMethod는 느림 캐쉬로 대체하던가 아니면 추가적인 방안이 필요함.
            if (interceptorType == STATIC_INTERCEPTOR) {
                after.append("  java.lang.Class[] methodArgsClassParams = new Class[]{java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class, java.lang.Object.class, java.lang.Throwable.class};");
            } else {
                after.append("  java.lang.Class[] methodArgsClassParams = new Class[]{java.lang.Object.class, java.lang.Object[].class, java.lang.Object.class, java.lang.Throwable.class};");
            }
            after.format("  java.lang.reflect.Method method = interceptor.getClass().getMethod(\"%1$s\", methodArgsClassParams);", "after");
            if (interceptorType == STATIC_INTERCEPTOR) {
                after.format("  java.lang.Object[] methodParams = new java.lang.Object[] { %1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, null };", target, ctClass.getName(), methodName, parameterTypeString, parameter, returnType);
            } else {
                after.format("  java.lang.Object[] methodParams = new java.lang.Object[] { %1$s, %2$s, %3$s, null };", target, parameter, returnType);
            }
            after.append("  method.invoke(interceptor, methodParams);");
            endAddFindInterceptorCode(after);
            after.end();
        } else {
            after.begin();

            if (interceptorType == STATIC_INTERCEPTOR) {
                after.format("  %1$s interceptor = com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAroundInterceptor.class.getName(), id);
                after.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, null);", target, ctClass.getName(), methodName, parameterTypeString, parameter, returnType);
            } else {
                after.format("  %1$s interceptor = com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry.getSimpleInterceptor(%2$d);", SimpleAroundInterceptor.class.getName(), id);
                after.format("  interceptor.after(%1$s, %2$s, %3$s, null);", target, parameter, returnType);
            }
            after.end();
        }
        final String buildAfter = after.toString();
        if (isDebug) {
            logger.debug("addAfterInterceptor after behavior:{} code:{}", behavior.getLongName(), buildAfter);
        }
        behavior.insertAfter(buildAfter);


        CodeBuilder catchCode = new CodeBuilder();
        if (useContextClassLoader) {
            catchCode.begin();
            beginAddFindInterceptorCode(id, catchCode, interceptorType);
            if(interceptorType == STATIC_INTERCEPTOR) {
                catchCode.append("  java.lang.Class[] methodArgsClassParams = new Class[]{java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class, java.lang.Object.class, java.lang.Throwable.class};");
            } else {
                catchCode.append("  java.lang.Class[] methodArgsClassParams = new Class[]{java.lang.Object.class, java.lang.Object[].class, java.lang.Object.class, java.lang.Throwable.class};");
            }
            catchCode.format("  java.lang.reflect.Method method = interceptor.getClass().getMethod(\"%1$s\", methodArgsClassParams);", "after");
            if (interceptorType == STATIC_INTERCEPTOR) {
                catchCode.format("  java.lang.Object[] methodParams = new java.lang.Object[] { %1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, null, $e };", target, ctClass.getName(), methodName, parameterTypeString, parameter);
            } else {
                catchCode.format("  java.lang.Object[] methodParams = new java.lang.Object[] { %1$s, %2$s, null, $e };", target, parameter);
            }
            catchCode.append("  method.invoke(interceptor, methodParams);");
            endAddFindInterceptorCode(catchCode);
            catchCode.append("  throw $e;");
            catchCode.end();
        } else {
            catchCode.begin();
            if (interceptorType == STATIC_INTERCEPTOR) {
                catchCode.format("  %1$s interceptor = com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAroundInterceptor.class.getName(), id);
                catchCode.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, null, $e);", target, ctClass.getName(), methodName, parameterTypeString, parameter);
            } else {
                catchCode.format("  %1$s interceptor = com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry.getSimpleInterceptor(%2$d);", SimpleAroundInterceptor.class.getName(), id);
                catchCode.format("  interceptor.after(%1$s, %2$s, null, $e);", target, parameter);
            }
            catchCode.append("  throw $e;");
            catchCode.end();
        }
        String buildCatch = catchCode.toString();
        if (isDebug) {
            logger.debug("addAfterInterceptor catch behavior:{} code:{}", behavior.getLongName(), buildCatch);
        }
        CtClass th = instrumentor.getClassPool().get("java.lang.Throwable");
        behavior.addCatch(buildCatch, th);

    }

    private void endAddFindInterceptorCode(CodeBuilder catchCode) {
        catchCode.format("}");
    }

    private void beginAddFindInterceptorCode(int id, CodeBuilder after, int interceptorType) {
        after.append("java.lang.ClassLoader contextClassLoader = java.lang.Thread.currentThread().getContextClassLoader();");
        after.append("if (contextClassLoader != null) {");
        after.append("  java.lang.Class interceptorRegistryClass = contextClassLoader.loadClass(\"com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry\");");
        if (interceptorType == STATIC_INTERCEPTOR) {
            after.append("  java.lang.reflect.Method getInterceptorMethod = interceptorRegistryClass.getMethod(\"getInterceptor\", new java.lang.Class[]{ int.class });");
        } else {
            after.append("  java.lang.reflect.Method getInterceptorMethod = interceptorRegistryClass.getMethod(\"getSimpleInterceptor\", new java.lang.Class[]{ int.class });");
        }
        after.format("  java.lang.Object[] interceptorParams = new java.lang.Object[] { java.lang.Integer.valueOf(%1$d) };", id);
        after.format("  java.lang.Object interceptor = getInterceptorMethod.invoke(interceptorRegistryClass, interceptorParams);");
    }

    private String getTarget(CtBehavior behavior) {
        boolean staticMethod = JavaAssistUtils.isStaticBehavior(behavior);
        if (staticMethod) {
            return "null";
        } else {
            return "this";
        }
    }

    public String getReturnType(CtBehavior behavior) throws NotFoundException {
        if (behavior instanceof CtMethod) {
            CtClass returnType = ((CtMethod) behavior).getReturnType();
            if (CtClass.voidType == returnType) {
                return "null";
            }
        }
        return "($w)$_";
    }


    private void addBeforeInterceptor(String methodName, int id, CtBehavior behavior, boolean useContextClassLoader, int interceptorType) throws CannotCompileException, NotFoundException {
        String target = getTarget(behavior);
        // 인터셉터 호출시 최대한 연산량을 줄이기 위해서 정보는 가능한 정적 데이터로 생성한다.
        String parameterDescription = null;
        if (interceptorType == STATIC_INTERCEPTOR) {
            parameterDescription = JavaAssistUtils.getParameterDescription(behavior.getParameterTypes());
        }
        String parameter = getParameter(behavior);

        CodeBuilder code = new CodeBuilder();
        if (useContextClassLoader) {
            code.begin();
//            java.lang.ClassLoader contextClassLoader = java.lang.Thread.currentThread().getContextClassLoader();
//            java.lang.Class<?> interceptorRegistryClass = contextClassLoader.loadClass("com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry");
//            java.lang.reflect.Method getInterceptorMethod = interceptorRegistryClass.getMethod("getInterceptor", int.class);
//            java.lang.Object interceptor = getInterceptorMethod.invoke(interceptorRegistryClass, 1);
//            java.lang.reflect.Method beforeMethod = interceptor.getClass().getMethod("before", java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class);
//            beforeMethod.invoke(interceptor, null, null, null, null, null);
//
            beginAddFindInterceptorCode(id, code, interceptorType);
            if(interceptorType == STATIC_INTERCEPTOR) {
                code.append("  java.lang.Class[] beforeMethodParams = new Class[]{java.lang.Object.class, java.lang.String.class, java.lang.String.class, java.lang.String.class, java.lang.Object[].class};");
            } else {
                code.append("  java.lang.Class[] beforeMethodParams = new Class[]{java.lang.Object.class, java.lang.Object[].class};");
            }
            code.format("  java.lang.reflect.Method beforeMethod = interceptor.getClass().getMethod(\"%1$s\", beforeMethodParams);", "before");
            if(interceptorType == STATIC_INTERCEPTOR) {
                code.format("  java.lang.Object[] beforeParams = new java.lang.Object[] { %1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s };", target, ctClass.getName(), methodName, parameterDescription, parameter);
            } else {
                code.format("  java.lang.Object[] beforeParams = new java.lang.Object[] { %1$s, %2$s };", target, parameter);
            }
            code.append("  beforeMethod.invoke(interceptor, beforeParams);");
            code.append("}");
            code.end();
        } else {
            code.begin();
            if (interceptorType == STATIC_INTERCEPTOR) {
                code.format("  %1$s interceptor = com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry.getInterceptor(%2$d);", StaticAroundInterceptor.class.getName(), id);
                code.format("  interceptor.before(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s);", target, ctClass.getName(), methodName, parameterDescription, parameter);
            } else {
                // simpleInterceptor인덱스에서 검색하여 typecasting을 제거한다.
                code.format("  %1$s interceptor = com.nhn.pinpoint.bootstrap.interceptor.InterceptorRegistry.getSimpleInterceptor(%2$d);", SimpleAroundInterceptor.class.getName(), id);
                code.format("  interceptor.before(%1$s, %2$s);", target, parameter);
            }
            code.end();
        }
        String buildBefore = code.toString();
        if (isDebug) {
            logger.debug("addStaticBeforeInterceptor catch behavior:{} code:{}", behavior.getLongName(), buildBefore);
        }

        if (behavior instanceof CtConstructor) {
            ((CtConstructor) behavior).insertBeforeBody(buildBefore);
        } else {
            behavior.insertBefore(buildBefore);
        }
    }

    private String getParameter(CtBehavior behavior) throws NotFoundException {
        CtClass[] parameterTypes = behavior.getParameterTypes();
        if (parameterTypes.length == 0) {
            return "null";
        }
        return "$args";
    }

    public boolean addDebugLogBeforeAfterMethod() {
        String className = this.ctClass.getName();
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
        int id = InterceptorRegistry.addInterceptor(loggingInterceptor);
        try {
            CtClass cc = this.instrumentor.getClassPool().get(className);
            CtMethod[] methods = cc.getDeclaredMethods();

            for (CtMethod method : methods) {
                if (method.isEmpty()) {
                    if (isDebug) {
                        logger.debug("{} is empty.", method.getLongName());
                    }
                    continue;
                }
                String methodName = method.getName();

                // TODO method의 prameter type을 interceptor에 별도 추가해야 될것으로 보임.
                String params = getParamsToString(method.getParameterTypes());
                addStaticAroundInterceptor(methodName, id, method, false);
            }
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return false;
    }

    /**
     * 제대로 동작안함 다시 봐야 될것 같음. 생성자일경우의 bytecode 수정시 에러가 남.
     *
     * @return
     */
    @Deprecated
    public boolean addDebugLogBeforeAfterConstructor() {
        String className = this.ctClass.getName();
        LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
        int id = InterceptorRegistry.addInterceptor(loggingInterceptor);
        try {
            CtClass cc = this.instrumentor.getClassPool().get(className);
            CtConstructor[] constructors = cc.getConstructors();

            for (CtConstructor constructor : constructors) {
                if (constructor.isEmpty()) {
                    if (isDebug) {
                        logger.debug("{} is empty.", constructor.getLongName());
                    }
                    continue;
                }
                String constructorName = constructor.getName();
                String params = getParamsToString(constructor.getParameterTypes());

                // constructor.insertAfter("{System.out.println(\"*****" +
                // constructorName + " Constructor:Param=(" + params +
                // ") is finished. \" + $args);}");
                // constructor.addCatch("{System.out.println(\"*****" +
                // constructorName + " Constructor:Param=(" + params +
                // ") is finished.\"); throw $e; }"
                // , instrumentor.getClassPool().get("java.lang.Throwable"));
                addStaticAroundInterceptor(constructorName, id, constructor, false);
            }
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return false;
    }

    private String getParamsToString(CtClass[] params) throws NotFoundException {
        StringBuilder sb = new StringBuilder(512);
        if (params.length != 0) {
            int paramsLength = params.length;
            for (int loop = paramsLength - 1; loop > 0; loop--) {
                sb.append(params[loop].getName()).append(",");
            }
        }
        String paramsStr = sb.toString();
        if (isDebug) {
            logger.debug("params type:{}", paramsStr);
        }
        return paramsStr;
    }

    private CtMethod getMethod(String methodName, String[] args) throws NotFoundException {
        CtClass[] params = JavaAssistUtils.getCtParameter(args, instrumentor.getClassPool());
        // cttime에는 직접 구현클래스를 조작해야 되므로 상속관계의 method를 찾으면 안됨.
        return ctClass.getDeclaredMethod(methodName, params);
    }

    private CtConstructor getConstructor(String[] args) throws NotFoundException {
        CtClass[] params = JavaAssistUtils.getCtParameter(args, instrumentor.getClassPool());
        return ctClass.getDeclaredConstructor(params);
    }


    @Override
    public byte[] toBytecode() {
        try {
            byte[] bytes = ctClass.toBytecode();
            ctClass.detach();
            return bytes;
        } catch (IOException e) {
            logger.info("IoException class:{} Caused:{}", ctClass.getName(),  e.getMessage(), e);
        } catch (CannotCompileException e) {
            logger.info("CannotCompileException class:{} Caused:{}", ctClass.getName(), e.getMessage(), e);
        }
        return null;
    }

    public Class<?> toClass() throws InstrumentException {
        try {
            return ctClass.toClass();
        } catch (CannotCompileException e) {
            throw new InstrumentException( "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
        }
    }

    public List<Method> getDeclaredMethods() throws NotFoundInstrumentException {
        return getDeclaredMethods(SkipMethodFilter.FILTER);
    }


	public List<Method> getDeclaredMethods(MethodFilter methodFilter) throws NotFoundInstrumentException {
        if (methodFilter == null) {
            throw new NullPointerException("methodFilter must not be null");
        }
        try {
            final CtMethod[] declaredMethod = ctClass.getDeclaredMethods();
            final List<Method> candidateList = new ArrayList<Method>(declaredMethod.length);
            for (CtMethod ctMethod : declaredMethod) {
                if (methodFilter.filter(ctMethod)) {
                    continue;
                }
                String methodName = ctMethod.getName();
                CtClass[] paramTypes = ctMethod.getParameterTypes();
                String[] parameterType = JavaAssistUtils.getParameterType(paramTypes);
                Method method = new Method(methodName, parameterType);
                candidateList.add(method);
            }
            return candidateList;
        } catch (NotFoundException e) {
            throw new NotFoundInstrumentException("getDeclaredMethods(), Caused:" + e.getMessage(), e);
        }
    }
	
	public boolean isInterceptable() {
		return !ctClass.isInterface() && !ctClass.isAnnotation() && !ctClass.isModified();
	}

	@Override
	public boolean hasDeclaredMethod(String methodName, String[] args) {
		try {
			CtClass[] params = JavaAssistUtils.getCtParameter(args, instrumentor.getClassPool());
			CtMethod m = ctClass.getDeclaredMethod(methodName, params);
			return m != null;
		} catch (NotFoundException e) {
			return false;
		}
	}

    /**
     * 가능한 String methodName, String desc을 사용하자. 편한대신에 속도가 상대적으로 좀 느리다.
     * @param methodName
     * @param args
     * @return
     */
    @Override
    public boolean hasMethod(String methodName, String[] args) {
        final String parameterDescription = JavaAssistUtils.getParameterDescription(args);
        final CtMethod[] methods = ctClass.getMethods();
        for (CtMethod method : methods) {
            if (methodName.equals(method.getName()) && method.getMethodInfo2().getDescriptor().startsWith(parameterDescription)) {
                return true;
            }

        }
        return false;
    }

    @Override
    public boolean hasMethod(String methodName, String desc) {
        try {
            CtMethod m = ctClass.getMethod(methodName, desc);
            return m != null;
        } catch (NotFoundException e) {
            return false;
        }
    }
	
	@Override
	public InstrumentClass getNestedClass(String className) {
		try {
			CtClass[] nestedClasses = ctClass.getNestedClasses();

			if (nestedClasses == null || nestedClasses.length == 0) {
				return null;
			}
			
			for (CtClass nested : nestedClasses) {
				if (nested.getName().equals(className)) {
					return new JavaAssistClass(this.instrumentor, nested);
				}
			}
		} catch (NotFoundException e) {
			return null;
		}
		return null;
	}
	
	@Override
	public void addGetter(String getterName, String variableName, String variableType) throws InstrumentException {
		try {
			// FIXME getField, getDeclaredField둘 중 뭐가 나을지. 자식 클래스에 getter를 만들려면 getField가 나을 것 같기도 하고.
			CtField traceVariable = ctClass.getField(variableName);
			CtMethod getterMethod = CtNewMethod.getter(getterName, traceVariable);
			ctClass.addMethod(getterMethod);
		} catch (NotFoundException ex) {
			throw new InstrumentException(variableName + " addVariableAccessor fail. Cause:" + ex.getMessage(), ex);
		} catch (CannotCompileException ex) {
            throw new InstrumentException(variableName + " addVariableAccessor fail. Cause:" + ex.getMessage(), ex);
        }
	}
}
