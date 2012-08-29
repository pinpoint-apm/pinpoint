package com.profiler.modifier.db.mysql;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

import javassist.ByteArrayClassPath;

import com.profiler.interceptor.Interceptor;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.interceptor.bci.InstrumentClass;
import com.profiler.modifier.AbstractModifier;

public class MySQLStatementModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(MySQLStatementModifier.class.getName());

	public MySQLStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}

	public String getTargetClass() {
		return "com/mysql/jdbc/StatementImpl";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)) {
			logger.info("Modifing. " + javassistClassName);
		}
		// checkLibrary(classLoader, javassistClassName);

		Interceptor interceptor = newInterceptor(classLoader, protectedDomain, "com.profiler.modifier.db.mysql.interceptors.ExecuteQueryMethodInterceptor");
		if (interceptor == null) {
			return null;
		}

		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		classPool.insertClassPath(new ByteArrayClassPath(javassistClassName, classFileBuffer));

		InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);
		aClass.addInterceptor("executeQuery", new String[] { "java.lang.String" }, interceptor);

		return aClass.toBytecode();

		// return changeMethod(javassistClassName, classFileBuffer);
	}

	// private byte[] changeMethod(String javassistClassName, byte[]
	// classfileBuffer) {
	// try {
	// CtClass cc = classPool.get(javassistClassName);
	//
	// updateExecuteQueryMethod(cc);
	//
	// printClassConvertComplete(javassistClassName);
	//
	// return cc.toBytecode();
	// } catch (Exception e) {
	// if (logger.isLoggable(Level.WARNING)) {
	// logger.log(Level.WARNING, e.getMessage(), e);
	// }
	// }
	// return null;
	// }
	//
	// private void updateExecuteQueryMethod(CtClass cc) throws Exception {
	// CtClass[] params = new CtClass[1];
	// params[0] = classPool.getCtClass("java.lang.String");
	// CtMethod method = cc.getDeclaredMethod("executeQuery", params);
	//
	// StringBuilder sb = new StringBuilder();
	// sb.append("{");
	// sb.append(DatabaseRequestTracer.FQCN + ".putSqlQuery(" +
	// TomcatProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$1);");
	// sb.append(DatabaseRequestTracer.FQCN + ".put(" +
	// TomcatProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + ");");
	// sb.append("}");
	//
	// method.insertAfter(sb.toString());
	// }
}