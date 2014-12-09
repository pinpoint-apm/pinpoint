package com.navercorp.pinpoint.profiler.modifier.orm.ibatis;

import org.slf4j.LoggerFactory;

import com.navercorp.pinpoint.bootstrap.Agent;
import com.navercorp.pinpoint.bootstrap.instrument.ByteCodeInstrumentor;
import com.navercorp.pinpoint.bootstrap.instrument.MethodFilter;
import com.navercorp.pinpoint.profiler.modifier.orm.ibatis.filter.SqlMapSessionMethodFilter;

/**
 * iBatis SqlMapSessionImpl Modifier
 * <p/>
 * Hooks onto <i>com.ibatis.sqlmap.engine.SqlMapSessionImpl</i>
 * <p/>
 * 
 * @author Hyun Jeong
 */
public final class SqlMapSessionImplModifier extends IbatisClientModifier {

	private static final MethodFilter sqlMapSessionMethodFilter = new SqlMapSessionMethodFilter();

	public static final String TARGET_CLASS_NAME = "com/ibatis/sqlmap/engine/impl/SqlMapSessionImpl";

	public SqlMapSessionImplModifier(ByteCodeInstrumentor byteCodeInstrumentor, Agent agent) {
		super(byteCodeInstrumentor, agent);
        this.logger = LoggerFactory.getLogger(this.getClass());
    }

	@Override
	public String getTargetClass() {
		return TARGET_CLASS_NAME;
	}


	@Override
	protected MethodFilter getIbatisApiMethodFilter() {
		return sqlMapSessionMethodFilter;
	}

}
