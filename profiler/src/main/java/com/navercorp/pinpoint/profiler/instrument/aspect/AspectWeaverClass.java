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

package com.navercorp.pinpoint.profiler.instrument.aspect;

import com.navercorp.pinpoint.bootstrap.instrument.aspect.Aspect;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.JointPoint;
import com.navercorp.pinpoint.bootstrap.instrument.aspect.PointCut;
import com.navercorp.pinpoint.profiler.instrument.MethodNameReplacer;
import com.navercorp.pinpoint.profiler.instrument.interceptor.CodeBuilder;

import javassist.*;
import javassist.expr.ExprEditor;
import javassist.expr.MethodCall;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author emeroad
 */
public class AspectWeaverClass {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    private static final MethodNameReplacer DEFAULT_METHOD_NAME_REPLACER = new DefaultMethodNameReplacer();

    private final MethodNameReplacer methodNameReplacer;


    public AspectWeaverClass() {
        methodNameReplacer = DEFAULT_METHOD_NAME_REPLACER;
    }

    public void weaving(CtClass sourceClass, CtClass adviceClass) throws NotFoundException, CannotCompileException {
        if (logger.isInfoEnabled()) {
            logger.info("weaving sourceClass:{} advice:{}", sourceClass.getName(), adviceClass.getName());
        }
        if (!isAspectClass(adviceClass)) {
            throw new RuntimeException("@Aspect not found. adviceClass:" + adviceClass);
        }
        // advice class hierarchy check,
        final boolean isSubClass = adviceClass.subclassOf(sourceClass);
        if (!isSubClass) {
            final CtClass superClass = adviceClass.getSuperclass();
            if (!superClass.getName().equals("java.lang.Object")) {
                throw new CannotCompileException("invalid class hierarchy. " + sourceClass.getName() + " adviceSuperClass:" + superClass.getName());
            }
        }

        copyUtilMethod(sourceClass, adviceClass);

        final List<CtMethod> pointCutMethodList = findAnnotationMethod(adviceClass, PointCut.class);
        final List<CtMethod> jointPointList = findAnnotationMethod(adviceClass, JointPoint.class);

        for (CtMethod adviceMethod : pointCutMethodList) {
            final CtMethod sourceMethod = sourceClass.getDeclaredMethod(adviceMethod.getName(), adviceMethod.getParameterTypes());
            if (!sourceMethod.getSignature().equals(adviceMethod.getSignature())) {
                throw new CannotCompileException("Signature miss match. method:" + adviceMethod.getName() + " source:" + sourceMethod.getSignature() + " advice:" + adviceMethod.getSignature());
            }
            if (logger.isInfoEnabled()) {
                logger.info("weaving method:{}{}", sourceMethod.getName(), sourceMethod.getSignature());
            }
            weavingMethod(sourceClass, sourceMethod, adviceMethod, jointPointList, isSubClass);
        }


    }

    private void copyUtilMethod(CtClass sourceClass, CtClass adviceClass) throws CannotCompileException {
        final List<CtMethod> utilMethodList = findUtilMethod(adviceClass);
        for (CtMethod method : utilMethodList) {
            final CtMethod copyMethod = CtNewMethod.copy(method, method.getName(), sourceClass, null);
            sourceClass.addMethod(copyMethod);
        }
    }

    private List<CtMethod> findUtilMethod(CtClass adviceClass) throws CannotCompileException {
        List<CtMethod> utilMethodList = new ArrayList<CtMethod>();
        for (CtMethod method : adviceClass.getDeclaredMethods()) {
            if (method.hasAnnotation(PointCut.class) || method.hasAnnotation(JointPoint.class)) {
                continue;
            }
            int modifiers = method.getModifiers();
            if (!Modifier.isPrivate(modifiers)) {
                throw new CannotCompileException("non private UtilMethod unsupported. method:" + method.getLongName());
            }
            utilMethodList.add(method);
        }
        return utilMethodList;
    }

    private boolean isAspectClass(CtClass aspectClass) {
        return aspectClass.hasAnnotation(Aspect.class);
    }

    private void weavingMethod(CtClass sourceClass, CtMethod sourceMethod, CtMethod adviceMethod, List<CtMethod> jointPointList, boolean isSubClass) throws CannotCompileException {
        final CtMethod copyMethod = copyMethod(sourceClass, sourceMethod);
        sourceClass.addMethod(copyMethod);

        sourceMethod.setBody(adviceMethod, null);

        sourceMethod.instrument(new JointPointMethodEditor(sourceClass, sourceMethod, copyMethod, jointPointList, isSubClass));
    }

    public class JointPointMethodEditor extends ExprEditor {
        private final CtClass sourceClass;
        private final CtMethod sourceMethod;
        private final CtMethod replaceMethod;
        private final List<CtMethod> jointPointList;
        private final boolean isSubClass;

        public JointPointMethodEditor(CtClass sourceClass, CtMethod sourceMethod, CtMethod replaceMethod, List<CtMethod> jointPointList, boolean isSubClass) {
            if (replaceMethod == null) {
                throw new NullPointerException("replaceMethod must not be null");
            }
            this.sourceClass = sourceClass;
            this.sourceMethod = sourceMethod;
            this.replaceMethod = replaceMethod;
            this.jointPointList = jointPointList;
            this.isSubClass = isSubClass;
        }

        @Override
        public void edit(MethodCall methodCall) throws CannotCompileException {


            final boolean joinPointMethod = isJoinPointMethod(jointPointList, methodCall.getMethodName(), methodCall.getSignature());
            if (joinPointMethod) {
                if (!methodCall.getSignature().equals(replaceMethod.getSignature())) {
                    throw new CannotCompileException("Signature miss match. method:" + sourceMethod.getName() + " source:" + sourceMethod.getSignature() + " jointPoint:" + replaceMethod.getSignature());
                }
                final String invokeSource = invokeSourceMethod();
                if (logger.isDebugEnabled()) {
                    logger.debug("JointPoint method {}{} -> invokeOriginal:{}", methodCall.getMethodName(), methodCall.getSignature(), invokeSource);
                }
                methodCall.replace(invokeSource);
            } else {
                if (isSubClass) {
                    // validate super class method
                    try {
                        CtMethod method = methodCall.getMethod();
                        CtClass declaringClass = method.getDeclaringClass();
                        if (sourceClass.subclassOf(declaringClass)) {
                            sourceClass.getMethod(methodCall.getMethodName(), methodCall.getSignature());
                        }
                    } catch (NotFoundException e) {
                        throw new CannotCompileException(e.getMessage(), e);
                    }
                }
            }
        }

        private boolean isJoinPointMethod(List<CtMethod> jointPointList, String methodName, String methodSignature) {
            for (CtMethod method : jointPointList) {
                if (method.getName().equals(methodName) && method.getSignature().equals(methodSignature)) {
                    return true;
                }
            }
            return false;
        }


        private String invokeSourceMethod() {
            CodeBuilder builder = new CodeBuilder(32);
            if (!isVoid(replaceMethod.getSignature())) {
                builder.append("$_=");
            }

            builder.format("%1$s($$);", methodNameReplacer.replaceMethodName(sourceMethod.getName()));
            return builder.toString();
        }

        public boolean isVoid(String signature) {
            return signature.endsWith("V");
        }
    }

    private CtMethod copyMethod(CtClass sourceClass, CtMethod sourceMethod) throws CannotCompileException {

        // need id?

        String copyMethodName = methodNameReplacer.replaceMethodName(sourceMethod.getName());

        final CtMethod copy = CtNewMethod.copy(sourceMethod, copyMethodName, sourceClass, null);

        // set private
        final int modifiers = copy.getModifiers();
        copy.setModifiers(Modifier.setPrivate(modifiers));

        return copy;

    }


    private List<CtMethod> findAnnotationMethod(CtClass ctClass, Class annotation) {
        if (ctClass == null) {
            throw new NullPointerException("ctClass must not be null");
        }
        if (annotation == null) {
            throw new NullPointerException("annotation must not be null");
        }

        final List<CtMethod> annotationList = new ArrayList<CtMethod>();

        for (CtMethod method : ctClass.getDeclaredMethods()) {
            if (method.hasAnnotation(annotation)) {
                annotationList.add(method);
            }
        }
        return annotationList;
    }

    public static class DefaultMethodNameReplacer implements MethodNameReplacer {
        public static final String PREFIX = "__";
        public static final String POSTFIX = "_$$pinpoint";

        public String replaceMethodName(String methodName) {
            if (methodName == null) {
                throw new NullPointerException("methodName must not be null");
            }
            return  PREFIX + methodName + POSTFIX;
        }
    }
}
