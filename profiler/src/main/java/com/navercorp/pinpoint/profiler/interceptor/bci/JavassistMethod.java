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
import javassist.Modifier;
import javassist.NotFoundException;
import javassist.bytecode.BadBytecode;
import javassist.bytecode.ByteArray;
import javassist.bytecode.Bytecode;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.CodeIterator;
import javassist.bytecode.ConstPool;
import javassist.bytecode.Descriptor;
import javassist.bytecode.LocalVariableAttribute;
import javassist.compiler.CompileError;
import javassist.compiler.Javac;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.context.TraceContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.ByteCodeMethodDescriptorSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptPoint;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorInstance;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptorRegistry;
import com.navercorp.pinpoint.bootstrap.interceptor.MethodDescriptor;
import com.navercorp.pinpoint.bootstrap.interceptor.SimpleAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.StaticAroundInterceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.TraceContextSupport;
import com.navercorp.pinpoint.bootstrap.interceptor.group.DefaultInterceptorInstance;
import com.navercorp.pinpoint.bootstrap.interceptor.group.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroupInvocation;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectRecipe;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginInstrumentContext;
import com.navercorp.pinpoint.bootstrap.plugin.annotation.Group;
import com.navercorp.pinpoint.profiler.interceptor.DefaultMethodDescriptor;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.plugin.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.plugin.objectfactory.InterceptorArgumentProvider;
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
        InterceptorInstance interceptor = InterceptorRegistry.findInterceptor(interceptorId);

        try {
            addInterceptor0(interceptor, interceptorId, InterceptPoint.AROUND);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior, e);
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

    private int addInterceptor0(String interceptorClassName, InterceptorGroup group, ExecutionPolicy policy, Object[] constructorArgs, InterceptPoint type) throws CannotCompileException, NotFoundException, InstrumentException, IllegalArgumentException, SecurityException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
        GroupInfo groupInfo = resolveGroupInfo(interceptorClassName, group, policy);
        Interceptor interceptor = createInterceptor(interceptorClassName, groupInfo, constructorArgs);

        DefaultInterceptorInstance holder = new DefaultInterceptorInstance(interceptor, groupInfo.getGroup(), groupInfo.getPolicy());
        int interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(holder);

        addInterceptor0(holder, interceptorId, type);

        return interceptorId;
    }

    public int addInterceptor0(Interceptor interceptor, InterceptorGroup group, InterceptPoint type) throws InstrumentException {
        if (!(interceptor instanceof StaticAroundInterceptor) && !(interceptor instanceof SimpleAroundInterceptor)) {
            throw new InstrumentException("unsupported interceptor type:" + interceptor);
        }

        if (interceptor instanceof TraceContextSupport) {
            final TraceContext traceContext = pluginContext.getTraceContext();
            ((TraceContextSupport) interceptor).setTraceContext(traceContext);
        }
        if (interceptor instanceof ByteCodeMethodDescriptorSupport) {
            ((ByteCodeMethodDescriptorSupport) interceptor).setMethodDescriptor(descriptor);
        }

        DefaultInterceptorInstance holder = new DefaultInterceptorInstance(interceptor, group, group == null ? null : ExecutionPolicy.BOUNDARY);
        int interceptorId = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addInterceptor(holder);

        try {
            addInterceptor0(holder, interceptorId, type);
        } catch (Exception e) {
            throw new InstrumentException(e);
        }

        return interceptorId;
    }

    public void addInterceptor0(int interceptorId, InterceptPoint type) throws InstrumentException {
        InterceptorInstance interceptor = InterceptorRegistry.findInterceptor(interceptorId);

        try {
            addInterceptor0(interceptor, interceptorId, type);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add interceptor " + interceptor.getClass().getName() + " to " + behavior, e);
        }
    }

    private Interceptor createInterceptor(String interceptorClassName, GroupInfo groupInfo, Object[] constructorArgs) {
        ClassLoader classLoader = declaringClass.getClassLoader();

        AutoBindingObjectFactory factory = new AutoBindingObjectFactory(pluginContext, classLoader);
        ObjectRecipe recipe = ObjectRecipe.byConstructor(interceptorClassName, constructorArgs);
        InterceptorArgumentProvider interceptorArgumentProvider = new InterceptorArgumentProvider(pluginContext.getTraceContext(), groupInfo.getGroup(), declaringClass, this);

        Interceptor interceptor = (Interceptor) factory.createInstance(recipe, interceptorArgumentProvider);

        return interceptor;
    }

    private void addInterceptor0(InterceptorInstance instance, int interceptorId, InterceptPoint point) throws CannotCompileException, NotFoundException {
        addLocalVariable(InvokeCodeGenerator.getInterceptorInstanceVar(interceptorId), InterceptorInstance.class);
        
        if (instance.getGroup() != null) {
            addLocalVariable(InvokeCodeGenerator.getInterceptorGroupInvocationVar(interceptorId), InterceptorGroupInvocation.class);
            point = InterceptPoint.AROUND;
        }
        
        int originalCodeOffset = -1;
        
        switch (point) {
        case AROUND:
            originalCodeOffset = addBeforeInterceptor(instance, interceptorId);
            addAfterInterceptor(instance, interceptorId, originalCodeOffset);
            break;
        case BEFORE:
            addBeforeInterceptor(instance, interceptorId);
            break;
        case AFTER:
            addAfterInterceptor(instance, interceptorId, originalCodeOffset);
            break;
        }
    }

    private static final Method findMethod(Class<?> interceptorClass, String name) {
        for (Method m : interceptorClass.getMethods()) {
            if (m.getName().equals(name)) {
                return m;
            }
        }

        return null;
    }

    private void addAfterInterceptor(InterceptorInstance instance, int interceptorId, int originalCodeOffset) throws NotFoundException, CannotCompileException {
        Class<?> interceptorClass = instance.getInterceptor().getClass();
        Method interceptorMethod = findMethod(interceptorClass, "after");

        if (interceptorMethod == null && instance.getGroup() == null) {
            if (isDebug) {
                logger.debug("Skip adding after interceptor becuase the interceptor doesn't have after method: {}", interceptorClass.getName());
            }
            return;
        }

        InvokeAfterCodeGenerator afterGenerator = new InvokeAfterCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, instance.getPolicy(), originalCodeOffset != -1, false);
        final String afterCode = afterGenerator.generate();

        if (isDebug) {
            logger.debug("addAfterInterceptor after behavior:{} code:{}", behavior.getLongName(), afterCode);
        }

        behavior.insertAfter(afterCode);

        InvokeAfterCodeGenerator catchGenerator = new InvokeAfterCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, instance.getPolicy(), originalCodeOffset != -1, true);
        String catchCode = catchGenerator.generate();

        if (isDebug) {
            logger.debug("addAfterInterceptor catch behavior:{} code:{}", behavior.getLongName(), catchCode);
        }

        CtClass throwable = behavior.getDeclaringClass().getClassPool().get("java.lang.Throwable");
        addCatch(originalCodeOffset, catchCode, throwable, "$e");
    }

    private int addBeforeInterceptor(InterceptorInstance instance, int interceptorId) throws CannotCompileException, NotFoundException {
        Class<?> interceptorClass = instance.getInterceptor().getClass();
        Method interceptorMethod = findMethod(interceptorClass, "before");

        if (interceptorMethod == null && instance.getGroup() == null) {
            if (isDebug) {
                logger.debug("Skip adding before interceptor becuase the interceptor doesn't have before method: {}", interceptorClass.getName());
            }
            return -1;
        }

        InvokeBeforeCodeGenerator generator = new InvokeBeforeCodeGenerator(interceptorId, interceptorClass, interceptorMethod, declaringClass, this, instance.getPolicy());
        String beforeCode = generator.generate();

        if (isDebug) {
            logger.debug("addBeforeInterceptor before behavior:{} code:{}", behavior.getLongName(), beforeCode);
        }

        if (behavior instanceof CtConstructor) {
            return insertBeforeConstructor(beforeCode);
        } else {
            return insertBefore(beforeCode);
        }
    }

    private void addLocalVariable(String name, Class<?> type) throws CannotCompileException, NotFoundException {
        behavior.addLocalVariable(name, behavior.getDeclaringClass().getClassPool().get(type.getName()));
    }

    private void setLocalVariableLength(int index) {
        CodeAttribute ca = behavior.getMethodInfo().getCodeAttribute();
        LocalVariableAttribute va = (LocalVariableAttribute) ca.getAttribute(LocalVariableAttribute.tag);
        byte[] info = va.get();

        ByteArray.write16bit(ca.iterator().getCodeLength(), info, index * 10 + 4);
    }

    private int insertBefore(String src) throws CannotCompileException {
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

            int pos = iterator.insertEx(b.get());
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
    
    public int insertBeforeConstructor(String src) throws CannotCompileException {
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
            int pos = iterator.insertEx(b.get());
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


    public void addCatch(int from, String src, CtClass exceptionType, String exceptionName) throws CannotCompileException {
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
