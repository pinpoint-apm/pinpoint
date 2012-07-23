package com.profiler.modifier.db.mysql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MySQLConnectionImplModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MySQLConnectionImplModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("MySQLConnectionImplModifier modifing. %s", javassistClassName);
        checkLibrary(classPool, javassistClassName, classLoader);
        return changeMethods(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethods(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateGetInstanceMethod(classPool, cc);
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
		CtMethod serviceMethod = cc.getDeclaredMethod("createStatement", null);

		logger.info("Changing createStatement method");

		serviceMethod.insertBefore(getCreateStatementMethodBeforeInsertCode());
		serviceMethod.insertAfter(getCreateStatementMethodAfterInsertCode());
	}

	private static String getCreateStatementMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----ConnectionImpl.createStatement() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getCreateStatementMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----ConnectionImpl.createStatement() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + ");");
		sb.append("}");
		return sb.toString();
	}

	private static void updateGetInstanceMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtClass[] params = new CtClass[5];
		params[0] = classPool.getCtClass("java.lang.String");
		params[1] = classPool.getCtClass("int");
		params[2] = classPool.getCtClass("java.util.Properties");
		params[3] = classPool.getCtClass("java.lang.String");
		params[4] = classPool.getCtClass("java.lang.String");
		CtMethod serviceMethod = cc.getDeclaredMethod("getInstance", params);

		logger.info("Changing getInstance method ");

		serviceMethod.insertBefore(getGetInstanceMethodBeforeInsertCode());
		serviceMethod.insertAfter(getGetInstanceMethodAfterInsertCode());
	}

	private static String getGetInstanceMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----ConnectionImpl.getInstance() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getGetInstanceMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----ConnectionImpl.getInstance() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$5);");
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
			sb.append("System.out.println(\"-----ConnectionImpl.close() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getCloseMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----ConnectionImpl.close() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + ");");
		sb.append("}");

		return sb.toString();
	}
}
