package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class CubridUStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(CubridUStatementModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateBindValueMethod(classPool, cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateBindValueMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[3];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("byte");
		params1[2] = classPool.getCtClass("java.lang.Object");
		CtMethod method = cc.getDeclaredMethod("bindValue", params1);

		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$3); }");
	}
}
