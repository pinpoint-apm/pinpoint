package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import com.profiler.config.TomcatProfilerConstant;
import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;

public class MySQLPreparedStatementModifier extends AbstractModifier {
	private final Logger logger = Logger.getLogger(MySQLPreparedStatementModifier.class.getName());

	public MySQLPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "com/mysql/jdbc/PreparedStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}

		checkLibrary(classLoader, javassistClassName);

//		Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteMethodInterceptor");
//		if (interceptor == null) {
//			return null;
//		}
//
//		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
//		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));
//
//		InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
//		aClass.addInterceptor("executeQuery", null, interceptor);

		return changeMethod(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateSetInternalMethod(cc);
			 updateExecuteQueryMethod(cc);
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
		CtMethod method1 = cc.getDeclaredMethod("setInternal", params1);

		method1.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2); }");

		CtClass[] params2 = new CtClass[2];
		params2[0] = classPool.getCtClass("int");
		params2[1] = classPool.getCtClass("byte[]");
		CtMethod method2 = cc.getDeclaredMethod("setInternal", params2);

		method2.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlParam($1,$2); }");
	}

	private void updateConstructor(CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();
		if (constructorList.length == 3) {
			for (CtConstructor constructor : constructorList) {
				CtClass params[] = constructor.getParameterTypes();
				if (params.length == 3) {
					constructor.insertBefore("{" + DatabaseRequestTracer.FQCN + ".putSqlQuery(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2); }");
				}
			}
		}
	}

	private void updateExecuteQueryMethod(CtClass cc) throws Exception {
		CtMethod method = cc.getDeclaredMethod("executeQuery", null);
		method.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
