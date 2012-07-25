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
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateSetParameterMethod(classPool, cc);
			updateExecuteQueryMethod(classPool, cc);
			updateConstructor(classPool, cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateSetParameterMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[5];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.Object");
		params1[2] = classPool.getCtClass("int");
		params1[3] = classPool.getCtClass("int");
		params1[4] = classPool.getCtClass("int");
		CtMethod method = cc.getDeclaredMethod("setParameter", params1);

		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlParam($1,$2);} ");
	}

	private static void updateConstructor(ClassPool classPool, CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		
		if (constructorList.length == 1) {
			CtConstructor constructor = constructorList[0];
			constructor.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
		}
	}

	private static void updateExecuteQueryMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
		serviceMethod.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
