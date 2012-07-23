package com.profiler.modifier.db.oracle;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class OraclePreparedStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(OraclePreparedStatementModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("OraclePreparedStatementModifier modifing. %s", javassistClassName);
        checkLibrary(classPool, javassistClassName, classLoader);
		// printClassInfo(javassistClassName);
		// return addBeforeAfterLogics(classPool,javassistClassName);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
			updateSetInternalMethod(classPool, cc);
			updateExecuteMethod(classPool, cc);
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

	private static void updateSetInternalMethod(ClassPool classPool, CtClass cc) throws Exception {
		// setInternal(int paramIndex, String val)
		CtClass[] params1 = new CtClass[2];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.String");
		CtMethod serviceMethod1 = cc.getDeclaredMethod("setStringInternal", params1);

		logger.info("Changing setInternal(int,String) method");

		serviceMethod1.insertBefore(getSetInternal1MethodBeforeInsertCode());

		// CtClass[] params2=new CtClass[2];
		// params2[0]=classPool.getCtClass("int");
		// params2[1]=classPool.getCtClass("byte[]");
		// CtMethod serviceMethod2=cc.getDeclaredMethod("setInternal", params2);
		// log("*** Changing setInternal(int,byte[]) method ");
		// serviceMethod2.insertBefore(getSetInternal2MethodBeforeInsertCode());
	}

	private static String getSetInternal1MethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"PreparedStatement.setInternal(int,String) method is called\");");
			sb.append("System.out.println(\"-----Position=\"+$1+\" Value=\"+$2);");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$2);");
		sb.append("}");
		return sb.toString();
	}

	private static String getSetInternal2MethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		// sb.append("System.out.println(\"PreparedStatement.setInternal(int,byte[]) method is called\");");
		// sb.append("System.out.println(\"-----Position=\"+$1+\" Value=\"+new String($2));");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$2);");
		sb.append("}");
		return sb.toString();
	}

	private static void updateConstructor(ClassPool classPool, CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		// if(constructorList.length==3) {

		logger.info("*** Changing Constructor");

		for (CtConstructor constructor : constructorList) {
			CtClass params[] = constructor.getParameterTypes();
			// System.out.println(params.length);
			// for(CtClass param:params) {
			// System.out.println(param.getName());
			// }
			if (params.length == 6) {
				constructor.insertBefore(getConstructorBeforeInsertCode());
			}
		}
		// }
	}

	private static String getConstructorBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----PreparedStatement's constructor is called\");");
			sb.append("System.out.println(\"-----Query=[\"+$2+\"]\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2);");
		sb.append("}");

		return sb.toString();
	}

	private static void updateExecuteMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
		logger.info("Changing execute method");

		serviceMethod.insertBefore(getExecuteQueryMethodBeforeInsertCode());
		serviceMethod.insertAfter(getExecuteMethodAfterInsertCode());
	}

	private static String getExecuteQueryMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();

		if (logger.isDebugEnabled()) {
			sb.append("{");
			sb.append("System.out.println(\"-----PreparedStatement.executeQuery() method is called\");");
			sb.append("}");
		}

		return sb.toString();
	}

	private static String getExecuteMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");

		if (logger.isDebugEnabled()) {
			sb.append("System.out.println(\"-----PreparedStatement.executeQuery() method is ended\");");
		}

		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
		sb.append("}");

		return sb.toString();
	}
}
