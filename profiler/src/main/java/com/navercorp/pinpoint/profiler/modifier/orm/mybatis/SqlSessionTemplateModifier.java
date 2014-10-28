package com.nhn.pinpoint.profiler.modifier.orm.mybatis;

import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;

/**
 * @author Hyun Jeong
 */
public class SqlSessionTemplateModifier extends MyBatisClientModifier {

	public static final String TARGET_CLASS_NAME = "org/mybatis/spring/SqlSessionTemplate";

	public SqlSessionTemplateModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}



}
