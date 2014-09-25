package com.nhn.pinpoint.profiler.modifier.orm.mybatis;

import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;

/**
 * @author Hyun Jeong
 */
public class DefaultSqlSessionModifier extends MyBatisClientModifier {

	public static final String TARGET_CLASS_NAME = "org/apache/ibatis/session/defaults/DefaultSqlSession";
	
	public DefaultSqlSessionModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
        logger = LoggerFactory.getLogger(this.getClass());
	}
	
	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}

}
