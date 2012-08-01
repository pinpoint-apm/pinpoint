package com.profiler.modifier.db.oracle;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OracleStatementModifier extends AbstractModifier {
	private final Logger logger = Logger.getLogger(OracleStatementModifier.class.getName());

	public OracleStatementModifier(ClassPool classPool) {
		super(classPool);
	}
	
	public String getTargetClass() {
		return "oracle/jdbc/driver/OracleStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)){
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
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateExecuteQueryMethod(CtClass cc) throws Exception {
		CtClass[] params = new CtClass[1];
		params[0] = classPool.getCtClass("java.lang.String");
		// CtMethod serviceMethod=cc.getDeclaredMethod("executeQuery", params);
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", params);

		StringBuilder sb = new StringBuilder();
		sb.append("{");
		sb.append(DatabaseRequestTracer.FQCN + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$1);");
		sb.append(DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
		sb.append("}");

		serviceMethod.insertAfter(sb.toString());
	}
}
