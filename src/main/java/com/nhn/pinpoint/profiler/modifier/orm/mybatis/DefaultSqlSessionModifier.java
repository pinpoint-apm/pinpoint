package com.nhn.pinpoint.profiler.modifier.orm.mybatis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;

/**
 * @author Hyun Jeong
 */
public class DefaultSqlSessionModifier extends MyBatisClientModifier {

	private final Logger logger = LoggerFactory.getLogger(this.getClass());

	public static final String TARGET_CLASS_NAME = "org/apache/ibatis/session/defaults/DefaultSqlSession";
	
	public DefaultSqlSessionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
	}
	
	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}

	@Override
	protected Logger getLogger() {
		return this.logger;
	}

}
