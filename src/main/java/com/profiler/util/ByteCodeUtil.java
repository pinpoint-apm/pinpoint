package com.profiler.util;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ByteCodeUtil {
    // TODO logger 를 별도로 써야 되는지 않는지 검토.
    private static final Logger logger = Logger.getLogger(ByteCodeUtil.class.getName());

    public static void printClassInfo(ClassPool classPool, String className) {
        if (!logger.isLoggable(Level.FINE)) {
            return;
        }
        logger.fine("Printing Class Info of [" + className + "]");
        try {
            logger.fine("try");

            String javaassistClassName = className.replace('/', '.');
            logger.fine("replace");

            CtClass cc = classPool.get(javaassistClassName);
            logger.fine("ClassName:" + javaassistClassName);

            CtConstructor[] constructorList = cc.getConstructors();

			for (CtConstructor cons : constructorList) {
				try {
					String signature = cons.getSignature();
                     logger.info("Constructor signature:" + signature);
                } catch (Exception e) {
					if(logger.isLoggable(Level.WARNING)) {
			           logger.log(Level.WARNING, e.getMessage(), e);
                    }
				}
			}

			CtMethod[] methodList = cc.getDeclaredMethods();

			for (CtMethod tempMethod : methodList) {
				try {
					String methodName = tempMethod.getLongName();

					logger.fine("MethodName:" + methodName);

					CtClass[] params = tempMethod.getParameterTypes();

					if (params.length != 0) {
						int paramsLength = params.length;
						for (int loop = paramsLength - 1; loop > 0; loop--) {
							logger.fine("Param" + loop + ":" + params[loop].getName());
						}
					} else {
                        logger.fine("    No params");
                    }
					logger.fine("ReturnType=" + tempMethod.getReturnType().getName());
				} catch (Exception methodException) {
					logger.log(Level.WARNING, "Exception :" + methodException.getMessage(), methodException);
				}
			}
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
               logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
	}
}
