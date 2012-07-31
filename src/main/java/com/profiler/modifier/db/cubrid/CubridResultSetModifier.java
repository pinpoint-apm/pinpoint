package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.modifier.AbstractModifier;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CubridResultSetModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(CubridResultSetModifier.class.getName());

	public CubridResultSetModifier(ClassPool classPool) {
		super(classPool);
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
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
            if(logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateNextMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("next", null);
		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".updateFetchCount(); }");
	}

	private void updateCloseMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("close", null);
		method.insertBefore("{" + TomcatProfilerConstant.CLASS_NAME_REQUEST_DATA_TRACER + ".addResultSetData(); } ");
	}
}
