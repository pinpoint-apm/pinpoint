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

import java.lang.reflect.InvocationTargetException;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptPoint;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedSimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.GroupedStaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.interceptor.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.plugin.interceptor.InterceptorFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class JavassistMethod implements InstrumentMethod {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ProfilerPluginContext pluginContext;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final CtBehavior behavior;
    private final InstrumentClass declaringClass;
    private final MethodDescriptor descriptor;
    
    public JavassistMethod(ProfilerPluginContext pluginContext, InterceptorRegistryBinder interceptorRegistryBinder, InstrumentClass declaringClass, CtBehavior behavior) {
        this.pluginContext = pluginContext;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
        this.behavior = behavior;
        this.declaringClass = declaringClass;
        
        String[] parameterVariableNames = JavaAssistUtils.getParameterVariableName(behavior);
        int lineNumber = JavaAssistUtils.getLineNumber(behavior);

        DefaultMethodDescriptor descriptor = new DefaultMethodDescriptor(behavior.getDeclaringClass().getName(), behavior.getName(), getParameterTypes(), parameterVariableNames);
        descriptor.setLineNumber(lineNumber);
        
        this.descriptor = descriptor;
    }

    @Override
    public String getName() {
        return behavior.getName();
    }

    @Override
    public String[] getParameterTypes() {
        return JavaAssistUtils.parseParameterSignature(behavior.getSignature());
    }

    @Override
    public int getModifiers() {
        return behavior.getModifiers();
    }
    
    @Override
    public boolean isConstructor() {
        return behavior instanceof CtConstructor;
    }

    @Override
    public MethodDescriptor getDescriptor() {
        return descriptor;
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object... constructorArgs) throws InstrumentException {
        return addInterceptor(interceptorClassName, null, null, constructorArgs);
    }
    
    @Override
    public int addInterceptor(String interceptorClassName, InterceptorGroup group, Object... constructorArgs) throws InstrumentException {
        return addInterceptor(interceptorClassName, group, null, constructorArgs);
    }


    @Override
    public int addInterceptor(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object... constructorArgs) throws InstrumentException {
        try { 
            return addInterceptor0(interceptorClassName, group, policy, constructorArgs, InterceptPoint.AROUND);
        } catch (InstrumentException e) {
            throw e;
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptorClassName + " to " + behavior, e);            
        }
    }

    @Override
    public void addInterceptor(int interceptorId) throws InstrumentException {
        Interceptor interceptor = InterceptorRegistry.findInterceptor(interceptorId);
        boolean isStatic = interceptor instanceof StaticAroundInterceptor;
        
        try { 
            addInterceptor0(interceptorId, InterceptPoint.AROUND, isStatic);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior, e);
        }
    }
    
    private int addInterceptor0(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object[] constructorArgs, InterceptPoint type) throws CannotCompileException, NotFoundException, InstrumentException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Interceptor interceptor = createInterceptor(interceptorClassName, group, policy, constructorArgs);
        
        int interceptorId;
        boolean isStatic = false;
        
        if (interceptor instanceof StaticAroundInterceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addStaticInterceptor((StaticAroundInterceptor) interceptor);
            isStatic = true;
        } else if (interceptor instanceof SimpleAroundInterceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor((SimpleAroundInterceptor) interceptor);
        } else {
            throw new InstrumentException("unsupported interceptor type:" + interceptor);
        }
        
        addInterceptor0(interceptorId, type, isStatic);
        
        return interceptorId;
    }

    public int addInterceptor0(Interceptor interceptor, InterceptorGroup group, InterceptPoint type) throws InstrumentException {
        boolean isStatic;
        
        if (interceptor instanceof StaticAroundInterceptor) {
            isStatic = true;
        } else if (interceptor instanceof SimpleAroundInterceptor) {
            isStatic = false;
        } else {
            throw new InstrumentException("unsupported interceptor type:" + interceptor);
        }
        
        if (interceptor instanceof TraceContextSupport) {
            final TraceContext traceContext = pluginContext.getTraceContext();
            ((TraceContextSupport)interceptor).setTraceContext(traceContext);
        }
        if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
            ((ByteCodeMethodDescriptorSupport)interceptor).setMethodDescriptor(descriptor);
        }
        
        
        if (group != null) {
            if (isStatic) {
                interceptor = new GroupedStaticAroundInterceptor((StaticAroundInterceptor)interceptor, group, ExecutionPolicy.BOUNDARY);
            } else {
                interceptor = new GroupedSimpleAroundInterceptor((SimpleAroundInterceptor)interceptor, group, ExecutionPolicy.BOUNDARY);
            }
        }
        
        int interceptorId;
        
        if (isStatic) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addStaticInterceptor((StaticAroundInterceptor) interceptor);
        } else {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor((SimpleAroundInterceptor) interceptor);
        }
        
        try {
            addInterceptor0(interceptorId, type, isStatic);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        
        return interceptorId;
    }
    
    public void addInterceptor0(int interceptorId, InterceptPoint type) throws InstrumentException {
        Interceptor interceptor = InterceptorRegistry.findInterceptor(interceptorId);
        boolean isStatic = interceptor instanceof StaticAroundInterceptor;
        
        try { 
            addInterceptor0(interceptorId, type, isStatic);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior, e);
        }
    }


    
    private Interceptor createInterceptor(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object[] constructorArgs) {
        ClassLoader classLoader = declaringClass.getClassLoader();
        InterceptorFactory factory = new AnnotatedInterceptorFactory(pluginContext);
        Interceptor interceptor = factory.getInterceptor(classLoader, interceptorClassName, constructorArgs, group, policy, declaringClass, this);
        
        return interceptor;
    }
    
    private void addInterceptor0(int interceptorId, InterceptPoint type, boolean isStatic) throws CannotCompileException, NotFoundException {
        switch (type) {
        case AROUND:
            addBeforeInterceptor(interceptorId, isStatic);
            addAfterInterceptor(interceptorId, isStatic);
            break;
        case BEFORE:
            addBeforeInterceptor(interceptorId, isStatic);
            break;
        case AFTER:
            addAfterInterceptor(interceptorId, isStatic);
            break;
        default:
            throw new UnsupportedOperationException("unsupported type");
        }
    }

    private void addAfterInterceptor(int interceptorId, boolean isStatic) throws NotFoundException, CannotCompileException {
        final String returnType = getReturnType(behavior);
        final String target = getTargetIdentifier(behavior);

        final String[] parameterType = JavaAssistUtils.parseParameterSignature(behavior.getSignature());
        String parameterTypeString = null;
        if (isStatic) {
            parameterTypeString = JavaAssistUtils.getParameterDescription(parameterType);
        }
        final String parameterIdentifier = getParameterIdentifier(parameterType);

        final CodeBuilder after = new CodeBuilder();
        after.begin();

        if (isStatic) {
            after.format("  %1$s interceptor = %2$s.getStaticInterceptor(%3$d);", StaticAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), interceptorId);
            after.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, %6$s, null);", target, behavior.getDeclaringClass().getName(), behavior.getName(), parameterTypeString, parameterIdentifier, returnType);
        } else {
            after.format("  %1$s interceptor = %2$s.getSimpleInterceptor(%3$d);", SimpleAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), interceptorId);
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
        if (isStatic) {
            catchCode.format("  %1$s interceptor = %2$s.getStaticInterceptor(%3$d);", StaticAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), interceptorId);
            catchCode.format("  interceptor.after(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s, null, $e);", target, behavior.getDeclaringClass().getName(), behavior.getName(), parameterTypeString, parameterIdentifier);
        } else {
            catchCode.format("  %1$s interceptor = %2$s.getSimpleInterceptor(%3$d);", SimpleAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), interceptorId);
            catchCode.format("  interceptor.after(%1$s, %2$s, null, $e);", target, parameterIdentifier);
        }
        catchCode.append("  throw $e;");
        catchCode.end();
        String buildCatch = catchCode.toString();
        if (isDebug) {
            logger.debug("addAfterInterceptor catch behavior:{} code:{}", behavior.getLongName(), buildCatch);
        }
        CtClass th = behavior.getDeclaringClass().getClassPool().get("java.lang.Throwable");
        behavior.addCatch(buildCatch, th);

    }

    private void addBeforeInterceptor(int interceptorId, boolean isStatic) throws CannotCompileException, NotFoundException {
        final String target = getTargetIdentifier(behavior);

        final String[] parameterType = JavaAssistUtils.parseParameterSignature(behavior.getSignature());
        
        // If possible, use static data to reduce interceptor overhead.
        String parameterDescription = null;
        if (isStatic) {
            parameterDescription = JavaAssistUtils.getParameterDescription(parameterType);
        }
        final String parameterIdentifier = getParameterIdentifier(parameterType);

        CodeBuilder code = new CodeBuilder();
        code.begin();
        if (isStatic) {
            code.format("  %1$s interceptor = %2$s.getStaticInterceptor(%3$d);", StaticAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), interceptorId);
            code.format("  interceptor.before(%1$s, \"%2$s\", \"%3$s\", \"%4$s\", %5$s);", target, behavior.getDeclaringClass().getName(), behavior.getName(), parameterDescription, parameterIdentifier);
        } else {
            // Separated getStaticInterceptor() with getSimpleInterceptor() to remove type casting cost.
            code.format("  %1$s interceptor = %2$s.getSimpleInterceptor(%3$d);", SimpleAroundInterceptor.class.getName(), interceptorRegistryBinder.getInterceptorRegistryClassName(), interceptorId);
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
    
    private String getTargetIdentifier(CtBehavior behavior) {
        boolean staticMethod = JavaAssistUtils.isStaticBehavior(behavior);
        if (staticMethod) {
            return "null";
        } else {
            return "this";
        }
    }

    private String getReturnType(CtBehavior behavior) {
        if (behavior instanceof CtMethod) {
            final String signature = behavior.getSignature();
            if (isVoid(signature)) {
                return "null";
            }
        }
        return "($w)$_";
    }

    private boolean isVoid(String signature) {
        return signature.endsWith("V");
    }
    
}
