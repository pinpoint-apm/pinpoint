/*
 * Copyright 2014 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.pinpoint.profiler.interceptor.bci;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.DefaultInterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.Type;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.LoggingInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.profiler.interceptor.DebugGroupDelegateSimpleInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.DebugGroupDelegateStaticInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.GroupDelegateSimpleInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.GroupDelegateStaticInterceptor;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.ApiUtils;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 */
public class JavaAssistClass implements InstrumentClass {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final JavaAssistByteCodeInstrumentor instrumentor;
    private final CtClass ctClass;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private static final int STATIC_INTERCEPTOR = 0;
    private static final int SIMPLE_INTERCEPTOR = 1;

    private static final int NOT_DEFINE_INTERCEPTOR_ID = -1;

    private static final String FIELD_PREFIX = "_$PINPOINT$_";
    private static final String SETTER_PREFIX = "_$PINPOINT$_set";
    private static final String GETTER_PREFIX = "_$PINPOINT$_get";
    private static final String MARKER_CLASS_NAME = "com.navercorp.pinpoint.bootstrap.interceptor.tracevalue.TraceValue";



    public JavaAssistClass(JavaAssistByteCodeInstrumentor instrumentor, CtClass ctClass, InterceptorRegistryBinder interceptorRegistryBinder) {
        this.instrumentor = instrumentor;
        this.ctClass = ctClass;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
    }

    @Override
    public boolean isInterface() {
        return this.ctClass.isInterface();
    }

    @Override
    public String getName() {
        return this.ctClass.getName();
    }

    @Override
    public String getSuperClass() {
        return this.ctClass.getClassFile2().getSuperclass();
    }

    @Override
    public String[] getInterfaces() {
        return this.ctClass.getClassFile2().getInterfaces();
    }

    @Override
    public boolean insertCodeBeforeConstructor(String[] args, String code) {
        try {
            CtConstructor constructor = getCtConstructor(args);
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
            CtConstructor constructor = getCtConstructor(args);
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
            CtClass type = getClassPool().get(variableType);
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

    @Override
    public void addTraceValue(Class<?> traceValue) throws InstrumentException {
        addTraceValue0(traceValue, null);
    }

    @Override
    public void addTraceValue(Class<?> traceValue, String initValue) throws InstrumentException {
        addTraceValue0(traceValue, initValue);
    }

    public void addTraceValue0(Class<?> traceValue, String initValue) throws InstrumentException {
        if (traceValue == null) {
            throw new NullPointerException("traceValue must not be null");
        }
        
        try {
            final CtClass ctValueHandler = instrumentor.getClass(traceValue.getClassLoader(), traceValue.getName());

            final java.lang.reflect.Method[] declaredMethods = traceValue.getDeclaredMethods();
            final String variableName = FIELD_PREFIX + ctValueHandler.getSimpleName();

            final CtField traceVariableType = determineTraceValueType(variableName, declaredMethods);

            boolean requiredField = false;
            for (java.lang.reflect.Method method : declaredMethods) {
                // TODO need to check duplicated getter/setter for the same type.
                if (isSetter(method)) {
                    CtMethod setterMethod = CtNewMethod.setter(method.getName(), traceVariableType);
                    ctClass.addMethod(setterMethod);
                    requiredField = true;
                } else if(isGetter(method)) {
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
        CtClass type = getClassPool().get(resolveType.getName());
        return new CtField(type, variableName, ctClass);
    }

    @Override
    public int addConstructorInterceptor(String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtConstructor behavior = getCtConstructor(args);
        return addInterceptor0(behavior, null, interceptor, NOT_DEFINE_INTERCEPTOR_ID, com.navercorp.pinpoint.bootstrap.instrument.Type.around);
    }

    @Override
    public int addConstructorInterceptor(String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtConstructor behavior = getCtConstructor(args);
        return addInterceptor0(behavior, null, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type);
    }

    @Override
    public int addAllConstructorInterceptor(Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException{
        return addAllConstructorInterceptor(interceptor, com.navercorp.pinpoint.bootstrap.instrument.Type.around);
    }

    @Override
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
            interceptorId = addInterceptor0(constructorList[0], null, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type);
        }
        if (length > 1) {
            for (int i = 1; i< length; i++) {
                addInterceptor0(constructorList[i], null, null, interceptorId, com.navercorp.pinpoint.bootstrap.instrument.Type.around);
            }
        }
        return interceptorId;
    }


    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getMethod(methodName, args);
        return addInterceptor0(behavior, methodName, interceptor, NOT_DEFINE_INTERCEPTOR_ID, com.navercorp.pinpoint.bootstrap.instrument.Type.around);
    }

    @Override
    public int addGroupInterceptor(String methodName, String[] args, Interceptor interceptor, String groupName) throws InstrumentException, NotFoundInstrumentException {
        final InterceptorGroupDefinition groupDefinition = new DefaultInterceptorGroupDefinition(groupName);
        return addGroupInterceptor(methodName, args, interceptor, groupDefinition);
    }

    @Override
    public int addGroupInterceptor(String methodName, String[] args, Interceptor interceptor, InterceptorGroupDefinition definition) throws InstrumentException, NotFoundInstrumentException {
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        if (definition == null) {
            throw new NullPointerException("definition must not be null");
        }
        final InterceptorGroupInvocation transaction = this.instrumentor.getInterceptorGroupTransaction(definition);
        interceptor = wrapGroupInterceptor(interceptor, transaction);
        return addInterceptor(methodName, args, interceptor);
    }


    @Override
    public int addGroupInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, String groupName) throws InstrumentException {
        final InterceptorGroupDefinition groupDefinition = new DefaultInterceptorGroupDefinition(groupName);
        return addGroupInterceptorIfDeclared(methodName, args, interceptor, groupDefinition);
    }

    @Override
    public int addGroupInterceptorIfDeclared(String methodName, String[] args, Interceptor interceptor, InterceptorGroupDefinition groupDefinition) throws InstrumentException {
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        if (groupDefinition == null) {
            throw new NullPointerException("groupDefinition must not be null");
        }

        final InterceptorGroupInvocation transaction = this.instrumentor.getInterceptorGroupTransaction(groupDefinition);

        if (hasDeclaredMethod(methodName, args)) {
            interceptor = wrapGroupInterceptor(interceptor, transaction);
            return addInterceptor(methodName, args, interceptor);
        } else {
            if (logger.isWarnEnabled()) {
                logger.warn("Method is not declared. class={}, methodName={}, args={}", ctClass.getName(), methodName, Arrays.toString(args));
            }
            return -1;
        }
    }

    private Interceptor wrapGroupInterceptor(Interceptor interceptor, InterceptorGroupInvocation transaction) {
        final Logger interceptorLogger = LoggerFactory.getLogger(interceptor.getClass());

        if (interceptor instanceof  SimpleAroundInterceptor) {
            if (interceptorLogger.isDebugEnabled()) {
                return new DebugGroupDelegateSimpleInterceptor((SimpleAroundInterceptor)interceptor, transaction);
            } else {
                return new GroupDelegateSimpleInterceptor((SimpleAroundInterceptor)interceptor, transaction);
            }
        }
        else if (interceptor instanceof StaticAroundInterceptor) {
            if (interceptorLogger.isDebugEnabled()) {
                return new DebugGroupDelegateStaticInterceptor((StaticAroundInterceptor)interceptor, transaction);
            } else {
                return new GroupDelegateStaticInterceptor((StaticAroundInterceptor)interceptor, transaction);
            }
        }
        throw new IllegalArgumentException("unknown TargetMethod ServiceTypeInfo:" + interceptor.getClass());

    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException {
        final CtBehavior behavior = getMethod(methodName, args);
        return addInterceptor0(behavior, methodName, null, interceptorId, com.navercorp.pinpoint.bootstrap.instrument.Type.around);
    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException {
        final CtBehavior behavior = getMethod(methodName, args);
        return addInterceptor0(behavior, methodName, null, interceptorId, type);
    }

    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor, Type type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final CtBehavior behavior = getMethod(methodName, args);
        return addInterceptor0(behavior, methodName, interceptor, NOT_DEFINE_INTERCEPTOR_ID, type);
    }


    private int addInterceptor0(CtBehavior behavior, String methodName, Interceptor interceptor, int interceptorId, Type type) throws InstrumentException, NotFoundInstrumentException {
        try {
            if (interceptor != null) {
                if (interceptor instanceof StaticAroundInterceptor) {
                    StaticAroundInterceptor staticAroundInterceptor = (StaticAroundInterceptor) interceptor;
                    interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addStaticInterceptor(staticAroundInterceptor);
                } else if (interceptor instanceof SimpleAroundInterceptor) {
                    SimpleAroundInterceptor simpleAroundInterceptor = (SimpleAroundInterceptor) interceptor;
                    interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor(simpleAroundInterceptor);
                } else {
                    throw new InstrumentException("unsupported TargetMethod ServiceTypeInfo:" + interceptor);
                }
                injectInterceptor(behavior, interceptor);

            } else {
                interceptor = InterceptorRegistry.findInterceptor(interceptorId);
            }

            if (interceptor instanceof StaticAroundInterceptor) {
                switch (type) {
                    case around:
                        addStaticAroundInterceptor(methodName, interceptorId, behavior);
                        break;
                    case before:
                        addStaticBeforeInterceptor(methodName, interceptorId, behavior);
                        break;
                    case after:
                        addStaticAfterInterceptor(methodName, interceptorId, behavior);
                        break;
                    default:
                        throw new UnsupportedOperationException("unsupported type");
                }
            } else if(interceptor instanceof SimpleAroundInterceptor) {
                switch (type) {
                    case around:
                        addSimpleAroundInterceptor(methodName, interceptorId, behavior);
                        break;
                    case before:
                        addSimpleBeforeInterceptor(methodName, interceptorId, behavior);
                        break;
                    case after:
                        addSimpleAfterInterceptor(methodName, interceptorId, behavior);
                        break;
                    default:
                        throw new UnsupportedOperationException("unsupported type");
                }
            } else {
                throw new IllegalArgumentException("unsupported");
            }
            return interceptorId;
        } catch (NotFoundException e) {
            throw new InstrumentException(getInterceptorName(interceptor) + " add fail. Cause:" + e.getMessage(), e);
        } catch (CannotCompileException e) {
            throw new InstrumentException(getInterceptorName(interceptor) + " add fail. Cause:" + e.getMessage(), e);
        }
    }

    private String getInterceptorName(Interceptor interceptor) {
        if (interceptor == null) {
            return "null";
        }
        return interceptor.getClass().getSimpleName();
    }

    private void injectInterceptor(CtBehavior behavior, Interceptor interceptor) throws NotFoundException {
        // First of all, traceContext must be injected.
        if (interceptor instanceof TraceContextSupport) {
            final TraceContext traceContext = instrumentor.getAgent().getTraceContext();
            ((TraceContextSupport)interceptor).setTraceContext(traceContext);
        }
        if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
            final MethodDescriptor methodDescriptor = createMethodDescriptor(behavior);
            ((ByteCodeMethodDescriptorSupport)interceptor).setMethodDescriptor(methodDescriptor);
        }
    }

    private MethodDescriptor createMethodDescriptor(CtBehavior behavior)  {
        DefaultMethodDescriptor methodDescriptor = new DefaultMethodDescriptor();

        String methodName = behavior.getName();
        methodDescriptor.setMethodName(methodName);

        methodDescriptor.setClassName(ctClass.getName());

        String[] parameterTypes = JavaAssistUtils.parseParameterSignature(behavior.getSignature());
        methodDescriptor.setParameterTypes(parameterTypes);

        String[] parameterVariableName = JavaAssistUtils.getParameterVariableName(behavior);
        methodDescriptor.setParameterVariableName(parameterVariableName);

        int lineNumber = JavaAssistUtils.getLineNumber(behavior);
        methodDescriptor.setLineNumber(lineNumber);

        String parameterDescriptor = ApiUtils.mergeParameterVariableNameDescription(parameterTypes, parameterVariableName);
        methodDescriptor.setParameterDescriptor(parameterDescriptor);

        String apiDescriptor = ApiUtils.mergeApiDescriptor(ctClass.getName(), methodName, parameterDescriptor);
        methodDescriptor.setApiDescriptor(apiDescriptor);

        return methodDescriptor;
    }

    private void addStaticAroundInterceptor(String methodName, int id, CtBehavior method) throws NotFoundException, CannotCompileException {
        addStaticBeforeInterceptor(methodName, id, method);
        addStaticAfterInterceptor(methodName, id, method);
    }

    private void addStaticBeforeInterceptor(String methodName, int id, CtBehavior behavior) throws CannotCompileException, NotFoundException {
        addBeforeInterceptor(methodName, id, behavior, STATIC_INTERCEPTOR);
    }

    private void addStaticAfterInterceptor(String methodName, int interceptorId, CtBehavior behavior) throws NotFoundException, CannotCompileException {
        addAfterInterceptor(methodName, interceptorId, behavior, STATIC_INTERCEPTOR);
    }

    private void addSimpleAroundInterceptor(String methodName, int interceptorId, CtBehavior behavior) throws NotFoundException, CannotCompileException {
        addSimpleBeforeInterceptor(methodName, interceptorId, behavior);
        addSimpleAfterInterceptor(methodName, interceptorId, behavior);
    }

    private void addSimpleBeforeInterceptor(String methodName, int interceptorId, CtBehavior behavior) throws NotFoundException, CannotCompileException {
        addBeforeInterceptor(methodName, interceptorId, behavior, SIMPLE_INTERCEPTOR);
    }

    private void addSimpleAfterInterceptor(String methodName, int interceptorId, CtBehavior behavior) throws NotFoundException, CannotCompileException {
        addAfterInterceptor(methodName, interceptorId, behavior, SIMPLE_INTERCEPTOR);
    }

    @Override
    public void weave(String adviceClassName, ClassLoader loader) throws InstrumentException {
        final NamedClassPool classPool = instrumentor.getClassPool(loader);
        CtClass adviceClass;
        try {
            adviceClass = classPool.get(adviceClassName);
        } catch (NotFoundException e) {
            throw new NotFoundInstrumentException(adviceClassName + " not found. Caused:" + e.getMessage(), e);
        }
        try {
            AspectWeaverClass weaverClass = new AspectWeaverClass();
            weaverClass.weaving(ctClass, adviceClass);
        } catch (CannotCompileException e) {
            throw new InstrumentException("weaving fail. sourceClassName:" + ctClass.getName() + " adviceClassName:" + adviceClassName + " Caused:" + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new InstrumentException("weaving fail. sourceClassName:" + ctClass.getName() + " adviceClassName:" + adviceClassName + " Caused:" + e.getMessage(), e);
        }
    }




    private void addAfterInterceptor(String methodName, int id, CtBehavior behavior, int interceptorType) throws NotFoundException, CannotCompileException {
        final String returnType = getReturnType(behavior);
        final String target = getTargetIdentifier(behavior);

        final String[] parameterType = JavaAssistUtils.parseParameterSignature(behavior.getSignature());
        String parameterTypeString = null;
        if (interceptorType == STATIC_INTERCEPTOR) {
            parameterTypeString = JavaAssistUtils.getParameterDescription(parameterType);
        }
        final String parameterIdentifier = getParameterIdentifier(parameterType);

        final CodeBuilder after = new CodeBuilder();
        after.begin();

        if (interceptorType == STATIC_INTERCEPTOR) {
            after.format("  %1$s interceptor = %2$s.getStaticInterceptor(%3$d);", StaticAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), id);
            after.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, null);", target, ctClass.getName(), methodName, parameterTypeString, parameterIdentifier, returnType);
        } else {
            after.format("  %1$s interceptor = %2$s.getSimpleInterceptor(%3$d);", SimpleAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), id);
            after.format("  interceptor.after(%1$s, %2$s, %3$s, null);", target, parameterIdentifier, returnType);
        }
        after.end();

        final String buildAfter = after.toString();
        if (isDebug) {
            logger.debug("addAfterInterceptor after behavior:{} code:{}", behavior.getLongName(), buildAfter);
        }
        behavior.insertAfter(buildAfter);


        CodeBuilder catchCode = new CodeBuilder();
        catchCode.begin();
        if (interceptorType == STATIC_INTERCEPTOR) {
            catchCode.format("  %1$s interceptor = %2$s.getStaticInterceptor(%3$d);", StaticAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), id);
            catchCode.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, null, $e);", target, ctClass.getName(), methodName, parameterTypeString, parameterIdentifier);
        } else {
            catchCode.format("  %1$s interceptor = %2$s.getSimpleInterceptor(%3$d);", SimpleAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), id);
            catchCode.format("  interceptor.after(%1$s, %2$s, null, $e);", target, parameterIdentifier);
        }
        catchCode.append("  throw $e;");
        catchCode.end();
        String buildCatch = catchCode.toString();
        if (isDebug) {
            logger.debug("addAfterInterceptor catch behavior:{} code:{}", behavior.getLongName(), buildCatch);
        }
        CtClass th = getClassPool().get("java.lang.Throwable");
        behavior.addCatch(buildCatch, th);

    }

    private ClassPool getClassPool() {
        return ctClass.getClassPool();
    }

    private String getTargetIdentifier(CtBehavior behavior) {
        boolean staticMethod = JavaAssistUtils.isStaticBehavior(behavior);
        if (staticMethod) {
            return "null";
        } else {
            return "this";
        }
    }

    public String getReturnType(CtBehavior behavior) {
        if (behavior instanceof CtMethod) {
            final String signature = behavior.getSignature();
            if (isVoid(signature)) {
                return "null";
            }
        }
        return "($w)$_";
    }

    public boolean isVoid(String signature) {
        return signature.endsWith("V");
    }


    private void addBeforeInterceptor(String methodName, int id, CtBehavior behavior, int interceptorType) throws CannotCompileException, NotFoundException {
        final String target = getTargetIdentifier(behavior);

        final String[] parameterType = JavaAssistUtils.parseParameterSignature(behavior.getSignature());
        
        // If possible, use static data to reduce interceptor overhead.
        String parameterDescription = null;
        if (interceptorType == STATIC_INTERCEPTOR) {
            parameterDescription = JavaAssistUtils.getParameterDescription(parameterType);
        }
        final String parameterIdentifier = getParameterIdentifier(parameterType);

        CodeBuilder code = new CodeBuilder();
        code.begin();
        if (interceptorType == STATIC_INTERCEPTOR) {
            code.format("  %1$s interceptor = %2$s.getStaticInterceptor(%3$d);", StaticAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), id);
            code.format("  interceptor.before(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s);", target, ctClass.getName(), methodName, parameterDescription, parameterIdentifier);
        } else {
            // Separated getStaticInterceptor() with getSimpleInterceptor() to remove type casting cost.
            code.format("  %1$s interceptor = %2$s.getSimpleInterceptor(%3$d);", SimpleAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), id);
            code.format("  interceptor.before(%1$s, %2$s);", target, parameterIdentifier);
        }
        code.end();
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

    private String getParameterIdentifier(String[] parameterTypes) {
        if (parameterTypes.length == 0) {
            return "null";
        }
        return "$args";
    }

    @Override
    public boolean addDebugLogBeforeAfterMethod() {
        final String className = this.ctClass.getName();
        final LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
        final int id = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor(loggingInterceptor);
        try {
            for (CtMethod method : ctClass.getDeclaredMethods()) {
                if (method.isEmpty()) {
                    if (isDebug) {
                        logger.debug("{} is empty.", method.getLongName());
                    }
                    continue;
                }
                String methodName = method.getName();
                addStaticAroundInterceptor(methodName, id, method);
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
     * Does not work properly. Cannot modify bytecode of constructor
     *
     * @return
     */
    @Deprecated
    public boolean addDebugLogBeforeAfterConstructor() {
        final String className = this.ctClass.getName();
        final LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
        final int id = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor(loggingInterceptor);
        try {
            for (CtConstructor constructor : ctClass.getConstructors()) {
                if (constructor.isEmpty()) {
                    if (isDebug) {
                        logger.debug("{} is empty.", constructor.getLongName());
                    }
                    continue;
                }
                String constructorName = constructor.getName();
                addStaticAroundInterceptor(constructorName, id, constructor);
            }
            return true;
        } catch (Exception e) {
            if (logger.isWarnEnabled()) {
                logger.warn(e.getMessage(), e);
            }
        }
        return false;
    }

    private CtMethod getMethod(String methodName, String[] args) throws NotFoundInstrumentException {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(args);
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            final String descriptor = method.getMethodInfo2().getDescriptor();
            // skip return type check
            if (descriptor.startsWith(jvmSignature)) {
                return method;
            }
        }
        throw new NotFoundInstrumentException(methodName + Arrays.toString(args) + " is not found in " + this.getName());
    }

    private CtConstructor getCtConstructor(String[] args) throws NotFoundInstrumentException {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(args);
        // constructor return type is void
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            final String descriptor = constructor.getMethodInfo2().getDescriptor();
            // skip return type check
            if (descriptor.startsWith(jvmSignature) && constructor.isConstructor()) {
                return constructor;
            }
        }
        throw new NotFoundInstrumentException("Constructor" + Arrays.toString(args) + " is not found in " + this.getName());
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

    public List<MethodInfo> getDeclaredMethods() {
        return getDeclaredMethods(SkipMethodFilter.FILTER);
    }


    public List<MethodInfo> getDeclaredMethods(MethodFilter methodFilter) {
        if (methodFilter == null) {
            throw new NullPointerException("methodFilter must not be null");
        }
        final CtMethod[] declaredMethod = ctClass.getDeclaredMethods();
        final List<MethodInfo> candidateList = new ArrayList<MethodInfo>(declaredMethod.length);
        for (CtMethod ctMethod : declaredMethod) {
            final MethodInfo method = new JavassistMethodInfo(ctMethod);
            if (methodFilter.accept(method)) {
                candidateList.add(method);
            }
        }
        
        return candidateList;
    }

    public MethodInfo getDeclaredMethod(String name, String[] parameterTypes) {
        final String methodSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        for (CtMethod ctMethod : ctClass.getDeclaredMethods()) {
            if (!ctMethod.getName().equals(name)) {
                continue;
            }
            final String descriptor = ctMethod.getMethodInfo2().getDescriptor();
            final boolean findMethod = descriptor.startsWith(methodSignature);
            if (findMethod) {
                return new JavassistMethodInfo(ctMethod);
            }
        }
        return null;
    }

    public MethodInfo getConstructor(String[] parameterTypes) {
        // Constructor signature
        final String constructorSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        for (CtConstructor ctConstructor : ctClass.getDeclaredConstructors()) {
           final String descriptor = ctConstructor.getMethodInfo2().getDescriptor();
           // end ()V
           final boolean findConstructor = descriptor.startsWith(constructorSignature);
           if (findConstructor) {
               return new JavassistMethodInfo(ctConstructor);
           }
        }
        return null;
    }


    public boolean isInterceptable() {
        return !ctClass.isInterface() && !ctClass.isAnnotation() && !ctClass.isModified();
    }

    @Override
    public boolean hasDeclaredMethod(String methodName, String[] args) {
        MethodInfo declaredMethod = getDeclaredMethod(methodName, args);
        if (declaredMethod == null) {
            return false;
        } else {
            return true;
        }
    }

   @Override
   public boolean hasMethod(String methodName, String[] parameterTypeArray, String returnType) {
       final String signature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypeArray, returnType);
       try {
           CtMethod m = ctClass.getMethod(methodName, signature);
           return m != null;
       } catch (NotFoundException e) {
           return false;
       }
   }
   
   @Override
   public boolean hasConstructor(String[] parameterTypeArray) {
       final String signature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypeArray, "void");
       try {
           CtConstructor c = ctClass.getConstructor(signature);
           return c != null;
       } catch (NotFoundException e) {
           return false;
       }
   }
   
   @Override
    public boolean hasField(String name, String type) {
        try {
            String vmType = type == null ? null : JavaAssistUtils.toJvmSignature(type);
            ctClass.getField(name, vmType);
        } catch (NotFoundException e) {
            return false;
        }
        
        return true;
    }

   @Override
   public InstrumentClass getNestedClass(String className) {
       CtClass[] nestedClasses;
       try {
           nestedClasses = ctClass.getNestedClasses();
       } catch (NotFoundException ex) {
           logger.warn("{} NestedClass Not Found {}", className, ex.getMessage(), ex);
           return null;
       }

       if (nestedClasses == null || nestedClasses.length == 0) {
           return null;
       }

       for (CtClass nested : nestedClasses) {
           if (nested.getName().equals(className)) {
               return new JavaAssistClass(this.instrumentor, nested, interceptorRegistryBinder);
           }
       }
       return null;
   }

    @Override
    public void addGetter(String getterName, String variableName, String variableType) throws InstrumentException {
        try {
            // FIXME Which is better? getField() or getDeclaredField()? getFiled() seems like better choice if we want to add getter to child classes.
            CtField traceVariable = ctClass.getField(variableName);
            CtMethod getterMethod = CtNewMethod.make("public " + traceVariable.getType().getName() + " " + getterName + "() { return " + variableName + "; }", ctClass);
            ctClass.addMethod(getterMethod);
        } catch (NotFoundException ex) {
            throw new InstrumentException(variableName + " addVariableAccessor fail. Cause:" + ex.getMessage(), ex);
        } catch (CannotCompileException ex) {
            throw new InstrumentException(variableName + " addVariableAccessor fail. Cause:" + ex.getMessage(), ex);
        }
    }
    
    @Override
    public void addGetter(Class<?> interfaceType, String fieldName) throws InstrumentException {
        java.lang.reflect.Method[] methods = interfaceType.getMethods();
        
        if (methods.length != 1) {
            throw new InstrumentException("Getter interface must have only one method: " + interfaceType.getName());
        }
        
        java.lang.reflect.Method getter = methods[0];
        
        if (getter.getParameterTypes().length != 0) {
            throw new InstrumentException("Getter interface method must be no-args and non-void: " + interfaceType.getName());
        }
        
        try {
            CtField field = ctClass.getField(fieldName);
            String expression;
            
            if (field.getType().isPrimitive()) {
                String fieldType = field.getType().getName();
                String wrapperType = getWrapperClassName(fieldType);
                expression = wrapperType + ".valueOf(" + fieldName + ")";
            } else {
                expression = fieldName;
            }
            
            CtMethod getterMethod = CtNewMethod.make("public " + getter.getReturnType().getName() + " " + getter.getName() + "() { return " + expression + "; }", ctClass);
            ctClass.addMethod(getterMethod);
        
            CtClass ctInterface = instrumentor.getClass(interfaceType.getClassLoader(), interfaceType.getName());
            ctClass.addInterface(ctInterface);
        } catch (NotFoundException ex) {
            throw new InstrumentException("Failed to add getter. No such field: " + fieldName, ex);
        } catch (Exception e) {
            // Cannot happen. Reaching here means a bug.   
            throw new InstrumentException("Fail to add getter: " + interfaceType.getName(), e);
        }
    }
    
    private String getWrapperClassName(String primitiveType) {
        if ("boolean".equals(primitiveType)) {
            return "java.lang.Boolean";
        } else if ("byte".equals(primitiveType)) {
            return "java.lang.Byte";
        } else if ("short".equals(primitiveType)) {
            return "java.lang.Short";
        } else if ("int".equals(primitiveType)) {
            return "java.lang.Integer";
        } else if ("long".equals(primitiveType)) {
            return "java.lang.Long";
        } else if ("float".equals(primitiveType)) {
            return "java.lang.Float";
        } else if ("double".equals(primitiveType)) {
            return "java.lang.Double";
        } else if ("void".equals(primitiveType)) {
            return "java.lang.Void";
        }

        throw new IllegalArgumentException(primitiveType);
    }
    
    private static CtMethod getMethod(CtClass ctClass, String methodName, String[] args) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(args);
        
        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            final String descriptor = method.getMethodInfo2().getDescriptor();
            if (descriptor.startsWith(jvmSignature)) {
                return method;
            }
        }
        
        return null;
    }
    
    @Override
    public void addDelegatorMethod(String methodName, String[] args) throws InstrumentException {
        if (getMethod(ctClass, methodName, args) != null) {
            throw new InstrumentException(getName() + "already have method(" + methodName  +").");
        }
        
        try {
            final CtClass superClass = ctClass.getSuperclass();
            CtMethod superMethod = getMethod(superClass, methodName, args);
            
            if (superMethod == null) {
                throw new NotFoundInstrumentException(methodName + Arrays.toString(args) + " is not found in " + superClass.getName());
            }
            
            CtMethod delegatorMethod = CtNewMethod.delegator(superMethod, ctClass);
            ctClass.addMethod(delegatorMethod);
        } catch (NotFoundException ex) {
            throw new InstrumentException(getName() + "don't have super class(" + getSuperClass()  +"). Cause:" + ex.getMessage(), ex);
        } catch (CannotCompileException ex) {
            throw new InstrumentException(methodName + " addDelegatorMethod fail. Cause:" + ex.getMessage(), ex);
        }
    }
}
