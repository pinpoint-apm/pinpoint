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
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethod(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

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

	private static void updateConstructor(ClassPool classPool, CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		
		for (CtConstructor constructor : constructorList) {
			CtClass params[] = constructor.getParameterTypes();
			
			if (params.length > 2) {
				StringBuilder sb = new StringBuilder();
				sb.append("{");
				sb.append("if($2 instanceof cubrid.jdbc.jci.UStatement) { ");
				sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2.getQuery());");
				sb.append("}}");

				constructor.insertBefore(sb.toString());
			}
		}
	}

	private static void updateExecuteQueryMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
		serviceMethod.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
