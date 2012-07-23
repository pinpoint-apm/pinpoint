package com.profiler.modifier.db.mssql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MSSQLPreparedStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MSSQLPreparedStatementModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("MSSQLPreparedStatementModifier modifing. %s", javassistClassName);
        checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateSetParameterMethod(classPool, cc);
			updateExecuteQueryMethod(classPool, cc);
			updateConstructor(classPool, cc);

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

	private static void updateSetParameterMethod(ClassPool classPool, CtClass cc) throws Exception {
		// setParameter(int,java.lang.Object,int,int,int)
		CtClass[] params1 = new CtClass[5];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.Object");
		params1[2] = classPool.getCtClass("int");
		params1[3] = classPool.getCtClass("int");
		params1[4] = classPool.getCtClass("int");
		CtMethod serviceMethod1 = cc.getDeclaredMethod("setParameter", params1);

		logger.info("Changing setParameter(int,Object,int,int,int) method ");

		serviceMethod1.insertBefore(getSetParameterBeforeInsertCode());
	}

	private static String getSetParameterBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----JtdsPreparedStatement.setParameter(int,String,int,int,int) method is called\");");
			sb.append("System.out.println(\"-----Position=\"+$1+\" Value=\"+$2);");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$2);");
		sb.append("}");
		return sb.toString();
	}

	private static void updateConstructor(ClassPool classPool, CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		if (constructorList.length == 1) {
			logger.info("Changing Constructor ");
			CtConstructor constructor = constructorList[0];
			constructor.insertAfter(getConstructorAfterInsertCode());
		}
	}

	private static String getConstructorAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----JtdsPreparedStatement's constructor is called\");");
			sb.append("System.out.println(\"-----Query=[\"+com.profiler.util.QueryStringUtil.removeCarriageReturn($2)+\"]\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2);");
		sb.append("}");
		return sb.toString();
	}

	private static void updateExecuteQueryMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);

		logger.info("Changing executeQuery() method.");

		serviceMethod.insertBefore(getExecuteQueryMethodBeforeInsertCode());
		serviceMethod.insertAfter(getExecuteQueryMethodAfterInsertCode());
	}

	private static String getExecuteQueryMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----JtdsPreparedStatement.execute() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getExecuteQueryMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----JtdsPreparedStatement.execute() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
		sb.append("}");

		return sb.toString();
	}
}
