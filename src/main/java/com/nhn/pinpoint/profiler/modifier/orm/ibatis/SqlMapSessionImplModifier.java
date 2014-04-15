package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

import java.security.ProtectionDomain;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.InstrumentClass;
import com.nhn.pinpoint.profiler.modifier.AbstractModifier;

/**
 * iBatis SqlMapSessionImpl Modifier
 * <p/>
 * Hooks onto <i>com.ibatis.sqlmap.engine.SqlMapSessionImpl</i>
 * <p/>
 * 
 * @author Hyun Jeong
 */
public class SqlMapSessionImplModifier extends AbstractModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	private static final String TARGET_CLASS_NAME = "com/ibatis/sqlmap/engine/SqlMapSessionImpl";

	public SqlMapSessionImplModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}

	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}

	@Override
	public byte[] modify(ClassLoader classLoader, String javassistClassName, ProtectionDomain protectedDomain, byte[] classFileBuffer) {
		if (logger.isInfoEnabled()) {
			logger.info("Modifying. {}", javassistClassName);
		}
		
		byteCodeInstrumentor.checkLibrary(classLoader, javassistClassName);
		try {
			
			InstrumentClass aClass = byteCodeInstrumentor.getClass(javassistClassName);

			return aClass.toBytecode();
		} catch (Throwable e) {
			logger.warn("SqlMapClient modifier error. Cause:{}", e.getMessage(), e);
			return null;
		}
	}

}
