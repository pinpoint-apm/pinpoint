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

import com.navercorp.pinpoint.profiler.interceptor.aspect.Aspect;
import com.navercorp.pinpoint.profiler.interceptor.aspect.JointPoint;
import com.navercorp.pinpoint.profiler.interceptor.aspect.PointCut;

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

    private final Logger logger = LoggerFactory.getLogger(this.getClass())

	private static final MethodNameReplacer DEFAULT_METHOD_NAME_REPLACER = new DefaultMethodNameReplace    ();

	private final MethodNameReplacer methodNameRep    acer;


	public AspectWeav       rClass() {
		methodNameReplacer = DEFAULT_METH        _NAME_REPLACER;
	}

	public void weaving(CtClass sourceClass, CtClass adviceClass) throws NotFoundException, C       nnotCompileException {
		          f (logger.isInfoEnabled()) {
			logger.info("weaving sourceClass:{} advice:{}", sourceCl             ss.getName(), adviceClass.ge          Name());
		}
		if (!isAspectClass(adviceClass)) {
			throw new Runtime             xception("@Aspect not foun       . adviceClass:" + adviceClass);
		}
		// advice class hiera       chy check,
		f          nal boolean isSubClass = adviceClass.subclassOf(s          urceClass);
		if (!isSubClass) {
			final CtClass             superClass = adviceClass.getSuperclass();
			if (!superClass.getName().equals("java.lang.Object")) {
				throw new CannotCom                      ileException("invalid class hiera       chy. " + sourceClass.getName() + " adviceSuperClass:" + superClass.getName());
			}
		}
       		copyUtilMethod(sourceClass, adviceClass);

		final List<CtMethod> pointCutMethodList         findAnnotationMethod(adviceClass, PointCut.cl          ss);
		final List<CtMethod> jointPointList = findAnnotationMethod(adviceClass, JointPoint.class);

		for (CtMeth          d adviceMethod : pointCutMethodList) {
			final CtMethod sourceMe             hod = sourceClass.getDeclaredMethod(adviceMethod.getName(), adviceMethod.getParameterTypes());
			if (!sourceMethod.getSignature().equals(adviceMethod.getSignature())) {                   				throw new Canno             CompileException("Signature miss match. method:" + adviceMethod.getName() + " so                   rce:" + sourceMethod.getSignature() + " advice:" + adviceMethod.getSignat              e());
			}
			if (logger.isInfoEnabled()) {
				logger.info("weaving method:{}{}", sourceMethod.get       ame(), sourceMethod.getSignature());
			}
			weavingMethod(sou       ceClass, sourceMethod, adviceMethod,          jointPointList, isSubClass);
		}


	}

	private void copyUtilMethod(CtClass sourceCl          ss, CtClass adviceClass) thr             ws CannotCompileException {
		final List<CtMethod> utilMethodList = findUtilMethod(advi       eClass);
		for (CtMethod method : utilMethodList) {
		       final CtMethod copyMethod = CtNewMethod.copy(method, m          thod.getName(), sourceClass, null);
			sourceClass.addMethod(copyMethod);
		}

                   	private List<CtMethod> find          tilMethod(CtClass adviceClass)              hrows CannotCompileException {
		List<CtMethod> utilMethodList = new ArrayList<CtMethod>();
		f                   r (CtMethod metho              : adviceClass.g        DeclaredMethods()) {
			if (method.hasAnnotation(P       intCut.class) || method.hasAnnotation(Joint        int.class)) {
				continue;
			}
			int modifiers = method.getModifiers();
			if (!Modifier.isPrivate(modifiers)) {
				throw new CannotCompileException("non private UtilMeth       d unsupported. method:" + method.getLongName());
			}
			utilM       thodList.add(method);
		}
		ret       rn utilMethodList;
	}

	private boolea        isAspectClass(CtClass aspectClass) {
		return aspectClass.hasAnnotation(Aspect.class);
	}

	private void weavingMe        od(CtClass sourceClass, CtMethod sourceMethod, CtMetho        adviceMethod, List<CtMethod>        ointPointList, boolean isSubClas       ) throws CannotCompileException {       		final CtMethod copyMethod = copyMethod       sourceClass, sourceMethod);
		       ourceClass.addMethod(copyMethod);

		sourceMethod.setBody(adviceMethod, null);

		sourceMethod.instrument(new JointPointMethodEditor(sourceClass,           ourceMethod, copyMetho             , jointPointList, isSubClass));
	}

	public class JointPo                   ntMethodEditor extend           ExprEditor {
		private fin          l CtClass sourceClass;
		priv          te final CtMethod sourceMethod;          		private final CtMetho              rep       aceMethod;
		private final List<CtMethod> jointPointList;
		private f          nal boolean isSubClass;

		public JointPointMethodEditor(CtClass sourceClass, CtMethod sourceMethod, CtMethod repla          eMethod, List<Ct             ethod> jointPointList, boolean isSubClass) {
			if (replaceMet                od == null) {
				throw new NullPointerException("replaceMethod must not be null");
			}
			this.sourceClass = sourceClass;
			this.sourceMethod = sourceMethod;
			this.rep                         aceMethod = replaceMethod;
			this.             ointPointList = jointP                intList;
			this.isSubClass = isSubClass;
		}

		@Override
		public void edit(MethodCall methodCall) throws CannotCompi                         eException {


			f          na              boolean                 oinPointMethod = isJ                                  inPointMethod(jointPointL                   st, methodCall.getMethodName(), methodCa                   l.getSignature());
			if (joinPoi                      tMethod) {
				if (!methodCall.getSignature().equals(replaceMet                                  od.getSigna                   ure())) {
					throw new CannotCompileEx                                                 eption("Signature miss match. method:" + sourceMethod.getName() + " source:" + sourceM          thod.getSignature() + " jointPoint             " + replaceMethod.getSignature());
				}
				final String invokeSource = invokeSour                eM                                           thod();
				if (logger.isDebugE          abled()) {
					logger.debug("JointP          int method {}{} -> invokeOriginal:{}",             methodCall.get                   ethodName(), methodCall.getSignature(), invokeSource);
				}
				methodCall.repla          e(invokeSource);
			              else {
				if (isSubClass) {
					/           validate super class met             od
					try {
						CtMethod method = methodCall.getMethod();
						CtClass declaringClass = method.g       tDeclari       gClass();
						if (sourceClass.subclassOf(declaringClass)) {
							sourceClass.g       tMethod(methodCall.getMethodName(), methodCall.getSignature());
						}
					} catch        NotFoundEx       eption e) {
						throw new CannotComp       leException(e.getMessage(), e);
					}
				}
		       }
		}

		    r    vate boolean isJoinPointMethod(List<CtMethod> jointPointList, String methodNam       , String methodSig          ature) {
			for (CtMethod method : jointPointList) {
             			if (method.getNa          e().equals(methodName) && method.getSignature().equals(m             thodSignature)) {
					return true;
				}
			}
			return fal       e;
		}


		private String invokeSourceMethod() {
	          	CodeBuilder builder = new CodeBu             lder(32);
			if (!i                      Void(replace        thod.getSignature())) {
				builder.append       "$_=");
			}

			builder.format("%1$s($$        ", methodNameReplacer.replaceMethodName(sourceMethod.getName()));
			return       builder.toString();
		}

		public boo       ean isVoid(String signature) {
			return signatu       e.endsWith("V");
		}
	}

	private CtMethod copyM          thod(CtClass source             lass, CtMethod sourceMethod) throws CannotCompileExcep                   ion {

		// need id?

		Stri          g copyMethodName = methodNameReplacer.replaceMethodName(sourceMethod.getName());

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

	public static interface MethodNameReplacer {
		String replaceMethodName(String methodName);
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
