package com.nhn.pinpoint.profiler.modifier.orm.ibatis.interceptor;

import com.nhn.pinpoint.bootstrap.config.ProfilerConfig;
import com.nhn.pinpoint.bootstrap.logging.PLogger;
import com.nhn.pinpoint.bootstrap.logging.PLoggerFactory;
import com.nhn.pinpoint.common.ServiceType;
import com.nhn.pinpoint.profiler.modifier.orm.SqlMapOperationInterceptor;

/**
 * @author Hyun Jeong
 * @author netspider
 */
public class IbatisSqlMapOperationInterceptor extends SqlMapOperationInterceptor {

	private final PLogger logger = PLoggerFactory.getLogger(this.getClass());

	public IbatisSqlMapOperationInterceptor(ServiceType serviceType) {
		super(serviceType);
	}

	@Override
	protected PLogger getLogger() {
		return this.logger;
	}

	@Override
	protected void initConfig() {
		if (traceContext != null) {
			final ProfilerConfig config = traceContext.getProfilerConfig();
			if (config != null) {
				this.enabled = config.isIBatisEnabled();
			}
		}
	}
}
