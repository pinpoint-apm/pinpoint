package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.profiler.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.interceptor.bci.Type;
import com.nhn.pinpoint.profiler.logging.Logger;
import com.nhn.pinpoint.profiler.logging.LoggerFactory;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.ConnectionCloseInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.PreparedStatementCreateInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.StatementCreateInterceptor;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.TransactionInterceptor;

public class CubridConnectionModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public CubridConnectionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDConnection";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. " + javassistClassName);
		}
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass cubridConnection = byteCodeInstrumentor.getClass(javassistClassName);

			cubridConnection.addTraceVariable("__url", "__setUrl", "__getUrl", "java.lang.Object");

			cubridConnection.addInterceptor("close", null, new ConnectionCloseInterceptor(), Type.before);
			cubridConnection.addInterceptor("createStatement", null, new StatementCreateInterceptor(), Type.after);
			cubridConnection.addInterceptor("prepareStatement", new String[] { "java.lang.String" }, new PreparedStatementCreateInterceptor());
			cubridConnection.addInterceptor("setAutoCommit", new String[] { "boolean" }, new TransactionInterceptor());
			cubridConnection.addInterceptor("commit", null, new TransactionInterceptor());
			cubridConnection.addInterceptor("rollback", null, new TransactionInterceptor());

			printClassConvertComplete(javassistClassName);

			return cubridConnection.toBytecode();
		} catch (InstrumentException e) {
			if (logger.isWarnEnabled()) {
				logger.warn(this.getClass().getSimpleName() + " modify fail. Cause:" + e.getMessage(), e);
			}
			return null;
		}
	}
}
