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
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.NotFoundException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.FieldAccessor;
import com.navercorp.pinpoint.bootstrap.MetadataAccessor;
import com.navercorp.pinpoint.bootstrap.instrument.DefaultInterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentException;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableClass;
import com.navercorp.pinpoint.bootstrap.instrument.InstrumentableMethod;
import com.navercorp.pinpoint.bootstrap.instrument.InterceptorGroupDefinition;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilters;
import com.navercorp.pinpoint.bootstrap.instrument.NotFoundInstrumentException;
import com.navercorp.pinpoint.bootstrap.interceptor.InterceptPoint;
import com.navercorp.pinpoint.bootstrap.interceptor.Interceptor;
import com.navercorp.pinpoint.bootstrap.interceptor.group.InterceptorGroup;
import com.navercorp.pinpoint.bootstrap.plugin.ProfilerPluginContext;
import com.navercorp.pinpoint.profiler.interceptor.InterceptorRegistryBinder;
import com.navercorp.pinpoint.profiler.interceptor.bci.AccessorAnalyzer.AccessorDetails;
import com.navercorp.pinpoint.profiler.interceptor.bci.GetterAnalyzer.GetterDetails;
import com.navercorp.pinpoint.profiler.util.JavaAssistUtils;

/**
 * @author emeroad
 * @author netspider
 * @author minwoo.jung
 */
public class JavassistClass implements InstrumentableClass {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final boolean isDebug = logger.isDebugEnabled();

    private final ProfilerPluginContext pluginContext;
//    private final JavassistClassPool instrumentClassPool;
    private final InterceptorRegistryBinder interceptorRegistryBinder;
    private final ClassLoader classLoader;
    private final CtClass ctClass;

    private static final String FIELD_PREFIX = "_$PINPOINT$_";
    private static final String SETTER_PREFIX = "_$PINPOINT$_set";
    private static final String GETTER_PREFIX = "_$PINPOINT$_get";



    public JavassistClass(ProfilerPluginContext pluginContext, InterceptorRegistryBinder interceptorRegistryBinder, ClassLoader classLoader, CtClass ctClass) {
        this.pluginContext = pluginContext;
        this.ctClass = ctClass;
        this.interceptorRegistryBinder = interceptorRegistryBinder;
        this.classLoader = classLoader;
    }
    
    public ClassLoader getClassLoader() {
        return classLoader;
    }

    @Override
    public boolean isInterceptable() {
        return !ctClass.isInterface() && !ctClass.isAnnotation() && !ctClass.isModified();
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
    
    private static CtMethod getCtMethod0(CtClass ctClass, String methodName, String[] parameterTypes) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
       
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
    
    private CtMethod getCtMethod(String methodName, String[] parameterTypes) throws NotFoundInstrumentException {
        CtMethod method = getCtMethod0(ctClass, methodName, parameterTypes);
        
        if (method == null) {
            throw new NotFoundInstrumentException(methodName + Arrays.toString(parameterTypes) + " is not found in " + this.getName());
        }
        
        return method;
    }

    public InstrumentableMethod getDeclaredMethod(String name, String[] parameterTypes) {
        CtMethod method = getCtMethod0(ctClass, name, parameterTypes);
        return method == null ? null : new JavassistMethod(pluginContext, interceptorRegistryBinder, this, method);
    }

    public List<InstrumentableMethod> getDeclaredMethods() {
        return getDeclaredMethods(MethodFilters.ACCEPT_ALL);
    }

    public List<InstrumentableMethod> getDeclaredMethods(MethodFilter methodFilter) {
        if (methodFilter == null) {
            throw new NullPointerException("methodFilter must not be null");
        }
        final CtMethod[] declaredMethod = ctClass.getDeclaredMethods();
        final List<InstrumentableMethod> candidateList = new ArrayList<InstrumentableMethod>(declaredMethod.length);
        for (CtMethod ctMethod : declaredMethod) {
            final InstrumentableMethod method = new JavassistMethod(pluginContext, interceptorRegistryBinder, this, ctMethod);
            if (methodFilter.accept(method)) {
                candidateList.add(method);
            }
        }
        
        return candidateList;
    }

    private CtConstructor getCtConstructor(String[] parameterTypes) throws NotFoundInstrumentException {
        CtConstructor constructor = getCtConstructor0(parameterTypes);
        
        if (constructor == null) {
            throw new NotFoundInstrumentException("Constructor" + Arrays.toString(parameterTypes) + " is not found in " + this.getName());
        }

        return constructor;
    }
    
    private CtConstructor getCtConstructor0(String[] parameterTypes) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        // constructor return type is void
        for (CtConstructor constructor : ctClass.getDeclaredConstructors()) {
            final String descriptor = constructor.getMethodInfo2().getDescriptor();
            // skip return type check
            if (descriptor.startsWith(jvmSignature) && constructor.isConstructor()) {
                return constructor;
            }
        }
        
        return null;
    }
    
    public InstrumentableMethod getConstructor(String[] parameterTypes) {
        CtConstructor constructor = getCtConstructor0(parameterTypes);
        return constructor == null ? null : new JavassistMethod(pluginContext, interceptorRegistryBinder, this, constructor);
    }
    

    @Override
    public boolean hasDeclaredMethod(String methodName, String[] args) {
        return getCtMethod0(ctClass, methodName, args) != null;
    }

    @Override
    public boolean hasDeclaredMethod(String methodName, String[] parameterTypes, String returnType) {
        final String signature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes, returnType);
        
        try {
            for (CtMethod method : ctClass.getDeclaredMethods(methodName)) {
                final String descriptor = method.getMethodInfo2().getDescriptor();
                if (descriptor.startsWith(signature)) {
                    return true;
                }
            }
        } catch (NotFoundException e) {
            return false;
        }
        
        return false;
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
    public boolean hasMethod(String methodName, String[] parameterTypes) {
        final String jvmSignature = JavaAssistUtils.javaTypeToJvmSignature(parameterTypes);
        
        for (CtMethod method : ctClass.getMethods()) {
            if (!method.getName().equals(methodName)) {
                continue;
            }
            final String descriptor = method.getMethodInfo2().getDescriptor();
            if (descriptor.startsWith(jvmSignature)) {
                return true;
            }
        }
        
        return false;

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
    public boolean hasField(String name) {
        return hasField(name, null);
    }

    @Override
    public void weave(String adviceClassName) throws InstrumentException {
        pluginContext.injectClass(classLoader, adviceClassName);
        
        CtClass adviceClass;
        try {
            adviceClass = ctClass.getClassPool().get(adviceClassName);
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

    @Override
    public void addDelegatorMethod(String methodName, String[] args) throws InstrumentException {
        if (getCtMethod0(ctClass, methodName, args) != null) {
            throw new InstrumentException(getName() + "already have method(" + methodName  +").");
        }
        
        try {
            final CtClass superClass = ctClass.getSuperclass();
            CtMethod superMethod = getCtMethod0(superClass, methodName, args);
            
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

    
    @Override
    public void addField(String accessorTypeName) throws InstrumentException {
        addField0(accessorTypeName, null);
    }

    @Override
    public void addField(String accessorTypeName, String initValExp) throws InstrumentException {
        addField0(accessorTypeName, initValExp);
    }
    
    private void addField0(String accessorTypeName, String initValExp) throws InstrumentException {
        try {
            Class<?> accessorType = pluginContext.injectClass(classLoader, accessorTypeName);
            AccessorDetails accessorDetails = new AccessorAnalyzer().analyze(accessorType);

            
            CtField newField = CtField.make("private " + accessorDetails.getFieldType().getName() + " " + FIELD_PREFIX + accessorTypeName.replace('.', '_').replace('$', '_') + ";", ctClass);

            if (initValExp == null) {
                ctClass.addField(newField);
            } else {
                ctClass.addField(newField, initValExp);
            }

            final CtClass accessorInterface = ctClass.getClassPool().get(accessorTypeName);
            ctClass.addInterface(accessorInterface);

            CtMethod getterMethod = CtNewMethod.getter(accessorDetails.getGetter().getName(), newField);
            ctClass.addMethod(getterMethod);

            CtMethod setterMethod = CtNewMethod.setter(accessorDetails.getSetter().getName(), newField);
            ctClass.addMethod(setterMethod);
        } catch (Exception e) {
            throw new InstrumentException("Failed to add field with accessor [" + accessorTypeName + "]. Cause:" + e.getMessage(), e);
        }
    }

    @Override
    public void addGetter(String getterTypeName, String fieldName) throws InstrumentException {
        try {
            Class<?> getterType = pluginContext.injectClass(classLoader, getterTypeName);
            GetterDetails getterDetails = new GetterAnalyzer().analyze(getterType);
            
            CtField field = ctClass.getField(fieldName);
            
            if (!field.getType().getName().equals(getterDetails.getFieldType().getName())) {
                throw new IllegalArgumentException("Return type of the getter is different with the field type. getterMethod: " + getterDetails.getGetter() + ", fieldType: " + field.getType().getName());
            }
            
            CtMethod getterMethod = CtNewMethod.getter(getterDetails.getGetter().getName(), field);
            ctClass.addMethod(getterMethod);
        
            CtClass ctInterface = ctClass.getClassPool().get(getterTypeName);
            ctClass.addInterface(ctInterface);
        } catch (Exception e) {
            throw new InstrumentException("Fail to add getter: " + getterTypeName, e);
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    // below methods will be removed soon
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
        
            CtClass ctInterface = ctClass.getClassPool().get(interfaceType.getName());
            ctClass.addInterface(ctInterface);
        } catch (NotFoundException ex) {
            throw new InstrumentException("Failed to add getter. No such field: " + fieldName, ex);
        } catch (Exception e) {
            // Cannot happen. Reaching here means a bug.   
            throw new InstrumentException("Fail to add getter: " + interfaceType.getName(), e);
        }
    }

    
    @Override
    public void addGetter(FieldAccessor fieldGetter, String fieldName) throws InstrumentException {
        Class<?> interfaceType = fieldGetter.getType();
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
        
            CtClass ctInterface = ctClass.getClassPool().get(interfaceType.getName());
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

    
    @Override
    public void addMetadata(MetadataAccessor metadata, String initialValue) throws InstrumentException {
        addMetadata0(metadata, initialValue);
    };
    
    @Override
    public void addMetadata(MetadataAccessor metadata) throws InstrumentException {
        addMetadata0(metadata, null);
    }
    
    public void addMetadata0(MetadataAccessor metadata, String initValue) throws InstrumentException {
        if (metadata == null) {
            throw new NullPointerException("traceValue must not be null");
        }
        
        try {
            ClassPool classPool = ctClass.getClassPool();
            final CtClass accessorType = classPool.get(metadata.getType().getName());
            final String fieldName = FIELD_PREFIX + accessorType.getSimpleName();
            final CtField field = new CtField(classPool.get(Object.class.getName()), fieldName, ctClass);

            ctClass.addInterface(accessorType);
            
            if (initValue == null) {
                ctClass.addField(field);
            } else {
                ctClass.addField(field, initValue);
            }
        } catch (CannotCompileException e) {
            throw new InstrumentException(metadata.getType() + " implements fail. Cause:" + e.getMessage(), e);
        } catch (NotFoundException e) {
            throw new InstrumentException(metadata.getType() + " implements fail. Cause:" + e.getMessage(), e);
        }
    }

    
    private ClassPool getClassPool() {
        return ctClass.getClassPool();
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
            CtMethod method = getCtMethod(methodName, args);
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
            CtMethod method = getCtMethod(methodName, args);
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
            final CtClass ctValueHandler = ctClass.getClassPool().get(traceValue.getName());

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
        final JavassistMethod behavior = (JavassistMethod)getConstructor(args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a constructor with parameter types: " + Arrays.toString(args));
        }
        
        return behavior.addInterceptor0(interceptor, null, InterceptPoint.AROUND);
    }

    @Override
    public int addConstructorInterceptor(String[] args, Interceptor interceptor, InterceptPoint type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        final JavassistMethod behavior = (JavassistMethod)getConstructor(args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a constructor with parameter types: " + Arrays.toString(args));
        }
        
        return behavior.addInterceptor0(interceptor, null, type);
    }

    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor) throws InstrumentException, NotFoundInstrumentException {
        if (methodName == null) {
            throw new NullPointerException("methodName must not be null");
        }
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        
        final JavassistMethod behavior = (JavassistMethod)getDeclaredMethod(methodName, args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a method " + methodName + " with parameter types: " + Arrays.toString(args));
        }
        
        return behavior.addInterceptor0(interceptor, null, InterceptPoint.AROUND);
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
        
        
        final JavassistMethod behavior = (JavassistMethod)getDeclaredMethod(methodName, args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a method " + methodName + " with parameter types: " + Arrays.toString(args));
        }
        
        InterceptorGroup group = pluginContext.getInterceptorGroup(definition.getName());
        return behavior.addInterceptor0(interceptor, group, InterceptPoint.AROUND);
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

        final JavassistMethod behavior = (JavassistMethod)getDeclaredMethod(methodName, args);
        
        if (behavior == null) {
            if (logger.isWarnEnabled()) {
                logger.warn("Method is not declared. class={}, methodName={}, args={}", ctClass.getName(), methodName, Arrays.toString(args));
            }
            return -1;
        }
        
        InterceptorGroup group = pluginContext.getInterceptorGroup(groupDefinition.getName());
        return behavior.addInterceptor0(interceptor, group, InterceptPoint.AROUND);
    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId) throws InstrumentException, NotFoundInstrumentException {
        final JavassistMethod behavior = (JavassistMethod)getDeclaredMethod(methodName, args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a method " + methodName + " with parameter types: " + Arrays.toString(args));
        }

        behavior.addInterceptor(interceptorId);
        
        return interceptorId;
    }

    @Override
    public int reuseInterceptor(String methodName, String[] args, int interceptorId, InterceptPoint type) throws InstrumentException, NotFoundInstrumentException {
        final JavassistMethod behavior = (JavassistMethod)getDeclaredMethod(methodName, args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a method " + methodName + " with parameter types: " + Arrays.toString(args));
        }

        behavior.addInterceptor0(interceptorId, type);
        
        return interceptorId;
    }

    @Override
    public int addInterceptor(String methodName, String[] args, Interceptor interceptor, InterceptPoint type) throws InstrumentException, NotFoundInstrumentException {
        if (interceptor == null) {
            throw new IllegalArgumentException("interceptor is null");
        }
        
        final JavassistMethod behavior = (JavassistMethod)getDeclaredMethod(methodName, args);
        
        if (behavior == null) {
            throw new NotFoundInstrumentException("Cannot find a method " + methodName + " with parameter types: " + Arrays.toString(args));
        }

        return behavior.addInterceptor0(interceptor, null, type);
    }



    @Override
    public boolean addDebugLogBeforeAfterMethod() {
//        final String className = this.ctClass.getName();
//        final LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
//        final int id = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor(loggingInterceptor);
//        try {
//            for (CtMethod method : ctClass.getDeclaredMethods()) {
//                if (method.isEmpty()) {
//                    if (isDebug) {
//                        logger.debug("{} is empty.", method.getLongName());
//                    }
//                    continue;
//                }
//                String methodName = method.getName();
//                addStaticAroundInterceptor(methodName, id, method);
//            }
//            return true;
//        } catch (Exception e) {
//            if (logger.isWarnEnabled()) {
//                logger.warn(e.getMessage(), e);
//            }
//        }
        return false;
    }

    /**
     * Does not work properly. Cannot modify bytecode of constructor
     *
     * @return
     */
    @Deprecated
    public boolean addDebugLogBeforeAfterConstructor() {
//        final String className = this.ctClass.getName();
//        final LoggingInterceptor loggingInterceptor = new LoggingInterceptor(className);
//        final int id = interceptorRegistryBinder.getInterceptorRegistryAdaptor().addSimpleInterceptor(loggingInterceptor);
//        try {
//            for (CtConstructor constructor : ctClass.getConstructors()) {
//                if (constructor.isEmpty()) {
//                    if (isDebug) {
//                        logger.debug("{} is empty.", constructor.getLongName());
//                    }
//                    continue;
//                }
//                String constructorName = constructor.getName();
//                addStaticAroundInterceptor(constructorName, id, constructor);
//            }
//            return true;
//        } catch (Exception e) {
//            if (logger.isWarnEnabled()) {
//                logger.warn(e.getMessage(), e);
//            }
//        }
        return false;
    }


    public Class<?> toClass() throws InstrumentException {
        try {
            return ctClass.toClass();
        } catch (CannotCompileException e) {
            throw new InstrumentException( "CannotCompileException class:" + ctClass.getName() + " " + e.getMessage(), e);
        }
    }


   @Override
   public InstrumentableClass getNestedClass(String className) {
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
               return new JavassistClass(pluginContext, interceptorRegistryBinder, classLoader, nested);
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
}
