package com.profiler.modifier;

import com.profiler.logging.Logger;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

public abstract class AbstractModifier {

	private static final Logger logger = Logger.getLogger(AbstractModifier.class);

	public static void printClassInfo(String className) {
		logger.debug("Printing Class Info of [" + className + "]");
		try {
			ClassPool pool = ClassPool.getDefault();
			logger.debug("try");
			String javaassistClassName = className.replace('/', '.');
			logger.debug("replace");
			CtClass cc = pool.get(javaassistClassName);
			logger.debug("ClassName:" + javaassistClassName);
			CtConstructor[] constructorList = cc.getConstructors();

			for (CtConstructor cons : constructorList) {
				try {
					String signature = cons.getSignature();
					logger.info("Constructor signature:%s", signature);
				} catch (Exception e) {
					logger.error(e.getMessage());
					e.printStackTrace();
				}
			}

			CtMethod[] methodList = cc.getDeclaredMethods();

			for (CtMethod tempMethod : methodList) {
				try {
					String methodName = tempMethod.getLongName();

					logger.debug("MethodName:" + methodName);

					CtClass[] params = tempMethod.getParameterTypes();

					if (params.length != 0) {
						int paramsLength = params.length;
						for (int loop = paramsLength - 1; loop > 0; loop--) {
							logger.debug("Param" + loop + ":" + params[loop].getName());
						}
					} else {
						logger.debug("    No params");
					}

					logger.debug("ReturnType=" + tempMethod.getReturnType().getName());
				} catch (Exception methodException) {
					logger.error("Exception : " + methodException.getMessage());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
	}

	public static byte[] addBeforeAfterLogics(ClassPool classPool, String javassistClassName) {
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

	public static void printClassConvertComplete(String javassistClassName) {
		logger.info("%s class is converted.", javassistClassName);
	}
}
