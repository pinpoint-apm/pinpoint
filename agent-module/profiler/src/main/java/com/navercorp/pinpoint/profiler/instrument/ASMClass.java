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
import com.navercorp.pinpoint.common.util.JvmUtils;
import com.navercorp.pinpoint.common.util.JvmVersion;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.ClassNode;

import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * @author jaehong.kim
 */
public class ASMClass implements InstrumentClass {
    private static final String FIELD_PREFIX = "_$PINPOINT$_";

    private final Logger logger = LogManager.getLogger(this.getClass());

    private final EngineComponent engineComponent;

    private final InstrumentContext pluginContext;

    private final ASMClassNodeAdapter classNode;
    private boolean modified = false;
    private String name;

    public static ASMClass load(EngineComponent engineComponent, final InstrumentContext pluginContext, final ClassLoader classLoader, ProtectionDomain protectionDomain, final ClassNode classNode) {
        ASMClassNodeAdapter classNodeAdapter = new ASMClassNodeAdapter(pluginContext, classLoader, protectionDomain, classNode);
        return new ASMClass(engineComponent, pluginContext, classNodeAdapter);
    }

    public ASMClass(EngineComponent engineComponent, final InstrumentContext pluginContext, final ASMClassNodeAdapter classNode) {
        this.engineComponent = Objects.requireNonNull(engineComponent, "engineComponent");
        this.pluginContext = pluginContext;
        this.classNode = Objects.requireNonNull(classNode, "classNode");
    }

    public ClassLoader getClassLoader() {
        return this.classNode.getClassLoader();
    }

    @Override
    public boolean isInterceptable() {
        if (isAnnotation() || isModified() || isRecord()) {
            return false;
        }
        // interface static method or default method is java 1.8 or later
        if (isInterface() && (this.classNode.getMajorVersion() < JvmVersion.JAVA_8.getClassVersion() || !JvmUtils.getVersion().onOrAfter(JvmVersion.JAVA_8))) {
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
    public InstrumentMethod getDeclaredMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "name");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        final ASMMethodNodeAdapter methodNode = this.classNode.getDeclaredMethod(methodName, desc);
        if (methodNode == null) {
            return null;
        }

        return new ASMMethod(this.engineComponent, this.pluginContext, this, methodNode);
    }

    @Override
    @Deprecated
    public InstrumentMethod getLambdaMethod(String... parameterTypes) {
        return getDeclaredMethod("get$Lambda", parameterTypes);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return getDeclaredMethods(MethodFilters.ACCEPT_ALL);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(final MethodFilter methodFilter) {
        Objects.requireNonNull(methodFilter, "methodFilter");

        final List<InstrumentMethod> candidateList = new ArrayList<>();
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
        final List<InstrumentMethod> candidateList = new ArrayList<>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredConstructors()) {
            final InstrumentMethod method = new ASMMethod(this.engineComponent, this.pluginContext, this, methodNode);
            candidateList.add(method);
        }
        return candidateList;
    }

    @Override
    public boolean hasDeclaredMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "methodName");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasDeclaredMethod(methodName, desc);
    }

    @Override
    public boolean hasMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "methodName");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasMethod(methodName, desc);
    }

    @Override
    public boolean hasEnclosingMethod(final String methodName, final String... parameterTypes) {
        Objects.requireNonNull(methodName, "methodName");

        final String desc = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        return this.classNode.hasOutClass(methodName, desc);
    }

    @Override
    public boolean hasConstructor(final String... parameterTypeArray) {
        return getConstructor(parameterTypeArray) == null ? false : true;
    }

    @Override
    public boolean hasField(String fieldName, String type) {
        Objects.requireNonNull(fieldName, "name");

        final String desc = type == null ? null : JavaAssistUtils.toJvmSignature(type);
        return this.classNode.getField(fieldName, desc) != null;
    }

    @Override
    public boolean hasField(String name) {
        return hasField(name, null);
    }

    @Override
    public void weave(final String adviceClassName) throws InstrumentException {
        Objects.requireNonNull(adviceClassName, "adviceClassName");

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
        Objects.requireNonNull(methodName, "methodName");

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
    public void addField(Class<?> accessorClass) throws InstrumentException {
        Objects.requireNonNull(accessorClass, "accessorClass");
        try {

            final AccessorAnalyzer.AccessorDetails accessorDetails = AccessorAnalyzer.instance().analyze(accessorClass);

            final Type type = accessorDetails.getFieldType();
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
    public void addGetter(Class<?> getterClass, String fieldName) throws InstrumentException {
        Objects.requireNonNull(getterClass, "getterClass");
        Objects.requireNonNull(fieldName, "fieldName");

        try {
            final GetterAnalyzer.GetterDetails getterDetails = GetterAnalyzer.instance().analyze(getterClass);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }
            Type fieldType = getterDetails.getFieldType();
            if (!fieldNode.getJavaType().equals(fieldType)) {
                throw new IllegalArgumentException("different return type. return=" + fieldType + ", field=" + fieldNode.getJavaType());
            }

            this.classNode.addGetterMethod(getterDetails.getGetter().getName(), fieldNode);
            this.classNode.addInterface(getterClass.getName());
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add getter: " + getterClass.getName(), e);
        }
    }

    @Override
    public void addSetter(Class<?> setterClass, String fieldName) throws InstrumentException {
        this.addSetter(setterClass, fieldName, false);
    }

    @Override
    public void addSetter(Class<?> setterClass, String fieldName, boolean removeFinal) throws InstrumentException {
        Objects.requireNonNull(setterClass, "setterClass");
        try {
            final SetterAnalyzer.SetterDetails setterDetails = SetterAnalyzer.instance().analyze(setterClass);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }

            final Type fieldType = setterDetails.getFieldType();
            if (!fieldNode.getJavaType().equals(fieldType)) {
                throw new IllegalArgumentException("Argument type of the setter is different with the field type. setterMethod: " + fieldType + ", fieldType: " + fieldNode.getJavaType());
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
        Objects.requireNonNull(filterTypeName, "type of @TargetFilter");

        ObjectBinderFactory objectBinderFactory = engineComponent.getObjectBinderFactory();
        final InterceptorArgumentProvider interceptorArgumentProvider = objectBinderFactory.newInterceptorArgumentProvider();
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
    public int addInterceptor(Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        return addInterceptor0(interceptorClass, null, null, null);
    }

    @Override
    public int addInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs ");
        return addInterceptor0(interceptorClass, constructorArgs, null, null);
    }

    @Override
    public int addInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass) throws InstrumentException {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(interceptorClass, "interceptorClass");

        return addScopedInterceptor0(filter, interceptorClass, null, null, null);
    }

    @Override
    public int addInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs) throws InstrumentException {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");

        return addScopedInterceptor0(filter, interceptorClass, constructorArgs, null, null);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        return addInterceptor0(interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, null, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "scope");
        return addInterceptor0(interceptorClass, null, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        return addInterceptor0(interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addScopedInterceptor0(filter, interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        return addScopedInterceptor0(filter, interceptorClass, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(scopeName, "scopeName");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addScopedInterceptor0(filter, interceptorClass, constructorArgs, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, Class<? extends Interceptor> interceptorClass, Object[] constructorArgs, InterceptorScope interceptorScope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Objects.requireNonNull(filter, "filter");
        Objects.requireNonNull(interceptorClass, "interceptorClass");
        Objects.requireNonNull(constructorArgs, "constructorArgs");
        Objects.requireNonNull(interceptorScope, "interceptorScope");
        Objects.requireNonNull(executionPolicy, "executionPolicy");
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
        Objects.requireNonNull(filter, "filter");

        final List<InstrumentClass> nestedClasses = new ArrayList<>();
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

    public boolean isRecord() {
        return classNode.isRecord();
    }

    @Override
    public byte[] toBytecode() {
        return classNode.toByteArray();
    }
}