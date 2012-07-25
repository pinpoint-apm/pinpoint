package com.profiler.modifier.db.mssql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MSSQLConnectionModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MSSQLConnectionModifier.class);

	public byte[] modify(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
		checkLibrary(classPool, javassistClassName, classLoader);
		return changeMethods(classPool, classLoader, javassistClassName, classFileBuffer);
	}

	private byte[] changeMethods(ClassPool classPool, ClassLoader classLoader, String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateCreateStatementMethod(classPool, cc);
			updateCloseMethod(classPool, cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private static void updateCreateStatementMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtClass[] params = new CtClass[2];
		params[0] = classPool.getCtClass("int");
		params[1] = classPool.getCtClass("int");
		CtMethod method = cc.getDeclaredMethod("createStatement", params);

		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + "); }");
	}

	private static void updateCloseMethod(ClassPool classPool, CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
	}
}
