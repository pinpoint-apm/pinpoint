/*
 * Copyright 2016 NAVER Corp.
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

import com.navercorp.pinpoint.bootstrap.instrument.ClassFilter;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentContext;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentMethod;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructors;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethods;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMClass implements InstrumentClass {
    private static final String FIELD_PREFIX = "_$PINPOINT$_";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final EngineComponent engineComponent;

    private final InstrumentContext pluginContext;

    private final ASMClassNodeAdapter classNode;
    private boolean modified = false;
    private String name;

    public ASMClass(EngineComponent engineComponent, final InstrumentContext pluginContext, final ClassLoader classLoader, ProtectionDomain protectionDomain, final ClassNode classNode) {
        this(engineComponent, pluginContext, new ASMClassNodeAdapter(pluginContext, classLoader, protectionDomain, classNode));
    }

    public ASMClass(EngineComponent engineComponent, final InstrumentContext pluginContext, final ASMClassNodeAdapter classNode) {
        this.engineComponent = Assert.requireNonNull(engineComponent, "engineComponent");
        this.pluginContext = pluginContext;
        this.classNode = Assert.requireNonNull(classNode, "classNode");
    }

    public ClassLoader getClassLoader() {
        return this.classNode.getClassLoader();
    }

    @Override
    public boolean isInterceptable() {
        if (isAnnotation() || isModified()) {
            return false;
        }
        // interface static method or default method is java 1.8 or later
        if (isInterface() && (this.classNode.getMajorVersion() < 52 || !JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_8))) {
            return false;
        }
        return true;
    }

    @Override
    public boolean isInterface() {
        return this.classNode.isInterface();
    }

    private boolean isAnnotation() {
        return this.classNode.isAnnotation();
    }

    @Override
    public String getName() {
        // for performance.
        if (this.name == null) {
            this.name = classNode.getName();
        }
        return this.name;
    }

    @Override
    public String getSuperClass() {
        return this.classNode.getSuperClassName();
    }

    @Override
    public String[] getInterfaces() {
        return this.classNode.getInterfaceNames();
    }

    @Override
    public InstrumentMethod getDeclaredMethod(final String name, final String... parameterTypes) {
        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        final ASMMethodNodeAdapter methodNode = this.classNode.getDeclaredMethod(name, desc);
        if (methodNode == null) {
            return null;
        }

        return new ASMMethod(this.engineComponent, this.pluginContext, this, methodNode);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return getDeclaredMethods(MethodFilters.ACCEPT_ALL);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(final MethodFilter methodFilter) {
        if (methodFilter == null) {
            throw new NullPointerException("methodFilter");
        }

        final List<InstrumentMethod> candidateList = new ArrayList<InstrumentMethod>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredMethods()) {
            final InstrumentMethod method = new ASMMethod(this.engineComponent, this.pluginContext, this, methodNode);
            if (methodFilter.accept(method)) {
                candidateList.add(method);
            }
        }

        return candidateList;
    }

    @Override
    public InstrumentMethod getConstructor(final String... parameterTypes) {
        return getDeclaredMethod("<init>", parameterTypes);
    }

    @Override
    public List<InstrumentMethod> getDeclaredConstructors() {
        final List<InstrumentMethod> candidateList = new ArrayList<InstrumentMethod>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredConstructors()) {
            final InstrumentMethod method = new ASMMethod(this.engineComponent, this.pluginContext, this, methodNode);
            candidateList.add(method);
        }
        return candidateList;
    }

    @Override
    public boolean hasDeclaredMethod(final String methodName, final String... parameterTypes) {
        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasDeclaredMethod(methodName, desc);
    }

    @Override
    public boolean hasMethod(final String methodName, final String... parameterTypes) {
        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasMethod(methodName, desc);
    }

    @Override
    public boolean hasEnclosingMethod(final String methodName, final String... parameterTypes) {
        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasOutClass(methodName, desc);
    }

    @Override
    public boolean hasConstructor(final String... parameterTypeArray) {
        return getConstructor(parameterTypeArray) == null ? false : true;
    }

    @Override
    public boolean hasField(String name, String type) {
        final String desc = type == null ? null : JavaAssistUtils.toJvmSignature(type);
        return this.classNode.getField(name, desc) != null;
    }

    @Override
    public boolean hasField(String name) {
        return hasField(name, null);
    }

    @Override
    public void weave(final String adviceClassName) throws InstrumentException {
        if (adviceClassName == null) {
            throw new NotFoundInstrumentException("advice class name");
        }

        final String classInternalName = JavaAssistUtils.javaNameToJvmName(adviceClassName);
        final ClassLoader classLoader = classNode.getClassLoader();
        final ProtectionDomain protectionDomain = classNode.getProtectionDomain();
        final ASMClassNodeAdapter adviceClassNode = ASMClassNodeAdapter.get(this.pluginContext, classLoader, protectionDomain, classInternalName);
        if (adviceClassNode == null) {
            throw new NotFoundInstrumentException(adviceClassName + " not found.");
        }

        final ASMAspectWeaver aspectWeaver = new ASMAspectWeaver();
        aspectWeaver.weaving(this.classNode, adviceClassNode);
        setModified(true);
    }

    @Override
    public InstrumentMethod addDelegatorMethod(final String methodName, final String... paramTypes) throws InstrumentException {
        // check duplicated method.
        if (getDeclaredMethod(methodName, paramTypes) != null) {
            throw new InstrumentException(getName() + " already have method(" + methodName + ").");
        }

        final ASMClassNodeAdapter superClassNode = ASMClassNodeAdapter.get(this.pluginContext, classNode.getClassLoader(), classNode.getProtectionDomain(), this.classNode.getSuperClassInternalName());
        if (superClassNode == null) {
            throw new NotFoundInstrumentException(getName() + " not found super class(" + this.classNode.getSuperClassInternalName() + ")");
        }

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(paramTypes);
        final ASMMethodNodeAdapter superMethodNode = superClassNode.getDeclaredMethod(methodName, desc);
        if (superMethodNode == null) {
            throw new NotFoundInstrumentException(methodName + desc + " is not found in " + superClassNode.getInternalName());
        }

        final ASMMethodNodeAdapter methodNode = this.classNode.addDelegatorMethod(superMethodNode);
        setModified(true);
        return new ASMMethod(this.engineComponent, this.pluginContext, this, methodNode);
    }

    @Override
    public void addField(final String accessorTypeName) throws InstrumentException {
        final Class<?> accessorClass = loadInterceptorClass(accessorTypeName);
        try {
            addField(accessorClass);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add field with accessor [" + accessorTypeName + "]. Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public void addField(Class<?> accessorClass) throws InstrumentException {
        Assert.requireNonNull(accessorClass, "accessorClass");
        try {

            final AccessorAnalyzer accessorAnalyzer = new AccessorAnalyzer();
            final AccessorAnalyzer.AccessorDetails accessorDetails = accessorAnalyzer.analyze(accessorClass);

            final Type type = Type.getType(accessorDetails.getFieldType());
            final String accessorTypeName = accessorClass.getName();
            final String fieldName = FIELD_PREFIX + JavaAssistUtils.javaClassNameToVariableName(accessorTypeName);
            final ASMFieldNodeAdapter fieldNode = this.classNode.addField(fieldName, type.getDescriptor());
            this.classNode.addInterface(accessorTypeName);
            this.classNode.addGetterMethod(accessorDetails.getGetter().getName(), fieldNode);
            this.classNode.addSetterMethod(accessorDetails.getSetter().getName(), fieldNode);
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add field with accessor [" + accessorClass.getName() + "]. Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public void addGetter(final String getterTypeName, final String fieldName) throws InstrumentException {
        final Class<?> accessorClass = loadInterceptorClass(getterTypeName);
        try {
            addGetter(accessorClass, fieldName);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add getter: " + accessorClass.getName(), e);
        }
    }

    @Override
    public void addGetter(Class<?> getterClass, String fieldName) throws InstrumentException {
        try {
            final GetterAnalyzer.GetterDetails getterDetails = new GetterAnalyzer().analyze(getterClass);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }

            final String fieldTypeName = JavaAssistUtils.javaClassNameToObjectName(getterDetails.getFieldType().getName());
            if (!fieldNode.getClassName().equals(fieldTypeName)) {
                throw new IllegalArgumentException("different return type. return=" + fieldTypeName + ", field=" + fieldNode.getClassName());
            }

            this.classNode.addGetterMethod(getterDetails.getGetter().getName(), fieldNode);
            this.classNode.addInterface(getterClass.getName());
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add getter: " + getterClass.getName(), e);
        }
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName) throws InstrumentException {
        this.addSetter(setterTypeName, fieldName, false);
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName, boolean removeFinal) throws InstrumentException {
        final Class<?> setterClass = loadInterceptorClass(setterTypeName);
        try {
            addSetter(setterClass, fieldName, removeFinal);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add setter: " + setterTypeName, e);
        }
    }

    @Override
    public void addSetter(Class<?> setterClass, String fieldName) throws InstrumentException {
        this.addSetter(setterClass, fieldName, false);
    }

    @Override
    public void addSetter(Class<?> setterClass, String fieldName, boolean removeFinal) throws InstrumentException {
        Assert.requireNonNull(setterClass, "setterClass");
        try {
            final SetterAnalyzer.SetterDetails setterDetails = new SetterAnalyzer().analyze(setterClass);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }

            final String fieldTypeName = JavaAssistUtils.javaClassNameToObjectName(setterDetails.getFieldType().getName());
            if (!fieldNode.getClassName().equals(fieldTypeName)) {
                throw new IllegalArgumentException("Argument type of the setter is different with the field type. setterMethod: " + fieldTypeName + ", fieldType: " + fieldNode.getClassName());
            }

            if (fieldNode.isStatic()) {
                throw new IllegalArgumentException("Cannot add setter to static fields. setterMethod: " + setterDetails.getSetter().getName() + ", fieldName: " + fieldName);
            }

            final int original = fieldNode.getAccess();
            boolean finalRemoved = false;
            if (fieldNode.isFinal()) {
                if (!removeFinal) {
                    throw new IllegalArgumentException("Cannot add setter to final field. setterMethod: " + setterDetails.getSetter().getName() + ", fieldName: " + fieldName);
                } else {
                    final int removed = original & ~Opcodes.ACC_FINAL;
                    fieldNode.setAccess(removed);
                    finalRemoved = true;
                }
            }

            try {
                this.classNode.addSetterMethod(setterDetails.getSetter().getName(), fieldNode);
                this.classNode.addInterface(setterClass.getName());
                setModified(true);
            } catch (Exception e) {
                if (finalRemoved) {
                    fieldNode.setAccess(original);
                }
                throw e;
            }
        } catch (Exception e) {
            throw new InstrumentException("Failed to add setter: " + setterClass.getName(), e);
        }
    }

    private Class<? extends Interceptor> loadInterceptorClass(String interceptorClassName) throws InstrumentException {
        try {
            final ClassLoader classLoader = classNode.getClassLoader();
            return this.pluginContext.injectClass(classLoader, interceptorClassName);
        } catch (Exception ex) {
            throw new InstrumentException(interceptorClassName + " not found Caused by:" + ex.getMessage(), ex);
        }
    }

    @Override
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor(interceptorClass);
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs ");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor(interceptorClass, constructorArgs);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, scopeName);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, interceptorScope);
    }


    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, scopeName);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, interceptorScope);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");

        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, scopeName, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(interceptorClass, constructorArgs, scopeName, executionPolicy);
    }


    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    private int addInterceptor0(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        int interceptorId = -1;

        final TargetMethods targetMethods = interceptorClass.getAnnotation(TargetMethods.class);
        if (targetMethods != null) {
            for (TargetMethod m : targetMethods.value()) {
                interceptorId = addInterceptor0(m, interceptorClass, constructorArgs, scope, executionPolicy);
            }
        }

        final TargetMethod targetMethod = interceptorClass.getAnnotation(TargetMethod.class);
        if (targetMethod != null) {
            interceptorId = addInterceptor0(targetMethod, interceptorClass, constructorArgs, scope, executionPolicy);
        }

        final TargetConstructors targetConstructors = interceptorClass.getAnnotation(TargetConstructors.class);
        if (targetConstructors != null) {
            for (TargetConstructor c : targetConstructors.value()) {
                interceptorId = addInterceptor0(c, interceptorClass, scope, executionPolicy, constructorArgs);
            }
        }

        final TargetConstructor targetConstructor = interceptorClass.getAnnotation(TargetConstructor.class);
        if (targetConstructor != null) {
            interceptorId = addInterceptor0(targetConstructor, interceptorClass, scope, executionPolicy, constructorArgs);
        }

        final TargetFilter targetFilter = interceptorClass.getAnnotation(TargetFilter.class);
        if (targetFilter != null) {
            interceptorId = addInterceptor0(targetFilter, interceptorClass, scope, executionPolicy, constructorArgs);
        }

        if (interceptorId == -1) {
            throw new PinpointException("No target is specified. At least one of @Targets, @TargetMethod, @TargetConstructor, @TargetFilter must present. interceptor: " + interceptorClass.getName());
        }

        return interceptorId;
    }

    private int addInterceptor0(TargetConstructor c, Class<? extends Interceptor> interceptorClass, InterceptorScope scope, ExecutionPolicy executionPolicy, Object... constructorArgs) throws InstrumentException {
        final InstrumentMethod constructor = getConstructor(c.value());

        if (constructor == null) {
            throw new NotFoundInstrumentException("Cannot find constructor with parameter types: " + Arrays.toString(c.value()));
        }
        // TODO casting fix
        return ((ASMMethod) constructor).addInterceptorInternal(interceptorClass, constructorArgs, scope, executionPolicy);
    }

    private int addInterceptor0(TargetMethod m, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        InstrumentMethod method = getDeclaredMethod(m.name(), m.paramTypes());

        if (method == null) {
            throw new NotFoundInstrumentException("Cannot find method " + m.name() + " with parameter types: " + Arrays.toString(m.paramTypes()));
        }
        // TODO casting fix
        return ((ASMMethod) method).addInterceptorInternal(interceptorClass, constructorArgs, scope, executionPolicy);
    }

    private int addInterceptor0(TargetFilter annotation, Class<? extends Interceptor> interceptorClass, InterceptorScope scope, ExecutionPolicy executionPolicy, Object[] constructorArgs) throws InstrumentException {
        final String filterTypeName = annotation.type();
        Assert.requireNonNull(filterTypeName, "type of @TargetFilter");

        ObjectBinderFactory objectBinderFactory = engineComponent.getObjectBinderFactory();
        final InterceptorArgumentProvider interceptorArgumentProvider = objectBinderFactory.newInterceptorArgumentProvider(this);
        final AutoBindingObjectFactory filterFactory = objectBinderFactory.newAutoBindingObjectFactory(pluginContext, classNode.getClassLoader(), interceptorArgumentProvider);
        final ObjectFactory objectFactory = ObjectFactory.byConstructor(filterTypeName, (Object[]) annotation.constructorArguments());
        final MethodFilter filter = (MethodFilter) filterFactory.createInstance(objectFactory);

        boolean singleton = annotation.singleton();
        int interceptorId = -1;

        for (InstrumentMethod m : getDeclaredMethods(filter)) {
            if (singleton && interceptorId != -1) {
                m.addInterceptor(interceptorId);
            } else {
                // TODO casting fix
                interceptorId = ((ASMMethod) m).addInterceptorInternal(interceptorClass, constructorArgs, scope, executionPolicy);
            }
        }

        if (interceptorId == -1) {
            logger.warn("No methods are intercepted. target:{}, interceptor:{}, methodFilter:{} ", this.classNode.getInternalName(), interceptorClass, filterTypeName);
        }

        return interceptorId;
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor(filter, interceptorClass);
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addInterceptor(filter, interceptorClass, constructorArgs);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(filter, interceptorClass, scopeName, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(filter, interceptorClass,  interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");

        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor(filter, interceptorClass, constructorArgs, scopeName, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final Class<? extends Interceptor> interceptorClass = loadInterceptorClass(interceptorClassName);
        return addScopedInterceptor0(filter, interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        return addInterceptor0(interceptorClass, null, null, null);
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs ");
        return addInterceptor0(interceptorClass, constructorArgs, null, null);
    }

    @Override
    public int addInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");

        return addScopedInterceptor0(filter, interceptorClass, null, null, null);
    }

    @Override
    public int addInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");

        return addScopedInterceptor0(filter, interceptorClass, constructorArgs, null, null);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, null, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(interceptorScope, "scope");
        return addInterceptor0(interceptorClass, null, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return addInterceptor0(interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addScopedInterceptor0(filter, interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return addScopedInterceptor0(filter, interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(scopeName, "scopeName");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addScopedInterceptor0(filter, interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter");
        Assert.requireNonNull(interceptorClass, "interceptorClass");
        Assert.requireNonNull(constructorArgs, "constructorArgs");
        Assert.requireNonNull(interceptorScope, "interceptorScope");
        Assert.requireNonNull(executionPolicy, "executionPolicy");
        return addScopedInterceptor0(filter, interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    private int addScopedInterceptor0(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        int interceptorId = -1;
        for (InstrumentMethod m : getDeclaredMethods(filter)) {
            if (interceptorId != -1) {
                m.addInterceptor(interceptorId);
            } else {
                // TODO casting fix
                interceptorId = ((ASMMethod) m).addInterceptorInternal(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
            }
        }

        if (interceptorId == -1) {
            logger.warn("No methods are intercepted. target:{}, interceptor:{}, methodFilter:{}", this.classNode.getInternalName(), interceptorClass, filter.getClass().getName());
        }

        return interceptorId;
    }

    @Override
    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        final List<InstrumentClass> nestedClasses = new ArrayList<InstrumentClass>();
        for (ASMClassNodeAdapter innerClassNode : this.classNode.getInnerClasses()) {
            final ASMNestedClass nestedClass = new ASMNestedClass(engineComponent, this.pluginContext, innerClassNode);
            if (filter.accept(nestedClass)) {
                nestedClasses.add(nestedClass);
            }
        }

        return nestedClasses;
    }

    public boolean isModified() {
        return modified;
    }

    void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public byte[] toBytecode() {
        return classNode.toByteArray();
    }
}