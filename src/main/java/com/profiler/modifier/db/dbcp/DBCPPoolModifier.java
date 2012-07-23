package com.profiler.modifier.db.dbcp;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class DBCPPoolModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(DBCPPoolModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("DBCPPoolModifier(PoolingDataSource$PoolGuardConnectionWrapper) modifing. %s", javassistClassName);
        checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateCloseMethod(classPool, cc);

			byte[] newClassfileBuffer = null;
			newClassfileBuffer = cc.toBytecode();
			// cc.writeFile();
			printClassConvertComplete(javassistClassName);
			return newClassfileBuffer;
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("close", null);
		logger.info("Changing close method ");
		// serviceMethod.insertBefore(getCloseMethodBeforeInsertCode());
		serviceMethod.insertAfter(getCloseMethodAfterInsertCode());
	}

	@SuppressWarnings("unused")
	private static String getCloseMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----PoolingDataSource$PoolGuardConnectionWrapper.close() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getCloseMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----PoolingDataSource$PoolGuardConnectionWrapper.close() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + ");");
		sb.append("}");
		return sb.toString();
	}
}
