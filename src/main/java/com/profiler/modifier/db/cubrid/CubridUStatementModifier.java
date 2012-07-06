package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class CubridUStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(CubridUStatementModifier.class);

	public static byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("CubridUStatementModifier modifing. %s", javassistClassName);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private static byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateBindValueMethod(classPool, cc);

			byte[] newClassfileBuffer = cc.toBytecode();
			// cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
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
		CtMethod serviceMethod1 = cc.getDeclaredMethod("bindValue", params1);

		logger.info("Changing bindValue(int, byte, String) method ");

		serviceMethod1.insertBefore(getBindValueMethodBeforeInsertCode());
	}

	private static String getBindValueMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"UStatement.setInternal(int,String) method is called\");");
			sb.append("System.out.println(\"-----Position=\"+$1+\" Value=\"+$3);");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$3);");
		sb.append("}");

		return sb.toString();
	}

}
