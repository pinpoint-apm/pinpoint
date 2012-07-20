package com.profiler.modifier;

import com.profiler.logging.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

public abstract class AbstractModifier implements Modifier {

	private static final Logger logger = Logger.getLogger(AbstractModifier.class);

	public byte[] addBeforeAfterLogics(ClassPool classPool, String javassistClassName) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			CtMethod[] methods = cc.getDeclaredMethods();

			for (CtMethod method : methods) {
				if (!method.isEmpty()) {
					String methodName = method.getName();
					CtClass[] params = method.getParameterTypes();
					StringBuilder sb = new StringBuilder();

					if (params.length != 0) {
						int paramsLength = params.length;

						for (int loop = paramsLength - 1; loop > 0; loop--) {
							sb.append(params[loop].getName()).append(",");
						}
						// sb.substring(0, sb.length()-2);
					}
					method.insertBefore("{System.out.println(\"*****" + javassistClassName + "." + methodName + "(" + sb + ") is started.\");}");
					method.insertAfter("{System.out.println(\"*****" + javassistClassName + "." + methodName + "(" + sb + ") is finished.\");}");
				} else {
					logger.warn(method.getLongName() + " is empty !!!!!");
				}
			}

			CtConstructor[] constructors = cc.getConstructors();

			for (CtConstructor constructor : constructors) {

				if (!constructor.isEmpty()) {
					CtClass[] params = constructor.getParameterTypes();
					StringBuilder sb = new StringBuilder();

					if (params.length != 0) {
						int paramsLength = params.length;
						for (int loop = paramsLength - 1; loop > 0; loop--) {
							sb.append(params[loop].getName()).append(",");
						}
						// sb.substring(0, sb.length()-2);
					}

					constructor.insertBefore("{System.out.println(\"*****" + javassistClassName + " Constructor:Param=(" + sb + ") is started.\");}");
					constructor.insertAfter("{System.out.println(\"*****" + javassistClassName + " Constructor:Param=(" + sb + ") is finished.\");}");
				} else {
					logger.warn(constructor.getLongName() + " is empty !!!!!");
				}
			}
			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
			return null;
		}
	}

	public void printClassConvertComplete(String javassistClassName) {
		logger.info("%s class is converted.", javassistClassName);
	}
}
