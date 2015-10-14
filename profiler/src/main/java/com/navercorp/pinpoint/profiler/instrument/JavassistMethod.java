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

package com.navercorp.pinpoint.profiler.instrument;

import java.lang.reflect.Method;

import javassist.CannotCompileException;
import javassist.CtBehavior;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.Instrumentor;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.Group;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.registry.InterceptorRegistry;
import com.navercorp.pinpoint.common.util.Asserts;
import com.navercorp.pinpoint.profiler.context.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InvokeAfterCodeGenerator;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InvokeBeforeCodeGenerator;
import com.navercorp.pinpoint.profiler.instrument.interceptor.InvokeCodeGenerator;
import com.navercorp.pinpoint.profiler.interceptor.factory.AnnotatedInterceptorFactory;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

public class JavassistMethod implements InstrumentMethod {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final Instrumentor pluginContext;
    private final InterceptorRegistryBinder interceptorRegistryBinder;

    private final CtBehavior behavior;
    private final InstrumentClass declaringClass;
    private final MethodDescriptor descriptor;

    public JavassistMethod(Instrumentor pluginContext, InterceptorRegistryBinder interceptorRegistryBinder, InstrumentClass declaringClass, CtBehavior behavior) {
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
                return ((CtMethod) behavior).getReturnType().getName();
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
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        return addInterceptor0(interceptorClassName, null, null, null);
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(constructorArgs, "constructorArgs");
        return addInterceptor0(interceptorClassName, constructorArgs, null, null);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, String groupName) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(groupName, "groupName");
        final InterceptorGroup interceptorGroup = this.pluginContext.getInterceptorGroup(groupName);
        return addInterceptor0(interceptorClassName, null, interceptorGroup, null);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, InterceptorGroup group) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(group, "group");
        return addInterceptor0(interceptorClassName, null, group, null);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, String groupName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(groupName, "groupName");
        Asserts.notNull(executionPolicy, "executionPolicy");
        final InterceptorGroup interceptorGroup = this.pluginContext.getInterceptorGroup(groupName);
        return addInterceptor0(interceptorClassName, null, interceptorGroup, executionPolicy);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(group, "group");
        Asserts.notNull(executionPolicy, "executionPolicy");
        return addInterceptor0(interceptorClassName, null, group, executionPolicy);
    }


    @Override
    public int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, String groupName) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(constructorArgs, "constructorArgs");
        Asserts.notNull(groupName, "groupName");
        final InterceptorGroup interceptorGroup = this.pluginContext.getInterceptorGroup(groupName);
        return addInterceptor0(interceptorClassName, constructorArgs, interceptorGroup, null);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorGroup group) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(constructorArgs, "constructorArgs");
        Asserts.notNull(group, "group");
        return addInterceptor0(interceptorClassName, constructorArgs, group, null);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, String groupName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(constructorArgs, "constructorArgs");
        Asserts.notNull(groupName, "groupName");
        Asserts.notNull(executionPolicy, "executionPolicy");
        final InterceptorGroup interceptorGroup = this.pluginContext.getInterceptorGroup(groupName);
        return addInterceptor0(interceptorClassName, constructorArgs, interceptorGroup, executionPolicy);
    }

    @Override
    public int addGroupedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException {
        Asserts.notNull(interceptorClassName, "interceptorClassName");
        Asserts.notNull(constructorArgs, "constructorArgs");
        Asserts.notNull(group, "group");
        Asserts.notNull(executionPolicy, "executionPolicy");
        return addInterceptor0(interceptorClassName, constructorArgs, group, executionPolicy);
    }

    @Override
    public void addInterceptor(int interceptorId) throws InstrumentException {
        Interceptor interceptor = InterceptorRegistry.getInterceptor(interceptorId);

        try {
            addInterceptor0(interceptor, interceptorId);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior.getLongName(), e);
        }
    }

    private GroupInfo resolveGroupInfo(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy) {
        Class<? extends Interceptor> interceptorType = pluginContext.injectClass(declaringClass.getClassLoader(), interceptorClassName);

        if (group == null) {
            Group interceptorGroup = interceptorType.getAnnotation(Group.class);

            if (interceptorGroup != null) {
                String groupName = interceptorGroup.value();
                group = pluginContext.getInterceptorGroup(groupName);
                policy = interceptorGroup.executionPolicy();
            }
        }

        if (group == null) {
            policy = null;
        } else if (policy == null) {
            policy = ExecutionPolicy.BOUNDARY;
        }

        return new GroupInfo(group, policy);
    }

    private static class GroupInfo {
        private final InterceptorGroup group;
        private final ExecutionPolicy policy;

        public GroupInfo(InterceptorGroup group, ExecutionPolicy policy) {
            this.group = group;
            this.policy = policy;
        }

        public InterceptorGroup getGroup() {
            return group;
        }

        public ExecutionPolicy getPolicy() {
            return policy;
        }
    }

    // for internal api
    int addInterceptorInternal(String interceptorClassName, Object[] constructorArgs, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException {
        if (interceptorClassName == null) {
            throw new NullPointerException("interceptorClassName must not be null");
        }
        return addInterceptor0(interceptorClassName, constructorArgs, group, executionPolicy);
    }

    private int addInterceptor0(String interceptorClassName, Object[] constructorArgs, InterceptorGroup group, ExecutionPolicy executionPolicy) throws InstrumentException {
        try {
            GroupInfo groupInfo = resolveGroupInfo(interceptorClassName, group, executionPolicy);
            Interceptor interceptor = createInterceptor(interceptorClassName, groupInfo, constructorArgs);
            int interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(interceptor);

            addInterceptor0(interceptor, interceptorId);
            return interceptorId;
        } catch (CannotCompileException ccex) {
            throw new InstrumentException("Failed to add interceptor " + interceptorClassName + " to " + behavior.getLongName(), ccex);
        } catch (NotFoundException nex) {
            throw new InstrumentException("Failed to add interceptor " + interceptorClassName + " to " + behavior.getLongName(), nex);
        }
    }

    private Interceptor createInterceptor(String interceptorClassName, GroupInfo groupInfo, Object[] constructorArgs) {
        ClassLoader classLoader = declaringClass.getClassLoader();
        
        AnnotatedInterceptorFactory factory = new AnnotatedInterceptorFactory(pluginContext);
        Interceptor interceptor = factory.getInterceptor(classLoader, interceptorClassName, constructorArgs, groupInfo.getGroup(), groupInfo.getPolicy(), declaringClass, this);

        return interceptor;
    }
    
    private void addInterceptor0(Interceptor interceptor, int interceptorId) throws CannotCompileException, NotFoundException {
        StringBuilder initVars = new StringBuilder();
        
        String interceptorInstanceVar = InvokeCodeGenerator.getInterceptorVar(interceptorId);
        addLocalVariable(interceptorInstanceVar, Interceptor.class);
        initVars.append(interceptorInstanceVar);
        initVars.append(" = null;");
        
        int originalCodeOffset = insertBefore(-1, initVars.toString());

        boolean localVarsInitialized = false;
        
        int offset = addBeforeInterceptor(interceptor, interceptorId, originalCodeOffset);
        
        if (offset != -1) {
            localVarsInitialized = true;
            originalCodeOffset = offset;
        }

        addAfterInterceptor(interceptor, interceptorId, localVarsInitialized, originalCodeOffset);
    }
    
    private static Method findMethod(Class<?> interceptorClass, String name) {
        for (Method m : interceptorClass.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }

        return null;
    }

    private void addAfterInterceptor(Interceptor interceptor, int interceptorId, boolean localVarsInitialized, int originalCodeOffset) throws NotFoundException, CannotCompileException {
        Class<?> interceptorClass = interceptor.getClass();
        Method interceptorMethod = findMethod(interceptorClass, "after");

        if (interceptorMethod == null) {
            if (isDebug) {
                logger.debug("Skip adding after interceptor becuase the interceptor doesn't have after method: {}", interceptorClass.getName());
            }
            return;
        }
        
        
        InvokeAfterCodeGenerator catchGenerator = new InvokeAfterCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, pluginContext.getTraceContext(), localVarsInitialized, true);
        String catchCode = catchGenerator.generate();
        
        if (isDebug) {
            logger.debug("addAfterInterceptor catch behavior:{} code:{}", behavior.getLongName(), catchCode);
        }
        
        CtClass throwable = behavior.getDeclaringClass().getClassPool().get("java.lang.Throwable");
        insertCatch(originalCodeOffset, catchCode, throwable, "$e");

        
        InvokeAfterCodeGenerator afterGenerator = new InvokeAfterCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, pluginContext.getTraceContext(), localVarsInitialized, false);
        final String afterCode = afterGenerator.generate();

        if (isDebug) {
            logger.debug("addAfterInterceptor after behavior:{} code:{}", behavior.getLongName(), afterCode);
        }

        behavior.insertAfter(afterCode);
    }

    private int addBeforeInterceptor(Interceptor interceptor, int interceptorId, int pos) throws CannotCompileException, NotFoundException {
        Class<?> interceptorClass = interceptor.getClass();
        Method interceptorMethod = findMethod(interceptorClass, "before");

        if (interceptorMethod == null) {
            if (isDebug) {
                logger.debug("Skip adding before interceptor becuase the interceptor doesn't have before method: {}", interceptorClass.getName());
            }
            return -1;
        }

        InvokeBeforeCodeGenerator generator = new InvokeBeforeCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, pluginContext.getTraceContext());
        String beforeCode = generator.generate();

        if (isDebug) {
            logger.debug("addBeforeInterceptor before behavior:{} code:{}", behavior.getLongName(), beforeCode);
        }

        return insertBefore(pos, beforeCode);
    }

    private void addLocalVariable(String name, Class<?> type) throws CannotCompileException, NotFoundException {
        behavior.addLocalVariable(name, behavior.getDeclaringClass().getClassPool().get(type.getName()));
    }
    
    private int insertBefore(int pos, String src) throws CannotCompileException {
        if (isConstructor()) {
            return insertBeforeConstructor(pos, src);
        } else {
            return insertBeforeMethod(pos, src);
        }
    }

    private int insertBeforeMethod(int pos, String src) throws CannotCompileException {
        CtClass cc = behavior.getDeclaringClass();
        CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
        if (ca == null)
            throw new CannotCompileException("no method body");

        CodeIterator iterator = ca.iterator();
        Javac jv = new Javac(cc);
        try {
            int nvars = jv.recordParams(behavior.getParameterTypes(), Modifier.isStatic(getModifiers()));
            jv.recordParamNames(ca, nvars);
            jv.recordLocalVariables(ca, 0);
            jv.recordType(getReturnType0());
            jv.compileStmnt(src);
            Bytecode b = jv.getBytecode();
            int stack = b.getMaxStack();
            int locals = b.getMaxLocals();

            if (stack > ca.getMaxStack())
                ca.setMaxStack(stack);

            if (locals > ca.getMaxLocals())
                ca.setMaxLocals(locals);
            
            if (pos != -1) { 
                iterator.insertEx(pos, b.get());
            } else {
                pos = iterator.insertEx(b.get());
            }
            
            iterator.insert(b.getExceptionTable(), pos);
            behavior.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
            
            return pos + b.length();
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        } catch (CompileError e) {
            throw new CannotCompileException(e);
        } catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }
    
    private int insertBeforeConstructor(int pos, String src) throws CannotCompileException {
        CtClass cc = behavior.getDeclaringClass();

        CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
        CodeIterator iterator = ca.iterator();
        Bytecode b = new Bytecode(behavior.getMethodInfo().getConstPool(),
                                  ca.getMaxStack(), ca.getMaxLocals());
        b.setStackDepth(ca.getMaxStack());
        Javac jv = new Javac(b, cc);
        try {
            jv.recordParams(behavior.getParameterTypes(), false);
            jv.recordLocalVariables(ca, 0);
            jv.compileStmnt(src);
            ca.setMaxStack(b.getMaxStack());
            ca.setMaxLocals(b.getMaxLocals());
            iterator.skipConstructor();
            if (pos != -1) { 
                iterator.insertEx(pos, b.get());
            } else {
                pos = iterator.insertEx(b.get());
            }
            iterator.insert(b.getExceptionTable(), pos);
            behavior.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
            
            return pos + b.length();
        }
        catch (NotFoundException e) {
            throw new CannotCompileException(e);
        }
        catch (CompileError e) {
            throw new CannotCompileException(e);
        }
        catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }


    private void insertCatch(int from, String src, CtClass exceptionType, String exceptionName) throws CannotCompileException {
        CtClass cc = behavior.getDeclaringClass();
        ConstPool cp = behavior.getMethodInfo().getConstPool();
        CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
        CodeIterator iterator = ca.iterator();
        Bytecode b = new Bytecode(cp, ca.getMaxStack(), ca.getMaxLocals());
        b.setStackDepth(1);
        Javac jv = new Javac(b, cc);
        try {
            jv.recordParams(behavior.getParameterTypes(), Modifier.isStatic(getModifiers()));
            jv.recordLocalVariables(ca, from);
            int var = jv.recordVariable(exceptionType, exceptionName);
            b.addAstore(var);
            jv.compileStmnt(src);

            int stack = b.getMaxStack();
            int locals = b.getMaxLocals();

            if (stack > ca.getMaxStack())
                ca.setMaxStack(stack);

            if (locals > ca.getMaxLocals())
                ca.setMaxLocals(locals);

            int len = iterator.getCodeLength();
            int pos = iterator.append(b.get());

            ca.getExceptionTable().add(from, len, len, cp.addClassInfo(exceptionType));
            iterator.append(b.getExceptionTable(), pos);
            behavior.getMethodInfo().rebuildStackMapIf6(cc.getClassPool(), cc.getClassFile2());
        } catch (NotFoundException e) {
            throw new CannotCompileException(e);
        } catch (CompileError e) {
            throw new CannotCompileException(e);
        } catch (BadBytecode e) {
            throw new CannotCompileException(e);
        }
    }

    private CtClass getReturnType0() throws NotFoundException {
        return Descriptor.getReturnType(behavior.getMethodInfo().getDescriptor(),
                                        behavior.getDeclaringClass().getClassPool());
    }
}
