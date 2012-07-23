package com.profiler.modifier.db.mssql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MSSQLConnectionModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MSSQLConnectionModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("MSSQLConnectionModifier modifing. %s", javassistClassName);
        checkLibrary(classPool, javassistClassName, classLoader);
        return changeMethods(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethods(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateCreateStatementMethod(classPool, cc);
			updateCloseMethod(classPool, cc);
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

	private static void updateCreateStatementMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtClass[] params = new CtClass[2];
		params[0] = classPool.getCtClass("int");
		params[1] = classPool.getCtClass("int");
		CtMethod serviceMethod = cc.getDeclaredMethod("createStatement", params);
		
		logger.info("Changing createStatement method ");
		
		serviceMethod.insertBefore(getCreateStatementMethodBeforeInsertCode());
		serviceMethod.insertAfter(getCreateStatementMethodAfterInsertCode());
	}

	private static String getCreateStatementMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----ConnectionJDBC2.createStatement() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getCreateStatementMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----ConnectionJDBC2.createStatement() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + ");");
		sb.append("}");
		return sb.toString();
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("close", null);

		logger.info("Changing close method ");

		serviceMethod.insertBefore(getCloseMethodBeforeInsertCode());
		serviceMethod.insertAfter(getCloseMethodAfterInsertCode());
	}

	private static String getCloseMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----ConnectionJDBC2.close() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getCloseMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----ConnectionJDBC2.close() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + ");");
		sb.append("}");

		return sb.toString();
	}
}
