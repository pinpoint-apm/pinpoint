package com.profiler.modifier.db.dbcp;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class DBCPBasicDataSourceModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(DBCPBasicDataSourceModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("DBCPBasicDataSourceModifier modifing. %s", javassistClassName);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateGetConnectionMethod(classPool, cc);
			// updateCloseMethod(classPool,cc);
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

	private static void updateGetConnectionMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("getConnection", null);

		logger.info("Changing getConnection method ");

		serviceMethod.insertAfter(getGetConnectionMethodAfterInsertCode());
	}

	@SuppressWarnings("unused")
	private static String getGetConnectionMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----BasicDataSource.getConnection() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getGetConnectionMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----BasicDataSource.getConnection() method is ended\");");
			sb.append("System.out.println(\"-----BasicDataSource.getConnection() \"+$0.getClass().getName());");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$0.getUrl());");
		sb.append("}");

		return sb.toString();
	}

}
