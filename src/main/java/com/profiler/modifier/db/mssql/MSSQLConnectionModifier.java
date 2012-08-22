package com.profiler.modifier.db.mssql;

import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MSSQLConnectionModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MSSQLConnectionModifier.class.getName());

	public MSSQLConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}
	
	public String getTargetClass() {
		return "net/sourceforge/jtds/jdbc/ConnectionJDBC2";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)){
		    logger.info("Modifing. " + javassistClassName);
        }
		checkLibrary(classLoader, javassistClassName);
		return changeMethods(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethods(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateCreateStatementMethod(cc);
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

	private void updateCreateStatementMethod(CtClass cc) throws Exception {
		CtClass[] params = new CtClass[2];
		params[0] = classPool.getCtClass("int");
		params[1] = classPool.getCtClass("int");
		CtMethod method = cc.getDeclaredMethod("createStatement", params);

		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + "); }");
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
	}
}
