package com.profiler.modifier.db.dbcp;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

public class DBCPBasicDataSourceModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(DBCPBasicDataSourceModifier.class.getName());

	public DBCPBasicDataSourceModifier(ClassPool classPool) {
		super(classPool);
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
		method.insertAfter("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".putConnection(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_GET_CONNECTION + ",$0.getUrl()); }");
	}
}
