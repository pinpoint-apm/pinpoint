package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class CubridPreparedStatementModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(CubridPreparedStatementModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("CubridPreparedStatementModifier modifing");
        checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);
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

	private static void updateConstructor(ClassPool classPool, CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		for (CtConstructor constructor : constructorList) {
			CtClass params[] = constructor.getParameterTypes();
			logger.info("*** Constructor param length=" + params.length);
			if (params.length > 2) {
				constructor.insertBefore(getConstructorBeforeInsertCode());
			}
		}
	}

	private static String getConstructorBeforeInsertCode() {
		logger.info("*** Changing Constructor ");
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		// sb.append("System.out.println(\"-----CUBRIDPreparedStatement constructor is called\");");
		sb.append("if($2 instanceof cubrid.jdbc.jci.UStatement) { ");
		// sb.append("System.out.println(\"-----Query=[\"+$2.getQuery()+\"]\");");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2.getQuery());");
		sb.append("}}");
		return sb.toString();
	}

	private static void updateExecuteQueryMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
		logger.info("*** Changing execute() method ");
		// serviceMethod.insertBefore(getExecuteQueryMethodBeforeInsertCode());
		serviceMethod.insertAfter(getExecuteQueryMethodAfterInsertCode());
	}

	@SuppressWarnings("unused")
	private static String getExecuteQueryMethodBeforeInsertCode() {
		StringBuilder sb = new StringBuilder();
		// sb.append("{");
		// sb.append("System.out.println(\"-----CUBRIDPreparedStatement.execute() method is called\");");
		// sb.append("}");
		return sb.toString();
	}

	private static String getExecuteQueryMethodAfterInsertCode() {
		StringBuilder sb = new StringBuilder();
		sb.append("{");
		// sb.append("System.out.println(\"-----CUBRIDPreparedStatement.execute() method is ended\");");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
		sb.append("}");
		return sb.toString();

	}
}
