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
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructor;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetConstructors;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetFilter;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethod;
import com.navercorp.pinpoint.bootstrap.interceptor.annotation.TargetMethods;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.ExecutionPolicy;
import com.navercorp.pinpoint.bootstrap.interceptor.scope.InterceptorScope;
import com.navercorp.pinpoint.bootstrap.plugin.ObjectFactory;
import com.navercorp.pinpoint.common.util.Assert;
import com.navercorp.pinpoint.exception.PinpointException;
import com.navercorp.pinpoint.profiler.interceptor.registry.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.metadata.ApiMetaDataService;
import com.navercorp.pinpoint.profiler.objectfactory.AutoBindingObjectFactory;
import com.navercorp.pinpoint.profiler.objectfactory.InterceptorArgumentProvider;
import com.navercorp.pinpoint.profiler.objectfactory.ObjectBinderFactory;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author jaehong.kim
 */
public class ASMClass implements InstrumentClass {
    private static final String FIELD_PREFIX = "_$PINPOINT$_";

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private final ObjectBinderFactory objectBinderFactory;
    private final InstrumentContext pluginContext;
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final ApiMetaDataService apiMetaDataService;
    private final ClassLoader classLoader;

    private final ASMClassNodeAdapter classNode;
    private boolean modified = false;
    private String name;

    public ASMClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext pluginContext, final InterceptorRegistryBinder interceptorRegistryBinder, ApiMetaDataService apiMetaDataService, final ClassLoader classLoader, final ClassNode classNode) {
        this(objectBinderFactory, pluginContext, interceptorRegistryBinder, apiMetaDataService, classLoader, new ASMClassNodeAdapter(pluginContext, classLoader, classNode));
    }

    public ASMClass(ObjectBinderFactory objectBinderFactory, final InstrumentContext pluginContext, final InterceptorRegistryBinder interceptorRegistryBinder, ApiMetaDataService apiMetaDataService, final ClassLoader classLoader, final ASMClassNodeAdapter classNode) {
        if (objectBinderFactory == null) {
            throw new NullPointerException("objectBinderFactory must not be null");
        }

//        if (pluginContext == null) {
//            throw new NullPointerException("pluginContext must not be null");
//        }
        if (apiMetaDataService == null) {
            throw new NullPointerException("apiMetaDataService must not be null");
        }

        this.objectBinderFactory = objectBinderFactory;
        this.pluginContext = pluginContext;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
        this.apiMetaDataService = apiMetaDataService;
        this.classLoader = classLoader;
        this.classNode = classNode;
        // for performance.
        this.name = classNode.getName();
    }

    public ClassLoader getClassLoader() {
        return this.classLoader;
    }

    @Override
    public boolean isInterceptable() {
        return !isInterface() && !isAnnotation() && !isModified();
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

        return new ASMMethod(this.objectBinderFactory, this.pluginContext, this.interceptorRegistryBinder, apiMetaDataService, this, methodNode);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods() {
        return getDeclaredMethods(MethodFilters.ACCEPT_ALL);
    }

    @Override
    public List<InstrumentMethod> getDeclaredMethods(final MethodFilter methodFilter) {
        if (methodFilter == null) {
            throw new NullPointerException("methodFilter must not be null");
        }

        final List<InstrumentMethod> candidateList = new ArrayList<InstrumentMethod>();
        for (ASMMethodNodeAdapter methodNode : this.classNode.getDeclaredMethods()) {
            final InstrumentMethod method = new ASMMethod(this.objectBinderFactory, this.pluginContext, this.interceptorRegistryBinder, apiMetaDataService, this, methodNode);
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
            throw new NotFoundInstrumentException("advice class name must not be null");
        }

        final ASMClassNodeAdapter adviceClassNode = ASMClassNodeAdapter.get(this.pluginContext, this.classLoader, JavaAssistUtils.javaNameToJvmName(adviceClassName));
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

        final ASMClassNodeAdapter superClassNode = ASMClassNodeAdapter.get(this.pluginContext, this.classLoader, this.classNode.getSuperClassInternalName());
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
        return new ASMMethod(this.objectBinderFactory, this.pluginContext, this.interceptorRegistryBinder, apiMetaDataService, this, methodNode);
    }

    @Override
    public void addField(final String accessorTypeName) throws InstrumentException {
        try {
            final Class<?> accessorType = this.pluginContext.injectClass(this.classLoader, accessorTypeName);
            final AccessorAnalyzer accessorAnalyzer = new AccessorAnalyzer();
            final AccessorAnalyzer.AccessorDetails accessorDetails = accessorAnalyzer.analyze(accessorType);

            final ASMFieldNodeAdapter fieldNode = this.classNode.addField(FIELD_PREFIX + JavaAssistUtils.javaClassNameToVariableName(accessorTypeName), accessorDetails.getFieldType());
            this.classNode.addInterface(accessorTypeName);
            this.classNode.addGetterMethod(accessorDetails.getGetter().getName(), fieldNode);
            this.classNode.addSetterMethod(accessorDetails.getSetter().getName(), fieldNode);
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add field with accessor [" + accessorTypeName + "]. Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public void addGetter(final String getterTypeName, final String fieldName) throws InstrumentException {
        try {
            final Class<?> getterType = this.pluginContext.injectClass(this.classLoader, getterTypeName);
            final GetterAnalyzer.GetterDetails getterDetails = new GetterAnalyzer().analyze(getterType);
            final ASMFieldNodeAdapter fieldNode = this.classNode.getField(fieldName, null);
            if (fieldNode == null) {
                throw new IllegalArgumentException("Not found field. name=" + fieldName);
            }

            final String fieldTypeName = JavaAssistUtils.javaClassNameToObjectName(getterDetails.getFieldType().getName());
            if (!fieldNode.getClassName().equals(fieldTypeName)) {
                throw new IllegalArgumentException("different return type. return=" + fieldTypeName + ", field=" + fieldNode.getClassName());
            }

            this.classNode.addGetterMethod(getterDetails.getGetter().getName(), fieldNode);
            this.classNode.addInterface(getterTypeName);
            setModified(true);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add getter: " + getterTypeName, e);
        }
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName) throws InstrumentException {
        this.addSetter(setterTypeName, fieldName, false);
    }

    @Override
    public void addSetter(String setterTypeName, String fieldName, boolean removeFinal) throws InstrumentException {
        try {
            final Class<?> setterType = this.pluginContext.injectClass(this.classLoader, setterTypeName);
            final SetterAnalyzer.SetterDetails setterDetails = new SetterAnalyzer().analyze(setterType);
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
                this.classNode.addInterface(setterTypeName);
                setModified(true);
            } catch (Exception e) {
                if (finalRemoved) {
                    fieldNode.setAccess(original);
                }
                throw e;
            }
        } catch (Exception e) {
            throw new InstrumentException("Failed to add setter: " + setterTypeName, e);
        }
    }

    @Override
    public int addInterceptor(String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        return addInterceptor0(interceptorClassName, null, null, null);
    }

    @Override
    public int addInterceptor(String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs  must not be null");
        return addInterceptor0(interceptorClassName, constructorArgs, null, null);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, null, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        return addInterceptor0(interceptorClassName, null, scope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, constructorArgs, interceptorScope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        return addInterceptor0(interceptorClassName, constructorArgs, scope, ExecutionPolicy.BOUNDARY);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return addInterceptor0(interceptorClassName, null, scope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addInterceptor0(interceptorClassName, constructorArgs, interceptorScope, executionPolicy);
    }


    @Override
    public int addScopedInterceptor(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return addInterceptor0(interceptorClassName, constructorArgs, scope, executionPolicy);
    }

    private int addInterceptor0(String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        int interceptorId = -1;
        final Class<?> interceptorType = this.pluginContext.injectClass(this.classLoader, interceptorClassName);

        final TargetMethods targetMethods = interceptorType.getAnnotation(TargetMethods.class);
        if (targetMethods != null) {
            for (TargetMethod m : targetMethods.value()) {
                interceptorId = addInterceptor0(m, interceptorClassName, constructorArgs, scope, executionPolicy);
            }
        }

        final TargetMethod targetMethod = interceptorType.getAnnotation(TargetMethod.class);
        if (targetMethod != null) {
            interceptorId = addInterceptor0(targetMethod, interceptorClassName, constructorArgs, scope, executionPolicy);
        }

        final TargetConstructors targetConstructors = interceptorType.getAnnotation(TargetConstructors.class);
        if (targetConstructors != null) {
            for (TargetConstructor c : targetConstructors.value()) {
                interceptorId = addInterceptor0(c, interceptorClassName, scope, executionPolicy, constructorArgs);
            }
        }

        final TargetConstructor targetConstructor = interceptorType.getAnnotation(TargetConstructor.class);
        if (targetConstructor != null) {
            interceptorId = addInterceptor0(targetConstructor, interceptorClassName, scope, executionPolicy, constructorArgs);
        }

        final TargetFilter targetFilter = interceptorType.getAnnotation(TargetFilter.class);
        if (targetFilter != null) {
            interceptorId = addInterceptor0(targetFilter, interceptorClassName, scope, executionPolicy, constructorArgs);
        }

        if (interceptorId == -1) {
            throw new PinpointException("No target is specified. At least one of @Targets, @TargetMethod, @TargetConstructor, @TargetFilter must present. interceptor: " + interceptorClassName);
        }

        return interceptorId;
    }

    private int addInterceptor0(TargetConstructor c, String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy, Object... constructorArgs) throws InstrumentException {
        final InstrumentMethod constructor = getConstructor(c.value());

        if (constructor == null) {
            throw new NotFoundInstrumentException("Cannot find constructor with parameter types: " + Arrays.toString(c.value()));
        }
        // TODO casting fix
        return ((ASMMethod) constructor).addInterceptorInternal(interceptorClassName, constructorArgs, scope, executionPolicy);
    }

    private int addInterceptor0(TargetMethod m, String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        InstrumentMethod method = getDeclaredMethod(m.name(), m.paramTypes());

        if (method == null) {
            throw new NotFoundInstrumentException("Cannot find method " + m.name() + " with parameter types: " + Arrays.toString(m.paramTypes()));
        }
        // TODO casting fix
        return ((ASMMethod) method).addInterceptorInternal(interceptorClassName, constructorArgs, scope, executionPolicy);
    }

    private int addInterceptor0(TargetFilter annotation, String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy, Object[] constructorArgs) throws InstrumentException {
        final String filterTypeName = annotation.type();
        Assert.requireNonNull(filterTypeName, "type of @TargetFilter must not be null");

        final InterceptorArgumentProvider interceptorArgumentProvider = objectBinderFactory.newInterceptorArgumentProvider(this);
        final AutoBindingObjectFactory filterFactory = objectBinderFactory.newAutoBindingObjectFactory(pluginContext, classLoader, interceptorArgumentProvider);
        final ObjectFactory objectFactory = ObjectFactory.byConstructor(filterTypeName, (Object[]) annotation.constructorArguments());
        final MethodFilter filter = (MethodFilter) filterFactory.createInstance(objectFactory);

        boolean singleton = annotation.singleton();
        int interceptorId = -1;

        for (InstrumentMethod m : getDeclaredMethods(filter)) {
            if (singleton && interceptorId != -1) {
                m.addInterceptor(interceptorId);
            } else {
                // TODO casting fix
                interceptorId = ((ASMMethod) m).addInterceptorInternal(interceptorClassName, constructorArgs, scope, executionPolicy);
            }
        }

        if (interceptorId == -1) {
            logger.warn("No methods are intercepted. target: " + this.classNode.getInternalName(), ", interceptor: " + interceptorClassName + ", methodFilter: " + filterTypeName);
        }

        return interceptorId;
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        return addScopedInterceptor0(filter, interceptorClassName, null, null, null);
    }

    @Override
    public int addInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        return addScopedInterceptor0(filter, interceptorClassName, constructorArgs, null, null);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addScopedInterceptor0(filter, interceptorClassName, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return addScopedInterceptor0(filter, interceptorClassName, null, scope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, String scopeName, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scopeName, "scopeName must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        final InterceptorScope interceptorScope = this.pluginContext.getInterceptorScope(scopeName);
        return addScopedInterceptor0(filter, interceptorClassName, null, interceptorScope, executionPolicy);
    }

    @Override
    public int addScopedInterceptor(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        Assert.requireNonNull(filter, "filter must not be null");
        Assert.requireNonNull(interceptorClassName, "interceptorClassName must not be null");
        Assert.requireNonNull(constructorArgs, "constructorArgs must not be null");
        Assert.requireNonNull(scope, "scope must not be null");
        Assert.requireNonNull(executionPolicy, "executionPolicy must not be null");
        return addScopedInterceptor0(filter, interceptorClassName, constructorArgs, scope, executionPolicy);
    }

    private int addScopedInterceptor0(MethodFilter filter, String interceptorClassName, Object[] constructorArgs, InterceptorScope scope, ExecutionPolicy executionPolicy) throws InstrumentException {
        int interceptorId = -1;
        for (InstrumentMethod m : getDeclaredMethods(filter)) {
            if (interceptorId != -1) {
                m.addInterceptor(interceptorId);
            } else {
                // TODO casting fix
                interceptorId = ((ASMMethod) m).addInterceptorInternal(interceptorClassName, constructorArgs, scope, executionPolicy);
            }
        }

        if (interceptorId == -1) {
            logger.warn("No methods are intercepted. target: " + this.classNode.getInternalName(), ", interceptor: " + interceptorClassName + ", methodFilter: " + filter.getClass().getName());
        }

        return interceptorId;
    }

    @Override
    public List<InstrumentClass> getNestedClasses(ClassFilter filter) {
        final List<InstrumentClass> nestedClasses = new ArrayList<InstrumentClass>();
        for (ASMClassNodeAdapter innerClassNode : this.classNode.getInnerClasses()) {
            final ASMNestedClass nestedClass = new ASMNestedClass(objectBinderFactory, this.pluginContext, this.interceptorRegistryBinder, apiMetaDataService, this.classLoader, innerClassNode);
            if (filter.accept(nestedClass)) {
                nestedClasses.add(nestedClass);
            }
        }

        return nestedClasses;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    @Override
    public byte[] toBytecode() {
        return classNode.toByteArray();
    }
}