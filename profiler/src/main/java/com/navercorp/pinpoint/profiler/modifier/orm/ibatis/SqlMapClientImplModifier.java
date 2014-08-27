package com.nhn.pinpoint.profiler.modifier.orm.ibatis;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.nhn.pinpoint.bootstrap.Agent;
import com.nhn.pinpoint.profiler.interceptor.bci.ByteCodeInstrumentor;
import com.nhn.pinpoint.profiler.interceptor.bci.MethodFilter;
import com.nhn.pinpoint.profiler.modifier.orm.ibatis.filter.SqlMapClientMethodFilter;

/**
 * iBatis SqlMapClientImpl Modifier
 * <p/>
 * Hooks onto <i>com.ibatis.sqlmap.engine.SqlMapClientImpl
 * <p/>
 * 
 * @author Hyun Jeong
 */
public final class SqlMapClientImplModifier extends IbatisClientModifier {

	private static final MethodFilter sqlMapClientMethodFilter = new SqlMapClientMethodFilter();
	
	public static final String TARGET_CLASS_NAME = "com/ibatis/sqlmap/engine/impl/SqlMapClientImpl";

	public SqlMapClientImplModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
	}

	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}


	@Override
	protected MethodFilter getIbatisApiMethodFilter() {
		return sqlMapClientMethodFilter;
	}

}
