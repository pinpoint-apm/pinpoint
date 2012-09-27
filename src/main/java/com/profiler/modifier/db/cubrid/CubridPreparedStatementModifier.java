package com.profiler.modifier.db.cubrid;

import com.profiler.config.ProfilerConstant;
import com.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.profiler.modifier.AbstractModifier;
import com.profiler.trace.DatabaseRequestTracer;
import javassist.CtClass;
import javassist.CtConstructor;
import javassist.CtMethod;

import java.security.ProtectionDomain;
import java.util.logging.Level;
import java.util.logging.Logger;

public class CubridPreparedStatementModifier extends AbstractModifier {

	private final Logger logger = Logger.getLogger(CubridPreparedStatementModifier.class.getName());

	public CubridPreparedStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor) {
		super(byteCodeInstrumentor);
	}
	
	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDPreparedStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isLoggable(Level.INFO)){
		    logger.info("Modifing. " + javassistClassName);
        }
		checkLibrary(classLoader, javassistClassName);
		return changeMethod(javassistClassName, classFileBuffer);
	}

	private byte[] changeMethod(String javassistClassName, byte[] classfileBuffer) {
		try {
			CtClass cc = classPool.get(javassistClassName);

			updateExecuteQueryMethod(cc);
			updateConstructor(cc);

			printClassConvertComplete(javassistClassName);

			return cc.toBytecode();
		} catch (Exception e) {
            if(logger.isLoggable(Level.WARNING)) {
			    logger.log(Level.WARNING, e.getMessage(), e);
            }
		}
		return null;
	}

	private void updateConstructor(CtClass cc) throws Exception {
		CtConstructor[] constructorList = cc.getConstructors();

		for (CtConstructor constructor : constructorList) {
			CtClass params[] = constructor.getParameterTypes();

			if (params.length > 2) {
				StringBuilder sb = new StringBuilder();
				sb.append("{");
				sb.append("if($2 instanceof cubrid.jdbc.jci.UStatement) { ");
				sb.append(DatabaseRequestTracer.FQCN + ".putSqlQuery(" + ProfilerConstant.REQ_DATA_TYPE_DB_QUERY + ",$2.getQuery());");
				sb.append("}}");

				constructor.insertBefore(sb.toString());
			}
		}
	}

	private  void updateExecuteQueryMethod(CtClass cc) throws Exception {
		CtMethod serviceMethod = cc.getDeclaredMethod("execute", null);
		serviceMethod.insertAfter("{" + DatabaseRequestTracer.FQCN + ".put(" + ProfilerConstant.REQ_DATA_TYPE_DB_EXECUTE_QUERY + "); }");
	}
}
