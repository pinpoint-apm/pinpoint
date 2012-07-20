package com.profiler.util;

import com.profiler.logging.Logger;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

public class ByteCodeUtil {
    // TODO logger 를 별도로 써야 되는지 않는지 검토.
    private static final Logger logger = Logger.getLogger(ByteCodeUtil.class);

    public static void printClassInfo(ClassPool classPool, String className) {
		logger.debug("Printing Class Info of [" + className + "]");
		try {
			logger.debug("try");
			String javaassistClassName = className.replace('/', '.');
			logger.debug("replace");
			CtClass cc = classPool.get(javaassistClassName);
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
}
