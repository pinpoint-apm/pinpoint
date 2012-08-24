package com.profiler.modifier.db.dbcp;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DBCPBasicDataSourceModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(DBCPBasicDataSourceModifier.class.getName());

	public DBCPBasicDataSourceModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "org/apache/commons/dbcp/BasicDataSource";
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

			updateGetConnectionMethod(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateGetConnectionMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("getConnection", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$0.getUrl()); }");
	}
}
