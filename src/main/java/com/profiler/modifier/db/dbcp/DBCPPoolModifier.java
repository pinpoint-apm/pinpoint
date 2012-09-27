package com.profiler.modifier.db.dbcp;

import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBCPPoolModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(DBCPPoolModifier.class.getName());

	public DBCPPoolModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}
	
	public String getTargetClass() {
		return "org/apache/commons/dbcp/PoolingDataSource$PoolGuardConnectionWrapper";
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

			updateCloseMethod(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
            if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
	}
}
