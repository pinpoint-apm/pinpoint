package com.profiler.modifier.db.mssql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MSSQLResultSetModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MSSQLResultSetModifier.class);

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
		CtMethod serviceMethod1 = cc.getDeclaredMethod("next", null);
		serviceMethod1.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".updateFetchCount(); }");
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("close", null);
		serviceMethod1.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".addResultSetData(); }");
	}
}