package com.profiler.modifier.db.oracle;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;

import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

import java.util.logging.Level;
import java.util.logging.Logger;

public class OraclePreparedStatementModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(OraclePreparedStatementModifier.class.getName());

	public OraclePreparedStatementModifier(ClassPool classPool) {
		super(classPool);
	}
	
	public String getTargetClass() {
		return "oracle/jdbc/driver/OraclePreparedStatement";
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

			updateSetInternalMethod(cc);
			updateExecuteMethod(cc);
			updateConstructor(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
			if (logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateSetInternalMethod(CtClass cc) throws Exception {
		CtClass[] params1 = new CtClass[2];
		params1[0] = classPool.getCtClass("int");
		params1[1] = classPool.getCtClass("java.lang.String");
		CtMethod serviceMethod1 = cc.getDeclaredMethod("setStringInternal", params1);

		serviceMethod1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2); }");

		// CtClass[] params2 = new CtClass[2];
		// params2[0] = classPool.getCtClass("int");
		// params2[1] = classPool.getCtClass("byte[]");
		// CtMethod serviceMethod2 = cc.getDeclaredMethod("setInternal",
		// params2);
		//
		// serviceMethod2.insertBefore("{" +
		// RequestDataTracer.FQCN +
		// ".putSqlParam($1,$2); {");
	}

	private void updateConstructor(CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();

		for (CtConstructor constructor : constructorList) {
			CtClass params[] = constructor.getParameterTypes();
			if (params.length == 6) {
				constructor.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
			}
		}
	}

	private void updateExecuteMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("execute", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
