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
import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.analysis.ControlFlow.Catcher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
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
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.interceptor.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.plugin.interceptor.InterceptorFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class JavassistMethod implements InstrumentMethod {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ProfilerPluginInstrumentContext pluginContext;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final CtBehavior behavior;
    private final InstrumentClass declaringClass;
    private final MethodDescriptor descriptor;
    
    public JavassistMethod(ProfilerPluginInstrumentContext pluginContext, InterceptorRegistryBinder interceptorRegistryBinder, InstrumentClass declaringClass, CtBehavior behavior) {
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
    public String getReturnType() {
        if (behavior instanceof CtMethod) {
            try {
                return ((CtMethod)behavior).getReturnType().getName();
            } catch (NotFoundException e) {
                return null;
            }
        }
        
        return null;
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
        
        try { 
            addInterceptor0(interceptor, interceptorId, InterceptPoint.AROUND);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior, e);
        }
    }
    
    private int addInterceptor0(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object[] constructorArgs, InterceptPoint type) throws CannotCompileException, NotFoundException, InstrumentException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Interceptor interceptor = createInterceptor(interceptorClassName, group, policy, constructorArgs);
        
        int interceptorId;
        
        if (interceptor instanceof StaticAroundInterceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addStaticInterceptor((StaticAroundInterceptor) interceptor);
        } else if (interceptor instanceof SimpleAroundInterceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor((SimpleAroundInterceptor) interceptor);
        } else if (interceptor instanceof Interceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor((Interceptor) interceptor);
        } else {
            throw new InstrumentException("unsupported interceptor type:" + interceptor);
        }
        
        addInterceptor0(interceptor, interceptorId, type);
        
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
            addInterceptor0(interceptor, interceptorId, type);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }
        
        return interceptorId;
    }
    
    public void addInterceptor0(int interceptorId, InterceptPoint type) throws InstrumentException {
        Interceptor interceptor = InterceptorRegistry.findInterceptor(interceptorId);
        
        try { 
            addInterceptor0(interceptor, interceptorId, type);
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
    
    private void addInterceptor0(Interceptor interceptor, int interceptorId, InterceptPoint point) throws CannotCompileException, NotFoundException {
        switch (point) {
        case AROUND:
            addBeforeInterceptor(interceptor, interceptorId);
            addAfterInterceptor(interceptor, interceptorId);
            break;
        case BEFORE:
            addBeforeInterceptor(interceptor, interceptorId);
            break;
        case AFTER:
            addAfterInterceptor(interceptor, interceptorId);
            break;
        default:
            throw new UnsupportedOperationException("unsupported type");
        }
    }
    
    private static final Method getBefore(Class<?> interceptorClass) {
        for (Method m : interceptorClass.getMethods()) {
            if (m.getName().equals("before")) {
                return m;
            }
        }
        
        return null;
    }
    
    private static final Method getAfter(Class<?> interceptorClass) {
        for (Method m : interceptorClass.getMethods()) {
            if (m.getName().equals("after")) {
                return m;
            }
        }
        
        return null;
    }

    private void addAfterInterceptor(Interceptor interceptor, int interceptorId) throws NotFoundException, CannotCompileException {
        Class<?> interceptorClass = interceptor.getClass();
        Method interceptorMethod = getAfter(interceptorClass);
        
        if (interceptorMethod == null) {
            if (isDebug) {
                logger.debug("Skip adding after interceptor becuase the interceptor doesn't have after method: {}", interceptorClass.getName());
            }
            return;
        }
        
        InterceptorInvokeCodeGenerator after = new InterceptorInvokeCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, false);
        final String buildAfter = after.generate();
        
        if (isDebug) {
            logger.debug("addAfterInterceptor after behavior:{} code:{}", behavior.getLongName(), buildAfter);
        }
        
        behavior.insertAfter(buildAfter);

        
        InterceptorInvokeCodeGenerator catchCode = new InterceptorInvokeCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, true);
        String buildCatch = catchCode.generate();
        
        if (isDebug) {
            logger.debug("addAfterInterceptor catch behavior:{} code:{}", behavior.getLongName(), buildCatch);
        }
        
        CtClass throwable = behavior.getDeclaringClass().getClassPool().get("java.lang.Throwable");
        behavior.addCatch(buildCatch, throwable);
    }

    private void addBeforeInterceptor(Interceptor interceptor, int interceptorId) throws CannotCompileException, NotFoundException {
        Class<?> interceptorClass = interceptor.getClass();
        Method interceptorMethod = getBefore(interceptorClass);
        
        if (interceptorMethod == null) {
            if (isDebug) {
                logger.debug("Skip adding before interceptor becuase the interceptor doesn't have before method: {}", interceptorClass.getName());
            }
            return;
        }

        
        InterceptorInvokeCodeGenerator before = new InterceptorInvokeCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, false);
        String buildBefore = before.generate();
        
        if (isDebug) {
            logger.debug("addStaticBeforeInterceptor catch behavior:{} code:{}", behavior.getLongName(), buildBefore);
        }

        if (behavior instanceof CtConstructor) {
            ((CtConstructor) behavior).insertBeforeBody(buildBefore);
        } else {
            behavior.insertBefore(buildBefore);
        }
    }
}
