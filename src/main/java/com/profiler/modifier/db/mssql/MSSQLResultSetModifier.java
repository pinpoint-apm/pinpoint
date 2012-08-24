package com.profiler.modifier.db.mssql;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.CtClass;
import javassist.CtMethod;


import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MSSQLResultSetModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MSSQLResultSetModifier.class.getName());

	public MSSQLResultSetModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}
	
	public String getTargetClass() {
		return "net/sourceforge/jtds/jdbc/JtdsResultSet";
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

			updateNextMethod(cc);
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

	private void updateNextMethod(CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("next", null);
		serviceMethod1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".updateFetchCount(); }");
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod serviceMethod1 = cc.getDeclaredMethod("close", null);
		serviceMethod1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".addResultSetData(); }");
	}
}