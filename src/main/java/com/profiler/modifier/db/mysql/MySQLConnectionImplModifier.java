package com.profiler.modifier.db.mysql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class MySQLConnectionImplModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MySQLConnectionImplModifier.class.getName());

	public MySQLConnectionImplModifier(ClassPool classPool) {
		super(classPool);
	}

	public String getTargetClass() {
		return "com/mysql/jdbc/ConnectionImpl";
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

			updateGetInstanceMethod(cc);
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
		CtMethod method = cc.getDeclaredMethod("createStatement", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + "); }");
	}

	private void updateGetInstanceMethod(CtClass cc) throws Exception {
		CtClass[] params = new CtClass[5];
		params[0] = classPool.getCtClass("java.lang.String");
		params[1] = classPool.getCtClass("int");
		params[2] = classPool.getCtClass("java.util.Properties");
		params[3] = classPool.getCtClass("java.lang.String");
		params[4] = classPool.getCtClass("java.lang.String");
		CtMethod method = cc.getDeclaredMethod("getInstance", params);

		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$5); }");
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
	}
}
