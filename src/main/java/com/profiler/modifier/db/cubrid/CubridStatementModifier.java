package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CubridStatementModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(CubridStatementModifier.class.getName());

	public CubridStatementModifier(ClassPool classPool) {
		super(classPool);
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
		    logger.info("Modifing. " + javassistClassName);
        }
		checkLibrary(classLoader, javassistClassName);
		return changeMethod(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateExecuteQueryMethod(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if(logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateExecuteQueryMethod(CtClass cc) throws Exception {
		CtClass[] params = new CtClass[1];
		params[0] = classPool.getCtClass("java.lang.String");
		CtMethod method = cc.getDeclaredMethod("executeQuery", params);

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$1);");
		sb.append(TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
		sb.append("}");

		method.insertAfter(sb.toString());
	}
}
