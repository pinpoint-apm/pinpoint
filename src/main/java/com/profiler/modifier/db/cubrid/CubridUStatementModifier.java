package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class CubridUStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(CubridUStatementModifier.class);

	public CubridUStatementModifier(ClassPool classPool) {
		super(classPool);
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classLoader, javassistClassName);
		return changeMethod(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateBindValueMethod(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private void updateBindValueMethod(CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[3];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("byte");
		params1[2] = classPool.getCtClass("java.lang.Object");
		CtMethod method = cc.getDeclaredMethod("bindValue", params1);

		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$3); }");
	}
}
