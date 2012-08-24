package com.profiler.modifier.db.mssql;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MSSQLPreparedStatementModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MSSQLPreparedStatementModifier.class.getName());

	public MSSQLPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "net/sourceforge/jtds/jdbc/JtdsPreparedStatement";
	}
	
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)){
		    logger.info("Modifing. " + javassistClassName);
        }
		checkLibrary(classLoader, javassistClassName);
		return changeMethod(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateSetParameterMethod(cc);
			updateExecuteQueryMethod(cc);
			updateConstructor(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateSetParameterMethod(CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[5];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.Object");
		params1[2] = classPool.getCtClass("int");
		params1[3] = classPool.getCtClass("int");
		params1[4] = classPool.getCtClass("int");
		CtMethod method = cc.getDeclaredMethod("setParameter", params1);

		method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2);} ");
	}

	private void updateConstructor(CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		
		if (constructorList.length == 1) {
			CtConstructor constructor = constructorList[0];
			constructor.insertAfter("{" + DatabaseRequestTracer.FQCN + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
		}
	}

	private void updateExecuteQueryMethod(CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
		serviceMethod.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
