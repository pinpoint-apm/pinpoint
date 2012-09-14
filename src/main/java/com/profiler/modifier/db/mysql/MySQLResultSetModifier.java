package com.profiler.modifier.db.mysql;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;
// TODO 추가 개발해야 될듯.
public class MySQLResultSetModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MySQLResultSetModifier.class.getName());

    public MySQLResultSetModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}
    
    public String getTargetClass() {
    	return "com/mysql/jdbc/ResultSetImpl";
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
		CtMethod method = cc.getDeclaredMethod("next", null);
		method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".updateFetchCount(); }");
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".addResultSetData(); }");
	}
}
