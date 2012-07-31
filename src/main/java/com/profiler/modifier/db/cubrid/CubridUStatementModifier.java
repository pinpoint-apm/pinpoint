package com.profiler.modifier.db.cubrid;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class CubridUStatementModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(CubridUStatementModifier.class.getName());

	public CubridUStatementModifier(ClassPool classPool) {
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

			updateBindValueMethod(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateBindValueMethod(CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[3];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("byte");
		params1[2] = classPool.getCtClass("java.lang.Object");
		CtMethod method = cc.getDeclaredMethod("bindValue", params1);

		method.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$3); }");
	}
}
