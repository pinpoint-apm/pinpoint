package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.Interceptor;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteQueryInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CubridStatementModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public CubridStatementModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDStatement";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. " + javassistClassName);
		}
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass statementClass = byteCodeInstrumentor.getClass(javassistClassName);

			statementClass.addInterceptor("executeQuery", new String[] { "java.lang.String" }, new StatementExecuteQueryInterceptor());

			// TODO 이거 고쳐야 됨.
			// Interceptor executeUpdate1 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
			// statementClass.addInterceptor("executeUpdate", new String[] { "java.lang.String" }, executeUpdate1);

			Interceptor executeUpdate2 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
			statementClass.addInterceptor("executeUpdate", new String[] { "java.lang.String", "int" }, executeUpdate2);

			// Interceptor executeUpdate3 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
			// statementClass.addInterceptor("execute", new String[] { "java.lang.String" }, executeUpdate3);

			Interceptor executeUpdate4 = byteCodeInstrumentor.newInterceptor(classLoader, protectedDomain, "com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementExecuteUpdateInterceptor");
			statementClass.addInterceptor("execute", new String[] { "java.lang.String", "int" }, executeUpdate4);

			statementClass.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");

			return statementClass.toBytecode();
		} catch (InstrumentException e) {
			if (logger.isWarnEnabled()) {
				logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
			}
			return null;
		}
	}
}
