package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class CubridResultSetModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(CubridResultSetModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateNextMethod(classPool, cc);
			updateCloseMethod(classPool, cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateNextMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("next", null);
		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".updateFetchCount(); }");
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".addResultSetData(); } ");
	}
}
