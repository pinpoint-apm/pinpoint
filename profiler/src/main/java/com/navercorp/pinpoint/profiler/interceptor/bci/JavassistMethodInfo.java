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
import java.util.concurrent.atomic.AtomicInteger;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.MethodInfo;
import com.navercorp.pinpoint.bootstrap.instrument.Type;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorInvoker;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.DefaultProfilerPluginContext;
import com.navercorp.pinpoint.profiler.plugin.interceptor.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.plugin.interceptor.InterceptorFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class JavassistMethodInfo implements MethodInfo {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final DefaultProfilerPluginContext pluginContext;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final CtBehavior behavior;
    private final InstrumentClass declaringClass;
    private final MethodDescriptor descriptor;
    
    private static final AtomicInteger invokerId = new AtomicInteger();
    
    public JavassistMethodInfo(DefaultProfilerPluginContext pluginContext, InterceptorRegistryBinder interceptorRegistryBinder, InstrumentClass declaringClass, CtBehavior behavior) {
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
            return addInterceptor0(interceptorClassName, group, policy, constructorArgs, Type.around);
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
            addInterceptor0(interceptor, null, null, interceptorId, Type.around, isStatic);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior, e);
        }
    }
    
    private int addInterceptor0(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object[] constructorArgs, Type type) throws CannotCompileException, NotFoundException, InstrumentException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Interceptor interceptor = createInterceptor(interceptorClassName, null, null, constructorArgs);
        
        int interceptorId;
        boolean isStatic = false;

        if (interceptor instanceof StaticAroundInterceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addStaticInterceptor((StaticAroundInterceptor) interceptor);
            isStatic = true;
        } else if (interceptor instanceof SimpleAroundInterceptor) {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor((SimpleAroundInterceptor) interceptor);
        } else {
            interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(interceptor);
        }

        addInterceptor0(interceptor, group, policy, interceptorId, type, isStatic);
        
        return interceptorId;
    }
    
    private Class<?> createInterceptorInvoker(Interceptor interceptor, InterceptorGroup group, ExecutionPolicy policy, int interceptorId) throws CannotCompileException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        if (group != null && policy == null) {
            policy = ExecutionPolicy.BOUNDARY;
        }
        
        CtClass invoker = behavior.getDeclaringClass().getClassPool().makeClass("com.navercorp.pinpoint.profiler.generated.InterceptorInvoker" + interceptorId);
        
        String fieldDeclaration = "private static final " + interceptor.getClass().getName() + " INTERCEPTOR = (" + interceptor.getClass().getName() + ") "+ InterceptorRegistry.class.getName() + ".findInterceptor(" + interceptorId + ");";
        logger.debug(fieldDeclaration);
        
        CtField interceptorField = CtField.make(fieldDeclaration, invoker);
        invoker.addField(interceptorField);

        CtField groupField = CtField.make("private static " + InterceptorGroup.class.getName() + " group;", invoker);
        CtField policyField = CtField.make("private static " + ExecutionPolicy.class.getName() + " policy;", invoker);

        invoker.addField(groupField);
        invoker.addField(policyField);
        
        invoker.addMethod(CtNewMethod.make("public static void setGroup(" + InterceptorGroup.class.getName() + " g) { group = g; }", invoker));
        invoker.addMethod(CtNewMethod.make("public static void setPolicy(" + ExecutionPolicy.class.getName() + " p) { policy = p; }", invoker));

        
        StringBuilder beforeBuilder = new StringBuilder("public static void before(");
        Method before = getBefore(interceptor.getClass());
        
        int index = 0;
        
        for (Class<?> paramType : before.getParameterTypes()) {
            if (index != 0) {
                beforeBuilder.append(',');
            }
            

            String typeName = JavaAssistUtils.toPinpointParameterType(paramType);
            beforeBuilder.append(typeName);
            beforeBuilder.append(" arg");
            beforeBuilder.append(index);
            
            index++;
        }
        
        beforeBuilder.append(") { try { ");
        
        beforeBuilder.append("if (group.getCurrentInvocation().tryEnter(policy)) {");
        beforeBuilder.append("INTERCEPTOR.before(");
        
        for (int i = 0; i < index; i++) {
            if (i != 0) {
                beforeBuilder.append(',');
            }

            beforeBuilder.append("arg");
            beforeBuilder.append(i);
        }
        
        
        String global = InterceptorInvoker.class.getName();
        
        beforeBuilder.append("); } else { " + global + ".logger.log(java.util.logging.Level.FINE, \"tryEnter() returns false: interceptorGroupTransaction: " + group.getName() + ", policy: " + policy + ". Skip interceptor " + interceptor.getClass().getName() + "\"); }}");
        beforeBuilder.append("catch (Throwable t) {");
        beforeBuilder.append("    if (" + global + ".throwException) { throw new RuntimeException(t); }");
        beforeBuilder.append("    else { " + global + ".logger.log(java.util.logging.Level.WARNING, \"Excetpion occured from interceptor\", t); }");
        beforeBuilder.append("}}");
        
        String beforeBody = beforeBuilder.toString();
        logger.debug(beforeBody);
        invoker.addMethod(CtNewMethod.make(beforeBody, invoker));
        
        
        StringBuilder afterBuilder = new StringBuilder("public static void after(");
        Method after = getAfter(interceptor.getClass());
        
        index = 0;
        
        for (Class<?> paramType : after.getParameterTypes()) {
            if (index != 0) {
                afterBuilder.append(',');
            }
            
            String typeName = JavaAssistUtils.toPinpointParameterType(paramType);
            afterBuilder.append(typeName);
            afterBuilder.append(" arg");
            afterBuilder.append(index);
            
            index++;
        }
        
        afterBuilder.append(") { try { ");
        afterBuilder.append(InterceptorGroupInvocation.class.getName() + " transaction = group.getCurrentInvocation();");
        afterBuilder.append("if (transaction.canLeave(policy)) { INTERCEPTOR.after(");
        
        for (int i = 0; i < index; i++) {
            if (i != 0) {
                afterBuilder.append(',');
            }

            afterBuilder.append("arg");
            afterBuilder.append(i);
        }
        
        afterBuilder.append("); transaction.leave(policy);} else { " + global + ".logger.log(java.util.logging.Level.FINE, \"canLeave() returns false: interceptorGroupTransaction: " + group.getName() + ", policy: " + policy + ". Skip interceptor " + interceptor.getClass().getName() + "\"); }}");
        afterBuilder.append("catch (Throwable t) {");
        afterBuilder.append("    if (" + global + ".throwException) { throw new RuntimeException(t); }");
        afterBuilder.append("    else { " + global + ".logger.log(java.util.logging.Level.WARNING, \"Excetpion occured from interceptor\", t); }");
        afterBuilder.append("}}");
        
        String afterBody = afterBuilder.toString();
        logger.debug(afterBody);
        invoker.addMethod(CtNewMethod.make(afterBody, invoker));
        
        Class<?> result = invoker.toClass(declaringClass.getClassLoader(), null);
        logger.debug("Created class: " + result);
        
        result.getMethod("setGroup", InterceptorGroup.class).invoke(null, group);
        result.getMethod("setPolicy", ExecutionPolicy.class).invoke(null, policy);
        
        return result;
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
    
    private Interceptor createInterceptor(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object[] constructorArgs) {
        ClassLoader classLoader = declaringClass.getClassLoader();
        InterceptorFactory factory = new AnnotatedInterceptorFactory(pluginContext);
        Interceptor interceptor = factory.getInterceptor(classLoader, interceptorClassName, constructorArgs, group, policy, declaringClass, this);
        
        return interceptor;
    }
    
    
    private void addInterceptor0(Interceptor interceptor, InterceptorGroup group, ExecutionPolicy policy, int interceptorId, Type type, boolean isStatic) throws CannotCompileException, NotFoundException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        Class<?> invokerClass = createInterceptorInvoker(interceptor, group, policy, interceptorId);
        
        boolean legacy = interceptor instanceof SimpleAroundInterceptor;
        
        switch (type) {
        case around:
            addBeforeInterceptor(invokerClass, legacy, isStatic);
            addAfterInterceptor(invokerClass, legacy, isStatic);
            break;
        case before:
            addBeforeInterceptor(invokerClass, legacy, isStatic);
            break;
        case after:
            addAfterInterceptor(invokerClass, legacy, isStatic);
            break;
        default:
            throw new UnsupportedOperationException("unsupported type");
        }
    }

    private void addAfterInterceptor(Class<?> invokerClass, boolean legacy, boolean isStatic) throws NotFoundException, CannotCompileException {
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
            after.format("  %1$s.after(%2$s, \"%3$s\", \"%4$s\", \"%5$s\", %6$s, %7$s, null);", invokerClass.getName(), target, behavior.getDeclaringClass(), behavior.getName(), parameterTypeString, parameterIdentifier, returnType);
        } else {
            if (legacy) {
                after.format("  %1$s.after(%2$s, %3$s, %4$s, null);", invokerClass.getName(), target, parameterIdentifier, returnType);
            } else {
                after.format("  %1$s.after(%2$s, %3$s, null", invokerClass.getName(), target, returnType);
                
                Method invokerAfter = getAfter(invokerClass);
                
                int i = 0;
                int argNum = parameterType.length;
                int invokerArgNum = invokerAfter.getParameterTypes().length - 3;
                int matchNum = Math.min(argNum, invokerArgNum);
                
                for (; i < matchNum; i++) {
                    after.append(", $" + (i + 1));
                }
                
                for (; i < invokerArgNum; i++) {
                    after.append(", null");
                }

                after.append(");");
            }
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
            catchCode.format("  %1$s.after(%2$s, \"%3$s\", \"%4$s\", \"%5$s\", %6$s, null, $e);", invokerClass.getName(), target, behavior.getDeclaringClass(), behavior.getName(), parameterTypeString, parameterIdentifier);
        } else {
            if (legacy) {
                catchCode.format("  %1$s.after(%2$s, %3$s, null, $e);", invokerClass.getName(), target, parameterIdentifier);
            } else {
                catchCode.format("  %1$s.after(%2$s, null, $e", invokerClass.getName(), target);
                
                Method invokerAfter = getAfter(invokerClass);
                
                int i = 0;
                int argNum = parameterType.length;
                int invokerArgNum = invokerAfter.getParameterTypes().length - 3;
                int matchNum = Math.min(argNum, invokerArgNum);
                
                for (; i < matchNum; i++) {
                    catchCode.append(", $" + (i + 1));
                }
                
                for (; i < invokerArgNum; i++) {
                    catchCode.append(", null");
                }
                
                catchCode.append(");");
            }
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

    private void addBeforeInterceptor(Class<?> invokerClass, boolean legacy, boolean isStatic) throws CannotCompileException, NotFoundException {
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
            code.format("  %1$s.before(%2$s, \"%3$s\", \"%4$s\", \"%5$s\", %6$s);", invokerClass.getName(), target, behavior.getDeclaringClass().getName(), behavior.getName(), parameterDescription, parameterIdentifier);
        } else {
            if (legacy) {
                // Separated getStaticInterceptor() with getSimpleInterceptor() to remove type casting cost.
                code.format("  %1$s.before(%2$s, %3$s);", invokerClass.getName(), target, parameterIdentifier);
            } else {
                code.format("  %1$s.before(%2$s", invokerClass.getName(), target);
                
                Method before = getBefore(invokerClass);
                
                int i = 0;
                int argNum = parameterType.length;
                int invokerArgNum = before.getParameterTypes().length - 1;
                int matchNum = Math.min(argNum, invokerArgNum);
                
                for (; i < matchNum; i++) {
                    code.append(", $" + (i + 1));
                }
                
                for (; i < invokerArgNum; i++) {
                    code.append(", null");
                }
                
                code.append(");");
            }
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
