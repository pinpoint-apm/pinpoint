package com.nhn.pinpoint.profiler.modifier.db.cubrid;

import java.security.ProtectionDomain;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentException;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;
import com.nhn.pinpoint.profiler.modifier.db.interceptor.DriverConnectInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CubridDriverModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public CubridDriverModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	public String getTargetClass() {
		return "cubrid/jdbc/driver/CUBRIDDriver";
	}

	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifing. {}", javassistClassName);
		}
		this.byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			InstrumentClass mysqlConnection = byteCodeInstrumentor.getClass(javassistClassName);

			mysqlConnection.addInterceptor("connect", new String[] { "java.lang.String", "java.util.Properties" }, new DriverConnectInterceptor());

            if (this.logger.isInfoEnabled()) {
                this.logger.info("{} class is converted.", javassistClassName);
            }

            return mysqlConnection.toBytecode();
		} catch (InstrumentException e) {
			if (logger.isWarnEnabled()) {
				logger.warn("{} modify fail. Cause:{}", this.getClass().getSimpleName(), e.getMessage(), e);
			}
			return null;
		}
	}
}
