package com.profiler.modifier.db.mysql;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.logging.Logger;
import com.profiler.modifier.AbstractModifier;

public class MySQLConnectionImplModifier extends AbstractModifier {

	private static final Logger logger = Logger.getLogger(MySQLConnectionImplModifier.class);

	public MySQLConnectionImplModifier(ClassPool classPool) {
		super(classPool);
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		logger.info("Modifing. %s", javassistClassName);
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
			logger.error(e.getMessage());
			e.printStackTrace();
		}
		return null;
	}

	private void updateCreateStatementMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("createStatement", null);
		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CREATE_STATEMENT + "); }");
	}

	private void updateGetInstanceMethod(CtClass cc) throws Exception {
		CtClass[] params = new CtClass[5];
		params[0] = classPool.getCtClass("java.lang.String");
		params[1] = classPool.getCtClass("int");
		params[2] = classPool.getCtClass("java.util.Properties");
		params[3] = classPool.getCtClass("java.lang.String");
		params[4] = classPool.getCtClass("java.lang.String");
		CtMethod method = cc.getDeclaredMethod("getInstance", params);

		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$5); }");
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_CLOSE_CONNECTION + "); }");
	}
}
